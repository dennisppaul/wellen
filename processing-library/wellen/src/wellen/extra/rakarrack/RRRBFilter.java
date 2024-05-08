/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2024 Dennis P Paul.
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

package wellen.extra.rakarrack;

/*
  ZynAddSubFX - a software synthesizer

  RBFilter.h - Several state-variable filters
  Copyright (C) 2002-2005 Nasca Octavian Paul
  Author: Nasca Octavian Paul

  Modified for rakarrack by Ryan Billing

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

public class RRRBFilter extends RRFilterI {

    private final float a_smooth_tc;
    private boolean abovenq;                //this is 1 if the frequency is above the nyquist
    private final float b_smooth_tc;
    private float bpg;
    private boolean en_mix;
    private boolean firsttime;
    private float freq;                     //Frequency given in Hz
    private float gain;                     //the gain of the filter (if are shelf/peak) filters
    private float hpg;
    private parameters ipar;
    private final float iper;               //inverse of PERIOD
    private float lpg;
    private boolean needsinterpolation;
    private boolean oldabovenq;
    private float oldf;
    private float oldq;
    private float oldsq;
    private final parameters par;
    private float q;                        //Q factor (resonance or Q factor)
    private boolean qmode;                  //set to true for compatibility to old presets.  0 means Q = 1/q
    private final fstage[] st;
    private int stages;                     //how many times the filter is applied (0.1,1->2,etc.)
    private int type;                       //The type of the filter (LPF1,HPF1,LPF2,HPF2...)
    public RRRBFilter(int Ftype, float Ffreq, float Fq, int Fstages) {
        st = new fstage[RRUtilities.MAX_FILTER_STAGES + 1];
        for (int i = 0; i < st.length; i++) {
            st[i] = new fstage();
        }
        parameters ipar = new parameters();
        par = new parameters();
        stages = Fstages;
        type = Ftype;
        freq = Ffreq;
        q = Fq;
        qmode = false;
        gain = 1.0f;
        outgain = 1.0f;
        needsinterpolation = false;
        firsttime = true;
        en_mix = false;
        oldq = 0.0f;
        oldsq = 0.0f;
        oldf = 0.0f;
        hpg = lpg = bpg = 0.0f;
        if (stages >= RRUtilities.MAX_FILTER_STAGES) {
            stages = RRUtilities.MAX_FILTER_STAGES;
        }
        cleanup();
        setfreq_and_q(Ffreq, Fq);
        iper = 1.0f / RRUtilities.fPERIOD;
        a_smooth_tc = RRUtilities.cSAMPLE_RATE / (RRUtilities.cSAMPLE_RATE + 0.01f);  //10ms time finalant for
        // averaging coefficients
        b_smooth_tc = 1.0f - a_smooth_tc;
    }

    public void cleanup() {
        for (int i = 0; i < RRUtilities.MAX_FILTER_STAGES + 1; i++) {
            st[i].low = 0.0f;
            st[i].high = 0.0f;
            st[i].band = 0.0f;
            st[i].notch = 0.0f;
        }
        oldabovenq = false;
        abovenq = false;
    }

    public void setmode(boolean mode) {
        qmode = mode;
    }

    public void setfreq(float frequency) {
        if (frequency > (RRUtilities.SAMPLE_RATE / 2.0f - 500.0f)) {
            frequency = RRUtilities.SAMPLE_RATE / 2.0f - 500.0f;
        }
        if (frequency < 0.1) {
            frequency = 0.1f;
        }
        float rap = freq / frequency;
        if (rap < 1.0) {
            rap = 1.0f / rap;
        }

        oldabovenq = abovenq;
        abovenq = frequency > (RRUtilities.SAMPLE_RATE / 2.0f - 500.0f);

        boolean nyquistthresh = (abovenq ^ oldabovenq);

        if ((rap > 3.0) || (nyquistthresh)) {                //if the frequency is changed fast, it needs
            // interpolation (now, filter and coeficients backup)
            if (!firsttime) {
                needsinterpolation = true;
            }
            ipar = par;
        }
        freq = frequency;

        if (!qmode) {
            computefiltercoefs();
        } else {
            computefiltercoefs_hiQ();
        }
        firsttime = false;

    }

    public void setfreq_and_q(float frequency, float q_) {
        q = q_;
        setfreq(frequency);
    }

    public void setq(float q_) {
        q = q_;
        if (!qmode) {
            computefiltercoefs();
        } else {
            computefiltercoefs_hiQ();
        }
    }

    public void settype(int type_) {
        type = type_;
        if (!qmode) {
            computefiltercoefs();
        } else {
            computefiltercoefs_hiQ();
        }
    }

    public void setgain(float dBgain) {
        gain = RRUtilities.dB2rap(dBgain);
        if (!qmode) {
            computefiltercoefs();
        } else {
            computefiltercoefs_hiQ();
        }
    }

    public void setstages(int stages_) {
        if (stages_ >= RRUtilities.MAX_FILTER_STAGES) {
            stages_ = RRUtilities.MAX_FILTER_STAGES - 1;
        }
        stages = stages_;
        cleanup();
        if (!qmode) {
            computefiltercoefs();
        } else {
            computefiltercoefs_hiQ();
        }
    }

    public void setmix(boolean mix, float lpmix, float bpmix, float hpmix) {
        en_mix = mix;
        lpg = lpmix;
        bpg = bpmix;
        hpg = hpmix;
    }

    public void singlefilterout(float[] smp, fstage x, parameters par) {
        int i;
        float out = 0.0f;
        switch (type) {
            case 0:
                out = x.low;
                break;
            case 1:
                out = x.high;
                break;
            case 2:
                out = x.band;
                break;
            case 3:
                out = x.notch;
                break;
        }

        float tmpq, tmpsq, tmpf, qdiff, sqdiff, fdiff;
        qdiff = (par.q - oldq) * iper;
        sqdiff = (par.q_sqrt - oldsq) * iper;
        fdiff = (par.f - oldf) * iper;
        tmpq = oldq;
        tmpsq = oldsq;
        tmpf = oldf;

        for (i = 0; i < RRUtilities.PERIOD; i++) {
            tmpq += qdiff;
            tmpsq += sqdiff;
            tmpf += fdiff;   //Modulation interpolation

            x.low = x.low + tmpf * x.band;
            x.high = tmpsq * smp[i] - x.low - tmpq * x.band;
            //x.high = smp[i] - x.low - tmpq * x.band;
            x.band = tmpf * x.high + x.band;

            if (en_mix) {
                smp[i] = lpg * x.low + hpg * x.high + bpg * x.band;
            } else {
                x.notch = x.high + x.low;
                smp[i] = out;
            }
        }

        oldf = par.f;
        oldq = par.q;
        oldsq = par.q_sqrt;
    }

    public void filterout(float[] smp) {
        int i;
        float[] ismp;

        if (needsinterpolation) {
            ismp = new float[RRUtilities.PERIOD];
            for (i = 0; i < RRUtilities.PERIOD; i++) {
                ismp[i] = smp[i];
            }
            for (i = 0; i < stages + 1; i++) {
                singlefilterout(ismp, st[i], ipar);
            }

            needsinterpolation = false;
        }

        for (i = 0; i < stages + 1; i++) {
            singlefilterout(smp, st[i], par);
        }

        for (i = 0; i < RRUtilities.PERIOD; i++) {
            smp[i] *= outgain;
        }
    }

    float filterout_s(float smp) {
        if (needsinterpolation) {
            for (int i = 0; i < stages + 1; i++) {
                smp = singlefilterout_s(smp, st[i], ipar);
            }
            needsinterpolation = false;
        }

        for (int i = 0; i < stages + 1; i++) {
            smp = singlefilterout_s(smp, st[i], par);
        }
        smp *= outgain;
        return smp;
    }

    private void computefiltercoefs() {
        par.f = 2.0f * RRUtilities.sinf(RRUtilities.PI * freq / RRUtilities.SAMPLE_RATE);
        if (par.f > 0.99999) {
            par.f = 0.99999f;
        }
        par.q = 1.0f - RRUtilities.atanf(RRUtilities.sqrtf(q)) * 2.0f / RRUtilities.PI;
        par.q = RRUtilities.powf(par.q, 1.0f / (float) (stages + 1));
        par.q_sqrt = RRUtilities.sqrtf(par.q);

    }

    private void computefiltercoefs_hiQ()  //potentially unstable at some settings, but better sound
    {
        par.f = 2.0f * RRUtilities.sinf(RRUtilities.PI * freq / RRUtilities.SAMPLE_RATE);
        if (par.f > 0.99999) {
            par.f = 0.99999f;
        }
        if (q < 0.5f) {
            q = 0.5f;
        }
        par.q = 1.0f / q;
        par.q = RRUtilities.powf(par.q, 1.0f / (float) (stages + 1));
        par.q_sqrt = 1.0f;

    }

    private float singlefilterout_s(float smp, fstage x, parameters par) {
        float out = 0.0f;
        switch (type) {
            case 0:
                out = x.low;
                break;
            case 1:
                out = x.high;
                break;
            case 2:
                out = x.band;
                break;
            case 3:
                out = x.notch;
                break;
        }

        oldq = b_smooth_tc * oldq + a_smooth_tc * par.q;
        oldsq = b_smooth_tc * oldsq + a_smooth_tc * par.q_sqrt;
        oldf = b_smooth_tc * oldf + a_smooth_tc * par.f;   //modulation interpolation

        x.low = x.low + oldf * x.band;
        x.high = oldsq * smp - x.low - oldq * x.band;
        //x.high = smp - x.low - oldq * x.band;
        x.band = oldf * x.high + x.band;

        if (en_mix) {
            smp = lpg * x.low + hpg * x.high + bpg * x.band;
        } else {
            x.notch = x.high + x.low;
            smp = out;
        }

        oldf = par.f;
        oldq = par.q;
        oldsq = par.q_sqrt;

        return (smp);
    }

    private static class fstage {
        float low, high, band, notch;
    }

    private static class parameters {
        float f, q, q_sqrt;
    }
}