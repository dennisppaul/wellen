package wellen.tests.rakarrack;

import static wellen.tests.rakarrack.RRUtilities.PERIOD;
import static wellen.tests.rakarrack.RRUtilities.dB2rap;
import static wellen.tests.rakarrack.RRUtilities.fabs;
import static wellen.tests.rakarrack.RRUtilities.memcpy;

public class RRStompBox {

    /*
    "StompBox Level","334","39",
    "StompBox Gain","335","39",
    "StompBox Low","336","39",
    "StompBox Mid","337","39",
    "StompBox High","338","39",
    */

    public static final int TYPE_ODIE = 0;
    public static final int TYPE_GRUNGE = 1;
    public static final int TYPE_PRO_CO_RAT = 2;
    public static final int TYPE_FAT_CAT = 3;
    public static final int TYPE_MXR_DIST_PLUS = 4;
    public static final int TYPE_DEATH_METAL = 5;
    public static final int TYPE_METAL_ZONE = 6;
    public static final int TYPE_CLASSIC_FUZZ = 7;
    public static final int NUM_TYPES = 8;

    public static final int PRESET_ODIE = 0;
    public static final int PRESET_GRUNGER = 1;
    public static final int PRESET_HARD_DISTORTION = 2;
    public static final int PRESET_RATTY = 3;
    public static final int PRESET_CLASSIC_DISTORTION = 4;
    public static final int PRESET_MORBIND_IMPALEMENT = 5;
    public static final int PRESET_MID_ELVE = 6;
    public static final int PRESET_FUZZ = 7;
    public static final int NUM_PRESETS = 8;

    private float HG;
    private float LG;
    private float MG;
    private int Pgain;
    private int Phigh;
    private int Plow;
    private int Pmid;
    private int Pmode;
    private int Ppreset;
    private int Pvolume;
    private float RGP2;
    private float RGPST;
    private final float[] efxoutl;
    private final float[] efxoutr;
    private float gain;
    private float highb;
    private final RRAnalogFilter lanti;
    private final RRAnalogFilter linput;
    private float lowb;
    private final RRAnalogFilter lpost;
    private final RRAnalogFilter lpre1;
    private final RRAnalogFilter lpre2;
    private final RRAnalogFilter ltonehg;
    private final RRAnalogFilter ltonelw;
    private final RRAnalogFilter ltonemd;
    private final RRWaveShaper lwshape;
    private final RRWaveShaper lwshape2;
    private float midb;
    private float pgain;
    private float pre1gain;
    private float pre2gain;
    private final RRAnalogFilter ranti;
    private final RRAnalogFilter rinput;
    private final RRAnalogFilter rpost;
    private final RRAnalogFilter rpre1;
    private final RRAnalogFilter rpre2;
    private final RRAnalogFilter rtonehg;
    private final RRAnalogFilter rtonelw;
    private final RRAnalogFilter rtonemd;
    private final RRWaveShaper rwshape;
    private final RRWaveShaper rwshape2;
    private float volume;

    public RRStompBox() {
        efxoutl = new float[PERIOD];
        efxoutr = new float[PERIOD];

        //default values
        Ppreset = PRESET_ODIE;
        Pvolume = 50;

        //left channel filters
        linput = new RRAnalogFilter(1, 80.0f, 1.0f, 0);
        lpre1 = new RRAnalogFilter(1, 630.0f, 1.0f, 0);   // LPF = 0, HPF = 1
        lpre2 = new RRAnalogFilter(1, 220.0f, 1.0f, 0);
        lpost = new RRAnalogFilter(0, 720.0f, 1.0f, 0);
        ltonehg = new RRAnalogFilter(1, 1500.0f, 1.0f, 0);
        ltonemd = new RRAnalogFilter(4, 1000.0f, 1.0f, 0);
        ltonelw = new RRAnalogFilter(0, 500.0f, 1.0f, 0);

        //Right channel filters
        rinput = new RRAnalogFilter(1, 80.0f, 1.0f, 0);
        rpre1 = new RRAnalogFilter(1, 630.0f, 1.0f, 0);   // LPF = 0, HPF = 1
        rpre2 = new RRAnalogFilter(1, 220.0f, 1.0f, 0);
        rpost = new RRAnalogFilter(0, 720.0f, 1.0f, 0);
        rtonehg = new RRAnalogFilter(1, 1500.0f, 1.0f, 0);
        rtonemd = new RRAnalogFilter(4, 1000.0f, 1.0f, 0);
        rtonelw = new RRAnalogFilter(0, 500.0f, 1.0f, 0);

        //Anti-aliasing for between stages
        ranti = new RRAnalogFilter(0, 6000.0f, 0.707f, 1);
        lanti = new RRAnalogFilter(0, 6000.0f, 0.707f, 1);

        rwshape = new RRWaveShaper();
        lwshape = new RRWaveShaper();
        rwshape2 = new RRWaveShaper();
        lwshape2 = new RRWaveShaper();

        cleanup();

        setpreset(Ppreset);
    }

    /*
     * Cleanup the effect
     */
    public void cleanup() {
        linput.cleanup();
        lpre1.cleanup();
        lpre2.cleanup();
        lpost.cleanup();
        ltonehg.cleanup();
        ltonemd.cleanup();
        ltonelw.cleanup();

        //right channel filters
        rinput.cleanup();
        rpre1.cleanup();
        rpre2.cleanup();
        rpost.cleanup();
        rtonehg.cleanup();
        rtonemd.cleanup();
        rtonelw.cleanup();

        ranti.cleanup();
        lanti.cleanup();

        rwshape.cleanup();
        lwshape.cleanup();
        rwshape2.cleanup();
        lwshape2.cleanup();
    }

    /*
     * Effect output
     */
    public void out(float[] smpsl, float[] smpsr) {
        switch (Pmode) {
            case TYPE_ODIE:
                sb_odie(smpsl, smpsr);
                break;
            case TYPE_GRUNGE:
            case TYPE_DEATH_METAL:
            case TYPE_METAL_ZONE:
                sb_grunge_death_metal_zone(smpsl, smpsr);
                break;
            case TYPE_PRO_CO_RAT:
            case TYPE_FAT_CAT:
                sb_rat_fat_cat(smpsl, smpsr);
                break;
            case TYPE_MXR_DIST_PLUS:
                sb_dist_plus(smpsl, smpsr);
                break;
            case TYPE_CLASSIC_FUZZ:
                sb_classic_fuzz(smpsl, smpsr);
                break;
        }
        memcpy(smpsl, efxoutl, efxoutl.length);
        memcpy(smpsr, efxoutr, efxoutr.length);
    }

    public void changepar(int npar, int value) {
        switch (npar) {
            case 0:
                setvolume(value);
                break;
            case 1:
                set_high(value);
                break;
            case 2:
                set_mid(value);
                break;
            case 3:
                set_low(value);
                break;
            case 4:
                set_gain(value);
                break;
            case 5:
                set_mode(value);
                break;
        }
        init_tone();
    }

    public void set_high(int value) {
        Phigh = value;
        if (value < 0) {
            highb = ((float) value) / 64.0f;
        }
        if (value > 0) {
            highb = ((float) value) / 32.0f;
        }
    }

    public void set_mid(int value) {
        Pmid = value;
        if (value < 0) {
            midb = ((float) value) / 64.0f;
        }
        if (value > 0) {
            midb = ((float) value) / 32.0f;
        }
    }

    public void set_low(int value) {
        Plow = value;
        if (value < 0) {
            lowb = ((float) value) / 64.0f;
        }
        if (value > 0) {
            lowb = ((float) value) / 32.0f;
        }
    }

    public void set_gain(int value) {
        Pgain = value;
        gain = dB2rap(50.0f * ((float) value) / 127.0f - 50.0f);
    }

    public void set_mode(int value) {
        Pmode = value;
        init_mode(Pmode);
    }

    public int getpar(int npar) {
        switch (npar) {
            case 0:
                return (Pvolume);
            case 1:
                return (Phigh);
            case 2:
                return (Pmid);
            case 3:
                return (Plow);
            case 4:
                return (Pgain);
            case 5:
                return (Pmode);
        }
        return 0;            //in case of bogus parameter number
    }

    public void setpreset(int npreset) {
        final int PRESET_SIZE = 6;
        int[][] presets = {
        //Odie
        {80, 32, 0, 32, 10, TYPE_PRO_CO_RAT},
        //Grunger
        {48, 10, -6, 55, 85, TYPE_GRUNGE},
        //Hard Dist.
        {48, -22, -6, 38, 12, TYPE_GRUNGE},
        //Ratty
        {48, -20, 0, 0, 70, TYPE_PRO_CO_RAT},
        //Classic Dist
        {50, 64, 0, 0, 110, TYPE_MXR_DIST_PLUS},
        //Morbid Impalement
        {38, 6, 6, 6, 105, TYPE_DEATH_METAL},
        //Mid Elve
        {48, 0, -12, 0, 127, TYPE_METAL_ZONE},
        //Fuzz
        {48, 0, 0, 0, 127, TYPE_CLASSIC_FUZZ}};

        for (int n = 0; n < PRESET_SIZE; n++) {
            changepar(n, presets[npreset][n]);
        }
        Ppreset = npreset;
        cleanup();
    }

    private void sb_classic_fuzz(float[] smpsl, float[] smpsr) {
        float tempr;
        float templ;
        float mfilter;
        float lfilter;
        float hfilter;
        lpre1.filterout(smpsl);
        rpre1.filterout(smpsr);
        linput.filterout(smpsl);
        rinput.filterout(smpsr);
        rwshape.waveshapesmps(PERIOD, smpsr, 19, 25, true);  //compress
        lwshape.waveshapesmps(PERIOD, smpsl, 19, 25, true);

        for (int i = 0; i < PERIOD; i++) {

            //left channel
            mfilter = ltonemd.filterout_s(smpsl[i]);

            templ = lpost.filterout_s(fabs(smpsl[i]));
            tempr = rpost.filterout_s(fabs(smpsr[i]));   //dynamic symmetry

            smpsl[i] += lowb * templ + midb * mfilter;      //In this case, lowb control tweaks symmetry

            //Right channel
            mfilter = rtonemd.filterout_s(smpsr[i]);
            smpsr[i] += lowb * tempr + midb * mfilter;

        }

        ranti.filterout(smpsr);
        lanti.filterout(smpsl);
        rwshape2.waveshapesmps(PERIOD, smpsr, 25, Pgain, true);  //JFET
        lwshape2.waveshapesmps(PERIOD, smpsl, 25, Pgain, true);
        lpre2.filterout(smpsl);
        rpre2.filterout(smpsr);

        for (int i = 0; i < PERIOD; i++) {
            //left channel
            lfilter = ltonelw.filterout_s(smpsl[i]);
            hfilter = ltonehg.filterout_s(smpsl[i]);

            efxoutl[i] = volume * ((1.0f - highb) * lfilter + highb * hfilter);  //classic BMP tone stack

            //Right channel
            lfilter = rtonelw.filterout_s(smpsr[i]);
            hfilter = rtonehg.filterout_s(smpsr[i]);

            efxoutr[i] = volume * ((1.0f - highb) * lfilter + highb * hfilter);
        }
    }

    private void sb_dist_plus(float[] smpsl, float[] smpsr) {
        float mfilter;
        float tempr;
        float templ;
        float lfilter;
        linput.filterout(smpsl);
        rinput.filterout(smpsr);

        for (int i = 0; i < PERIOD; i++) {
            templ = smpsl[i];
            tempr = smpsr[i];
            smpsl[i] += lpre1.filterout_s(pre1gain * gain * templ);
            smpsr[i] += rpre1.filterout_s(pre1gain * gain * tempr);  //Low freq gain stage
        }


        rwshape.waveshapesmps(PERIOD, smpsl, 24, 1, true);  // Op amp limiting
        lwshape.waveshapesmps(PERIOD, smpsr, 24, 1, true);

        ranti.filterout(smpsr);
        lanti.filterout(smpsl);

        rwshape2.waveshapesmps(PERIOD, smpsl, 29, 1, false);  // diode limit
        lwshape2.waveshapesmps(PERIOD, smpsr, 29, 1, false);


        for (int i = 0; i < PERIOD; i++) {
            //left channel
            lfilter = ltonelw.filterout_s(smpsl[i]);
            mfilter = ltonemd.filterout_s(smpsl[i]);

            efxoutl[i] = 0.5f * ltonehg.filterout_s(volume * (smpsl[i] + lowb * lfilter + midb * mfilter));

            //Right channel
            lfilter = rtonelw.filterout_s(smpsr[i]);
            mfilter = rtonemd.filterout_s(smpsr[i]);

            efxoutr[i] = 0.5f * rtonehg.filterout_s(volume * (smpsr[i] + lowb * lfilter + midb * mfilter));
        }
    }

    private void sb_rat_fat_cat(float[] smpsl, float[] smpsr) {
        float mfilter;
        float templ;
        float tempr;
        float lfilter;
        linput.filterout(smpsl);
        rinput.filterout(smpsr);

        for (int i = 0; i < PERIOD; i++) {
            templ = smpsl[i];
            tempr = smpsr[i];
            smpsl[i] += lpre1.filterout_s(pre1gain * gain * templ);
            smpsr[i] += rpre1.filterout_s(pre1gain * gain * tempr);  //Low freq gain stage
            smpsl[i] += lpre2.filterout_s(pre2gain * gain * templ);
            smpsr[i] += rpre2.filterout_s(pre2gain * gain * tempr); //High freq gain stage

        }

        rwshape.waveshapesmps(PERIOD, smpsl, 24, 1, true);  // Op amp limiting
        lwshape.waveshapesmps(PERIOD, smpsr, 24, 1, true);

        ranti.filterout(smpsr);
        lanti.filterout(smpsl);

        rwshape2.waveshapesmps(PERIOD, smpsl, 23, 1, false);  // hard comp
        lwshape2.waveshapesmps(PERIOD, smpsr, 23, 1, false);

        for (int i = 0; i < PERIOD; i++) {
            //left channel
            lfilter = ltonelw.filterout_s(smpsl[i]);
            mfilter = ltonemd.filterout_s(smpsl[i]);

            efxoutl[i] = 0.5f * ltonehg.filterout_s(volume * (smpsl[i] + lowb * lfilter + midb * mfilter));

            //Right channel
            lfilter = rtonelw.filterout_s(smpsr[i]);
            mfilter = rtonemd.filterout_s(smpsr[i]);

            efxoutr[i] = 0.5f * rtonehg.filterout_s(volume * (smpsr[i] + lowb * lfilter + midb * mfilter));
        }
    }

    private void sb_grunge_death_metal_zone(float[] smpsl, float[] smpsr) {
        float hfilter;
        float templ;
        float lfilter;
        float mfilter;
        float tempr;
        linput.filterout(smpsl);
        rinput.filterout(smpsr);

        for (int i = 0; i < PERIOD; i++) {
            templ = smpsl[i] * (gain * pgain + 0.01f);
            tempr = smpsr[i] * (gain * pgain + 0.01f);
            smpsl[i] += lpre1.filterout_s(templ);
            smpsr[i] += rpre1.filterout_s(tempr);
        }
        rwshape.waveshapesmps(PERIOD, smpsl, 24, 1, true);  // Op amp limiting
        lwshape.waveshapesmps(PERIOD, smpsr, 24, 1, true);

        ranti.filterout(smpsr);
        lanti.filterout(smpsl);

        rwshape2.waveshapesmps(PERIOD, smpsl, 23, Pgain, true);  // hard comp
        lwshape2.waveshapesmps(PERIOD, smpsr, 23, Pgain, true);


        for (int i = 0; i < PERIOD; i++) {
            smpsl[i] = smpsl[i] + RGP2 * lpre2.filterout_s(smpsl[i]);
            smpsr[i] = smpsr[i] + RGP2 * rpre2.filterout_s(smpsr[i]);
            smpsl[i] = smpsl[i] + RGPST * lpost.filterout_s(smpsl[i]);
            smpsr[i] = smpsr[i] + RGPST * rpost.filterout_s(smpsr[i]);

            //left channel
            lfilter = ltonelw.filterout_s(smpsl[i]);
            mfilter = ltonemd.filterout_s(smpsl[i]);
            hfilter = ltonehg.filterout_s(smpsl[i]);

            efxoutl[i] = 0.1f * volume * (smpsl[i] + lowb * lfilter + midb * mfilter + highb * hfilter);

            //Right channel
            lfilter = rtonelw.filterout_s(smpsr[i]);
            mfilter = rtonemd.filterout_s(smpsr[i]);
            hfilter = rtonehg.filterout_s(smpsr[i]);

            efxoutr[i] = 0.1f * volume * (smpsr[i] + lowb * lfilter + midb * mfilter + highb * hfilter);

        }
    }

    private void sb_odie(float[] smpsl, float[] smpsr) {
        float lfilter;
        float hfilter;
        float mfilter;
        lpre2.filterout(smpsl);
        rpre2.filterout(smpsr);
        rwshape.waveshapesmps(PERIOD, smpsl, 28, 20, true);  //Valve2
        lwshape.waveshapesmps(PERIOD, smpsr, 28, 20, true);
        ranti.filterout(smpsr);
        lanti.filterout(smpsl);
        lpre1.filterout(smpsl);
        rpre1.filterout(smpsr);
        rwshape2.waveshapesmps(PERIOD, smpsl, 28, Pgain, true);  //Valve2
        lwshape2.waveshapesmps(PERIOD, smpsr, 28, Pgain, true);

        lpost.filterout(smpsl);
        rpost.filterout(smpsr);

        for (int i = 0; i < PERIOD; i++) {
            //left channel
            lfilter = ltonelw.filterout_s(smpsl[i]);
            mfilter = ltonemd.filterout_s(smpsl[i]);
            hfilter = ltonehg.filterout_s(smpsl[i]);

            efxoutl[i] = 0.5f * volume * (smpsl[i] + lowb * lfilter + midb * mfilter + highb * hfilter);

            //Right channel
            lfilter = rtonelw.filterout_s(smpsr[i]);
            mfilter = rtonemd.filterout_s(smpsr[i]);
            hfilter = rtonehg.filterout_s(smpsr[i]);

            efxoutr[i] = 0.5f * volume * (smpsr[i] + lowb * lfilter + midb * mfilter + highb * hfilter);
        }
    }

    /*
     * Parameter control
     */
    private void init_mode(int value) {
        int tinput = 1;
        float finput = 80.0f;
        float qinput = 1.0f;
        int sinput = 0;

        int tpre1 = 1;
        float fpre1 = 708.0f;
        float qpre1 = 1.0f;
        int spre1 = 0;

        int tpre2 = 1;
        float fpre2 = 30.0f;
        float qpre2 = 1.0f;
        int spre2 = 0;

        int tpost = 0;
        float fpost = 720.0f;
        float qpost = 1.0f;
        int spost = 0;

        int ttonehg = 1;
        float ftonehg = 1500.0f;
        float qtonehg = 1.0f;
        int stonehg = 0;

        int ttonemd = 4;
        float ftonemd = 720.0f;
        float qtonemd = 1.0f;
        int stonemd = 0;

        int ttonelw = 0;
        float ftonelw = 500.0f;
        float qtonelw = 1.0f;
        int stonelw = 0;

        switch (value) {
            case TYPE_ODIE:
                tinput = 1;
                finput = 80.0f;
                qinput = 1.0f;
                sinput = 0;

                tpre1 = 1;
                fpre1 = 630.0f;
                qpre1 = 1.0f;
                spre1 = 0;

                tpre2 = 1;
                fpre2 = 220.0f;
                qpre2 = 1.0f;
                spre2 = 0;

                tpost = 0;
                fpost = 720.0f;
                qpost = 1.0f;
                spost = 0;

                ttonehg = 1;
                ftonehg = 1500.0f;
                qtonehg = 1.0f;
                stonehg = 0;

                ttonemd = 4;
                ftonemd = 720.0f;
                qtonemd = 1.0f;
                stonemd = 0;

                ttonelw = 0;
                ftonelw = 500.0f;
                qtonelw = 1.0f;
                stonelw = 0;
                break;
            case TYPE_GRUNGE:
                // Some key filter stages based upon a schematic for a grunge pedal
                // Total gain up to 25,740/2 (91dB)
                // Fc1 =  999.02  Gain = 110 = 40.8dB
                // Q1 =  2.9502
                // gain stage 1rst order HP @ 340 Hz, Gain = 21.3 ... 234 (26dB ... 47dB)
                // Fc2 =  324.50
                // Q2 =  4.5039
                // Fc3 =  5994.1
                // Q3 =  1.7701
                // Fc4 =  127.80
                // Q4 =  3.7739

                tinput = 4;         //Pre-Emphasis filter
                finput = 1000.0f;
                qinput = 2.95f;
                sinput = 0;
                pgain = 110.0f;

                tpre1 = 0;         //Gain stage reduce aliasing
                fpre1 = 6000.0f;
                qpre1 = 0.707f;
                spre1 = 2;

                tpre2 = 4;        //being used as a recovery filter, gain = 10
                fpre2 = 324.5f;
                qpre2 = 4.5f;
                spre2 = 0;
                RGP2 = 10.0f;

                tpost = 4;       //The other recovery filter, gain = 3
                fpost = 6000.0f;
                qpost = 1.77f;
                spost = 0;
                RGPST = 3.0f;

                ttonehg = 1;       //high shelf ranging 880 to 9700 Hz, gain 10
                ftonehg = 4000.0f;
                qtonehg = 1.0f;
                stonehg = 0;

                ttonemd = 4;       // Pedal has no mid filter so I'll make up my own
                ftonemd = 1000.0f;
                qtonemd = 2.0f;
                stonemd = 0;

                ttonelw = 4;       //Low Eq band, peaking type, gain = up to 22.
                ftonelw = 128.0f;
                qtonelw = 3.8f;
                stonelw = 0;
                break;

            case TYPE_PRO_CO_RAT:
                //ProCo Rat Distortion emulation
                // Some key filter stages based upon a schematic for a grunge pedal

                tinput = 0;         //Reduce some noise aliasing
                finput = 5000.0f;
                qinput = 1.0f;
                sinput = 3;

                tpre1 = 1;         //Gain stage high boost, gain = 1 ... 268 (max)
                fpre1 = 60.0f;
                qpre1 = 1.0f;
                spre1 = 0;
                pre1gain = 268.0f;

                tpre2 = 1;        //being used as a recovery filter, gain = 1 ... 3000
                fpre2 = 1539.0f;
                qpre2 = 1.0f;
                spre2 = 0;
                pre2gain = 3000.0f;

                tpost = 0;       //Not used...initialized to "something"
                fpost = 6000.0f;
                qpost = 1.77f;
                spost = 0;

                ttonehg = 0;       //frequency sweeping LPF
                ftonehg = 1000.0f;
                qtonehg = 1.0f;
                stonehg = 0;

                ttonemd = 4;       // Pedal has no mid filter so I'll make up my own
                ftonemd = 700.0f;
                qtonemd = 2.0f;
                stonemd = 0;

                ttonelw = 0;       //Pedal has no Low filter, so make up my own...Low Eq band, peaking type
                ftonelw = 328.0f;  //Mild low boost
                qtonelw = 0.50f;
                stonelw = 1;
                break;

            case TYPE_FAT_CAT:
                //Fat Cat Distortion emulation
            case TYPE_MXR_DIST_PLUS:
                //MXR Dist+ emulation (many below filters unuse)
                // Some key filter stages based upon a schematic for a grunge pedal

                tinput = 0;         //Reduce some noise aliasing
                finput = 5000.0f;
                qinput = 1.0f;
                sinput = 3;

                tpre1 = 1;         //Gain stage high boost, gain = 1 ... 100 (max)
                fpre1 = 33.0f;
                qpre1 = 1.0f;
                spre1 = 0;
                pre1gain = 100.0f;

                tpre2 = 1;        //being used as a recovery filter, gain = 1 ... 1700
                fpre2 = 861.0f;
                qpre2 = 1.0f;
                spre2 = 0;
                pre2gain = 1700.0f;

                tpost = 0;       //Not used...initialized to "something"
                fpost = 6000.0f;
                qpost = 1.77f;
                spost = 0;

                ttonehg = 0;       //frequency sweeping LPF
                ftonehg = 1000.0f;
                qtonehg = 1.0f;
                stonehg = 0;

                ttonemd = 4;       // Pedal has no mid filter so I'll make up my own
                ftonemd = 700.0f;
                qtonemd = 2.0f;
                stonemd = 0;

                ttonelw = 0;       //Pedal has no Low filter, so make up my own...Low Eq band, peaking type
                ftonelw = 328.0f;  //Mild low boost
                qtonelw = 0.50f;
                stonelw = 1;
                break;

            case TYPE_DEATH_METAL:
                // Some key filter stages based upon a schematic for a grunge pedal

                tinput = 4;         //Pre-Emphasis filter
                finput = 6735.4f;
                qinput = 0.43f;
                sinput = 0;
                pgain = 110.0f;

                tpre1 = 0;         //Gain stage reduce aliasing
                fpre1 = 6000.0f;
                qpre1 = 0.707f;
                spre1 = 2;

                tpre2 = 4;        //being used as a recovery filter, gain = 10
                fpre2 = 517.0f;
                qpre2 = 7.17f;
                spre2 = 0;
                RGP2 = 1.0f;

                tpost = 4;       //The other recovery filter, gain = 10
                fpost = 48.0f;
                qpost = 6.68f;
                spost = 0;
                RGPST = 10.0f;

                ttonehg = 1;       //high shelf ranging 880 to 9700 Hz, gain 11
                ftonehg = 4000.0f;
                qtonehg = 1.0f;
                stonehg = 0;
                HG = 11.0f;

                ttonemd = 4;       // Mid band EQ gain 11
                ftonemd = 1017.0f;
                qtonemd = 1.15f;
                stonemd = 0;
                MG = 11.0f;

                ttonelw = 4;       //Low Eq band, peaking type, gain = up to 22.
                ftonelw = 107.0f;
                qtonelw = 3.16f;
                stonelw = 0;
                LG = 22.0f;

                break;
            case TYPE_METAL_ZONE:
                //Metal Zone
                // Some key filter stages based upon a schematic for a grunge pedal

                tinput = 4;         //Pre-Emphasis filter
                finput = 952.53f;
                qinput = 2.8f;
                sinput = 0;
                pgain = 100.0f;

                tpre1 = 0;         //Gain stage reduce aliasing
                fpre1 = 6000.0f;
                qpre1 = 0.707f;
                spre1 = 2;

                tpre2 = 4;        //being used as a recovery filter, gain = 10
                fpre2 = 4894.0f;
                qpre2 = 2.16f;
                spre2 = 0;
                RGP2 = 3.3f;

                tpost = 4;       //The other recovery filter, gain = 10
                fpost = 105.0f;
                qpost = 14.62f;
                spost = 0;
                RGPST = 7.0f;

                ttonehg = 1;       //high shelf ranging 880 to 9700 Hz, gain 11
                ftonehg = 4000.0f;
                qtonehg = 1.0f;
                stonehg = 0;
                HG = 10.0f;

                ttonemd = 4;       // Mid band EQ gain 11
                ftonemd = 1017.0f;
                qtonemd = 1.15f;
                stonemd = 0;
                MG = 11.0f;

                ttonelw = 4;       //Low Eq band, peaking type, gain = up to 22.
                ftonelw = 105.50f;
                qtonelw = 3.11f;
                stonelw = 0;
                LG = 3.33f;

                break;

            case TYPE_CLASSIC_FUZZ:  //Classic Fuzz
                tinput = 1;
                finput = 80.0f;
                qinput = 1.0f;
                sinput = 0;

                tpre1 = 0;
                fpre1 = 4500.0f;
                qpre1 = 1.0f;
                spre1 = 1;

                tpre2 = 1;
                fpre2 = 40.0f;
                qpre2 = 1.0f;
                spre2 = 0;

                tpost = 0;
                fpost = 2.0f;
                qpost = 1.0f;
                spost = 0;

                ttonehg = 1;
                ftonehg = 397.0f;
                qtonehg = 1.0f;
                stonehg = 0;

                ttonemd = 4;
                ftonemd = 515.0f;  //sort of like a stuck wahwah
                qtonemd = 4.0f;
                stonemd = 0;

                ttonelw = 0;
                ftonelw = 295.0f;
                qtonelw = 1.0f;
                stonelw = 0;
                break;
        }

        //left channel filters
        //  RRAnalogFilter (unsigned char Ftype, float Ffreq, float Fq,unsigned char Fstages);
        // LPF = 0, HPF = 1
        linput.settype(tinput);
        linput.setfreq_and_q(finput, qinput);
        linput.setstages(sinput);

        lpre1.settype(tpre1);
        lpre1.setfreq_and_q(fpre1, qpre1);
        lpre1.setstages(spre1);

        lpre2.settype(tpre2);
        lpre2.setfreq_and_q(fpre2, qpre2);
        lpre2.setstages(spre2);

        lpost.settype(tpost);
        lpost.setfreq_and_q(fpost, qpost);
        lpost.setstages(spost);

        ltonehg.settype(ttonehg);
        ltonehg.setfreq_and_q(ftonehg, qtonehg);
        ltonehg.setstages(stonehg);

        ltonemd.settype(ttonemd);
        ltonemd.setfreq_and_q(ftonemd, qtonemd);
        ltonemd.setstages(stonemd);

        ltonelw.settype(ttonelw);
        ltonelw.setfreq_and_q(ftonelw, qtonelw);
        ltonelw.setstages(stonelw);

        //right channel filters

        rinput.settype(tinput);
        rinput.setfreq_and_q(finput, qinput);
        rinput.setstages(sinput);

        rpre1.settype(tpre1);
        rpre1.setfreq_and_q(fpre1, qpre1);
        rpre1.setstages(spre1);

        rpre2.settype(tpre2);
        rpre2.setfreq_and_q(fpre2, qpre2);
        rpre2.setstages(spre2);

        rpost.settype(tpost);
        rpost.setfreq_and_q(fpost, qpost);
        rpost.setstages(spost);

        rtonehg.settype(ttonehg);
        rtonehg.setfreq_and_q(ftonehg, qtonehg);
        rtonehg.setstages(stonehg);

        rtonemd.settype(ttonemd);
        rtonemd.setfreq_and_q(ftonemd, qtonemd);
        rtonemd.setstages(stonemd);

        rtonelw.settype(ttonelw);
        rtonelw.setfreq_and_q(ftonelw, qtonelw);
        rtonelw.setstages(stonelw);
    }

    private void init_tone() {
        float varf;
        switch (Pmode) {
            case TYPE_ODIE:
                varf = 2533.0f + highb * 1733.0f;  //High tone ranges from 800 to 6000Hz
                rtonehg.setfreq(varf);
                ltonehg.setfreq(varf);
                if (highb > 0.0f) {
                    highb = ((float) Phigh) / 8.0f;
                }
                break;

            case TYPE_GRUNGE:
                varf = 3333.0f + highb * 2500.0f;  //High tone ranges from 833 to 8333Hz
                rtonehg.setfreq(varf);
                ltonehg.setfreq(varf);

                if (highb > 0.0f) {
                    highb = ((float) Phigh) / 16.0f;
                }
                if (lowb > 0.0f) {
                    lowb = ((float) Plow) / 18.0f;
                }
                break;

            case TYPE_PRO_CO_RAT:
            case TYPE_FAT_CAT:
                varf = 3653.0f + highb * 3173.0f;  //High tone ranges from ~480 to 10k
                rtonehg.setfreq(varf);
                ltonehg.setfreq(varf);
                break;
            case TYPE_MXR_DIST_PLUS:
                varf = gain * 700.0f + 20.0f;
                rpre1.setfreq(varf);
                lpre1.setfreq(varf);
                pre1gain = 212.0f;
                varf = 3653.0f + highb * 3173.0f;  //High tone ranges from ~480 to 10k
                rtonehg.setfreq(varf);
                ltonehg.setfreq(varf);
                break;
            case TYPE_DEATH_METAL: //Death Metal
            case TYPE_METAL_ZONE: //Mid Elves Own
                varf = 3653.0f + highb * 3173.0f;  //High tone ranges from ~480 to 10k
                rtonehg.setfreq(varf);
                ltonehg.setfreq(varf);

                if (highb > 0.0f) {
                    highb = HG * ((float) Phigh) / 64.0f;
                }
                if (lowb > 0.0f) {
                    lowb = LG * ((float) Plow) / 64.0f;
                }
                if (midb > 0.0f) {
                    midb = MG * ((float) Pmid) / 64.0f;
                }
                break;

            case TYPE_CLASSIC_FUZZ:
                highb = ((float) Phigh + 64) / 127.0f;
                varf = 40.0f + gain * 200.0f;
                linput.setfreq(varf);
                rinput.setfreq(varf);
                if (midb > 0.0f) {
                    midb = ((float) Pmid) / 8.0f;
                }
                lowb = ((float) Plow) / 64.0f;

                varf = 1085.0f - lowb * 1000.0f;
                lpre1.setfreq(varf);
                rpre1.setfreq(varf);
                break;
        }
    }

    private void setvolume(int value) {
        Pvolume = value;
        volume = (float) Pvolume / 127.0f;
    }
}