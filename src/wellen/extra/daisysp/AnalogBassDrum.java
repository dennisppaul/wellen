package wellen.extra.daisysp;

/**
 * @author Ben Sergentanis
 * @date Jan 2021
 * @brief 808 bass drum model, revisited.
 *         <p>
 *         Ported from pichenettes/eurorack/plaits/dsp/drums/analog_bass_drum.h to an independent module. Original code
 *         written by Emilie Gillet in 2016.
 */
public class AnalogBassDrum {

    private float accent_, f0_, tone_, decay_;
    private float attack_fm_amount_, self_fm_amount_;
    private float fm_pulse_lp_;
    private int fm_pulse_remaining_samples_;
    private float lp_out_;
    private boolean mTrigger = false;
    //for use in sin + cos osc. in sustain mode
    private float phase_;
    private float pulse_;
    private float pulse_height_;
    private float pulse_lp_;
    private int pulse_remaining_samples_;
    private final Svf resonator_ = new Svf();
    private float retrig_pulse_;
    private float sample_rate_;
    private float sustain_gain_;
    private float tone_lp_;
    private boolean trig_, sustain_;

    public void Init(float sample_rate) {
        sample_rate_ = sample_rate;

        trig_ = false;

        pulse_remaining_samples_ = 0;
        fm_pulse_remaining_samples_ = 0;
        pulse_ = 0.0f;
        pulse_height_ = 0.0f;
        pulse_lp_ = 0.0f;
        fm_pulse_lp_ = 0.0f;
        retrig_pulse_ = 0.0f;
        lp_out_ = 0.0f;
        tone_lp_ = 0.0f;
        sustain_gain_ = 0.0f;
        phase_ = 0.f;


        SetSustain(false);
        SetAccent(.1f);
        SetFreq(50.f);
        SetTone(.1f);
        SetDecay(.3f);
        SetSelfFmAmount(1.f);
        SetAttackFmAmount(.5f);

        resonator_.Init(sample_rate_);
    }

    public float Process() {
        float s = Process(mTrigger);
        mTrigger = false;
        return s;
    }

    public float Process(boolean trigger) {
        final int kTriggerPulseDuration = (int) (1.0e-3 * sample_rate_);
        final int kFMPulseDuration = (int) (6.0e-3 * sample_rate_);
        final float kPulseDecayTime = 0.2e-3f * sample_rate_;
        final float kPulseFilterTime = 0.1e-3f * sample_rate_;
        final float kRetrigPulseDuration = 0.05f * sample_rate_;

        final float scale = 0.001f / f0_;
        final float q = 1500.0f * DaisySP.powf(2.f, DaisySP.kOneTwelfth * decay_ * 80.0f);
        final float tone_f = DaisySP.fmin(4.0f * f0_ * DaisySP.powf(2.f, DaisySP.kOneTwelfth * tone_ * 108.0f), 1.0f);
        final float exciter_leak = 0.08f * (tone_ + 0.25f);


        if (trigger || trig_) {
            trig_ = false;

            pulse_remaining_samples_ = kTriggerPulseDuration;
            fm_pulse_remaining_samples_ = kFMPulseDuration;
            pulse_height_ = 3.0f + 7.0f * accent_;
            lp_out_ = 0.0f;
        }

        // Q39 / Q40
        float pulse = 0.0f;
        if (pulse_remaining_samples_ != 0) {
            --pulse_remaining_samples_;
            pulse = pulse_remaining_samples_ != 0 ? pulse_height_ : pulse_height_ - 1.0f;
            pulse_ = pulse;
        } else {
            pulse_ *= 1.0f - 1.0f / kPulseDecayTime;
            pulse = pulse_;
        }
        if (sustain_) {
            pulse = 0.0f;
        }

        // C40 / R163 / R162 / D83
        pulse_lp_ = DaisySP.fonepole_return(pulse_lp_, pulse, 1.0f / kPulseFilterTime);
        pulse = Diode((pulse - pulse_lp_) + pulse * 0.044f);

        // Q41 / Q42
        float fm_pulse = 0.0f;
        if (fm_pulse_remaining_samples_ != 0) {
            --fm_pulse_remaining_samples_;
            fm_pulse = 1.0f;
            // C39 / C52
            retrig_pulse_ = fm_pulse_remaining_samples_ != 0 ? 0.0f : -0.8f;
        } else {
            // C39 / R161
            retrig_pulse_ *= 1.0f - 1.0f / kRetrigPulseDuration;
        }
        if (sustain_) {
            fm_pulse = 0.0f;
        }
        fm_pulse_lp_ = DaisySP.fonepole_return(fm_pulse_lp_, fm_pulse, 1.0f / kPulseFilterTime);

        // Q43 and R170 leakage
        float punch = 0.7f + Diode(10.0f * lp_out_ - 1.0f);

        // Q43 / R165
        float attack_fm = fm_pulse_lp_ * 1.7f * attack_fm_amount_;
        float self_fm = punch * 0.08f * self_fm_amount_;
        float f = f0_ * (1.0f + attack_fm + self_fm);
        f = DaisySP.fclamp(f, 0.0f, 0.4f);

        float resonator_out;
        if (sustain_) {
            sustain_gain_ = accent_ * decay_;
            phase_ += f;
            phase_ = phase_ >= 1.f ? phase_ - 1.f : phase_;

            resonator_out = DaisySP.sin(DaisySP.TWOPI_F * phase_) * sustain_gain_;
            lp_out_ = DaisySP.cos(DaisySP.TWOPI_F * phase_) * sustain_gain_;
        } else {
            resonator_.SetFreq(f * sample_rate_);
            //resonator_.SetRes(1.0f + q * f);
            resonator_.SetRes(.4f * q * f);

            resonator_.Process((pulse - retrig_pulse_ * 0.2f) * scale);
            resonator_out = resonator_.Band();
            lp_out_ = resonator_.Low();
        }

        tone_lp_ = DaisySP.fonepole_return(tone_lp_, pulse * exciter_leak + resonator_out, tone_f);

        return tone_lp_;
    }

    public void Trig() {
        mTrigger = true;
        trig_ = true;
    }

    public void SetSustain(boolean sustain) {
        sustain_ = sustain;
    }

    public void SetAccent(float accent) {
        accent_ = DaisySP.fclamp(accent, 0.f, 1.f);
    }

    public void SetFreq(float f0) {
        f0 /= sample_rate_;
        f0_ = DaisySP.fclamp(f0, 0.f, .5f);
    }

    public void SetTone(float tone) {
        tone_ = DaisySP.fclamp(tone, 0.f, 1.f);
    }

    public void SetDecay(float decay) {
        decay_ = decay * .1f;
        decay_ -= .1f;
    }

    public void SetAttackFmAmount(float attack_fm_amount) {
        attack_fm_amount_ = attack_fm_amount * 50.f;
    }

    public void SetSelfFmAmount(float self_fm_amount) {
        self_fm_amount_ = self_fm_amount * 50.f;
    }

    private float Diode(float x) {
        if (x >= 0.0f) {
            return x;
        } else {
            x *= 2.0f;
            return 0.7f * x / (1.0f + DaisySP.fabsf(x));
        }
    }
}
