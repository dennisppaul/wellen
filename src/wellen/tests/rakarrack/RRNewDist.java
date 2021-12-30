package wellen.tests.rakarrack;

import static wellen.tests.rakarrack.RRUtilities.PERIOD;
import static wellen.tests.rakarrack.RRUtilities.dB2rap;
import static wellen.tests.rakarrack.RRUtilities.expf;
import static wellen.tests.rakarrack.RRUtilities.logf;
import static wellen.tests.rakarrack.RRUtilities.powf;

public class RRNewDist {

    public static final int PRESET_NEW_DIST_1 = 0;
    public static final int PRESET_NEW_DIST_2 = 1;
    public static final int PRESET_NEW_DIST_3 = 2;
    public static final int NUM_PRESETS = 3;

    private final RRAnalogFilter DCl;
    private final RRAnalogFilter DCr;
    private int Pdrive;             //the input amplification
    private int Phpf;               //highpass filter
    private int Plevel;             //the ouput amplification
    private int Plpf;               //lowpass filter
    private int Plrcross;           // L/R Mixing
    private int Pnegate;            //if the input is negated
    private int Poctave;            //mix sub octave
    private int Ppanning;           //Panning
    private boolean Pprefiltering;  //if you want to do the filtering before the distorsion
    private int Ppreset;
    private int Prfreq;
    private int Ptype;              //Distorsion type
    private int Pvolume;            //Volumul or E/R
    private final RRAnalogFilter blockDCl;
    private final RRAnalogFilter blockDCr;
    private final RRFilter filterl;
    private final RRFilterParams filterpars;
    private final RRFilter filterr;
    private final RRAnalogFilter hpfl;
    private final RRAnalogFilter hpfr;
    //    private final float[] inpll = new float[4096];
    //    private final float[] inplr = new float[4096];
    private final RRAnalogFilter lpfl;
    private final RRAnalogFilter lpfr;
    private float lrcross;
    private float octave_memoryl;
    private float octave_memoryr;
    private float octmix;
    private final float[] octoutl;
    private final float[] octoutr;
    private float outvolume;
    private float panning;
    private float rfreq;
    private float togglel;
    private float toggler;
    private final RRWaveShaper wshapel;
    private final RRWaveShaper wshaper;

    public RRNewDist() {
        octoutl = new float[PERIOD];
        octoutr = new float[PERIOD];

        lpfl = new RRAnalogFilter(2, 22000, 1, 0);
        lpfr = new RRAnalogFilter(2, 22000, 1, 0);
        hpfl = new RRAnalogFilter(3, 20, 1, 0);
        hpfr = new RRAnalogFilter(3, 20, 1, 0);
        blockDCl = new RRAnalogFilter(2, 75.0f, 1, 0);
        blockDCr = new RRAnalogFilter(2, 75.0f, 1, 0);
        wshapel = new RRWaveShaper();
        wshaper = new RRWaveShaper();

        blockDCl.setfreq(75.0f);
        blockDCr.setfreq(75.0f);

        DCl = new RRAnalogFilter(3, 30, 1, 0);
        DCr = new RRAnalogFilter(3, 30, 1, 0);
        DCl.setfreq(30.0f);
        DCr.setfreq(30.0f);

        filterpars = new RRFilterParams(0, 64, 64);
        filterpars.Pcategory = 2;
        filterpars.Ptype = 0;
        filterpars.Pfreq = 72;
        filterpars.Pq = 76;
        filterpars.Pstages = 0;
        filterpars.Pgain = 76;

        filterl = new RRFilter(filterpars);
        filterr = new RRFilter(filterpars);

        //default values
        Ppreset = 0;
        Pvolume = 50;
        Plrcross = 40;
        Pdrive = 1;
        Plevel = 32;
        Ptype = 0;
        Pnegate = 0;
        Plpf = 127;
        Phpf = 0;
        Prfreq = 64;
        Pprefiltering = false;
        Poctave = 0;
        togglel = 1.0f;
        octave_memoryl = -1.0f;
        toggler = 1.0f;
        octave_memoryr = -1.0f;
        octmix = 0.0f;

        Ppreset = 0;
        setpreset(Ppreset);

        cleanup();
    }

    public void cleanup() {
        lpfl.cleanup();
        hpfl.cleanup();
        lpfr.cleanup();
        hpfr.cleanup();
        blockDCr.cleanup();
        blockDCl.cleanup();
        DCl.cleanup();
        DCr.cleanup();
    }

    public void applyfilters(float[] efxoutl, float[] efxoutr) {
        lpfl.filterout(efxoutl);
        hpfl.filterout(efxoutl);
        lpfr.filterout(efxoutr);
        hpfr.filterout(efxoutr);
    }

    public void out(float[] smpsl, float[] smpsr) {
        float[] efxoutl = smpsl;
        float[] efxoutr = smpsr;

//        float inputvol = .5f;
//
//        if (Pnegate != 0) {
//            inputvol *= -1.0f;
//        }

        if (Pprefiltering) {
            applyfilters(efxoutl, efxoutr);
        }

        wshapel.waveshapesmps(PERIOD, efxoutl, Ptype, Pdrive, true);
        wshaper.waveshapesmps(PERIOD, efxoutr, Ptype, Pdrive, true);

        if (octmix > 0.01f) {
            for (int i = 0; i < PERIOD; i++) {
                float lout = efxoutl[i];
                float rout = efxoutr[i];

                if ((octave_memoryl < 0.0f) && (lout > 0.0f)) {
                    togglel *= -1.0f;
                }
                octave_memoryl = lout;

                if ((octave_memoryr < 0.0f) && (rout > 0.0f)) {
                    toggler *= -1.0f;
                }
                octave_memoryr = rout;

                octoutl[i] = lout * togglel;
                octoutr[i] = rout * toggler;
            }
            blockDCr.filterout(octoutr);
            blockDCl.filterout(octoutl);
        }

        filterl.filterout(efxoutl);
        filterr.filterout(efxoutr);

        if (!Pprefiltering) {
            applyfilters(efxoutl, efxoutr);
        }

        float level = dB2rap(60.0f * (float) Plevel / 127.0f - 40.0f);

        for (int i = 0; i < PERIOD; i++) {
            float lout = efxoutl[i];
            float rout = efxoutr[i];

            float l = lout * (1.0f - lrcross) + rout * lrcross;
            float r = rout * (1.0f - lrcross) + lout * lrcross;

            if (octmix > 0.01f) {
                lout = l * (1.0f - octmix) + octoutl[i] * octmix;
                rout = r * (1.0f - octmix) + octoutr[i] * octmix;
            } else {
                lout = l;
                rout = r;
            }

            efxoutl[i] = lout * level * panning;
            efxoutr[i] = rout * level * (1.0f - panning);
        }

        DCr.filterout(efxoutr);
        DCl.filterout(efxoutl);
    }

    public void setvolume(int Pvolume) {
        this.Pvolume = Pvolume;
        outvolume = (float) Pvolume / 127.0f;
        if (Pvolume == 0) {
            cleanup();
        }
    }

    public void setpanning(int Ppanning) {
        this.Ppanning = Ppanning;
        panning = ((float) Ppanning + 0.5f) / 127.0f;
    }


    public void setlrcross(int Plrcross) {
        this.Plrcross = Plrcross;
        lrcross = (float) Plrcross / 127.0f * 1.0f;
    }

    public void setlpf(int value) {
        Plpf = value;
        float fr = (float) Plpf;
        lpfl.setfreq(fr);
        lpfr.setfreq(fr);
    }

    public void sethpf(int value) {
        Phpf = value;
        float fr = (float) Phpf;
        hpfl.setfreq(fr);
        hpfr.setfreq(fr);
    }

    public void setoctave(int Poctave) {
        this.Poctave = Poctave;
        octmix = (float) (Poctave) / 127.0f;
    }

    public void setpreset(int npreset) {
        final int PRESET_SIZE = 11;
        final int NUM_PRESETS = 3;
        int[][] presets = {
        //NewDist 1
        {0, 64, 64, 83, 85, 15, 0, 2437, 169, 68, 0},
        //NewDist 2
        {0, 64, 64, 95, 75, 6, 0, 3459, 209, 60, 1},
        //NewDist 3
        {0, 64, 64, 43, 97, 16, 0, 2983, 118, 83, 0}};

        for (int n = 0; n < PRESET_SIZE; n++) {
            changepar(n, presets[npreset][n]);
        }

        Ppreset = npreset;
        cleanup();
    }

    public void changepar(int npar, int value) {
        switch (npar) {
            case 0:
                setvolume(value);
                break;
            case 1:
                setpanning(value);
                break;
            case 2:
                setlrcross(value);
                break;
            case 3:
                Pdrive = value;
                break;
            case 4:
                Plevel = value;
                break;
            case 5:
                Ptype = value;
                break;
            case 6:
                if (value > 1) {
                    value = 1;
                }
                Pnegate = value;
                break;
            case 7:
                setlpf(value);
                break;
            case 8:
                sethpf(value);
                break;
            case 9:
                Prfreq = value;
                rfreq = expf(powf((float) value / 127.0f, 0.5f) * logf(25000.0f)) + 40.0f;
                filterl.setfreq(rfreq);
                filterr.setfreq(rfreq);

                break;
            case 10:
                Pprefiltering = value > 0;
                break;
            case 11:
                setoctave(value);
                break;
        }
    }

    public int getpar(int npar) {
        switch (npar) {
            case 0:
                return (Pvolume);
            case 1:
                return (Ppanning);
            case 2:
                return (Plrcross);
            case 3:
                return (Pdrive);
            case 4:
                return (Plevel);
            case 5:
                return (Ptype);
            case 6:
                return (Pnegate);
            case 7:
                return (Plpf);
            case 8:
                return (Phpf);
            case 9:
                return (Prfreq);
            case 10:
                return (Pprefiltering ? 1 : 0);
            case 11:
                return (Poctave);
        }
        return (0);
    }
}