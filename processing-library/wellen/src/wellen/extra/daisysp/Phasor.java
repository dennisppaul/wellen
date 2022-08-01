package wellen.extra.daisysp;

import static wellen.extra.daisysp.DaisySP.TWOPI_F;

/**
 * Generates a normalized signal moving from 0-1 at the specified frequency.
 */
public class Phasor {

    private float freq_;
    private float sample_rate_, inc_, phs_;

    /**
     * Initializes the Phasor module sample rate, and freq are in Hz initial phase is in radians Additional Init
     * functions have defaults when arg is not specified: - phs = 0.0f - freq = 1.0f
     */
    public void Init(float sample_rate, float freq, float initial_phase) {
        sample_rate_ = sample_rate;
        phs_ = initial_phase;
        SetFreq(freq);
    }

    /**
     * Initialize phasor with samplerate and freq
     */
    public void Init(float sample_rate, float freq) {
        Init(sample_rate, freq, 0.0f);
    }

    /**
     * Initialize phasor with samplerate
     */
    public void Init(float sample_rate) {
        Init(sample_rate, 1.0f, 0.0f);
    }

    /**
     * processes Phasor and returns current value
     */
    public float Process() {
        float out;
        out = phs_ / TWOPI_F;
        phs_ += inc_;
        if (phs_ > TWOPI_F) {
            phs_ -= TWOPI_F;
        }
        if (phs_ < 0.0f) {
            phs_ = 0.0f;
        }
        return out;
    }

    /**
     * Sets frequency of the Phasor in Hz
     */
    public void SetFreq(float freq) {
        freq_ = freq;
        inc_ = (TWOPI_F * freq_) / sample_rate_;
    }

    /**
     * Returns current frequency value in Hz
     */
    public float GetFreq() {
        return freq_;
    }
}
