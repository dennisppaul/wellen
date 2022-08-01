package wellen.extra.daisysp;

import static wellen.extra.daisysp.Oscillator.WAVE_FORM.WAVE_SIN;

/**
 * Synthesis of several waveforms, including polyBLEP bandlimited waveforms.
 */
public class Oscillator {

    private static final float TWO_PI_RECIP = 1.0f / DSP.TWOPI_F;

    private WAVE_FORM waveform_;
    private float amp_, freq_;
    private float sr_, sr_recip_, phase_, phase_inc_;
    private float last_out_, last_freq_;
    private boolean eor_, eoc_;

    /**
     * Choices for output waveforms, POLYBLEP are appropriately labeled. Others are naive forms.
     */
    public enum WAVE_FORM {
        WAVE_SIN,
        WAVE_TRI,
        WAVE_SAW,
        WAVE_RAMP,
        WAVE_SQUARE,
        WAVE_POLYBLEP_TRI,
        WAVE_POLYBLEP_SAW,
        WAVE_POLYBLEP_SQUARE,
        WAVE_LAST,
    }

    /**
     * Initializes the Oscillator
     * <p>
     * \param sample_rate - sample rate of the audio engine being run, and the frequency that the Process function will
     * be called.
     * <p>
     * Defaults: - freq_ = 100 Hz - amp_ = 0.5 - waveform_ = sine wave.
     */
    public void Init(float sample_rate) {
        sr_ = sample_rate;
        sr_recip_ = 1.0f / sample_rate;
        freq_ = 100.0f;
        amp_ = 0.5f;
        phase_ = 0.0f;
        phase_inc_ = CalcPhaseInc(freq_);
        waveform_ = WAVE_SIN;
        eoc_ = true;
        eor_ = true;
    }

    /**
     * Changes the frequency of the Oscillator, and recalculates phase increment.
     */
    public void SetFreq(float f) {
        freq_ = f;
        phase_inc_ = CalcPhaseInc(f);
    }

    /**
     * Sets the amplitude of the waveform.
     */
    public void SetAmp(float a) {
        amp_ = a;
    }

    /**
     * Sets the waveform to be synthesized by the Process() function.
     */
    public void SetWaveform(WAVE_FORM wf) {
//        waveform_ = wf < WAVE_LAST ? wf : WAVE_SIN;
        waveform_ = wf;
    }

    /**
     * Returns true if cycle is at end of rise. Set during call to Process.
     */
    public boolean IsEOR() {
        return eor_;
    }

    /**
     * Returns true if cycle is at end of rise. Set during call to Process.
     */
    public boolean IsEOC() {
        return eoc_;
    }

    /**
     * Returns true if cycle rising.
     */
    public boolean IsRising() {
        return phase_ < DSP.PI_F;
    }

    /**
     * Returns true if cycle falling.
     */
    public boolean IsFalling() {
        return phase_ >= DSP.PI_F;
    }

    /**
     * Adds a value 0.0-1.0 (mapped to 0.0-TWO_PI) to the current phase. Useful for PM and "FM" synthesis.
     */
    void PhaseAdd(float _phase) {
        phase_ += (_phase * DSP.TWOPI_F);
    }

    /**
     * Resets the phase to the input argument. If no argumeNt is present, it will reset phase to 0.0;
     */
    void Reset(float _phase) {
        phase_ = _phase;
    }

    void Reset() {
        phase_ = 0.0f;
    }

    /**
     * Processes the waveform to be generated, returning one sample. This should be called once per sample period.
     */
    public float Process() {
        float out, t;
        switch (waveform_) {
            case WAVE_SIN:
                out = DSP.sinf(phase_);
                break;
            case WAVE_TRI:
                t = -1.0f + (2.0f * phase_ * TWO_PI_RECIP);
                out = 2.0f * (DSP.fabsf(t) - 0.5f);
                break;
            case WAVE_SAW:
                out = -1.0f * (((phase_ * TWO_PI_RECIP * 2.0f)) - 1.0f);
                break;
            case WAVE_RAMP:
                out = ((phase_ * TWO_PI_RECIP * 2.0f)) - 1.0f;
                break;
            case WAVE_SQUARE:
                out = phase_ < DSP.PI_F ? (1.0f) : -1.0f;
                break;
            case WAVE_POLYBLEP_TRI:
                t = phase_ * TWO_PI_RECIP;
                out = phase_ < DSP.PI_F ? 1.0f : -1.0f;
                out += Polyblep(phase_inc_, t);
                out -= Polyblep(phase_inc_, DSP.fmodf(t + 0.5f, 1.0f));
                // Leaky Integrator:
                // y[n] = A + x[n] + (1 - A) * y[n-1]
                out = phase_inc_ * out + (1.0f - phase_inc_) * last_out_;
                last_out_ = out;
                break;
            case WAVE_POLYBLEP_SAW:
                t = phase_ * TWO_PI_RECIP;
                out = (2.0f * t) - 1.0f;
                out -= Polyblep(phase_inc_, t);
                out *= -1.0f;
                break;
            case WAVE_POLYBLEP_SQUARE:
                t = phase_ * TWO_PI_RECIP;
                out = phase_ < DSP.PI_F ? 1.0f : -1.0f;
                out += Polyblep(phase_inc_, t);
                out -= Polyblep(phase_inc_, DSP.fmodf(t + 0.5f, 1.0f));
                out *= 0.707f; // ?
                break;
            default:
                out = 0.0f;
                break;
        }
        phase_ += phase_inc_;
        if (phase_ > DSP.TWOPI_F) {
            phase_ -= DSP.TWOPI_F;
            eoc_ = true;
        } else {
            eoc_ = false;
        }
        eor_ = (phase_ - phase_inc_ < DSP.PI_F && phase_ >= DSP.PI_F);

        return out * amp_;
    }

    public float output() {
        return Process();
    }

    private float CalcPhaseInc(float f) {
        return (DSP.TWOPI_F * f) * sr_recip_;
    }

    private static float Polyblep(float phase_inc, float t) {
        float dt = phase_inc * TWO_PI_RECIP;
        if (t < dt) {
            t /= dt;
            return t + t - t * t - 1.0f;
        } else if (t > 1.0f - dt) {
            t = (t - 1.0f) / dt;
            return t * t + t + t + 1.0f;
        } else {
            return 0.0f;
        }
    }
}
