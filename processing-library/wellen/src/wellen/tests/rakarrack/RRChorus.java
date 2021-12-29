package wellen.tests.rakarrack;

import static wellen.tests.rakarrack.RREffectLFO.TYPE_SINE;
import static wellen.tests.rakarrack.RREffectLFO.TYPE_TRIANGLE;
import static wellen.tests.rakarrack.RRUtilities.F2I;
import static wellen.tests.rakarrack.RRUtilities.MAX_CHORUS_DELAY;
import static wellen.tests.rakarrack.RRUtilities.PERIOD;
import static wellen.tests.rakarrack.RRUtilities.SAMPLE_RATE;
import static wellen.tests.rakarrack.RRUtilities.SampleStereo;
import static wellen.tests.rakarrack.RRUtilities.fPERIOD;
import static wellen.tests.rakarrack.RRUtilities.fmodf;
import static wellen.tests.rakarrack.RRUtilities.lrintf;
import static wellen.tests.rakarrack.RRUtilities.powf;

public class RRChorus {
    public static final int PRESET_CHORUS_1 = 0;
    public static final int PRESET_CHORUS_2 = 1;
    public static final int PRESET_CHORUS_3 = 2;
    public static final int PRESET_CELESTE_1 = 3;
    public static final int PRESET_CELESTE_2 = 4;
    public static final int PRESET_FLANGE_1 = 5;
    public static final int PRESET_FLANGE_2 = 6;
    public static final int PRESET_FLANGE_3 = 7;
    public static final int PRESET_FLANGE_4 = 8;
    public static final int PRESET_FLANGE_5 = 9;
    public static final int NUM_PRESETS = 10;
    public static final int PARAM_VOLUME = 0;
    public static final int PARAM_PANNING = 1;
    public static final int PARAM_LFO_FREQ = 2;
    public static final int PARAM_LFO_RND = 3;
    public static final int PARAM_LFO_TYPE = 4;
    public static final int PARAM_LFO_STEREO = 5;
    public static final int PARAM_DEPTH = 6;
    public static final int PARAM_DELAY = 7;
    public static final int PARAM_FEEDBACK = 8;
    public static final int PARAM_LEFT_RIGHT_CROSS = 9;
    public static final int PARAM_FLANGE_MODE = 10;
    public static final int PARAM_OUT_SUB = 11;
    public static final int NUM_PARAMS = 12;
    private int Pdelay;         //the delay (ms)
    private int Pdepth;         //the depth of the Chorus(ms)
    private int Pfb;            //feedback
    private boolean Pflangemode;    //how the LFO is scaled, to result chorus or flange
    private int Plrcross;       //feedback
    private boolean Poutsub;        //if I wish to substract the output instead of the adding it
    private int Ppanning;
    private int Ppreset;
    private int Pvolume;
    private float delay;
    private final float[] delayl;
    private final float[] delayr;
    private float depth;
    //    private float dl1;
    private float dl2;
    //    private int dlhi;
//    private int dlhi2;
    private int dlk;
    //    private float dllo;
    //    private float dr1;
    private float dr2;
    private int drk;
    private float fb;
    private final RREffectLFO lfo;        //lfo-ul chorus
    private final SampleStereo lfo_sample;
    private float lrcross;
    private final int maxdelay;
    //    private float mdel;
    private float outvolume;     //this is the volume of effect and is public because need it in system effect.
    private float panning;

    public RRChorus() {
        dlk = 0;
        drk = 0;
        maxdelay = lrintf(MAX_CHORUS_DELAY / 1000.0f * SAMPLE_RATE);
        delayl = new float[maxdelay];
        delayr = new float[maxdelay];
        lfo_sample = new SampleStereo();
        lfo = new RREffectLFO();

        Ppreset = PRESET_CHORUS_1;
        setpreset(Ppreset);

        lfo.effectlfoout(lfo_sample);
        dl2 = getdelay(lfo_sample.left);
        dr2 = getdelay(lfo_sample.right);
        cleanup();
    }

    public void reset() {
        dlk = 0;
        drk = 0;
        dl2 = 0;
        dr2 = 0;
        lfo.reset();
    }

    public void out(float[] smpsl) {
        out(smpsl, null);
    }

    public void out(float[] smpsl, float[] smpsr) {
        final float[] efxoutl = smpsl;
        final float[] efxoutr = smpsr;

        float dl1 = dl2;
        float dr1 = dr2;
        lfo.effectlfoout(lfo_sample);

        dl2 = getdelay(lfo_sample.left);
        dr2 = getdelay(lfo_sample.right);

        for (int i = 0; i < PERIOD; i++) {
            float inl = smpsl[i];
            float inr = 0.0f;
            if (efxoutr != null) {
                inr = smpsr[i];

                //LRcross
                float l = inl;
                float r = inr;
                inl = l * (1.0f - lrcross) + r * lrcross;
                inr = r * (1.0f - lrcross) + l * lrcross;
            }

            //Left channel

            //compute the delay in samples using linear interpolation between the lfo delays
            float mdel = (dl1 * (float) (PERIOD - i) + dl2 * (float) i) / fPERIOD;
            if (++dlk >= maxdelay) {
                dlk = 0;
            }
            float tmp = (float) dlk - mdel + (float) maxdelay * 2.0f;    //where should I get the sample from

            int dlhi = F2I(tmp);
            dlhi %= maxdelay;

            int dlhi2 = (dlhi - 1 + maxdelay) % maxdelay;
            float dllo = 1.0f - fmodf(tmp, 1.0f);
            efxoutl[i] = delayl[dlhi2] * dllo + delayl[dlhi] * (1.0f - dllo);
            delayl[dlk] = inl + efxoutl[i] * fb;

            //Right channel

            if (efxoutr != null) {
                //compute the delay in samples using linear interpolation between the lfo delays
                mdel = (dr1 * (float) (PERIOD - i) + dr2 * (float) i) / fPERIOD;
                if (++drk >= maxdelay) {
                    drk = 0;
                }
                tmp = (float) drk - mdel + (float) maxdelay * 2.0f;    //where should I get the sample from

                dlhi = F2I(tmp);
                dlhi %= maxdelay;

                dlhi2 = (dlhi - 1 + maxdelay) % maxdelay;
                dllo = 1.0f - fmodf(tmp, 1.0f);
                efxoutr[i] = delayr[dlhi2] * dllo + delayr[dlhi] * (1.0f - dllo);
                delayr[dlk] = inr + efxoutr[i] * fb;
            }
        }

        if (Poutsub) {
            for (int i = 0; i < PERIOD; i++) {
                efxoutl[i] *= -1.0f;
                if (efxoutr != null) {
                    efxoutr[i] *= -1.0f;
                }
            }
        }

        for (int i = 0; i < PERIOD; i++) {
            efxoutl[i] *= panning;
            if (efxoutr != null) {
                efxoutr[i] *= (1.0f - panning);
            }
        }
    }

    public void cleanup() {
        for (int i = 0; i < maxdelay; i++) {
            delayl[i] = 0.0f;
            delayr[i] = 0.0f;
        }
    }

    public void setpreset(int npreset) {
        final int[][] presets = {
        //Chorus1
        {64, 64, 33, 0, TYPE_SINE, 90, 40, 85, 64, 119, 0, 0},
        //Chorus2
        {64, 64, 19, 0, TYPE_SINE, 98, 56, 90, 64, 19, 0, 0},
        //Chorus3
        {64, 64, 7, 0, TYPE_TRIANGLE, 42, 97, 95, 90, 127, 0, 0},
        //Celeste1
        {64, 64, 1, 0, TYPE_SINE, 42, 115, 18, 90, 127, 0, 0},
        //Celeste2
        {64, 64, 7, 117, TYPE_SINE, 50, 115, 9, 31, 127, 0, 1},
        //Flange1
        {64, 64, 39, 0, TYPE_SINE, 60, 23, 3, 62, 0, 0, 0},
        //Flange2
        {64, 64, 9, 34, TYPE_TRIANGLE, 40, 35, 3, 109, 0, 0, 0},
        //Flange3
        {64, 64, 31, 34, TYPE_TRIANGLE, 94, 35, 3, 54, 0, 0, 1},
        //Flange4
        {64, 64, 14, 0, TYPE_TRIANGLE, 62, 12, 19, 97, 0, 0, 0},
        //Flange5
        {64, 64, 34, 105, TYPE_SINE, 24, 39, 19, 17, 0, 0, 1}};

        npreset %= presets.length;
        for (int n = 0; n < presets[npreset].length; n++) {
            changepar(n, presets[npreset][n]);
        }
        Ppreset = npreset;
    }

    public void changepar(int npar, int value) {
        switch (npar) {
            case PARAM_VOLUME:
                setvolume(value);
                break;
            case PARAM_PANNING:
                setpanning(value);
                break;
            case PARAM_LFO_FREQ:
                lfo.Pfreq = value;
                lfo.updateparams();
                break;
            case PARAM_LFO_RND:
                lfo.Prandomness = value;
                lfo.updateparams();
                break;
            case PARAM_LFO_TYPE:
                lfo.PLFOtype = value;
                lfo.updateparams();
                break;
            case PARAM_LFO_STEREO:
                lfo.Pstereo = value;
                lfo.updateparams();
                break;
            case PARAM_DEPTH:
                setdepth(value);
                break;
            case PARAM_DELAY:
                setdelay(value);
                break;
            case PARAM_FEEDBACK:
                setfb(value);
                break;
            case PARAM_LEFT_RIGHT_CROSS:
                setlrcross(value);
                break;
            case PARAM_FLANGE_MODE:
                Pflangemode = value > 0;
                break;
            case PARAM_OUT_SUB:
                Poutsub = value > 0;
                break;
        }
    }

    public int getpar(int npar) {
        switch (npar) {
            case PARAM_VOLUME:
                return (Pvolume);
            case PARAM_PANNING:
                return (Ppanning);
            case PARAM_LFO_FREQ:
                return (lfo.Pfreq);
            case PARAM_LFO_RND:
                return (lfo.Prandomness);
            case PARAM_LFO_TYPE:
                return (lfo.PLFOtype);
            case PARAM_LFO_STEREO:
                return (lfo.Pstereo);
            case PARAM_DEPTH:
                return (Pdepth);
            case PARAM_DELAY:
                return (Pdelay);
            case PARAM_FEEDBACK:
                return (Pfb);
            case PARAM_LEFT_RIGHT_CROSS:
                return (Plrcross);
            case PARAM_FLANGE_MODE:
                return (Pflangemode ? 1 : 0);
            case PARAM_OUT_SUB:
                return (Poutsub ? 1 : 0);
            default:
                return (0);
        }
    }

    /*
     * get the delay value in samples; xlfo is the current lfo value
     */
    private float getdelay(float xlfo) {
        float result;
        if (!Pflangemode) {
            result = (delay + xlfo * depth) * SAMPLE_RATE;
        } else {
            result = 0;
        }

        //check if it is too big delay(caused by errornous setdelay() and setdepth()
        if ((result + 0.5) >= maxdelay) {
//            fprintf (stderr, "%s",
//                     "WARNING: Chorus.C::getdelay(..) too big delay (see setdelay and setdepth funcs.)\n");
//            printf ("%f %d\n", result, maxdelay);
            result = (float) maxdelay - 1.0f;
        }
        return (result);
    }

    private void setdepth(int Pdepth) {
        this.Pdepth = Pdepth;
        depth = (powf(8.0f, ((float) Pdepth / 127.0f) * 2.0f) - 1.0f) / 1000.0f;    //seconds
    }

    private void setdelay(int Pdelay) {
        this.Pdelay = Pdelay;
        delay = (powf(10.0f, ((float) Pdelay / 127.0f) * 2.0f) - 1.0f) / 1000.0f;    //seconds
    }

    private void setfb(int Pfb) {
        this.Pfb = Pfb;
        fb = ((float) Pfb - 64.0f) / 64.1f;
    }

    private void setvolume(int Pvolume) {
        this.Pvolume = Pvolume;
        outvolume = (float) Pvolume / 127.0f;
    }

    private void setpanning(int Ppanning) {
        this.Ppanning = Ppanning;
        panning = ((float) Ppanning + .5f) / 127.0f;
    }

    private void setlrcross(int Plrcross) {
        this.Plrcross = Plrcross;
        lrcross = (float) Plrcross / 127.0f;
    }
}