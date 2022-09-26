package wellen.extra.daisysp;

import static wellen.extra.daisysp.DaisySP.fabsf;
import static wellen.extra.daisysp.DaisySP.fclamp;
import static wellen.extra.daisysp.DaisySP.fmax;
import static wellen.extra.daisysp.DaisySP.fmin;
import static wellen.extra.daisysp.DaisySP.kOneTwelfth;
import static wellen.extra.daisysp.DaisySP.powf;
import static wellen.extra.daisysp.DaisySP.rand_kRandFrac;
import static wellen.extra.daisysp.DaisySP.sqrtf;

/**
 * @author Ben Sergentanis
 * @brief Naive snare drum model (two modulated oscillators + filtered noise).
 * @date Jan 2021
 *         <p>
 *         Uses a few magic numbers taken from the 909 schematics: \n
 *         <p>
 *         - Ratio between the two modes of the drum set to 1.47. \n
 *         <p>
 *         - Funky coupling between the two modes. \n
 *         <p>
 *         - Noise coloration filters and envelope shapes for the snare. \n \n
 *         <p>
 *         Ported from pichenettes/eurorack/plaits/dsp/drums/synthetic_snare_drum.h \n to an independent module. \n
 *         <p>
 *         Original code written by Emilie Gillet in 2016. \n
 */
public class SyntheticSnareDrum {

    /**
     * Init the module \param sample_rate Audio engine sample rate
     */
    public void Init(float sample_rate) {
        sample_rate_ = sample_rate;

        phase_[0] = 0.0f;
        phase_[1] = 0.0f;
        drum_amplitude_ = 0.0f;
        snare_amplitude_ = 0.0f;
        fm_ = 0.0f;
        hold_counter_ = 0;
        sustain_gain_ = 0.0f;

        SetSustain(false);
        SetAccent(.6f);
        SetFreq(200.f);
        SetFmAmount(.1f);
        SetDecay(.3f);
        SetSnappy(.7f);

        trig_ = false;

        drum_lp_.Init(sample_rate_);
        snare_hp_.Init(sample_rate_);
        snare_lp_.Init(sample_rate_);
    }

    /**
     * Get the next sample.
     *
     * @param trigger True = hit the drum. This argument is optional.
     */
    public float Process(boolean trigger) {
        final float decay_xt = decay_ * (1.0f + decay_ * (decay_ - 1.0f));
        final float drum_decay = 1.0f - 1.0f / (0.015f * sample_rate_) * powf(2.f,
                                                                              kOneTwelfth * (-decay_xt * 72.0f - fm_amount_ * 12.0f + snappy_ * 7.0f));

        final float snare_decay = 1.0f - 1.0f / (0.01f * sample_rate_) * powf(2.f,
                                                                              kOneTwelfth * (-decay_ * 60.0f - snappy_ * 7.0f));
        final float fm_decay = 1.0f - 1.0f / (0.007f * sample_rate_);

        float snappy = snappy_ * 1.1f - 0.05f;
        snappy = fclamp(snappy, 0.0f, 1.0f);

        final float drum_level = sqrtf(1.0f - snappy);
        final float snare_level = sqrtf(snappy);

        final float snare_f_min = fmin(10.0f * f0_, 0.5f);
        final float snare_f_max = fmin(35.0f * f0_, 0.5f);

        snare_hp_.SetFreq(snare_f_min * sample_rate_);
        snare_lp_.SetFreq(snare_f_max * sample_rate_);
        snare_lp_.SetRes(0.5f + 2.0f * snappy);

        drum_lp_.SetFreq(3.0f * f0_ * sample_rate_);

        if (trigger || trig_) {
            trig_ = false;
            snare_amplitude_ = drum_amplitude_ = 0.3f + 0.7f * accent_;
            fm_ = 1.0f;
            phase_[0] = phase_[1] = 0.0f;
            hold_counter_ = (int) ((0.04f + decay_ * 0.03f) * sample_rate_);
        }

        even = !even;
        if (sustain_) {
            sustain_gain_ = snare_amplitude_ = accent_ * decay_;
            drum_amplitude_ = snare_amplitude_;
            fm_ = 0.0f;
        } else {
            // Compute all D envelopes.
            // The envelope for the drum has a very long tail.
            // The envelope for the snare has a "hold" stage which lasts between
            // 40 and 70 ms
            drum_amplitude_ *= (drum_amplitude_ > 0.03f || even) ? drum_decay : 1.0f;
            if (hold_counter_ != 0) {
                --hold_counter_;
            } else {
                snare_amplitude_ *= snare_decay;
            }
            fm_ *= fm_decay;
        }

        // The 909 circuit has a funny kind of oscillator coupling - the signal
        // leaving Q40's collector and resetting all oscillators allow some
        // intermodulation.
        float reset_noise = 0.0f;
        float reset_noise_amount = (0.125f - f0_) * 8.0f;
        reset_noise_amount = fclamp(reset_noise_amount, 0.0f, 1.0f);
        reset_noise_amount *= reset_noise_amount;
        reset_noise_amount *= fm_amount_;
        reset_noise += phase_[0] > 0.5f ? -1.0f : 1.0f;
        reset_noise += phase_[1] > 0.5f ? -1.0f : 1.0f;
        reset_noise *= reset_noise_amount * 0.025f;

        float f = f0_ * (1.0f + fm_amount_ * (4.0f * fm_));
        phase_[0] += f;
        phase_[1] += f * 1.47f;
        if (reset_noise_amount > 0.1f) {
            if (phase_[0] >= 1.0f + reset_noise) {
                phase_[0] = 1.0f - phase_[0];
            }
            if (phase_[1] >= 1.0f + reset_noise) {
                phase_[1] = 1.0f - phase_[1];
            }
        } else {
            if (phase_[0] >= 1.0f) {
                phase_[0] -= 1.0f;
            }
            if (phase_[1] >= 1.0f) {
                phase_[1] -= 1.0f;
            }
        }

        float drum = -0.1f;
        drum += DistortedSine(phase_[0]) * 0.60f;
        drum += DistortedSine(phase_[1]) * 0.25f;
        drum *= drum_amplitude_ * drum_level;

        drum_lp_.Process(drum);
        drum = drum_lp_.Low();

        float noise = rand_kRandFrac();
        snare_lp_.Process(noise);
        float snare = snare_lp_.Low();
        snare_hp_.Process(snare);
        snare = snare_hp_.High();
        snare = (snare + 0.1f) * (snare_amplitude_ + fm_) * snare_level;

        return snare + drum; // It's a snare, it's a drum, it's a snare drum.
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
     * Make the drum ring out infinitely. \param sustain True = infinite sustain.
     */
    public void SetSustain(boolean sustain) {
        sustain_ = sustain;
    }


    /**
     * Set how much accent to use \param accent Works 0-1.
     */
    public void SetAccent(float accent) {
        accent_ = fclamp(accent, 0.f, 1.f);
    }

    /**
     * Set the drum's root frequency \param f0 Freq in Hz
     */
    public void SetFreq(float f0) {
        f0 /= sample_rate_;
        f0_ = fclamp(f0, 0.f, 1.f);
    }

    /**
     * Set the amount of fm sweep. \param fm_amount Works from 0 - 1.
     */
    public void SetFmAmount(float fm_amount) {
        fm_amount = fclamp(fm_amount, 0.f, 1.f);
        fm_amount_ = fm_amount * fm_amount;
    }

    /**
     * Set the length of the drum decay \param decay Works with positive numbers
     */
    public void SetDecay(float decay) {
        decay_ = fmax(decay, 0.f);
    }

    /**
     * Sets the mix between snare and drum. \param snappy 1 = just snare. 0 = just drum.
     */
    public void SetSnappy(float snappy) {
        snappy_ = fclamp(snappy, 0.f, 1.f);
    }

    private float DistortedSine(float phase) {
        float triangle = (phase < 0.5f ? phase : 1.0f - phase) * 4.0f - 1.3f;
        return 2.0f * triangle / (1.0f + fabsf(triangle));
    }

    private boolean even = true;
    private float sample_rate_;
    private boolean trig_;
    private boolean sustain_;
    private float accent_, f0_, fm_amount_, decay_, snappy_;

    private final float[] phase_ = new float[2];
    private float drum_amplitude_;
    private float snare_amplitude_;
    private float fm_;
    private float sustain_gain_;
    private int hold_counter_;

    private final Svf drum_lp_ = new Svf();
    private final Svf snare_hp_ = new Svf();
    private final Svf snare_lp_ = new Svf();

    private boolean mTrigger = false;
}
