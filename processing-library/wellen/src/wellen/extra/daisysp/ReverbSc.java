package wellen.extra.daisysp;

import java.util.Arrays;

import static wellen.extra.daisysp.DaisySP.cosf;
import static wellen.extra.daisysp.DaisySP.sqrtf;

/**
 * Stereo Reverb
 * <p>
 * Reverb SC:               Ported from csound/soundpipe
 * <p>
 * Original author(s):        Sean Costello, Istvan Varga
 * <p>
 * Year:                    1999, 2005
 * <p>
 * Ported to soundpipe by:  Paul Batchelor
 * <p>
 * Ported by:                Stephen Hensley
 */
public class ReverbSc {

    private static final float DEFAULT_SRATE = 48000.0f;
    private static final int DELAYPOS_MASK = 0x0FFFFFFF;
    private static final int DELAYPOS_SCALE = 0x10000000;
    private static final int DELAYPOS_SHIFT = 28;
    private static final int DSY_REVERBSC_MAX_SIZE = 98936;
    private static final float MAX_PITCHMOD = 20.0f;
    private static final float MAX_SRATE = 1000000.0f;
    private static final float MIN_SRATE = 5000.0f;
    private static final float M_PI = 3.14159265358979323846f;
    private static final int NUM_DELAY_LINES = 8;
    private static final int REVSC_NOT_OK = 1;
    private static final int REVSC_OK = 0;

    //    private static int DelayLineBytesAlloc(float sr, int n) {
//        int n_bytes = 0;
//        n_bytes += (DelayLineMaxSamples(sr, n) * (int) sizeof( float));
//        return n_bytes;
//    }
    private static final float kJpScale = 0.25f;
    private static final float kOutputGain = 0.35f;
    private static final float[][] kReverbParams = new float[][]{{(2473.0f / DEFAULT_SRATE), 0.0010f, 3.100f, 1966.0f},
                                                                 {(2767.0f / DEFAULT_SRATE), 0.0011f, 3.500f, 29491.0f},
                                                                 {(3217.0f / DEFAULT_SRATE), 0.0017f, 1.110f, 22937.0f},
                                                                 {(3557.0f / DEFAULT_SRATE), 0.0006f, 3.973f, 9830.0f},
                                                                 {(3907.0f / DEFAULT_SRATE), 0.0010f, 2.341f, 20643.0f},
                                                                 {(4127.0f / DEFAULT_SRATE), 0.0011f, 1.897f, 22937.0f},
                                                                 {(2143.0f / DEFAULT_SRATE), 0.0017f, 0.891f, 29491.0f},
                                                                 {(1933.0f / DEFAULT_SRATE), 0.0006f, 3.221f,
                                                                  14417.0f}};
    private float damp_fact_;
    /* kReverbParams[n][0] = delay time (in seconds)                     */
    /* kReverbParams[n][1] = random variation in delay time (in seconds) */
    /* kReverbParams[n][2] = random variation frequency (in 1/sec)       */
    /* kReverbParams[n][3] = random seed (0 - 32767)                     */
    private final ReverbScDl[] delay_lines_ = new ReverbScDl[NUM_DELAY_LINES];
    private float feedback_, lpfreq_;
    private float i_sample_rate_, i_pitch_mod_, i_skip_init_;
    private int init_done_;
    private float mLeft = 0;
    private float mRight = 0;
    private float prv_lpfreq_;
    private float sample_rate_;

    private static int DelayLineMaxSamples(float sr, int n) {
        float max_del;
        max_del = kReverbParams[n][0];
        max_del += (kReverbParams[n][1] * 1.125);
        return (int) (max_del * sr + 16.5);
    }

    /**
     * Initializes the reverb module, and sets the sample_rate at which the Process function will be called. Returns 0
     * if all good, or 1 if it runs out of delay times exceed maximum allowed.
     */
    public int Init(float sample_rate) {
        for (int i = 0; i < delay_lines_.length; i++) {
            delay_lines_[i] = new ReverbScDl();
        }

        i_sample_rate_ = sample_rate;
        sample_rate_ = sample_rate;
        feedback_ = 0.97f;
        lpfreq_ = 10000;
        i_pitch_mod_ = 1;
        i_skip_init_ = 0;
        damp_fact_ = 1.0f;
        prv_lpfreq_ = 0.0f;
        init_done_ = 1;
        int n_bytes = 0;
        for (int i = 0; i < delay_lines_.length; i++) {
            if (n_bytes > DSY_REVERBSC_MAX_SIZE) {
                return 1;
            }
            delay_lines_[i].buf = new float[DelayLineMaxSamples(sample_rate, i)];
            InitDelayLine(delay_lines_[i], i);
            n_bytes += DelayLineMaxSamples(sample_rate, i);
        }
        return 0;
    }

    /**
     * Process the input through the reverb, and updates values of out with the new processed signal.
     */
    public int Process(final float in1, final float in2, float[] out) {
        float a_in_l, a_in_r, a_out_l, a_out_r;
        float vm1, v0, v1, v2, am1, a0, a1, a2, frac;
        ReverbScDl lp;
        int read_pos;
        int n;
        int buffer_size; /* Local copy */
        float damp_fact = damp_fact_;

        //if (init_done_ <= 0) return REVSC_NOT_OK;
        if (init_done_ <= 0) {
            return REVSC_NOT_OK;
        }

        /* calculate tone filter coefficient if frequency changed */
        if (lpfreq_ != prv_lpfreq_) {
            prv_lpfreq_ = lpfreq_;
            damp_fact = 2.0f - cosf(prv_lpfreq_ * (2.0f * M_PI) / sample_rate_);
            damp_fact = damp_fact_ = damp_fact - sqrtf(damp_fact * damp_fact - 1.0f);
        }

        /* calculate "resultant junction pressure" and mix to input signals */

        a_in_l = a_out_l = a_out_r = 0.0f;
        for (n = 0; n < 8; n++) {
            a_in_l += delay_lines_[n].filter_state;
        }
        a_in_l *= kJpScale;
        a_in_r = a_in_l + in2;
        a_in_l = a_in_l + in1;

        /* loop through all delay lines */

        for (n = 0; n < 8; n++) {
            lp = delay_lines_[n];
            buffer_size = lp.buffer_size;

            /* send input signal and feedback to delay line */

            lp.buf[lp.write_pos] = ((((n & 1) != 0) ? a_in_r : a_in_l) - lp.filter_state);
            if (++lp.write_pos >= buffer_size) {
                lp.write_pos -= buffer_size;
            }

            /* read from delay line with cubic interpolation */

            if (lp.read_pos_frac >= DELAYPOS_SCALE) {
                lp.read_pos += (lp.read_pos_frac >> DELAYPOS_SHIFT);
                lp.read_pos_frac &= DELAYPOS_MASK;
            }
            if (lp.read_pos >= buffer_size) {
                lp.read_pos -= buffer_size;
            }
            read_pos = lp.read_pos;
            frac = (float) lp.read_pos_frac * (1.0f / (float) DELAYPOS_SCALE);

            /* calculate interpolation coefficients */

            a2 = frac * frac;
            a2 -= 1.0;
            a2 *= (1.0 / 6.0);
            a1 = frac;
            a1 += 1.0;
            a1 *= 0.5;
            am1 = a1 - 1.0f;
            a0 = 3.0f * a2;
            a1 -= a0;
            am1 -= a2;
            a0 -= frac;

            /* read four samples for interpolation */

            if (read_pos > 0 && read_pos < (buffer_size - 2)) {
                vm1 = (lp.buf[read_pos - 1]);
                v0 = (lp.buf[read_pos]);
                v1 = (lp.buf[read_pos + 1]);
                v2 = (lp.buf[read_pos + 2]);
            } else {
                /* at buffer wrap-around, need to check index */

                if (--read_pos < 0) {
                    read_pos += buffer_size;
                }
                vm1 = lp.buf[read_pos];
                if (++read_pos >= buffer_size) {
                    read_pos -= buffer_size;
                }
                v0 = lp.buf[read_pos];
                if (++read_pos >= buffer_size) {
                    read_pos -= buffer_size;
                }
                v1 = lp.buf[read_pos];
                if (++read_pos >= buffer_size) {
                    read_pos -= buffer_size;
                }
                v2 = lp.buf[read_pos];
            }
            v0 = (am1 * vm1 + a0 * v0 + a1 * v1 + a2 * v2) * frac + v0;

            /* update buffer read position */

            lp.read_pos_frac += lp.read_pos_frac_inc;

            /* apply feedback gain and lowpass filter */

            v0 *= feedback_;
            v0 = (lp.filter_state - v0) * damp_fact + v0;
            lp.filter_state = v0;

            /* mix to output */

            if ((n & 1) != 0) {
                a_out_r += v0;
            } else {
                a_out_l += v0;
            }

            /* start next random line segment if current one has reached endpoint */

            if (--(lp.rand_line_cnt) <= 0) {
                NextRandomLineseg(lp, n);
            }
        }
        /* someday, use a_out_r for multimono out */

        out[0] = a_out_l * kOutputGain;
        out[1] = a_out_r * kOutputGain;
        return REVSC_OK;
    }

    public void Process(final float pLeft, final float pRight) {
        float[] mOut = new float[2];
        Process(pLeft, pRight, mOut);
        mLeft = mOut[0];
        mRight = mOut[1];
    }

    public float Process(final float pSignal) {
        Process(pSignal, pSignal);
        return GetLeft();
    }

    /**
     * Get the left channel's last sample
     */
    public float GetLeft() {
        return mLeft;
    }

    /**
     * Get the right channel's last sample
     */
    public float GetRight() {
        return mRight;
    }

    /**
     * controls the reverb time. reverb tail becomes infinite when set to 1.0
     *
     * @param fb - sets reverb time. range: 0.0 to 1.0
     */
    public void SetFeedback(final float fb) {
        feedback_ = fb;
    }

    /**
     * controls the internal dampening filter's cutoff frequency.
     *
     * @param freq - low pass frequency. range: 0.0 to sample_rate / 2
     */
    public void SetLpFreq(final float freq) {
        lpfreq_ = freq;
    }
//    private final float[] aux_ = new float[DSY_REVERBSC_MAX_SIZE];

    private int InitDelayLine(ReverbScDl lp, int n) {
        float read_pos;
        /* int     i; */

        /* calculate length of delay line */
        lp.buffer_size = DelayLineMaxSamples(sample_rate_, n);
        lp.dummy = 0;
        lp.write_pos = 0;
        /* set random seed */
        lp.seed_val = (int) (kReverbParams[n][3] + 0.5);
        /* set initial delay time */
        read_pos = (float) lp.seed_val * kReverbParams[n][1] / 32768;
        read_pos = kReverbParams[n][0] + (read_pos * i_pitch_mod_);
        read_pos = (float) lp.buffer_size - (read_pos * sample_rate_);
        lp.read_pos = (int) read_pos;
        read_pos = (read_pos - (float) lp.read_pos) * (float) DELAYPOS_SCALE;
        lp.read_pos_frac = (int) (read_pos + 0.5);
        /* initialise first random line segment */
        NextRandomLineseg(lp, n);
        /* clear delay line to zero */
        lp.filter_state = 0.0f;
        Arrays.fill(lp.buf, 0);
        return REVSC_OK;
    }

    private void NextRandomLineseg(ReverbScDl lp, int n) {
        float prv_del, nxt_del, phs_inc_val;

        /* update random seed */
        if (lp.seed_val < 0) {
            lp.seed_val += 0x10000;
        }
        lp.seed_val = (lp.seed_val * 15625 + 1) & 0xFFFF;
        if (lp.seed_val >= 0x8000) {
            lp.seed_val -= 0x10000;
        }
        /* length of next segment in samples */
        lp.rand_line_cnt = (int) ((sample_rate_ / kReverbParams[n][2]) + 0.5);
        prv_del = (float) lp.write_pos;
        prv_del -= ((float) lp.read_pos + ((float) lp.read_pos_frac / (float) DELAYPOS_SCALE));
        while (prv_del < 0.0) prv_del += lp.buffer_size;
        prv_del = prv_del / sample_rate_; /* previous delay time in seconds */
        nxt_del = (float) lp.seed_val * kReverbParams[n][1] / 32768.0f;
        /* next delay time in seconds */
        nxt_del = kReverbParams[n][0] + (nxt_del * i_pitch_mod_);
        /* calculate phase increment per sample */
        phs_inc_val = (prv_del - nxt_del) / (float) lp.rand_line_cnt;
        phs_inc_val = phs_inc_val * sample_rate_ + 1.0f;
        lp.read_pos_frac_inc = (int) (phs_inc_val * DELAYPOS_SCALE + 0.5);
    }

    /**
     * Delay line for internal reverb use
     */
    private static class ReverbScDl {
        float[] buf;           /*< buffer ptr */
        int buffer_size;       /*< buffer size */
        int dummy;             /*<  dummy var */
        float filter_state;    /*< state of filter */
        int rand_line_cnt;     /*< number of random lines */
        int read_pos;          /*< read position */
        int read_pos_frac;     /*< fractional component of read pos */
        int read_pos_frac_inc; /*< increment for fractional */
        int seed_val;          /*< randseed */
        int write_pos;         /*< write position */
    }
}
