package wellen.tests.rakarrack;

import static wellen.tests.rakarrack.RRUtilities.PERIOD;
import static wellen.tests.rakarrack.RRUtilities.SAMPLE_RATE;
import static wellen.tests.rakarrack.RRUtilities.cSAMPLE_RATE;
import static wellen.tests.rakarrack.RRUtilities.dB2rap;
import static wellen.tests.rakarrack.RRUtilities.fabs;

public class RRSustainer {

    private int Ppreset;
    private int Psustain;       //Compression amount
    private int Pvolume;        //Output Level
    private final float calpha;
    private final float cbeta;
    private float compeak;
    private float compenv;
    private float compg;
    private float cpthresh;
    private float cratio;
    private float cthresh;
    private float fsustain;
    private final int hold;
    private float input;
    private float level;
    private float oldcompenv;
    private final float prls;
    private int timer;
    private float tmpgain;

    public RRSustainer() {
        Pvolume = 64;
        Psustain = 64;
        fsustain = 0.5f;
        level = 0.5f;

        float tmp = 0.01f;  //10 ms decay time on peak detectors
        prls = 1.0f - (cSAMPLE_RATE / (cSAMPLE_RATE + tmp));

        tmp = 0.05f; //50 ms att/rel on compressor
        calpha = cSAMPLE_RATE / (cSAMPLE_RATE + tmp);
        cbeta = 1.0f - calpha;
        cthresh = 0.25f;
        cratio = 0.25f;

        timer = 0;
        hold = (int) (SAMPLE_RATE * 0.0125);  //12.5ms
        cleanup();
    }

    public void cleanup() {
        compeak = 0.0f;
        compenv = 0.0f;
        oldcompenv = 0.0f;
        cpthresh = cthresh; //dynamic threshold
    }


    public void out(float[] smpsl, float[] smpsr) {
        float auxtempl;
        float auxtempr;
        float auxcombi;

        //apply compression to auxresampled
        for (int i = 0; i < PERIOD; i++) {
            auxtempl = input * smpsl[i];
            auxtempr = input * smpsr[i];
            auxcombi = 0.5f * (auxtempl + auxtempr);
            if (fabs(auxcombi) > compeak) {
                compeak = fabs(auxcombi);   //First do peak detection on the signal
                timer = 0;
            }
            if (timer > hold) {
                compeak *= prls;
                timer--;
            }
            timer++;
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

            smpsl[i] = auxtempl * tmpgain * level;
            smpsr[i] = auxtempr * tmpgain * level;
        }
        //End compression
    }

    public void setpreset(int npreset) {
        final int PRESET_SIZE = 2;
        final int NUM_PRESETS = 3;
        int[][] presets = {
        //Moderate
        {79, 54},
        //Extreme
        {16, 127},
        //Mild
        {120, 15}
        };

        for (int n = 0; n < PRESET_SIZE; n++) {
            changepar(n, presets[npreset][n]);
        }
        Ppreset = npreset;
    }

    public void changepar(int npar, int value) {
        switch (npar) {
            case 0:
                Pvolume = value;
                level = dB2rap(-30.0f * (1.0f - ((float) Pvolume / 127.0f)));
                break;
            case 1:
                Psustain = value;
                fsustain = (float) Psustain / 127.0f;
                cratio = 1.25f - fsustain;
                input = dB2rap(42.0f * fsustain - 6.0f);
                cthresh = 0.25f + fsustain;
                break;
        }
    }

    public int getpar(int npar) {
        switch (npar) {
            case 0:
                return (Pvolume);
            case 1:
                return (Psustain);
        }
        return (0);
    }
}