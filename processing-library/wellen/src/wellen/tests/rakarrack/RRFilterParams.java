package wellen.tests.rakarrack;

import static wellen.tests.rakarrack.RRUtilities.*;

public class RRFilterParams {

    private int Dfreq;
    private int Dq;
    //stored default parameters
    private int Dtype;
    private int Pcategory;    //Filter category (Analog/Formant/StVar)
    private int Pcenterfreq, Poctavesfreq;    //the center frequency of the res. func., and the number of octaves
    private int Pformantslowness;    //how slow varies the formants
    private int Pfreq;        // Frequency (64-central frequency)
    private int Pfreqtrack;    //how the filter frequency is changing according the note frequency
    private int Pgain;        //filter's output gain
    //Formant filter parameters
    private int Pnumformants;    //how many formants are used
    private int Pq;        // Q parameters (resonance or bandwidth)
    private final Sequence[] Psequence = new Sequence[FF_MAX_SEQUENCE];
    private int Psequencereversed;    //if the input from filter envelopes/LFOs/etc. is reversed(negated)
    private int Psequencesize;    //how many vowels are in the sequence
    private int Psequencestretch;    //how the sequence is stretched (how the input from filter envelopes/LFOs/etc.
    // is "stretched")
    private int Pstages;    //filter stages+1
    private int Ptype;        // Filter type  (for analog lpf,hpf,bpf..)
    private int Pvowelclearness;    //how vowels are kept clean (how much try to avoid "mixed" vowels)
    private final Vowel[] Pvowels = new Vowel[FF_MAX_VOWELS];
    private boolean changed;

    public RRFilterParams() {
        for (int i = 0; i < FF_MAX_VOWELS; i++) {
            Pvowels[i] = new Vowel();
        }
        for (int i = 0; i < FF_MAX_SEQUENCE; i++) {
            Psequence[i] = new Sequence();
        }
    }

    public RRFilterParams(int Ptype_, int Pfreq_, int Pq_) {
        // setpresettype("Pfilter");
        Dtype = Ptype_;
        Dfreq = Pfreq_;
        Dq = Pq_;

        changed = false;
        defaults();
    }

    /*
     * Parameter control
     */
    private float getfreq() {
        return (((float) Pfreq / 64.0f - 1.0f) * 5.0f);
    }

    /*
     * Get the parameters from other FilterParams
     */
    private float getq() {
        return (expf(powf((float) Pq / 127.0f, 2) * logf(1000.0f)) - 0.9f);
    }

    private float getfreqtracking(float notefreq) {
        return (logf(notefreq / 440.0f) * ((float) Pfreqtrack - 64.0f) / (64.0f * LOG_2));
    }

    private float getgain() {
        return (((float) Pgain / 64.0f - 1.0f) * 30.0f);    //-30..30dB
    }

    /*
     * Get the center frequency of the formant's graph
     */
    private float getcenterfreq() {
        return (10000.0f * powf(10.0f, -(1.0f - (float) Pcenterfreq / 127.0f) * 2.0f));
    }

    /*
     * Get the number of octave that the formant functions applies to
     */
    private float getoctavesfreq() {
        return (0.25f + 10.0f * (float) Poctavesfreq / 127.0f);
    }

    /*
     * Get the frequency from x, where x is [0..1]
     */
    private float getfreqx(float x) {
        if (x > 1.0) {
            x = 1.0f;
        }
        float octf = powf(2.0f, getoctavesfreq());
        return (getcenterfreq() / sqrtf(octf) * powf(octf, x));
    }

    /*
     * Get the x coordinate from frequency (used by the UI)
     */
    private float getfreqpos(float freq) {
        return ((logf(freq) - logf(getfreqx(0.0f))) / logf(2.0f) / getoctavesfreq());
    }

    /*
     * Get the freq. response of the formant filter
     */
    private void formantfilterH(int nvowel, int nfreqs, float[] freqs) {
        float[] c = new float[3];
        float[] d = new float[3];
        float filter_freq, filter_q, filter_amp;
        float omega, sn, cs, alpha;

        for (int i = 0; i < nfreqs; i++) {
            freqs[i] = 0.0f;
        }

        //for each formant...
        for (int nformant = 0; nformant < Pnumformants; nformant++) {
            //compute formant parameters(frequency,amplitude,etc.)
            filter_freq = getformantfreq(Pvowels[nvowel].formants[nformant].freq);
            filter_q = getformantq(Pvowels[nvowel].formants[nformant].q) * getq();
            if (Pstages > 0) {
                filter_q = (filter_q > 1.0 ? powf(filter_q, 1.0f / ((float) Pstages + 1)) : filter_q);
            }

            filter_amp = getformantamp(Pvowels[nvowel].formants[nformant].amp);

            if (filter_freq <= (SAMPLE_RATE / 2 - 100.0)) {
                omega = 2.0f * PI * filter_freq / SAMPLE_RATE;
                sn = sinf(omega);
                cs = cosf(omega);
                alpha = sn / (2.0f * filter_q);
                float tmp = 1.0f + alpha;
                c[0] = alpha / tmp * sqrtf(filter_q + 1.0f);
                c[1] = 0;
                c[2] = -alpha / tmp * sqrtf(filter_q + 1.0f);
                d[1] = -2.0f * cs / tmp * (-1.0f);
                d[2] = (1.0f - alpha) / tmp * (-1.0f);
            } else {
                continue;
            }

            for (int i = 0; i < nfreqs; i++) {
                float freq = getfreqx((float) i / (float) nfreqs);
                if (freq > SAMPLE_RATE / 2.0f) {
                    for (int tmp = i; tmp < nfreqs; tmp++) {
                        freqs[tmp] = 0.0f;
                    }
                    break;
                }

                float fr = freq / SAMPLE_RATE * PI * 2.0f;
                float x = c[0], y = 0.0f;
                for (int n = 1; n < 3; n++) {
                    x += cosf((float) n * fr) * c[n];
                    y -= sinf((float) n * fr) * c[n];
                }

                float h = x * x + y * y;
                x = 1.0f;
                y = 0.0f;
                for (int n = 1; n < 3; n++) {
                    x -= cosf((float) n * fr) * d[n];
                    y += sinf((float) n * fr) * d[n];
                }
                h = h / (x * x + y * y);

                freqs[i] += powf(h, ((float) Pstages + 1.0f) / 2.0f) * filter_amp;
            }
        }
        for (int i = 0; i < nfreqs; i++) {
            if (freqs[i] > 0.000000001f) {
                freqs[i] = rap2dB(freqs[i]) + getgain();
            } else {
                freqs[i] = -90.0f;
            }
        }
    }

    /*
     * Transforms a parameter to the real value
     */
    private float getformantfreq(int freq) {
        float result = getfreqx((float) freq / 127.0f);
        return (result);
    }

    private float getformantamp(int amp) {
        float result = powf(0.1f, (1.0f - (float) amp / 127.0f) * 4.0f);
        return (result);
    }

    private float getformantq(int q) {
        //temp
        float result = powf(25.0f, ((float) q - 32.0f) / 64.0f);
        return (result);
    }

    private void defaults() {
        Ptype = Dtype;
        Pfreq = Dfreq;
        Pq = Dq;

        Pstages = 0;
        Pfreqtrack = 64;
        Pgain = 64;
        Pcategory = 0;

        Pnumformants = 3;
        Pformantslowness = 64;
        for (int j = 0; j < FF_MAX_VOWELS; j++) {
            defaults(j);
        }

        Psequencesize = 3;
        for (int i = 0; i < FF_MAX_SEQUENCE; i++) {
            Psequence[i].nvowel = i % FF_MAX_VOWELS;
        }

        Psequencestretch = 40;
        Psequencereversed = 0;
        Pcenterfreq = 64;        //1 kHz
        Poctavesfreq = 64;
        Pvowelclearness = 64;
    }

    private void defaults(int n) {
        int j = n;
        for (int i = 0; i < FF_MAX_FORMANTS; i++) {
            Pvowels[j].formants[i].freq = (int) (random() * 127.0); //some random freqs
            Pvowels[j].formants[i].q = 64;
            Pvowels[j].formants[i].amp = 127;
        }
    }

    private void getfromFilterParams(RRFilterParams pars) {
        defaults();

        if (pars == null) {
            return;
        }

        Ptype = pars.Ptype;
        Pfreq = pars.Pfreq;
        Pq = pars.Pq;

        Pstages = pars.Pstages;
        Pfreqtrack = pars.Pfreqtrack;
        Pgain = pars.Pgain;
        Pcategory = pars.Pcategory;

        Pnumformants = pars.Pnumformants;
        Pformantslowness = pars.Pformantslowness;
        for (int j = 0; j < FF_MAX_VOWELS; j++) {
            for (int i = 0; i < FF_MAX_FORMANTS; i++) {
                Pvowels[j].formants[i].freq = pars.Pvowels[j].formants[i].freq;
                Pvowels[j].formants[i].q = pars.Pvowels[j].formants[i].q;
                Pvowels[j].formants[i].amp = pars.Pvowels[j].formants[i].amp;
            }
        }

        Psequencesize = pars.Psequencesize;
        for (int i = 0; i < FF_MAX_SEQUENCE; i++) {
            Psequence[i].nvowel = pars.Psequence[i].nvowel;
        }

        Psequencestretch = pars.Psequencestretch;
        Psequencereversed = pars.Psequencereversed;
        Pcenterfreq = pars.Pcenterfreq;
        Poctavesfreq = pars.Poctavesfreq;
        Pvowelclearness = pars.Pvowelclearness;
    }

    private static class Vowel {
        Formant[] formants = new Formant[FF_MAX_FORMANTS];

        private Vowel() {
            for (int i = 0; i < FF_MAX_FORMANTS; i++) {
                formants[i] = new Formant();
            }
        }

        private static class Formant {
            int freq, amp, q;    //frequency,amplitude,Q
        }
    }

    private static class Sequence {
        int nvowel;    //the vowel from the position
    }
}
