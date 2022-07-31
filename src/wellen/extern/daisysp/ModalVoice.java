package wellen.extern.daisysp;

/**
 * brief Simple modal synthesis voice with a mallet exciter: click -> LPF -> resonator. author Ben Sergentanis date Jan
 * 2021 The click can be replaced by continuous white noise. \n \n Ported from
 * pichenettes/eurorack/plaits/dsp/physical_modelling/modal_voice.h \n and
 * pichenettes/eurorack/plaits/dsp/physical_modelling/modal_voice.cc \n to an independent module. \n Original code
 * written by Emilie Gillet in 2016. \n
 */
public class ModalVoice {

    private float sample_rate_;

    private boolean sustain_, trig_;
    private float f0_, structure_, brightness_, damping_;
    private float density_, accent_;
    private float aux_;

    private final ResonatorSvf excitation_filter_ = new ResonatorSvf(1);
    private final Resonator resonator_ = new Resonator();
    private final Dust dust_ = new Dust();

    /**
     * Initialize the module \param sample_rate Audio engine sample rate
     */
    public void Init(float sample_rate) {
        sample_rate_ = sample_rate;
        aux_ = 0.f;

        excitation_filter_.Init();
        resonator_.Init(0.015f, 24, sample_rate_);
        excitation_filter_.Init();
        dust_.Init();

        SetSustain(false);
        SetFreq(440.f);
        SetAccent(.3f);
        SetStructure(.6f);
        SetBrightness(.8f);
        SetDamping(.6f);
    }

    /**
     * Continually excite the resonator with noise. \param sustain True turns on the noise.
     */
    public void SetSustain(boolean sustain) {
        sustain_ = sustain;
    }

    /**
     * Strike the resonator.
     */
    public void Trig() {
        trig_ = true;
    }

    /**
     * Set the resonator root frequency. \param freq Frequency in Hz.
     */
    public void SetFreq(float freq) {
        resonator_.SetFreq(freq);
        f0_ = freq / sample_rate_;
        f0_ = DSP.fclamp(f0_, 0.f, .25f);
    }

    /**
     * Hit the resonator a bit harder. \param accent Works 0-1.
     */
    public void SetAccent(float accent) {
        accent_ = DSP.fclamp(accent, 0.f, 1.f);
    }

    /**
     * Changes the general charater of the resonator (stiffness, brightness) \param structure Works best from 0-1
     */
    public void SetStructure(float structure) {
        resonator_.SetStructure(structure);
    }

    /**
     * Set the brighness of the resonator, and the noise density. \param brightness Works best 0-1
     */
    public void SetBrightness(float brightness) {
        brightness_ = DSP.fclamp(brightness, 0.f, 1.f);
        density_ = brightness_ * brightness_;
    }

    /**
     * How long the resonant body takes to decay. \param damping Works best 0-1
     */
    public void SetDamping(float damping) {
        damping_ = DSP.fclamp(damping, 0.f, 1.f);
    }

    /**
     * Get the raw excitation signal. Must call Process() first.
     */
    public float GetAux() {
        return aux_;
    }

    /**
     * Get the next sample \param trigger Strike the resonator. Defaults to false.
     */
    public float Process() {
        return Process(false);
    }

    public float Process(boolean trigger) {
        float brightness = brightness_ + 0.25f * accent_ * (1.0f - brightness_);
        float damping = damping_ + 0.25f * accent_ * (1.0f - damping_);

        final float range = sustain_ ? 36.0f : 60.0f;
        final float f = sustain_ ? 4.0f * f0_ : 2.0f * f0_;
        final float cutoff = DSP.fmin(
        f * DSP.powf(2.f, DSP.kOneTwelfth * ((brightness * (2.0f - brightness) - 0.5f) * range)),
        0.499f);
        final float q = sustain_ ? 0.7f : 1.5f;

        float temp = 0.f;
        // Synthesize excitation signal.
        if (sustain_) {
            final float dust_f = 0.00005f + 0.99995f * density_ * density_;
            dust_.SetDensity(dust_f);
            temp = dust_.Process() * (4.0f - dust_f * 3.0f) * accent_;
        } else if (trigger || trig_) {
            final float attenuation = 1.0f - damping * 0.5f;
            final float amplitude = (0.12f + 0.08f * accent_) * attenuation;
            temp = amplitude * DSP.powf(2.f, DSP.kOneTwelfth * (cutoff * cutoff * 24.0f)) / cutoff;
            trig_ = false;
        }

        final float one = 1.0f;
        temp = excitation_filter_.Process(ResonatorSvf.FilterMode.LOW_PASS, false, new float[]{cutoff}, new float[]{q},
                                          new float[]{one}, temp);

        aux_ = temp;

        resonator_.SetBrightness(brightness);
        resonator_.SetDamping(damping);

        return resonator_.Process(temp);
    }
}
