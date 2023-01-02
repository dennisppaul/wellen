package wellen.extra.daisysp;

import static wellen.extra.daisysp.DaisySP.fabsf;

/**
 * @author Ben Sergentanis
 * @brief Simple 2 operator FM synth voice.
 * @date November, 2020
 */
public class Fm2 {
    private static final float kIdxScalar = 0.2f;
    private static final float kIdxScalarRecip = 1.f / kIdxScalar;
    private final Oscillator car_ = new Oscillator();
    private float freq_, lfreq_, ratio_, lratio_;
    private float idx_;
    private final Oscillator mod_ = new Oscillator();

    /**
     * Initializes the FM2 module.
     *
     * @param samplerate - The sample rate of the audio engine being run.
     */
    public void Init(float samplerate) {
        //init oscillators
        car_.Init(samplerate);
        mod_.Init(samplerate);

        //set some reasonable values
        lfreq_ = 440.f;
        lratio_ = 2.f;
        SetFrequency(lfreq_);
        SetRatio(lratio_);

        car_.SetAmp(1.f);
        mod_.SetAmp(1.f);

        car_.SetWaveform(Oscillator.WAVE_FORM.WAVE_SIN);
        mod_.SetWaveform(Oscillator.WAVE_FORM.WAVE_SIN);

        idx_ = 1.f;
    }

    /**
     * Returns the next sample
     */
    public float Process() {
        if (lratio_ != ratio_ || lfreq_ != freq_) {
            lratio_ = ratio_;
            lfreq_ = freq_;
            car_.SetFreq(lfreq_);
            mod_.SetFreq(lfreq_ * lratio_);
        }

        float modval = mod_.Process();
        car_.PhaseAdd(modval * idx_);
        return car_.Process();
    }

    /**
     * Carrier freq. setter
     *
     * @param freq Carrier frequency in Hz
     */
    public void SetFrequency(float freq) {
        freq_ = fabsf(freq);
    }

    /**
     * Set modulator freq. relative to carrier
     *
     * @param ratio New modulator freq = carrier freq. * ratio
     */
    public void SetRatio(float ratio) {
        ratio_ = fabsf(ratio);
    }

    /**
     * Index setter
     *
     * @param index FM depth, 5 = 2PI rads
     */
    public void SetIndex(float index) {
        idx_ = index * kIdxScalar;
    }

    /**
     * Returns the current FM index.
     */
    public float GetIndex() {
        return idx_ * kIdxScalarRecip;
    }

    /**
     * Resets both oscillators
     */
    public void Reset() {
        car_.Reset();
        mod_.Reset();
    }
}
