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
  Vocoder.h - Vocoder Effect

  Author: Ryam Billing & Josep Andreu

  Adapted effect structure of ZynAddSubFX - a software synthesizer
  Author: Nasca Octavian Paul

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

import static wellen.Wellen.clamp;

public class RRVocoder implements EffectStereo {

    /*
    "Vocoder Input","298","35",
    "Vocoder Level","302","35",
    "Vocoder Muf.","299","35",
    "Vocoder Pan","297","35",
    "Vocoder Q","300","35",
    "Vocoder Ring","301","35",
    "Vocoder WD","296","35",
    */

    public static final int PARAM_VOLUME = 0;
    public static final int PARAM_PANNING = 1;
    public static final int PARAM_MUFFLE = 2;
    public static final int PARAM_Q = 3;
    public static final int PARAM_INPUT = 4;
    public static final int PARAM_LEVEL = 5;
    public static final int PARAM_RING = 6;
    public static final int NUM_PARAMS = 7;
    public static final int PRESET_VOCODER_1 = 0;
    public static final int PRESET_VOCODER_2 = 1;
    public static final int PRESET_VOCODER_3 = 2;
    public static final int PRESET_VOCODER_4 = 3;
    public static final int NUM_PRESETS = 4;
    public float[] auxresampled;
    //    public float[] efxoutl;
    //    public float[] efxoutr;
    public float outvolume;
    public float vulevel;
    private int Pband;
    private int Pinput;
    private int Plevel;         //This should only adjust the level of the IR effect, and not wet/dry mix
    private final int Plrcross; // L/R Mixing  // This is a mono effect, so lrcross and panning are pointless
    private int Pmuffle;
    private int Ppanning;       //Panning
    private int Ppreset;
    private int Pqq;
    private int Pring;
    private int Pvolume;        //This is master wet/dry mix like other FX...but I am finding it is not useful
    private final int VOC_BANDS;
    private float alpha;
    private float beta;
    private final float calpha;
    private final float cbeta;
    private float compeak;
    private float compenv;
    private float compg;
    private float cpthresh;
    private final float cratio;
    private final float cthresh;
    private final fbank[] filterbank;
    private final float gate;
    private float lpanning, rpanning, input, level;
    private float oldcompenv;
    private final float prls;
    private float ringworm;
    private final RRAnalogFilter vhp;
    private final RRAnalogFilter vlp;

    public RRVocoder(float[] auxresampled_, int bands) {
        VOC_BANDS = bands;
//        efxoutl = efxoutl_;
//        efxoutr = efxoutr_;
        auxresampled = auxresampled_;
        //default values
        Ppreset = PRESET_VOCODER_1;
        Pvolume = 50;
        Plevel = 0;
        Pinput = 0;
        Ppanning = 64;
        Plrcross = 100;

        filterbank = new fbank[VOC_BANDS];
//        private final float[] tmpaux;
//        private final float[] tmpl;
//        private final float[] tmpr;
//        private final float[] tsmpsl;
//        private final float[] tsmpsr;
//
//        tmpl = new float[PERIOD];
//        tmpr = new float[PERIOD];
//        tsmpsl = new float[PERIOD];
//        tsmpsr = new float[PERIOD];
//        tmpaux = new float[PERIOD];

        Pmuffle = 10;
        float tmp = 0.01f;  //10 ms decay time on peak detectors
        alpha = RRUtilities.cSAMPLE_RATE / (RRUtilities.cSAMPLE_RATE + tmp);
        beta = 1.0f - alpha;
        prls = beta;
        gate = 0.005f;

        tmp = 0.05f; //50 ms att/rel on compressor
        calpha = RRUtilities.cSAMPLE_RATE / (RRUtilities.cSAMPLE_RATE + tmp);
        cbeta = 1.0f - calpha;
        cthresh = 0.25f;
        cpthresh = cthresh; //dynamic threshold
        cratio = 0.25f;

        float center;
        float qq;

        for (int i = 0; i < VOC_BANDS; i++) {
            center = (float) i * 20000.0f / ((float) VOC_BANDS);
            qq = 60.0f;
            filterbank[i] = new fbank();
            filterbank[i].l = new RRAnalogFilter(4, center, qq, 0);
            filterbank[i].r = new RRAnalogFilter(4, center, qq, 0);
            filterbank[i].aux = new RRAnalogFilter(4, center, qq, 0);
        }

        vlp = new RRAnalogFilter(2, 4000.0f, 1.0f, 1);
        vhp = new RRAnalogFilter(3, 200.0f, 0.707f, 1);

        setbands(VOC_BANDS, 200.0f, 4000.0f);
        setpreset(Ppreset);
    }

    public void cleanup() {
        for (int k = 0; k < VOC_BANDS; k++) {
            filterbank[k].l.cleanup();
            filterbank[k].r.cleanup();
            filterbank[k].aux.cleanup();
            filterbank[k].speak = 0.0f;
            filterbank[k].gain = 0.0f;
            filterbank[k].oldgain = 0.0f;

        }
        vhp.cleanup();
        vlp.cleanup();

        compeak = compg = compenv = oldcompenv = 0.0f;
    }

    public void out(float[] smpsl, float[] smpsr) {
        final float[] tmpl = new float[RRUtilities.PERIOD];
        final float[] tmpr = new float[RRUtilities.PERIOD];
        final float[] tsmpsl = new float[RRUtilities.PERIOD];
        final float[] tsmpsr = new float[RRUtilities.PERIOD];
        final float[] tmpaux = new float[RRUtilities.PERIOD];

        float tempgain;
        float maxgain = 0.0f;
        float auxtemp, tmpgain;

        RRUtilities.memcpy(tmpaux, auxresampled, auxresampled.length);

        //apply compression to auxresampled
        for (int i = 0; i < RRUtilities.PERIOD; i++) {
            auxtemp = input * tmpaux[i];
            if (RRUtilities.fabs(auxtemp) > compeak) {
                compeak = RRUtilities.fabs(auxtemp);   //First do peak detection on the signal
            }
            compeak *= prls;
            compenv = cbeta * oldcompenv + calpha * compeak;       //Next average into envelope follower
            oldcompenv = compenv;

            if (compenv > cpthresh)                                //if envelope of signal exceeds thresh, then compress
            {
                compg = cpthresh + cpthresh * (compenv - cpthresh) / compenv;
                cpthresh = cthresh + cratio * (compg - cpthresh);   //cpthresh changes dynamically
                tmpgain = compg / compenv;
            } else {
                tmpgain = 1.0f;
            }

            if (compenv < cpthresh) {
                cpthresh = compenv;
            }
            if (cpthresh < cthresh) {
                cpthresh = cthresh;
            }

            tmpaux[i] = auxtemp * tmpgain;

            tmpaux[i] = vlp.filterout_s(tmpaux[i]);
            tmpaux[i] = vhp.filterout_s(tmpaux[i]);

        }

        //End compression

        RRUtilities.memcpy(tsmpsl, smpsl, smpsl.length);
        RRUtilities.memcpy(tsmpsr, smpsr, smpsr.length);

        RRUtilities.memset(tmpl, 0, RRUtilities.PERIOD);
        RRUtilities.memset(tmpr, 0, RRUtilities.PERIOD);

        for (int j = 0; j < VOC_BANDS; j++) {
            for (int i = 0; i < RRUtilities.PERIOD; i++) {
                auxtemp = tmpaux[i];

                if (filterbank[j].speak < gate) {
                    filterbank[j].speak = 0.0f;  //gate
                }
                if (auxtemp > maxgain) {
                    maxgain = auxtemp; //vu meter level.
                }

                auxtemp = filterbank[j].aux.filterout_s(auxtemp);
                if (RRUtilities.fabs(auxtemp) > filterbank[j].speak) {
                    filterbank[j].speak = RRUtilities.fabs(auxtemp);  //Leaky Peak detector
                }

                filterbank[j].speak *= prls;

                filterbank[j].gain = beta * filterbank[j].oldgain + alpha * filterbank[j].speak;
                filterbank[j].oldgain = filterbank[j].gain;


                tempgain = (1.0f - ringworm) * filterbank[j].oldgain + ringworm * auxtemp;

                tmpl[i] += filterbank[j].l.filterout_s(tsmpsl[i]) * tempgain;
                tmpr[i] += filterbank[j].r.filterout_s(tsmpsr[i]) * tempgain;
            }
        }

        for (int i = 0; i < RRUtilities.PERIOD; i++) {
            tmpl[i] *= lpanning * level;
            tmpr[i] *= rpanning * level;
        }

        RRUtilities.memcpy(smpsl, tmpl, RRUtilities.PERIOD);
        RRUtilities.memcpy(smpsr, tmpr, RRUtilities.PERIOD);
//        memcpy(efxoutl, tmpl, PERIOD);
//        memcpy(efxoutr, tmpr, PERIOD);

        vulevel = clamp(RRUtilities.rap2dB(maxgain), -48.0f, 15.0f);
    }

    public void setpreset(int npreset) {
        final int NUM_PRESETS = 4;
        final int[][] presets = {
        //Vocoder 1
        {0, 64, 10, 70, 70, 40, 0},
        //Vocoder 2
        {0, 64, 14, 80, 70, 40, 32},
        //Vocoder 3
        {0, 64, 20, 90, 70, 40, 64},
        //Vocoder 4
        {0, 64, 30, 100, 70, 40, 127}};

        for (int n = 0; n < presets[npreset].length; n++) {
            changepar(n, presets[npreset][n]);
        }

        Ppreset = npreset;
    }

    public void changepar(int npar, int value) {
        float tmp;
        switch (npar) {
            case PARAM_VOLUME:
                setvolume(value);
                break;
            case PARAM_PANNING:
                setpanning(value);
                break;
            case PARAM_MUFFLE:
                Pmuffle = value;
                tmp = (float) Pmuffle;
                tmp *= 0.0001f + tmp / 64000;
                alpha = RRUtilities.cSAMPLE_RATE / (RRUtilities.cSAMPLE_RATE + tmp);
                beta = 1.0f - alpha;
                break;
            case PARAM_Q:
                Pqq = value;
                tmp = (float) value;
                adjustq(tmp);
                break;
            case PARAM_INPUT:
                Pinput = value;
                input = RRUtilities.dB2rap(75.0f * (float) Pinput / 127.0f - 40.0f);
                break;
            case PARAM_LEVEL:
                Plevel = value;
                level = RRUtilities.dB2rap(60.0f * (float) Plevel / 127.0f - 40.0f);
                break;
            case PARAM_RING:
                Pring = value;
                ringworm = (float) Pring / 127.0f;
                break;
        }
    }

    public int getpar(int npar) {
        switch (npar) {
            case PARAM_VOLUME:
                return (Pvolume);
            case PARAM_PANNING:
                return (Ppanning);
            case PARAM_MUFFLE:
                return (Pmuffle);
            case PARAM_Q:
                return (Pqq);
            case PARAM_INPUT:
                return (Pinput);
            case PARAM_LEVEL:
                return (Plevel);
            case PARAM_RING:
                return (Pring);
        }
        return 0;
    }

    /*
     * Parameter control
     */
    public void setvolume(int Pvolume) {
        this.Pvolume = Pvolume;
        outvolume = (float) Pvolume / 127.0f;
        if (Pvolume == 0) {
            cleanup();
        }
    }

    public void setpanning(int Ppanning) {
        this.Ppanning = Ppanning;
        lpanning = ((float) Ppanning + 0.5f) / 127.0f;
        rpanning = 1.0f - lpanning;
    }

    private void setbands(int numbands, float startfreq, float endfreq) {
        float start = startfreq;   //useful variables
        float endband = endfreq;
        float fnumbands = (float) numbands;
        float[] output = new float[VOC_BANDS + 1];

        //calculate intermediate values
        float pwer = RRUtilities.logf(endband / start) / RRUtilities.log(2.0f);

        for (int k = 0; k <= VOC_BANDS; k++) {
            output[k] = start * RRUtilities.powf(2.0f, ((float) k) * pwer / fnumbands);
        }
        for (int k = 0; k < VOC_BANDS; k++) {
            final float o = output[k + 1] - output[k];

            filterbank[k].sfreq = output[k] + o * 0.5f;
            filterbank[k].sq = filterbank[k].sfreq / o;

            filterbank[k].l.setfreq_and_q(filterbank[k].sfreq, filterbank[k].sq);
            filterbank[k].r.setfreq_and_q(filterbank[k].sfreq, filterbank[k].sq);
            filterbank[k].aux.setfreq_and_q(filterbank[k].sfreq, filterbank[k].sq);
        }
        cleanup();
    }

    private void init_filters() {
        float ff, qq;

        for (int ii = 0; ii < VOC_BANDS; ii++) {
            ff = filterbank[ii].sfreq;
            qq = filterbank[ii].sq;
            filterbank[ii].l.setfreq_and_q(ff, qq);
            filterbank[ii].r.setfreq_and_q(ff, qq);
            filterbank[ii].aux.setfreq_and_q(ff, qq);
        }
    }

    private void adjustq(float q) {
        for (int ii = 0; ii < VOC_BANDS; ii++) {
            filterbank[ii].l.setq(q);
            filterbank[ii].r.setq(q);
            filterbank[ii].aux.setq(q);
        }
    }

    private static class fbank {
        RRAnalogFilter l, r, aux;
        float sfreq, sq, speak, gain, oldgain;
    }
}