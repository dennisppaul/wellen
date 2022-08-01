package wellen.extra.daisysp;

/**
 * @author Ben Sergentanis
 * @brief Oscillator Bank module.
 * @date Dec 2020
 * <p>
 * A mixture of 7 sawtooth and square waveforms in the style of divide-down organs
 * <p>
 * Ported from pichenettes/eurorack/plaits/dsp/oscillator/string_synth_oscillator.h to an independent module.
 * <p>
 * Original code written by Emilie Gillet in 2016.
 */
public class OscillatorBank {

    private float phase_;
    private float next_sample_;
    private int segment_;
    private float gain_;
    private final float[] registration_ = new float[7];
    private final float[] unshifted_registration_ = new float[7];

    private float frequency_;
    private float saw_8_gain_;
    private float saw_4_gain_;
    private float saw_2_gain_;
    private float saw_1_gain_;

    private float sample_rate_;
    private boolean recalc_, recalc_gain_;

    /**
     * Init string synth module \param sample_rate Audio engine sample rate
     */
    public void Init(float sample_rate) {
        sample_rate_ = sample_rate;

        phase_ = 0.0f;
        next_sample_ = 0.0f;
        segment_ = 0;

        frequency_ = 0.f;
        saw_8_gain_ = 0.0f;
        saw_4_gain_ = 0.0f;
        saw_2_gain_ = 0.0f;
        saw_1_gain_ = 0.0f;

        recalc_ = recalc_gain_ = true;
        SetGain(1.f);

        for (int i = 0; i < 7; i++) {
            registration_[i] = 0.f;
            unshifted_registration_[i] = 0.f;
        }
        SetSingleAmp(1.f, 0);
        SetFreq(440.f);
    }


    /**
     * Get next floating point sample
     */
    public float Process() {
        if (recalc_) {
            recalc_ = false;
            frequency_ *= 8.0f;

            // Deal with very high frequencies by shifting everything 1 or 2 octave
            // down: Instead of playing the 1st harmonic of a 8kHz wave, we play the
            // second harmonic of a 4kHz wave.
            int shift = 0;
            while (frequency_ > 0.5f) {
                shift += 2;
                frequency_ *= 0.5f;
            }

            for (int i = 0; i < 7; i++) {
                registration_[i] = 0.f;
            }

            for (int i = 0; i < 7 - shift; i++) {
                registration_[i + shift] = unshifted_registration_[i];
            }
        }

        if (recalc_gain_ || recalc_) {
            saw_8_gain_ = (registration_[0] + 2.0f * registration_[1]) * gain_;
            saw_4_gain_ = (registration_[2] - registration_[1] + 2.0f * registration_[3]) * gain_;
            saw_2_gain_ = (registration_[4] - registration_[3] + 2.0f * registration_[5]) * gain_;
            saw_1_gain_ = (registration_[6] - registration_[5]) * gain_;
        }

        float this_sample_ = next_sample_;
        next_sample_ = 0.0f;

        phase_ += frequency_;
        int next_segment_ = (int) (phase_);
        if (next_segment_ != segment_) {
            float discontinuity = 0.0f;
            if (next_segment_ == 8) {
                phase_ -= 8.0f;
                next_segment_ -= 8;
                discontinuity -= saw_8_gain_;
            }
            if ((next_segment_ & 3) == 0) {
                discontinuity -= saw_4_gain_;
            }
            if ((next_segment_ & 1) == 0) {
                discontinuity -= saw_2_gain_;
            }
            discontinuity -= saw_1_gain_;
            if (discontinuity != 0.0f) {
                float fraction = phase_ - (float) (next_segment_);
                float t = fraction / frequency_;
                this_sample_ += DSP.ThisBlepSample(t) * discontinuity;
                next_sample_ += DSP.NextBlepSample(t) * discontinuity;
            }
        }
        segment_ = next_segment_;

        next_sample_ += (phase_ - 4.0f) * saw_8_gain_ * 0.125f;
        next_sample_ += (phase_ - (float) (segment_ & 4) - 2.0f) * saw_4_gain_ * 0.25f;
        next_sample_ += (phase_ - (float) (segment_ & 6) - 1.0f) * saw_2_gain_ * 0.5f;
        next_sample_ += (phase_ - (float) (segment_ & 7) - 0.5f) * saw_1_gain_;

        return 2.0f * this_sample_;
    }

    /**
     * Set oscillator frequency (8' oscillator) \param freq Frequency in Hz
     */
    public void SetFreq(float freq) {
        freq = freq / sample_rate_;
        freq = freq > 0.5f ? 0.5f : freq;
        recalc_ = cmp(freq, frequency_) || recalc_;
        frequency_ = freq;
    }

    /**
     * Set amplitudes of 7 oscillators. 0-6 are Saw 8', Square 8', Saw 4', Square 4', Saw 2', Square 2', Saw 1' \param
     * amplitudes array of 7 floating point amplitudes. Must sum to 1.
     */
    public void SetAmplitudes(float[] amplitudes) {
        for (int i = 0; i < 7; i++) {
            recalc_ = cmp(unshifted_registration_[i], amplitudes[i]) || recalc_;
            unshifted_registration_[i] = amplitudes[i];
        }
    }

    /**
     * Set a single amplitude \param amp Amplitude to set. \param idx Which wave's amp to set
     */
    public void SetSingleAmp(float amp, int idx) {
        if (idx < 0 || idx > 6) {
            return;
        }
        recalc_ = cmp(unshifted_registration_[idx], amp) || recalc_;
        unshifted_registration_[idx] = amp;
    }

    /**
     * Set overall gain. \param gain Gain to set. 0-1.
     */
    public void SetGain(float gain) {
        gain = gain > 1.f ? 1.f : gain;
        gain = gain < 0.f ? 0.f : gain;
        recalc_gain_ = cmp(gain, gain_) || recalc_gain_;
        gain_ = gain;
    }

    private boolean cmp(float a, float b) {
        return DSP.fabsf(a - b) > .0000001;
    }
}
