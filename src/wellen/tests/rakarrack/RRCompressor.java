package wellen.tests.rakarrack;

import static wellen.tests.rakarrack.RRUtilities.LOG_2;
import static wellen.tests.rakarrack.RRUtilities.PERIOD;
import static wellen.tests.rakarrack.RRUtilities.SAMPLE_RATE;
import static wellen.tests.rakarrack.RRUtilities.cSAMPLE_RATE;
import static wellen.tests.rakarrack.RRUtilities.dB2rap;
import static wellen.tests.rakarrack.RRUtilities.fabs;
import static wellen.tests.rakarrack.RRUtilities.fabsf;
import static wellen.tests.rakarrack.RRUtilities.logf;
import static wellen.tests.rakarrack.RRUtilities.rap2dB;

public class RRCompressor {

    /*
    "Compressor A.Time","142","1",
    "Compressor Knee","145","1",
    "Compressor Output","147","1",
    "Compressor Ratio","144","1",
    "Compressor R.Time","143","1",
    "Compressor Threshold","146","1",
    */

    public static final int PRESET_2_to_1 = 0;
    public static final int PRESET_4_to_1 = 1;
    public static final int PRESET_8_to_1 = 2;
    public static final int PRESET_FINAL_LIMITER = 3;
    public static final int PRESET_HARMONIC_ENHANCER = 4;
    public static final int PRESET_BAND_COMP_BAND = 5;
    public static final int PRESET_END_COMP_BAND = 6;
    public static final int NUM_PRESETS = 7;
    public static final int PARAM_THRESHOLD = 0;
    public static final int PARAM_RATIO = 1;
    public static final int PARAM_OUTPUT = 2;
    public static final int PARAM_ATT = 3;
    public static final int PARAM_REL = 4;
    public static final int PARAM_A_OUT = 5;
    public static final int PARAM_KNEE = 6;
    public static final int PARAM_STEREO = 7;
    public static final int PARAM_PEAK = 8;
    public static final int NUM_PARAMS = 9;
    private static final float MIN_GAIN = 0.00001f;        // -100dB  This will help prevent evaluation of denormal
    public boolean a_out;
    public int clipping;
    public int limit;
    public boolean peak;
    public boolean stereo;
    public int tatt;            // attack time  (ms)
    public int tknee;
    public int toutput;
    public int tratio;
    public int trel;            // release time (ms)
    public int tthreshold;
    private int Ppreset;
    private float att, attr, attl;
    private float coeff_kk;
    private float coeff_knee;
    private float coeff_kratio;
    private float coeff_ratio;
    private float eratio;            // dynamic ratio
    private final int hold;
    private float knee;
    private float kpct;
    private float kratio;            // ratio maximum for knee region
    private float lgain;
    private float lgain_old;
    private float lgain_t;
    private float lpeak;
    private int ltimer;
    private float lvolume;
    private float lvolume_db;
    private float makeup;            // make-up gain
    private float makeuplin;
    private float outlevel;
    private float ratio;            // ratio
    private float rel, relr, rell;
    private float relcnst, attfinal;
    private float rgain;
    private float rgain_old;
    private float rgain_t;
    private float rpeak;
    private int rtimer;
    private float rvolume;
    private float rvolume_db;
    private float thres_db;        // threshold
    private float thres_mx;

    public RRCompressor() {
        rvolume = 0.0f;
        rvolume_db = 0.0f;
        lvolume = 0.0f;
        lvolume_db = 0.0f;
        tthreshold = -24;
        tratio = 4;
        toutput = -10;
        tatt = 20;
        trel = 50;
        a_out = true;
        stereo = false;
        tknee = 30;
        rgain = 1.0f;
        rgain_old = 1.0f;
        lgain = 1.0f;
        lgain_old = 1.0f;
        lgain_t = 1.0f;
        rgain_t = 1.0f;
        ratio = 1.0f;
        kpct = 0.0f;
        peak = false;
        lpeak = 0.0f;
        rpeak = 0.0f;
        rell = relr = attr = attl = 1.0f;

        ltimer = rtimer = 0;
        hold = (int) (SAMPLE_RATE * 0.0125);  //12.5ms
        clipping = 0;
        limit = 0;

        Ppreset = PRESET_2_to_1;
        setpreset(Ppreset);
    }

    public void cleanup() {
        lgain = rgain = 1.0f;
        lgain_old = rgain_old = 1.0f;
        rpeak = 0.0f;
        lpeak = 0.0f;
        limit = 0;
        clipping = 0;
    }

    public void changepar(int np, int value) {
        switch (np) {
            case PARAM_THRESHOLD:
                tthreshold = value;
                thres_db = (float) tthreshold;    //implicit type cast int to float
                break;
            case PARAM_RATIO:
                tratio = value;
                ratio = (float) tratio;
                break;
            case PARAM_OUTPUT:
                toutput = value;
                break;
            case PARAM_ATT:
                tatt = value;
                att = cSAMPLE_RATE / (((float) value / 1000.0f) + cSAMPLE_RATE);
                attr = att;
                attl = att;
                break;
            case PARAM_REL:
                trel = value;
                rel = cSAMPLE_RATE / (((float) value / 1000.0f) + cSAMPLE_RATE);
                rell = rel;
                relr = rel;
                break;
            case PARAM_A_OUT:
                a_out = (value > 0);
                break;
            case PARAM_KNEE:
                tknee = value;  //knee expressed a percentage of range between thresh and zero dB
                kpct = (float) tknee / 100.1f;
                break;
            case PARAM_STEREO:
                stereo = (value > 0);
                break;
            case PARAM_PEAK:
                peak = (value > 0);
                break;
        }

        kratio = logf(ratio) / LOG_2;  //  Log base 2 relationship matches slope
        knee = -kpct * thres_db;

        coeff_kratio = 1.0f / kratio;
        coeff_ratio = 1.0f / ratio;
        coeff_knee = 1.0f / knee;

        coeff_kk = knee * coeff_kratio;

        thres_mx = thres_db + knee;  //This is the value of the input when the output is at t+k
        makeup = -thres_db - knee / kratio + thres_mx / ratio;
        makeuplin = dB2rap(makeup);
        if (a_out) {
            outlevel = dB2rap((float) toutput) * makeuplin;
        } else {
            outlevel = dB2rap((float) toutput);
        }
    }

    public int getpar(int np) {
        switch (np) {
            case PARAM_THRESHOLD:
                return (tthreshold);
            case PARAM_RATIO:
                return (tratio);
            case PARAM_OUTPUT:
                return (toutput);
            case PARAM_ATT:
                return (tatt);
            case PARAM_REL:
                return (trel);
            case PARAM_A_OUT:
                return (a_out ? 1 : 0);
            case PARAM_KNEE:
                return (tknee);
            case PARAM_STEREO:
                return (stereo ? 1 : 0);
            case PARAM_PEAK:
                return (peak ? 1 : 0);
        }
        return 0;
    }

    public void setpreset(int npreset) {
        final int NUM_PRESETS = 7;
        int[][] presets = {
        //2:1
        {-30, 2, -6, 20, 120, 1, 0, 0, 0},
        //4:1
        {-26, 4, -8, 20, 120, 1, 10, 0, 0},
        //8:1
        {-24, 8, -12, 20, 35, 1, 30, 0, 0},
        //Final Limiter
        {-1, 15, 0, 5, 250, 0, 0, 1, 1},
        //HarmonicEnhancer
        {-20, 15, -3, 5, 50, 0, 0, 1, 1},
        //Band CompBand
        {-3, 2, 0, 5, 50, 1, 0, 1, 0},
        //End CompBand
        {-60, 2, 0, 10, 500, 1, 0, 1, 1},};

        for (int n = 0; n < presets[npreset].length; n++) {
            changepar(n, presets[npreset][n]);
        }

        Ppreset = npreset;
    }

    public void out(float[] efxoutl, float[] efxoutr) {
        for (int i = 0; i < PERIOD; i++) {
            float rdelta;
            float ldelta;

            //Right Channel
            if (peak) {
                if (rtimer > hold) {
                    rpeak *= 0.9998f;   //The magic number corresponds to ~0.1s based on T/(RC + T), 
                    rtimer--;
                }
                if (ltimer > hold) {
                    lpeak *= 0.9998f;    //leaky peak detector.
                    ltimer--;  //keeps the timer from eventually exceeding max int & rolling over
                }
                ltimer++;
                rtimer++;
                if (rpeak < fabs(efxoutr[i])) {
                    rpeak = fabs(efxoutr[i]);
                    rtimer = 0;
                }
                if (lpeak < fabs(efxoutl[i])) {
                    lpeak = fabs(efxoutl[i]);
                    ltimer = 0;
                }

                if (lpeak > 20.0f) {
                    lpeak = 20.0f;
                }
                if (rpeak > 20.0f) {
                    rpeak = 20.0f; //keeps limiter from getting locked up when signal levels go way out of bounds
                }
                // (like hundreds)

            } else {
                rpeak = efxoutr[i];
                lpeak = efxoutl[i];
            }

            if (stereo) {
                rdelta = fabsf(rpeak);
                if (rvolume < 0.9f) {
                    attr = att;
                    relr = rel;
                } else if (rvolume < 1.0f) {
                    attr = att + ((1.0f - att) * (rvolume - 0.9f) * 10.0f);    //dynamically change attack time for
                    // limiting mode
                    relr = rel / (1.0f + (rvolume - 0.9f) * 9.0f);  //release time gets longer when signal is above
                    // limiting
                } else {
                    attr = 1.0f;
                    relr = rel * 0.1f;
                }

                if (rdelta > rvolume) {
                    rvolume = attr * rdelta + (1.0f - attr) * rvolume;
                } else {
                    rvolume = relr * rdelta + (1.0f - relr) * rvolume;
                }


                rvolume_db = rap2dB(rvolume);
                if (rvolume_db < thres_db) {
                    rgain = outlevel;
                } else if (rvolume_db < thres_mx) {
                    //Dynamic ratio that depends on volume.  As can be seen, ratio starts
                    //at something negligibly larger than 1 once volume exceeds thres, and increases toward selected
                    // ratio by the time it has reached thres_mx.  --Transmogrifox

                    eratio = 1.0f + (kratio - 1.0f) * (rvolume_db - thres_db) * coeff_knee;
                    rgain = outlevel * dB2rap(thres_db + (rvolume_db - thres_db) / eratio - rvolume_db);
                } else {
                    rgain = outlevel * dB2rap(thres_db + coeff_kk + (rvolume_db - thres_mx) * coeff_ratio - rvolume_db);
                    limit = 1;
                }

                if (rgain < MIN_GAIN) {
                    rgain = MIN_GAIN;
                }
                rgain_t = .4f * rgain + .6f * rgain_old;
            }

            //Left Channel
            if (stereo) {
                ldelta = fabsf(lpeak);
            } else {
                ldelta = 0.5f * (fabsf(lpeak) + fabsf(rpeak));
            }
            //It's not as efficient to check twice, but it's small expense worth code clarity

            if (lvolume < 0.9f) {
                attl = att;
                rell = rel;
            } else if (lvolume < 1.0f) {
                attl = att + ((1.0f - att) * (lvolume - 0.9f) * 10.0f);    //dynamically change attack time for
                // limiting mode
                rell = rel / (1.0f + (lvolume - 0.9f) * 9.0f);  //release time gets longer when signal is above limiting
            } else {
                attl = 1.0f;
                rell = rel * 0.1f;
            }

            if (ldelta > lvolume) {
                lvolume = attl * ldelta + (1.0f - attl) * lvolume;
            } else {
                lvolume = rell * ldelta + (1.0f - rell) * lvolume;
            }

            lvolume_db = rap2dB(lvolume);

            if (lvolume_db < thres_db) {
                lgain = outlevel;
            } else if (lvolume_db < thres_mx)  //knee region
            {
                eratio = 1.0f + (kratio - 1.0f) * (lvolume_db - thres_db) * coeff_knee;
                lgain = outlevel * dB2rap(thres_db + (lvolume_db - thres_db) / eratio - lvolume_db);
            } else {
                lgain = outlevel * dB2rap(thres_db + coeff_kk + (lvolume_db - thres_mx) * coeff_ratio - lvolume_db);
                limit = 1;
            }

            if (lgain < MIN_GAIN) {
                lgain = MIN_GAIN;
            }
            lgain_t = .4f * lgain + .6f * lgain_old;

            if (stereo) {
                efxoutl[i] *= lgain_t;
                efxoutr[i] *= rgain_t;
                rgain_old = rgain;
                lgain_old = lgain;
            } else {
                efxoutl[i] *= lgain_t;
                efxoutr[i] *= lgain_t;
                lgain_old = lgain;
            }

            if (peak) {
                if (efxoutl[i] > 0.999f) {            //output hard limiting
                    efxoutl[i] = 0.999f;
                    clipping = 1;
                }
                if (efxoutl[i] < -0.999f) {
                    efxoutl[i] = -0.999f;
                    clipping = 1;
                }
                if (efxoutr[i] > 0.999f) {
                    efxoutr[i] = 0.999f;
                    clipping = 1;
                }
                if (efxoutr[i] < -0.999f) {
                    efxoutr[i] = -0.999f;
                    clipping = 1;
                }
                //highly probably there is a more elegant way to do that, but what the hey...    
            }
        }
    }
}
