/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2022 Dennis P Paul.
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package wellen.effect;

/*
  ZynAddSubFX - a software synthesizer

  SV Filter.h - Several state-variable filters
  Copyright (C) 2002-2005 Nasca Octavian Paul
  Author: Nasca Octavian Paul

  Modified for rakarrack by Josep Andreu

  This program is free software; you can redistribute it and/or modify
  it under the terms of version 2 of the GNU General Public License
  as published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License (version 2) for more details.

  You should have received a copy of the GNU General Public License (version 2)
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

*/

import static wellen.effect.RRUtilities.MAX_FILTER_STAGES;
import static wellen.effect.RRUtilities.PERIOD;
import static wellen.effect.RRUtilities.PI;
import static wellen.effect.RRUtilities.SAMPLE_RATE;
import static wellen.effect.RRUtilities.atanf;
import static wellen.effect.RRUtilities.dB2rap;
import static wellen.effect.RRUtilities.fPERIOD;
import static wellen.effect.RRUtilities.powf;
import static wellen.effect.RRUtilities.sqrtf;

public class RRSVFilter extends RRFilterI {

    private boolean abovenq;            //this is 1 if the frequency is above the nyquist
    private boolean firsttime;
    private float freq;                 //Frequency given in Hz
    private float gain;                 //the gain of the filter (if are shelf/peak) filters
    private parameters ipar;
    private boolean needsinterpolation;
    private boolean oldabovenq;
    private final parameters par;
    private float q;                    //Q factor (resonance or Q factor)
    private final fstage[] st = new fstage[MAX_FILTER_STAGES + 1];
    private int stages;                 //how many times the filter is applied (0.1,1->2,etc.)
    private int type;                   //The type of the filter (LPF1,HPF1,LPF2,HPF2...)

    public RRSVFilter(int Ftype, float Ffreq, float Fq, int Fstages) {
        par = new parameters();
        for (int i = 0; i < st.length; i++) {
            st[i] = new fstage();
        }
        stages = Fstages;
        type = Ftype;
        freq = Ffreq;
        q = Fq;
        gain = 1.0f;
        outgain = 1.0f;
        needsinterpolation = false;
        firsttime = true;
        if (stages >= MAX_FILTER_STAGES) {
            stages = MAX_FILTER_STAGES;
        }
        cleanup();
        setfreq_and_q(Ffreq, Fq);
    }

    public void cleanup() {
        for (int i = 0; i < MAX_FILTER_STAGES + 1; i++) {
            st[i].low = 0.0f;
            st[i].high = 0.0f;
            st[i].band = 0.0f;
            st[i].notch = 0.0f;
        }
        oldabovenq = false;
        abovenq = false;
    }

    public void setfreq(float frequency) {
        if (frequency < 0.1f) {
            frequency = 0.1f;
        }
        float rap = freq / frequency;
        if (rap < 1.0f) {
            rap = 1.0f / rap;
        }

        oldabovenq = abovenq;
        abovenq = frequency > (SAMPLE_RATE / 2.0f - 500.0f);
        boolean nyquistthresh = (abovenq ^ oldabovenq);

        if ((rap > 3.0) || (nyquistthresh)) {
            //if the frequency is changed fast, it needs
            // interpolation (now, filter and coeficients backup)
            if (!firsttime) {
                needsinterpolation = true;
            }
            ipar = par;
        }
        freq = frequency;
        computefiltercoefs();
        firsttime = false;
    }

    public void setfreq_and_q(float frequency, float q_) {
        q = q_;
        setfreq(frequency);
    }

    public void setq(float q_) {
        q = q_;
        computefiltercoefs();
    }

    public void settype(int type_) {
        type = type_;
        computefiltercoefs();
    }

    public void setgain(float dBgain) {
        gain = dB2rap(dBgain);
        computefiltercoefs();
    }

    public void setstages(int stages_) {
        if (stages_ >= MAX_FILTER_STAGES) {
            stages_ = MAX_FILTER_STAGES - 1;
        }
        stages = stages_;
        cleanup();
        computefiltercoefs();
    }

    public void filterout(float[] smp) {
        int i;
        float[] ismp = null;

        if (needsinterpolation) {
            ismp = new float[PERIOD];
            for (i = 0; i < PERIOD; i++) {
                ismp[i] = smp[i];
            }
            for (i = 0; i < stages + 1; i++) {
                singlefilterout(ismp, st[i], ipar);
            }
        }

        for (i = 0; i < stages + 1; i++) {
            singlefilterout(smp, st[i], par);
        }

        if (needsinterpolation) {
            for (i = 0; i < PERIOD; i++) {
                float x = (float) i / fPERIOD;
                smp[i] = ismp[i] * (1.0f - x) + smp[i] * x;
            }
            needsinterpolation = false;
        }

        for (i = 0; i < PERIOD; i++) {
            smp[i] *= outgain;
        }
    }

    private void singlefilterout(float[] smp, fstage x, parameters par) {
//        switch (type) {
//            case 0:
//                out = x.low;
//                break;
//            case 1:
//                out = x.high;
//                break;
//            case 2:
//                out = x.band;
//                break;
//            case 3:
//                out = x.notch;
//                break;
//        }

        for (int i = 0; i < PERIOD; i++) {
            x.low = x.low + par.f * x.band;
            x.high = par.q_sqrt * smp[i] - x.low - par.q * x.band;
            x.band = par.f * x.high + x.band;
            x.notch = x.high + x.low;
            switch (type) {
                case 0:
                    smp[i] = x.low;
                    break;
                case 1:
                    smp[i] = x.high;
                    break;
                case 2:
                    smp[i] = x.band;
                    break;
                case 3:
                    smp[i] = x.notch;
                    break;
            }
        }
    }

    private void computefiltercoefs() {
        par.f = freq / SAMPLE_RATE * 4.0f;
        if (par.f > 0.99999) {
            par.f = 0.99999f;
        }
        par.q = 1.0f - atanf(sqrtf(q)) * 2.0f / PI;
        par.q = powf(par.q, 1.0f / (float) (stages + 1));
        par.q_sqrt = sqrtf(par.q);
    }

    private static class fstage {
        float band;
        float high;
        float low;
        float notch;
    }

    private static class parameters {
        float f;
        float q;
        float q_sqrt;
    }
}