package wellen.extra.daisysp;

import static wellen.extra.daisysp.DSP.TWOPI_F;
import static wellen.extra.daisysp.DSP.cosf;
import static wellen.extra.daisysp.DSP.sqrtf;

/**
 * A first-order recursive low-pass filter with variable frequency response.
 */
public class Tone {
    /**
     * Initializes the Tone module. sample_rate - The sample rate of the audio engine being run.
     */
    public void Init(float sample_rate) {
        prevout_ = 0.0f;
        freq_ = 100.0f;
        c1_ = 0.5f;
        c2_ = 0.5f;
        sample_rate_ = sample_rate;
    }

    /**
     * Processes one sample through the filter and returns one sample. in - input signal
     */
    public float Process(float in) {
        float out;

        out = c1_ * in + c2_ * prevout_;
        prevout_ = out;

        return out;
    }

    /**
     * Sets the cutoff frequency or half-way point of the filter.
     *
     * @param freq - frequency value in Hz. Range: Any positive value.
     */
    public void SetFreq(float freq) {
        freq_ = freq;
        CalculateCoefficients();
    }

    /**
     * @return the current value for the cutoff frequency or half-way point of the filter.
     */
    public float GetFreq() {
        return freq_;
    }

    private void CalculateCoefficients() {
        float b, c1, c2;
        b = 2.0f - cosf(TWOPI_F * freq_ / sample_rate_);
        c2 = b - sqrtf(b * b - 1.0f);
        c1 = 1.0f - c2;
        c1_ = c1;
        c2_ = c2;
    }

    private float out_, prevout_, in_, freq_, c1_, c2_, sample_rate_;
}
