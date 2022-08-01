package wellen.extra.daisysp;

import static wellen.extra.daisysp.DSP.TWOPI_F;
import static wellen.extra.daisysp.DSP.atanf;
import static wellen.extra.daisysp.DSP.fabsf;
import static wellen.extra.daisysp.DSP.fclamp;
import static wellen.extra.daisysp.DSP.fmin;
import static wellen.extra.daisysp.DSP.fonepole_return;
import static wellen.extra.daisysp.DSP.kOneTwelfth;
import static wellen.extra.daisysp.DSP.powf;
import static wellen.extra.daisysp.DSP.rand_kRandFrac;
import static wellen.extra.daisysp.StringOsc.StringNonLinearity.STRING_NON_LINEARITY_CURVED_BRIDGE;
import static wellen.extra.daisysp.StringOsc.StringNonLinearity.STRING_NON_LINEARITY_DISPERSION;

/**
 * @author Ben Sergentanis
 * @brief Comb filter / KS string.
 * @date Jan 2021 "Lite" version of the implementation used in Rings
 * <p>
 * Ported from pichenettes/eurorack/plaits/dsp/oscillator/formant_oscillator.h to an independent module. Original code
 * written by Emilie Gillet in 2016.
 */
public class StringOsc {

    enum StringNonLinearity {
        STRING_NON_LINEARITY_CURVED_BRIDGE,
        STRING_NON_LINEARITY_DISPERSION
    }

    /**
     * Initialize the module. @param sample_rate Audio engine sample rate
     */
    public void Init(float sample_rate) {
        sample_rate_ = sample_rate;

        SetFreq(440.f);
        non_linearity_amount_ = .5f;
        brightness_ = .5f;
        damping_ = .5f;

        string_.Init();
        stretch_.Init();
        Reset();

        SetFreq(440.f);
        SetDamping(.8f);
        SetNonLinearity(.1f);
        SetBrightness(.5f);

        crossfade_.Init();
    }

    /**
     * Clear the delay line
     */
    public void Reset() {
        string_.Reset();
        stretch_.Reset();
        iir_damping_filter_.Init(sample_rate_);

        dc_blocker_.Init(sample_rate_);

        dispersion_noise_ = 0.0f;
        curved_bridge_ = 0.0f;
        out_sample_[0] = out_sample_[1] = 0.0f;
        src_phase_ = 0.0f;
    }

    /**
     * Get the next floating point sample
     *
     * @param in Signal to excite the string.
     */
    public float Process(final float in) {
        if (non_linearity_amount_ <= 0.0f) {
            non_linearity_amount_ *= -1;
            float ret = ProcessInternal(STRING_NON_LINEARITY_CURVED_BRIDGE, in);
            non_linearity_amount_ *= -1;
            return ret;
        } else {
            return ProcessInternal(STRING_NON_LINEARITY_DISPERSION, in);
        }
    }

    /**
     * Set the string frequency.
     *
     * @param freq Frequency in Hz
     */
    public void SetFreq(float freq) {
        freq /= sample_rate_;
        frequency_ = fclamp(freq, 0.f, .25f);
    }

    /**
     * Set the string's behavior.
     *
     * @param non_linearity_amount -1 to 0 is curved bridge, 0 to 1 is dispersion.
     */
    public void SetNonLinearity(float non_linearity_amount) {
        non_linearity_amount_ = fclamp(non_linearity_amount, 0.f, 1.f);
    }

    /**
     * Set the string's overall brightness @param Works 0-1.
     */
    public void SetBrightness(float brightness) {
        brightness_ = fclamp(brightness, 0.f, 1.f);
    }

    /**
     * Set the string's decay time.
     *
     * @param damping Works 0-1.
     */
    public void SetDamping(float damping) {
        damping_ = fclamp(damping, 0.f, 1.f);
    }

    private float ProcessInternal(StringNonLinearity non_linearity, final float in) {
        float brightness = brightness_;

        float delay = 1.0f / frequency_;
        delay = fclamp(delay, 4.f, kDelayLineSize - 4.0f);

        // If there is not enough delay time in the delay line, we play at the
        // lowest possible note and we upsample on the fly with a shitty linear
        // interpolator. We don't care because it's a corner case (frequency_ < 11.7Hz)
        float src_ratio = delay * frequency_;
        if (src_ratio >= 0.9999f) {
            // When we are above 11.7 Hz, we make sure that the linear interpolator
            // does not get in the way.
            src_phase_ = 1.0f;
            src_ratio = 1.0f;
        }

        float damping_cutoff = fmin(12.0f + damping_ * damping_ * 60.0f + brightness * 24.0f, 84.0f);
        float damping_f = fmin(frequency_ * powf(2.f, damping_cutoff * kOneTwelfth), 0.499f);

        // Crossfade to infinite decay.
        if (damping_ >= 0.95f) {
            float to_infinite = 20.0f * (damping_ - 0.95f);
            brightness += to_infinite * (1.0f - brightness);
            damping_f += to_infinite * (0.4999f - damping_f);
            damping_cutoff += to_infinite * (128.0f - damping_cutoff);
        }

        float temp_f = damping_f * sample_rate_;
        iir_damping_filter_.SetFreq(temp_f);

        float ratio = powf(2.f, damping_cutoff * kOneTwelfth);
        float damping_compensation = 1.f - 2.f * atanf(1.f / ratio) / (TWOPI_F);

        float stretch_point = non_linearity_amount_ * (2.0f - non_linearity_amount_) * 0.225f;
        float stretch_correction = (160.0f / sample_rate_) * delay;
        stretch_correction = fclamp(stretch_correction, 1.f, 2.1f);

        float noise_amount_sqrt = non_linearity_amount_ > 0.75f ? 4.0f * (non_linearity_amount_ - 0.75f) : 0.0f;
        float noise_amount = noise_amount_sqrt * noise_amount_sqrt * 0.1f;
        float noise_filter = 0.06f + 0.94f * brightness * brightness;

        float bridge_curving_sqrt = non_linearity_amount_;
        float bridge_curving = bridge_curving_sqrt * bridge_curving_sqrt * 0.01f;

        float ap_gain = -0.618f * non_linearity_amount_ / (0.15f + fabsf(non_linearity_amount_));

        src_phase_ += src_ratio;
        if (src_phase_ > 1.0f) {
            src_phase_ -= 1.0f;

            delay = delay * damping_compensation;
            float s = 0.0f;

            if (non_linearity == STRING_NON_LINEARITY_DISPERSION) {
                float noise = rand_kRandFrac() - 0.5f;
                dispersion_noise_ = fonepole_return(dispersion_noise_, noise, noise_filter);
                delay *= 1.0f + dispersion_noise_ * noise_amount;
            } else {
                delay *= 1.0f - curved_bridge_ * bridge_curving;
            }

            if (non_linearity == STRING_NON_LINEARITY_DISPERSION) {
                float ap_delay = delay * stretch_point;
                float main_delay = delay - ap_delay * (0.408f - stretch_point * 0.308f) * stretch_correction;
                if (ap_delay >= 4.0f && main_delay >= 4.0f) {
                    s = string_.Read(main_delay);
                    s = stretch_.Allpass(s, (int) ap_delay, ap_gain);
                } else {
                    s = string_.ReadHermite(delay);
                }
            } else {
                s = string_.ReadHermite(delay);
            }

            if (non_linearity == STRING_NON_LINEARITY_CURVED_BRIDGE) {
                float value = fabsf(s) - 0.025f;
                float sign = s > 0.0f ? 1.0f : -1.5f;
                curved_bridge_ = (fabsf(value) + value) * sign;
            }

            s += in;
            s = fclamp(s, -20.f, +20.f);

            s = dc_blocker_.Process(s);

            s = iir_damping_filter_.Process(s);
            string_.Write(s);

            out_sample_[1] = out_sample_[0];
            out_sample_[0] = s;
        }

        crossfade_.SetPos(src_phase_);
        return crossfade_.Process(out_sample_[1], out_sample_[0]);
    }

    private static final int kDelayLineSize = 1024;

    private final DelayLine string_ = new DelayLine(kDelayLineSize);
    private final DelayLine stretch_ = new DelayLine(kDelayLineSize / 4);

    private float frequency_, non_linearity_amount_, brightness_, damping_;
    private float sample_rate_;
    private final Tone iir_damping_filter_ = new Tone();
    private final DcBlock dc_blocker_ = new DcBlock();
    private final CrossFade crossfade_ = new CrossFade();

    private float dispersion_noise_;
    private float curved_bridge_;

    // Very crappy linear interpolation upsampler used for low pitches that
    // do not fit the delay line. Rarely used.
    private float src_phase_;
    private final float[] out_sample_ = new float[2];
}
