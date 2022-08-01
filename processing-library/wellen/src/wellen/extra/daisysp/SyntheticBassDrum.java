package wellen.extra.daisysp;

import static wellen.extra.daisysp.DaisySP.*;

/**
 * @author Ben Sergentanis
 * @brief Naive bass drum model (modulated oscillator with FM + envelope).
 * @date Jan 2021 Inadvertently 909-ish.
 * <p>
 * Ported from pichenettes/eurorack/plaits/dsp/drums/synthetic_bass_drum.h to an independent module.
 * <p>
 * Original code written by Emilie Gillet in 2016.
 */
public class SyntheticBassDrum {

    /**
     * Init the module \param sample_rate Audio engine sample rate.
     */
    public void Init(float sample_rate) {
        sample_rate_ = sample_rate;

        trig_ = false;

        phase_ = 0.0f;
        phase_noise_ = 0.0f;
        f0_ = 0.0f;
        fm_ = 0.0f;
        fm_lp_ = 0.0f;
        body_env_lp_ = 0.0f;
        body_env_ = 0.0f;
        body_env_pulse_width_ = 0;
        fm_pulse_width_ = 0;
        tone_lp_ = 0.0f;
        sustain_gain_ = 0.0f;

        SetFreq(100.f);
        SetSustain(false);
        SetAccent(.2f);
        SetTone(.6f);
        SetDecay(.7f);
        SetDirtiness(.3f);
        SetFmEnvelopeAmount(.6f);
        SetFmEnvelopeDecay(.3f);

        click_.Init(sample_rate);
        noise_.Init();
    }

    /**
     * Generates a distorted sine wave
     */
    public float DistortedSine(float phase, float phase_noise, float dirtiness) {
        phase += phase_noise * dirtiness;

        //MAKE_INTEGRAL_FRACTIONAL(phase);
        int phase_integral = (int) (phase);
        float phase_fractional = phase - (float) (phase_integral);

        phase = phase_fractional;
        float triangle = (phase < 0.5f ? phase : 1.0f - phase) * 4.0f - 1.0f;
        float sine = 2.0f * triangle / (1.0f + fabsf(triangle));
        float clean_sine = sinf(TWOPI_F * (phase + 0.75f));
        return sine + (1.0f - dirtiness) * (clean_sine - sine);
    }

    /**
     * Transistor VCA simulation. \param s Input sample. \param gain VCA gain.
     */
    public float TransistorVCA(float s, float gain) {
        s = (s - 0.6f) * gain;
        return 3.0f * s / (2.0f + fabsf(s)) + gain * 0.3f;
    }

    /**
     * Get the next sample. \param trigger True triggers the BD. This is optional.
     */
    public float Process(boolean trigger) {
        float dirtiness = dirtiness_;
        dirtiness *= fmax(1.0f - 8.0f * new_f0_, 0.0f);

        final float fm_decay = 1.0f - 1.0f / (0.008f * (1.0f + fm_envelope_decay_ * 4.0f) * sample_rate_);

        final float body_env_decay = 1.0f - 1.0f / (0.02f * sample_rate_) * powf(2.f, (-decay_ * 60.0f) * kOneTwelfth);
        final float transient_env_decay = 1.0f - 1.0f / (0.005f * sample_rate_);
        final float tone_f = fmin(4.0f * new_f0_ * powf(2.f, (tone_ * 108.0f) * kOneTwelfth), 1.0f);
        final float transient_level = tone_;

        if (trigger || trig_) {
            trig_ = false;
            fm_ = 1.0f;
            body_env_ = transient_env_ = 0.3f + 0.7f * accent_;
            body_env_pulse_width_ = (int) (sample_rate_ * 0.001f);
            fm_pulse_width_ = (int) (sample_rate_ * 0.0013f);
        }

        sustain_gain_ = accent_ * decay_;

        phase_noise_ = fonepole_return(phase_noise_, rand_kRandFrac() - 0.5f, 0.002f);

        float mix = 0.0f;

        if (sustain_) {
            f0_ = new_f0_;
            phase_ += f0_;
            if (phase_ >= 1.0f) {
                phase_ -= 1.0f;
            }
            float body = DistortedSine(phase_, phase_noise_, dirtiness);
            mix -= TransistorVCA(body, sustain_gain_);
        } else {
            if (fm_pulse_width_ != 0) {
                --fm_pulse_width_;
                phase_ = 0.25f;
            } else {
                fm_ *= fm_decay;
                float fm = 1.0f + fm_envelope_amount_ * 3.5f * fm_lp_;
                f0_ = new_f0_;
                phase_ += fmin(f0_ * fm, 0.5f);
                if (phase_ >= 1.0f) {
                    phase_ -= 1.0f;
                }
            }

            if (body_env_pulse_width_ != 0) {
                --body_env_pulse_width_;
            } else {
                body_env_ *= body_env_decay;
                transient_env_ *= transient_env_decay;
            }

            final float envelope_lp_f = 0.1f;
            body_env_lp_ = fonepole_return(body_env_lp_, body_env_, envelope_lp_f);
            transient_env_lp_ = fonepole_return(transient_env_lp_, transient_env_, envelope_lp_f);
            fm_lp_ = fonepole_return(fm_lp_, fm_, envelope_lp_f);

            float body = DistortedSine(phase_, phase_noise_, dirtiness);
            float transient_ = click_.Process((body_env_pulse_width_ != 0) ? 0.0f : 1.0f) + noise_.Process();

            mix -= TransistorVCA(body, body_env_lp_);
            mix -= transient_ * transient_env_lp_ * transient_level;
        }

        tone_lp_ = fonepole_return(tone_lp_, mix, tone_f);
        return tone_lp_;
    }

    public float Process() {
        float s = Process(mTrigger);
        mTrigger = false;
        return s;
    }

    /**
     * Trigger the drum
     */
    public void Trig() {
        mTrigger = true;
        trig_ = true;
    }


    /**
     * Allows the drum to play continuously
     *
     * @param sustain True sets the drum on infinite sustain.
     */
    public void SetSustain(boolean sustain) {
        sustain_ = sustain;
    }

    /**
     * Sets the amount of accent.
     *
     * @param accent Works 0-1.
     */
    public void SetAccent(float accent) {
        accent_ = fclamp(accent, 0.f, 1.f);
    }

    /**
     * Set the bass drum's root frequency.
     *
     * @param freq in Hz.
     */
    public void SetFreq(float freq) {
        freq /= sample_rate_;
        new_f0_ = fclamp(freq, 0.f, 1.f);
    }

    /**
     * Sets the overall bright / darkness of the drum.
     *
     * @param tone Works 0-1.
     */
    public void SetTone(float tone) {
        tone_ = fclamp(tone, 0.f, 1.f);
    }

    /**
     * Sets how long the drum's volume takes to decay.
     *
     * @param decay 0-1.
     */
    public void SetDecay(float decay) {
        decay = fclamp(decay, 0.f, 1.f);
        decay_ = decay * decay;
    }

    /**
     * Makes things grimy
     *
     * @param dirtiness Works 0-1.
     */
    public void SetDirtiness(float dirtiness) {
        dirtiness_ = fclamp(dirtiness, 0.f, 1.f);
    }

    /**
     * Sets how much of a pitch sweep the drum experiences when triggered.
     *
     * @param fm_envelope_amount Works 0-1.
     */
    public void SetFmEnvelopeAmount(float fm_envelope_amount) {
        fm_envelope_amount_ = fclamp(fm_envelope_amount, 0.f, 1.f);
    }

    /**
     * Sets how long the initial pitch sweep takes. \param fm_envelope_decay Works 0-1.
     */
    public void SetFmEnvelopeDecay(float fm_envelope_decay) {
        fm_envelope_decay = fclamp(fm_envelope_decay, 0.f, 1.f);
        fm_envelope_decay_ = fm_envelope_decay * fm_envelope_decay;
    }

    private float sample_rate_;

    private boolean trig_;
    private boolean sustain_;
    private float accent_, new_f0_, tone_, decay_;
    private float dirtiness_, fm_envelope_amount_, fm_envelope_decay_;

    private float f0_;
    private float phase_;
    private float phase_noise_;

    private float fm_;
    private float fm_lp_;
    private float body_env_;
    private float body_env_lp_;
    private float transient_env_;
    private float transient_env_lp_;

    private float sustain_gain_;

    private float tone_lp_;

    private final SyntheticBassDrumClick click_ = new SyntheticBassDrumClick();
    private final SyntheticBassDrumAttackNoise noise_ = new SyntheticBassDrumAttackNoise();

    private int body_env_pulse_width_;
    private int fm_pulse_width_;

    private boolean mTrigger = false;
}
