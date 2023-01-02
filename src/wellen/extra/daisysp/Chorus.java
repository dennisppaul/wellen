package wellen.extra.daisysp;

import static wellen.extra.daisysp.DaisySP.fclamp;
import static wellen.extra.daisysp.DaisySP.fmax;
import static wellen.extra.daisysp.DaisySP.fmin;

/**
 * @author Ben Sergentanis
 * @brief Chorus Effect.
 * @date Jan 2021 Based on
 *         https://www.izotope.com/en/learn/understanding-chorus-flangers-and-phasers-in-audio-production.html \n and
 *         https://www.researchgate.net/publication/236629475_Implementing_Professional_Audio_Effects_with_DSPs \n
 */
public class Chorus {

    private final ChorusEngine[] engines_ = new ChorusEngine[2];
    private float gain_frac_;
    private final float[] pan_ = new float[2];
    private float sigl_, sigr_;

    /**
     * Initialize the module
     *
     * @param sample_rate Audio engine sample rate
     */
    public void Init(float sample_rate) {
        for (int i = 0; i < engines_.length; i++) {
            engines_[i] = new ChorusEngine();
            engines_[i].Init(sample_rate);
        }
        SetPan(.25f, .75f);

        gain_frac_ = .5f;
        sigl_ = sigr_ = 0.f;
    }

    /**
     * Get the net floating point sample. Defaults to left channel.
     *
     * @param in Sample to process
     */
    public float Process(float in) {
        sigl_ = 0.f;
        sigr_ = 0.f;

        for (int i = 0; i < 2; i++) {
            float sig = engines_[i].Process(in);
            sigl_ += (1.f - pan_[i]) * sig;
            sigr_ += pan_[i] * sig;
        }

        sigl_ *= gain_frac_;
        sigr_ *= gain_frac_;

        return sigl_;
    }

    /**
     * Get the left channel's last sample
     */
    public float GetLeft() {
        return sigl_;
    }

    /**
     * Get the right channel's last sample
     */
    public float GetRight() {
        return sigr_;
    }

    /**
     * Pan both channels individually.
     *
     * @param panl Pan the left channel. 0 is left, 1 is right.
     * @param panr Pan the right channel.
     */
    public void SetPan(float panl, float panr) {
        pan_[0] = fclamp(panl, 0.f, 1.f);
        pan_[1] = fclamp(panr, 0.f, 1.f);
    }

    /**
     * Pan both channels.
     *
     * @param pan Where to pan both channels to. 0 is left, 1 is right.
     */
    public void SetPan(float pan) {
        SetPan(pan, pan);
    }

    /**
     * Set both lfo depths individually.
     *
     * @param depthl Left channel lfo depth. Works 0-1.
     * @param depthr Right channel lfo depth.
     */
    public void SetLfoDepth(float depthl, float depthr) {
        engines_[0].SetLfoDepth(depthl);
        engines_[1].SetLfoDepth(depthr);
    }

    /**
     * Set both lfo depths.
     *
     * @param depth Both channels lfo depth. Works 0-1.
     */
    public void SetLfoDepth(float depth) {
        SetLfoDepth(depth, depth);
    }

    /**
     * Set both lfo frequencies individually.
     *
     * @param freql Left channel lfo freq in Hz.
     * @param freqr Right channel lfo freq in Hz.
     */
    public void SetLfoFreq(float freql, float freqr) {
        engines_[0].SetLfoFreq(freql);
        engines_[1].SetLfoFreq(freqr);
    }

    /**
     * Set both lfo frequencies.
     *
     * @param freq Both channel lfo freqs in Hz.
     */
    public void SetLfoFreq(float freq) {
        SetLfoFreq(freq, freq);
    }

    /**
     * Set both channel delay amounts individually.
     *
     * @param delayl Left channel delay amount. Works 0-1.
     * @param delayr Right channel delay amount.
     */
    public void SetDelay(float delayl, float delayr) {
        engines_[0].SetDelay(delayl);
        engines_[1].SetDelay(delayr);
    }

    /**
     * Set both channel delay amounts.
     *
     * @param delay Both channel delay amount. Works 0-1.
     */
    public void SetDelay(float delay) {
        SetDelay(delay, delay);
    }

    /**
     * Set both channel delay individually.
     *
     * @param msl Left channel delay in ms.
     * @param msr Right channel delay in ms.
     */
    public void SetDelayMs(float msl, float msr) {
        engines_[0].SetDelayMs(msl);
        engines_[1].SetDelayMs(msr);
    }

    /**
     * Set both channel delay in ms.
     *
     * @param ms Both channel delay amounts in ms.
     */
    public void SetDelayMs(float ms) {
        SetDelayMs(ms, ms);
    }

    /**
     * Set both channels feedback individually.
     *
     * @param feedbackl Left channel feedback. Works 0-1.
     * @param feedbackr Right channel feedback.
     */
    public void SetFeedback(float feedbackl, float feedbackr) {
        engines_[0].SetFeedback(feedbackl);
        engines_[1].SetFeedback(feedbackr);
    }

    /**
     * Set both channels feedback.
     *
     * @param feedback Both channel feedback. Works 0-1.
     */
    public void SetFeedback(float feedback) {
        SetFeedback(feedback, feedback);
    }

    /**
     * @author Ben Sergentanis
     * @brief Single Chorus engine. Used in Chorus.
     */
    public static class ChorusEngine {

        private static final int kDelayLength = 2400; // 50 ms at 48kHz = .05 * 48000
        private final DelayLine del_ = new DelayLine(kDelayLength);
        private float delay_;
        private float feedback_;
        private float lfo_amp_;
        private float lfo_freq_;
        //triangle lfos
        private float lfo_phase_;
        private float sample_rate_;

        /**
         * Initialize the module
         *
         * @param sample_rate Audio engine sample rate.
         */
        public void Init(float sample_rate) {
            sample_rate_ = sample_rate;

            del_.Init();
            lfo_amp_ = 0.f;
            feedback_ = .2f;
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
            del_.SetDelay(lfo_sig + delay_);

            float out = del_.Read();
            del_.Write(in + out * feedback_);

            return (in + out) * .5f; //equal mix
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
         * @param delay Tuned for 0-1. Maps to .1 to 50 ms.
         */
        public void SetDelay(float delay) {
            delay = (.1f + delay * 7.9f); //.1 to 8 ms
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

        /**
         * Set the feedback amount.
         *
         * @param feedback Amount from 0-1.
         */
        public void SetFeedback(float feedback) {
            feedback_ = fclamp(feedback, 0.f, 1.f);
        }

        private float ProcessLfo() {
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
}
