package wellen.extra.daisysp;

import static wellen.extra.daisysp.DSP.fclamp;
import static wellen.extra.daisysp.DSP.fmax;
import static wellen.extra.daisysp.DSP.fmin;

/**
 * @brief Flanging Audio Effect
 * <p>
 * Generates a modulating phase shifted copy of a signal, and recombines with the original to create a 'flanging' sound
 * effect.
 */
public class Flanger {
    /**
     * Initialize the modules \param sample_rate Audio engine sample rate.
     */
    public void Init(float sample_rate) {
        sample_rate_ = sample_rate;

        SetFeedback(.2f);

        del_.Init();
        lfo_amp_ = 0.f;
        SetDelay(.75f);

        lfo_phase_ = 0.f;
        SetLfoFreq(.3f);
        SetLfoDepth(.9f);
    }

    /**
     * Get the next sample
     *
     * @param in Sample to process
     */
    public float Process(float in) {
        float lfo_sig = ProcessLfo();
        del_.SetDelay(1.f + lfo_sig + delay_);

        float out = del_.Read();
        del_.Write(in + out * feedback_);

        return (in + out) * .5f; //equal mix
    }

    /**
     * How much of the signal to feedback into the delay line.
     *
     * @param feedback Works 0-1.
     */
    public void SetFeedback(float feedback) {
        feedback_ = fclamp(feedback, 0.f, 1.f);
        feedback_ *= .97f;
    }

    /**
     * How much to modulate the delay by.
     *
     * @param depth Works 0-1.
     */
    public void SetLfoDepth(float depth) {
        depth = fclamp(depth, 0.f, .93f);
        lfo_amp_ = depth * delay_;
    }

    /**
     * Set lfo frequency.
     *
     * @param freq Frequency in Hz
     */
    public void SetLfoFreq(float freq) {
        freq = 4.f * freq / sample_rate_;
        freq *= lfo_freq_ < 0.f ? -1.f : 1.f;  //if we're headed down, keep going
        lfo_freq_ = fclamp(freq, -.25f, .25f); //clip at +/- .125 * sr
    }


    /**
     * Set the internal delay rate.
     *
     * @param delay Tuned for 0-1. Maps to .1 to 7 ms.
     */
    public void SetDelay(float delay) {
        delay = (.1f + delay * 6.9f); //.1 to 7 ms
        SetDelayMs(delay);
    }

    /**
     * Set the delay time in ms.
     *
     * @param ms Delay time in ms.
     */
    public void SetDelayMs(float ms) {
        ms = fmax(.1f, ms);
        delay_ = ms * .001f * sample_rate_; //ms to samples

        lfo_amp_ = fmin(lfo_amp_, delay_); //clip this if needed
    }

    private float sample_rate_;
    private static final int kDelayLength = 960; // 20 ms at 48kHz = .02 * 48000

    private float feedback_;

    //triangle lfos
    private float lfo_phase_;
    private float lfo_freq_;
    private float lfo_amp_;

    private float delay_;

    private final DelayLine del_ = new DelayLine(kDelayLength);

    float ProcessLfo() {
        lfo_phase_ += lfo_freq_;

        //wrap around and flip direction
        if (lfo_phase_ > 1.f) {
            lfo_phase_ = 1.f - (lfo_phase_ - 1.f);
            lfo_freq_ *= -1.f;
        } else if (lfo_phase_ < -1.f) {
            lfo_phase_ = -1.f - (lfo_phase_ + 1.f);
            lfo_freq_ *= -1.f;
        }

        return lfo_phase_ * lfo_amp_;
    }
}
