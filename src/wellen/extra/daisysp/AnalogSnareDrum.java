package wellen.extra.daisysp;

/**
 * @author Ben Sergentanis
 * @brief 808 snare drum model, revisited.
 * @date Jan 2021
 * <p>
 * Ported from pichenettes/eurorack/plaits/dsp/drums/analog_snare_drum.h to an independent module.
 * <p>
 * Original code written by Emilie Gillet in 2016.
 */
public class AnalogSnareDrum {

    private static final int kNumModes = 5;
    private static final float[] kModeFrequencies = {1.00f, 2.00f, 3.18f, 4.16f, 5.62f};
    private float sample_rate_;
    private float f0_, tone_, accent_, snappy_, decay_;
    private boolean sustain_;
    private boolean trig_;
    private int pulse_remaining_samples_;
    private float pulse_;
    private float pulse_height_;
    private float pulse_lp_;
    private float noise_envelope_;
    private float sustain_gain_;
    private final Svf[] resonator_ = new Svf[kNumModes];
    private final Svf noise_filter_ = new Svf();

    // Replace the resonators in "free running" (sustain) mode.
    private final float[] phase_ = new float[kNumModes];

    /**
     * Init the module \param sample_rate Audio engine sample rate
     */
    public void Init(float sample_rate) {
        sample_rate_ = sample_rate;

        trig_ = false;

        pulse_remaining_samples_ = 0;
        pulse_ = 0.0f;
        pulse_height_ = 0.0f;
        pulse_lp_ = 0.0f;
        noise_envelope_ = 0.0f;
        sustain_gain_ = 0.0f;

        SetSustain(false);
        SetAccent(.6f);
        SetFreq(200.f);
        SetDecay(.3f);
        SetSnappy(.7f);
        SetTone(.5f);

        for (int i = 0; i < kNumModes; ++i) {
            resonator_[i] = new Svf();
            resonator_[i].Init(sample_rate_);
            phase_[i] = 0.f;
        }
        noise_filter_.Init(sample_rate_);
    }

    /**
     * Trigger the drum
     */
    public void Trig() {
        trig_ = true;
    }

    public void SetSustain(boolean sustain) {
        sustain_ = sustain;
    }

    /**
     * Set how much accent to use \param accent Works 0-1.
     */
    public void SetAccent(float accent) {
        accent_ = DSP.fclamp(accent, 0.f, 1.f);
    }

    /**
     * Set the drum's root frequency \param f0 Freq in Hz
     */
    public void SetFreq(float f0) {
        f0 = f0 / sample_rate_;
        f0_ = DSP.fclamp(f0, 0.f, .4f);
    }

    /**
     * Set the brightness of the drum tone. \param tone Works 0-1. 1 = bright, 0 = dark.
     */
    public void SetTone(float tone) {
        tone_ = DSP.fclamp(tone, 0.f, 1.f);
        tone_ *= 2.f;
    }

    /**
     * Set the length of the drum decay \param decay Works with positive numbers
     */
    public void SetDecay(float decay) {
        decay_ = decay;
//        decay_ = fmax(decay, 0.0f);
    }

    /**
     * Sets the mix between snare and drum. \param snappy 1 = just snare. 0 = just drum.
     */
    public void SetSnappy(float snappy) {
        snappy_ = DSP.fclamp(snappy, 0.f, 1.f);
    }

    /**
     * Get the next sample \param trigger Hit the drum with true. Defaults to false.
     */
    public float Process() {
        return Process(false);
    }

    public float Process(boolean trigger) {
        final float decay_xt = decay_ * (1.0f + decay_ * (decay_ - 1.0f));
        final int kTriggerPulseDuration = (int) (1.0e-3f * sample_rate_);
        final float kPulseDecayTime = 0.1e-3f * sample_rate_;
        final float q = 2000.0f * DSP.powf(2.f, DSP.kOneTwelfth * decay_xt * 84.0f);
        final float noise_envelope_decay = 1.0f - 0.0017f * DSP.powf(2.f,
                                                                     DSP.kOneTwelfth * (-decay_ * (50.0f + snappy_ * 10.0f)));
        final float exciter_leak = snappy_ * (2.0f - snappy_) * 0.1f;

        float snappy = snappy_ * 1.1f - 0.05f;
        snappy = DSP.fclamp(snappy, 0.0f, 1.0f);

        float tone = tone_;

        if (trigger || trig_) {
            trig_ = false;
            pulse_remaining_samples_ = kTriggerPulseDuration;
            pulse_height_ = 3.0f + 7.0f * accent_;
            noise_envelope_ = 2.0f;
        }

        float[] f = new float[kNumModes];
        float[] gain = new float[kNumModes];

        for (int i = 0; i < kNumModes; ++i) {
            f[i] = DSP.fmin(f0_ * kModeFrequencies[i], 0.499f);
            resonator_[i].SetFreq(f[i] * sample_rate_);
            //        resonator_[i].SetRes(1.0f + f[i] * (i == 0 ? q : q * 0.25f));
            resonator_[i].SetRes((f[i] * (i == 0 ? q : q * 0.25f)) * 0.2f);
        }

        if (tone < 0.666667f) {
            // 808-style (2 modes)
            tone *= 1.5f;
            gain[0] = 1.5f + (1.0f - tone) * (1.0f - tone) * 4.5f;
            gain[1] = 2.0f * tone + 0.15f;
            for (int i = 2; i < kNumModes; i++) {
                gain[i] = 0.f;
            }
        } else {
            // What the 808 could have been if there were extra modes!
            tone = (tone - 0.666667f) * 3.0f;
            gain[0] = 1.5f - tone * 0.5f;
            gain[1] = 2.15f - tone * 0.7f;
            for (int i = 2; i < kNumModes; ++i) {
                gain[i] = tone;
                tone *= tone;
            }
        }

        float f_noise = f0_ * 16.0f;
        DSP.fclamp(f_noise, 0.0f, 0.499f);
        noise_filter_.SetFreq(f_noise * sample_rate_);
        //noise_filter_.SetRes(1.0f + f_noise * 1.5f);
        noise_filter_.SetRes(f_noise * 1.5f);

        // Q45 / Q46
        float pulse = 0.0f;
        if (pulse_remaining_samples_ != 0) {
            --pulse_remaining_samples_;
            pulse = pulse_remaining_samples_ != 0 ? pulse_height_ : pulse_height_ - 1.0f;
            pulse_ = pulse;
        } else {
            pulse_ *= 1.0f - 1.0f / kPulseDecayTime;
            pulse = pulse_;
        }

        float sustain_gain_value = sustain_gain_ = accent_ * decay_;

        // R189 / C57 / R190 + C58 / C59 / R197 / R196 / IC14
        pulse_lp_ = DSP.fclamp(pulse_lp_, pulse, 0.75f);

        float shell = 0.0f;
        for (int i = 0; i < kNumModes; ++i) {
            float excitation = i == 0 ? (pulse - pulse_lp_) + 0.006f * pulse : 0.026f * pulse;

            phase_[i] += f[i];
            phase_[i] = phase_[i] >= 1.f ? phase_[i] - 1.f : phase_[i];

            resonator_[i].Process(excitation);

            shell += gain[i] * (sustain_ ? DSP.sin(
            phase_[i] * DSP.TWOPI_F) * sustain_gain_value * 0.25f : resonator_[i].Band() + excitation * exciter_leak);
        }
        shell = SoftClip(shell);

        // C56 / R194 / Q48 / C54 / R188 / D54
        float noise = 2.0f * DSP.randf() - 1.0f;
        if (noise < 0.0f) {
            noise = 0.0f;
        }
        noise_envelope_ *= noise_envelope_decay;
        noise *= (sustain_ ? sustain_gain_value : noise_envelope_) * snappy * 2.0f;

        // C66 / R201 / C67 / R202 / R203 / Q49
        noise_filter_.Process(noise);
        noise = noise_filter_.Band();

        // IC13
        return noise + shell * (1.0f - snappy);
    }

    private float SoftLimit(float x) {
        return x * (27.0f + x * x) / (27.0f + 9.0f * x * x);
    }

    private float SoftClip(float x) {
        if (x < -3.0f) {
            return -1.0f;
        } else if (x > 3.0f) {
            return 1.0f;
        } else {
            return SoftLimit(x);
        }
    }
}
