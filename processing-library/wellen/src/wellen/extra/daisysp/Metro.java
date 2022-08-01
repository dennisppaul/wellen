package wellen.extra.daisysp;

import static wellen.extra.daisysp.DaisySP.TWOPI_F;

/**
 * Creates a clock signal at a specific frequency.
 */
public class Metro {
    /**
     * Initializes Metro module.
     *
     * @param freq        frequency at which new clock signals will be generated Input Range:
     * @param sample_rate sample rate of audio engine Input range:
     */
    public void Init(float freq, float sample_rate) {
        freq_ = freq;
        phs_ = 0.0f;
        sample_rate_ = sample_rate;
        phs_inc_ = (TWOPI_F * freq_) / sample_rate_;
    }

    /**
     * checks current state of Metro object and updates state if necesary.
     */
    public boolean Process() {
        phs_ += phs_inc_;
        if (phs_ >= TWOPI_F) {
            phs_ -= TWOPI_F;
            return true;
        }
        return false;
    }

    /**
     * resets phase to 0
     */
    public void Reset() {
        phs_ = 0.0f;
    }

    /**
     * Sets frequency at which Metro module will run at.
     */
    public void SetFreq(float freq) {
        freq_ = freq;
        phs_inc_ = (TWOPI_F * freq_) / sample_rate_;
    }


    /**
     * Returns current value for frequency.
     */
    public float GetFreq() {
        return freq_;
    }

    private float freq_;
    private float phs_, sample_rate_, phs_inc_;

}
