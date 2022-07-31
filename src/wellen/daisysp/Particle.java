package wellen.daisysp;

import static wellen.daisysp.DSP.fclamp;
import static wellen.daisysp.DSP.fmin;
import static wellen.daisysp.DSP.powf;
import static wellen.daisysp.DSP.randf;
import static wellen.daisysp.DSP.sqrtf;

/**
 * @author Ported by Ben Sergentanis
 * @brief Random impulse train processed by a resonant filter.
 * @date Jan 2021 Noise processed by a sample and hold running at a target frequency. \n \n Ported from
 * pichenettes/eurorack/plaits/dsp/noise/particle.h \n to an independent module. \n Original code written by Emilie
 * Gillet in 2016. \n
 */
public class Particle {
    /**
     * Initialize the module \param sample_rate Audio engine sample rate.
     */
    public void Init(float sample_rate) {
        sample_rate_ = sample_rate;
        sync_ = false;
        aux_ = 0.f;
        SetFreq(440.f);
        resonance_ = .9f;
        density_ = .5f;
        gain_ = 1.f;
        spread_ = 1.f;
        SetRandomFreq(sample_rate_ / 48.f); //48 is the default block size
        rand_phase_ = 0.f;
        pre_gain_ = 0.0f;
        filter_.Init(sample_rate_);
        filter_.SetDrive(.7f);
    }

    /**
     * Get the next sample
     */
    public float Process() {
        float u = randf();
        float s = 0.0f;
        if (u <= density_ || sync_) {
            s = u <= density_ ? u * gain_ : s;
            rand_phase_ += rand_freq_;
            if (rand_phase_ >= 1.f || sync_) {
                rand_phase_ = rand_phase_ >= 1.f ? rand_phase_ - 1.f : rand_phase_;
//                final float u = 2.0f * rand() - 1.0f;
                u = 2.0f * randf() - 1.0f;
                final float f = fmin(powf(2.f, kRatioFrac * spread_ * u) * frequency_, .25f);
                pre_gain_ = 0.5f / sqrtf(resonance_ * f * sqrtf(density_));
                filter_.SetFreq(f * sample_rate_);
                filter_.SetRes(resonance_);
            }
        }
        aux_ = s;
        filter_.Process(pre_gain_ * s);
        return filter_.Band();
    }

    /**
     * Get the raw noise output. Must call Process() first.
     */
    public float GetNoise() {
        return aux_;
    }

    /**
     * Set the resonant filter frequency \param freq Frequency in Hz
     */
    public void SetFreq(float freq) {
        freq /= sample_rate_;
        frequency_ = fclamp(freq, 0.f, 1.f);
    }

    /**
     * Set the filter resonance \param resonance Works 0-1
     */
    public void SetResonance(float resonance) {
        resonance_ = fclamp(resonance, 0.f, 1.f);
    }

    /**
     * How often to randomize filter frequency \param freq Frequency in Hz.
     */
    public void SetRandomFreq(float freq) {
        freq /= sample_rate_;
        rand_freq_ = fclamp(freq, 0.f, 1.f);
    }

    /**
     * Noise density \param Works 0-1.
     */
    public void SetDensity(float density) {
        density_ = fclamp(density * .3f, 0.f, 1.f);
    }

    /**
     * Overall module gain \param Works 0-1.
     */
    public void SetGain(float gain) {
        gain_ = fclamp(gain, 0.f, 1.f);
    }

    /**
     * How much to randomize the set filter frequency. \param spread Works over positive numbers.
     */
    public void SetSpread(float spread) {
        spread_ = spread < 0.f ? 0.f : spread;
    }

    /**
     * Force randomize the frequency. \param sync True to randomize freq.
     */
    public void SetSync(boolean sync) {
        sync_ = sync;
    }

    private static final float kRatioFrac = 1.f / 12.f;
    private float sample_rate_;
    private float aux_, frequency_, density_, gain_, spread_, resonance_;
    private boolean sync_;

    private float rand_phase_;
    private float rand_freq_;

    private float pre_gain_;
    private Svf filter_;
}
