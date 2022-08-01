package wellen.extra.daisysp;

import static wellen.extra.daisysp.DaisySP.DSY_MAX;
import static wellen.extra.daisysp.DaisySP.fclamp;
import static wellen.extra.daisysp.DaisySP.fmin;
import static wellen.extra.daisysp.DaisySP.kOneTwelfth;
import static wellen.extra.daisysp.DaisySP.powf;
import static wellen.extra.daisysp.DaisySP.rand_kRandFrac;

/**
 * @author Ben Sergentanis
 * @brief Extended Karplus-Strong, with all the niceties from Rings
 * @date Jan 2021
 * <p>
 * Ported from pichenettes/eurorack/plaits/dsp/physical_modelling/string_voice.h and
 * pichenettes/eurorack/plaits/dsp/physical_modelling/string_voice.cc to an independent module.
 * <p>
 * Original code written by Emilie Gillet in 2016.
 */
public class StringVoice {

    /**
     * Initialize the module
     *
     * @param sample_rate Audio engine sample rate
     */
    public void Init(float sample_rate) {
        sample_rate_ = sample_rate;

        excitation_filter_.Init(sample_rate);
        string_.Init(sample_rate_);
        dust_.Init();
        remaining_noise_samples_ = 0;

        SetSustain(false);
        SetFreq(440.f);
        SetAccent(.8f);
        SetStructure(.7f);
        SetBrightness(.2f);
        SetDamping(.7f);
    }

    /**
     * Reset the string oscillator
     */
    public void Reset() {
        string_.Reset();
    }

    /**
     * Get the next sample
     *
     * @param trigger Strike the string. Defaults to false.
     */
    public float Process(boolean trigger) {
        final float brightness = brightness_ + .25f * accent_ * (1.f - brightness_);
        final float damping = damping_ + .25f * accent_ * (1.f - damping_);

        // Synthesize excitation signal.
        if (trigger || trig_ || sustain_) {
            trig_ = false;
            final float range = 72.0f;
            final float f = 4.0f * f0_;
            final float cutoff = fmin(f * powf(2.f, kOneTwelfth * (brightness * (2.0f - brightness) - 0.5f) * range),
                                      0.499f);
            final float q = sustain_ ? 1.0f : 0.5f;
            remaining_noise_samples_ = (int) (1.0f / f0_);
            excitation_filter_.SetFreq(cutoff * sample_rate_);
            excitation_filter_.SetRes(q);
        }

        float temp = 0.f;

        if (sustain_) {
            final float dust_f = 0.00005f + 0.99995f * density_ * density_;
            dust_.SetDensity(dust_f);
            temp = dust_.Process() * (8.0f - dust_f * 6.0f) * accent_;
        } else if (remaining_noise_samples_ != 0) {
            temp = 2.0f * rand_kRandFrac() - 1.0f;
            remaining_noise_samples_--;
            remaining_noise_samples_ = (int) DSY_MAX(remaining_noise_samples_, 0.f);
        }

        excitation_filter_.Process(temp);
        temp = excitation_filter_.Low();

        aux_ = temp;

        string_.SetBrightness(brightness);
        string_.SetDamping(damping);

        return string_.Process(temp);
    }

    public float Process() {
        float s = Process(mTrigger);
        mTrigger = false;
        return s;
    }

    /**
     * Continually excite the string with noise.
     *
     * @param sustain True turns on the noise.
     */
    public void SetSustain(boolean sustain) {
        sustain_ = sustain;
    }

    /**
     * Strike the string.
     */
    public void Trig() {
        trig_ = true;
        mTrigger = true;
    }

    /**
     * Set the string root frequency.
     *
     * @param freq Frequency in Hz.
     */
    public void SetFreq(float freq) {
        string_.SetFreq(freq);
        f0_ = freq / sample_rate_;
        f0_ = fclamp(f0_, 0.f, .25f);
    }

    /**
     * Hit the string a bit harder. Influences brightness and decay.
     *
     * @param accent Works 0-1.
     */
    public void SetAccent(float accent) {
        accent_ = fclamp(accent, 0.f, 1.f);
    }

    /**
     * Changes the string's nonlinearity (string type).
     *
     * @param structure Works 0-1. 0-.26 is curved bridge, .26-1 is dispersion.
     */
    public void SetStructure(float structure) {
        structure = fclamp(structure, 0.f, 1.f);
        final float non_linearity = structure < 0.24f ? (structure - 0.24f) * 4.166f : (structure > 0.26f ?
                                                                                        (structure - 0.26f) * 1.35135f : 0.0f);
        string_.SetNonLinearity(non_linearity);
    }

    /**
     * Set the brighness of the string, and the noise density.
     *
     * @param brightness Works best 0-1
     */
    public void SetBrightness(float brightness) {
        brightness_ = fclamp(brightness, 0.f, 1.f);
        density_ = brightness_ * brightness_;
    }

    /**
     * How long the resonant body takes to decay relative to the accent level.
     *
     * @param damping Works best 0-1. Full damp is only achieved with full accent.
     */
    public void SetDamping(float damping) {
        damping_ = fclamp(damping, 0.f, 1.f);
    }


    /**
     * Get the raw excitation signal. Must call Process() first.
     */
    public float GetAux() {
        return aux_;
    }

    private float sample_rate_;

    private boolean sustain_, trig_;
    private float f0_, brightness_, damping_;
    private float density_, accent_;
    private float aux_;

    private final Dust dust_ = new Dust();
    private final Svf excitation_filter_ = new Svf();
    private final StringOsc string_ = new StringOsc();
    private int remaining_noise_samples_;

    private boolean mTrigger = false;
}
