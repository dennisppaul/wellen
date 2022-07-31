package wellen.daisysp;

import wellen.Wellen;

import static wellen.daisysp.DSP.randf;

/**
 * Produces a naturally decaying plucked string or drum sound based on the Karplus-Strong algorithms.
 * <p>
 * Ported from soundpipe to DaisySP
 * <p>
 * This code was originally extracted from the Csound opcode "pluck"
 * <p>
 * Original Author(s): Barry Vercoe, John ffitch Year: 1991
 * <p>
 * Location: OOps/ugens4.c
 */
public class Pluck {
    /**
     * The method of natural decay that the algorithm will use. - RECURSIVE: 1st order recursive filter, with coefs .5.
     * - WEIGHTED_AVERAGE: weighted averaging.
     */
    public static final int PLUCK_MODE_RECURSIVE = 0;
    public static final int PLUCK_MODE_WEIGHTED_AVERAGE = 1;

    private static final int PLUKMIN = 64;
    private static final float INTERPFACTOR = 256.0f;
    private static final int INTERPFACTOR_I = 255;

    private float amp_, freq_, decay_, damp_, ifreq_;
    private float sicps_;
    private int phs256_, npts_, maxpts_;
    private float[] buf_;
    private float sample_rate_;
    private boolean init_;
    private int mode_;

    private boolean mTrigger = false;

    /**
     * Initializes the Pluck module.
     *
     * @param sample_rate: Sample rate of the audio engine being run.
     * @param buf:         buffer used as an impulse when triggering the Pluck algorithm
     * @param npts:        number of elementes in buf.
     * @param mode:        Sets the mode of the algorithm.
     */
    public void Init(float sample_rate, float[] buf, int npts, int mode) {
        amp_ = 0.5f;
        freq_ = 300;
        decay_ = 1.0f;
        sample_rate_ = sample_rate;
        mode_ = mode;

        maxpts_ = npts;
        npts_ = npts;
        buf_ = buf;

        Reinit();
        /* tuned pitch convt */
        sicps_ = (npts * 256.0f + 128.0f) * (1.0f / sample_rate_);
        init_ = true;
    }

    public void Init() {
        Init(Wellen.DEFAULT_SAMPLING_RATE, new float[256], 256, PLUCK_MODE_RECURSIVE);
        SetAmp(0.5f);
        SetDecay(0.95f);
        SetDamp(0.9f);
    }

    public void Trig() {
        mTrigger = true;
    }

    /**
     * Processes the waveform to be generated, returning one sample. This should be called once per sample period.
     */
    public float Process() {
        float s = Process(mTrigger);
        mTrigger = false;
        return s;
    }

    public float Process(boolean trig) {
        float[] fp;
        float out;
        int phs256, phsinc, ltwopi, offset;
        float coeff;

        // unused variable
        // float inv_coeff;

        float frac, diff;
        float dampmin = 0.42f;

        if (trig) {
            init_ = false;
            Reinit();
        }

        if (init_) {
            return 0;
        }
        // Set Coeff for mode.
        switch (mode_) {
            case PLUCK_MODE_RECURSIVE:
                coeff = ((0.5f - dampmin) * damp_) + dampmin;
                break;
            case PLUCK_MODE_WEIGHTED_AVERAGE:
                coeff = 0.05f + (damp_ * 0.90f);
                break;
            default:
                coeff = 0.5f;
                break;
        }

        // variable set but not used
        //inv_coeff = 1.0f - coeff;

        phsinc = (int) (freq_ * sicps_);
        phs256 = phs256_;
        ltwopi = npts_ << 8;
        offset = phs256 >> 8;

//        fp = ( float *)buf_ + offset; /* lookup position   */
        fp = buf_; /* lookup position   */
        int fp_index = offset;

        diff = fp[1 + fp_index] - fp[0 + fp_index];
        frac = (float) (phs256 & 255) / 256.0f; /*  w. interpolation */
        out = (fp[0 + fp_index] + diff * frac) * amp_;   /*  gives output val */
        if ((phs256 += phsinc) >= ltwopi) {
            int nn;
            float preval;
            phs256 -= ltwopi;
            fp = buf_;
            fp_index = 0;
            preval = fp[fp_index];
            fp[fp_index] = fp[npts_];
//            fp++;
            fp_index++;
            nn = npts_;
            do {
                /* 1st order recursive filter*/
                //preval = (*fp + preval) * coeff;
                /* weighted average - stretches decay times */
                switch (mode_) {
                    case PLUCK_MODE_RECURSIVE:
                        preval = (fp[fp_index] + preval) * coeff;
                        break;
                    case PLUCK_MODE_WEIGHTED_AVERAGE:
                        preval = (fp[fp_index] * coeff) + (preval * (1.0f - coeff));
                        break;
                    default:
                        break;
                }
//            *fp++ = preval;
                fp[fp_index] = preval;
                fp_index++;
            } while (--nn > 0);
        }
        phs256_ = phs256;
        return out;
    }

    /**
     * Sets the amplitude of the output signal. Input range: 0-1?
     */
    public void SetAmp(float amp) {
        amp_ = amp;
    }

    /**
     * Sets the frequency of the output signal in Hz. Input range: Any positive value
     */
    public void SetFreq(float freq) {
        freq_ = freq;
    }

    /**
     * Sets the time it takes for a triggered note to end in seconds. Input range: 0-1
     */
    public void SetDecay(float decay) {
        decay_ = decay;
    }

    /**
     * Sets the dampening factor applied by the filter (based on PLUCK_MODE) Input range: 0-1
     */
    public void SetDamp(float damp) {
        damp_ = damp;
    }

    /**
     * Sets the mode of the algorithm.
     */
    public void SetMode(int mode) {
        mode_ = mode;
    }

    /**
     * Returns the current value for amp.
     */
    public float GetAmp() {
        return amp_;
    }

    /**
     * Returns the current value for freq.
     */
    public float GetFreq() {
        return freq_;
    }

    /**
     * Returns the current value for decay.
     */
    public float GetDecay() {
        return decay_;
    }

    /**
     * Returns the current value for damp.
     */
    public float GetDamp() {
        return damp_;
    }

    /**
     * Returns the current value for mode.
     */
    public int GetMode() {
        return mode_;
    }

    private void Reinit() {
        int n;
        float val = 0;
        float[] ap = buf_;
        //npts_ = (int )roundf(decay_ * (float)(maxpts_ - PLUKMIN) + PLUKMIN);
        npts_ = (int) (decay_ * (float) (maxpts_ - PLUKMIN) + PLUKMIN);
        //sicps_ = ((float)npts_ * INTERPFACTOR + INTERPFACTOR/2.0f) * (1.0f / _sr);
        sicps_ = ((float) npts_ * 256.0f + 128.0f) * (1.0f / sample_rate_);

        int c = 0;
        for (n = npts_; n-- > 0; ) {
            val = randf();
//        *ap++ = (val * 2.0f) - 1.0f;
            ap[c] = (val * 2.0f) - 1.0f;
            c++;
        }
        phs256_ = 0;
    }
}
