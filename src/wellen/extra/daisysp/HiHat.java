package wellen.extra.daisysp;

import static wellen.extra.daisysp.DaisySP.fabsf;
import static wellen.extra.daisysp.DaisySP.fclamp;
import static wellen.extra.daisysp.DaisySP.fmax;
import static wellen.extra.daisysp.DaisySP.kOneTwelfth;
import static wellen.extra.daisysp.DaisySP.powf;
import static wellen.extra.daisysp.DaisySP.rand_kRandFrac;

/**
 * @author Ben Sergentanis
 * @brief 808 HH, with a few extra parameters to push things to the CY territory...
 * @date Jan 2021
 *         <p>
 *         The template parameter MetallicNoiseSource allows another kind of "metallic noise" to be used, for results
 *         which are more similar to KR-55 or FM hi-hats.
 *         <p>
 *         Ported from pichenettes/eurorack/plaits/dsp/drums/hihat.h to an independent module.
 *         <p>
 *         Original code written by Emilie Gillet in 2016.
 */
public class HiHat {

    public HiHat() {
        this(new SquareNoise(), new LinearVCA(), true);
    }

    public HiHat(MetallicNoiseSource pMetallicNoise, VCA pVCA, boolean pResonance) {
        metallic_noise_ = pMetallicNoise;
        vca = pVCA;
        resonance = pResonance;
    }

    /**
     * Initialize the module
     *
     * @param sample_rate Audio engine sample rate
     */
    public void Init(float sample_rate) {
        sample_rate_ = sample_rate;

        trig_ = false;

        envelope_ = 0.0f;
        noise_clock_ = 0.0f;
        noise_sample_ = 0.0f;
        sustain_gain_ = 0.0f;

        SetFreq(3000.f);
        SetTone(.5f);
        SetDecay(.2f);
        SetNoisiness(.8f);
        SetAccent(.8f);
        SetSustain(false);

        metallic_noise_.Init(sample_rate_);
        noise_coloration_svf_.Init(sample_rate_);
        hpf_.Init(sample_rate_);
    }

    /**
     * Get the next sample
     *
     * @param trigger Hit the hihat with true. Defaults to false.
     */
    public float Process(boolean trigger) {
        final float envelope_decay = 1.0f - 0.003f * SemitonesToRatio(-decay_ * 84.0f);
        final float cut_decay = 1.0f - 0.0025f * SemitonesToRatio(-decay_ * 36.0f);

        if (trigger || trig_) {
            trig_ = false;

            envelope_ = (1.5f + 0.5f * (1.0f - decay_)) * (0.3f + 0.7f * accent_);
        }

        // Process the metallic noise.
        float out = metallic_noise_.Process(2.0f * f0_);

        // Apply BPF on the metallic noise.
        float cutoff = 150.0f / sample_rate_ * SemitonesToRatio(tone_ * 72.0f);

        cutoff = fclamp(cutoff, 0.0f, 16000.0f / sample_rate_);

        noise_coloration_svf_.SetFreq(cutoff * sample_rate_);
        noise_coloration_svf_.SetRes(resonance ? 3.0f + 6.0f * tone_ : 1.0f);

        noise_coloration_svf_.Process(out);
        out = noise_coloration_svf_.Band();

        // This is not at all part of the 808 circuit! But to add more variety, we
        // add a variable amount of clocked noise to the output of the 6 schmitt
        // trigger oscillators.
        float noise_f = f0_ * (16.0f + 16.0f * (1.0f - noisiness_));
        noise_f = fclamp(noise_f, 0.0f, 0.5f);

        noise_clock_ += noise_f;
        if (noise_clock_ >= 1.0f) {
            noise_clock_ -= 1.0f;
            noise_sample_ = rand_kRandFrac() - 0.5f;
        }
        out += noisiness_ * (noise_sample_ - out);

        // Apply VCA.
        sustain_gain_ = accent_ * decay_;
//        VCA vca;
        envelope_ *= envelope_ > 0.5f ? envelope_decay : cut_decay;
        out = vca.operator(out, sustain_ ? sustain_gain_ : envelope_);

        hpf_.SetFreq(cutoff * sample_rate_);
        hpf_.SetRes(.5f);
        hpf_.Process(out);
        out = hpf_.High();

        return out;
    }

    public float Process() {
        float s = Process(false);
        return s;
    }

    /**
     * Trigger the hihat
     */
    public void Trig() {
        trig_ = true;
    }

    public static final int VCA_LINEAR = 0;
    public static final int VCA_SWING = 1;

    public void SetVCA(int pVCA) {
        switch (pVCA) {
            case VCA_LINEAR:
                vca = new LinearVCA();
                break;
            case VCA_SWING:
                vca = new SwingVCA();
                break;
        }
    }

    public static final int METALLIC_NOISE_SQUARE = 0;
    public static final int METALLIC_NOISE_RING_MOD = 1;

    public void SetMetallicNoise(int pMetallicNoise) {
        switch (pMetallicNoise) {
            case METALLIC_NOISE_SQUARE:
                metallic_noise_ = new SquareNoise();
                metallic_noise_.Init(sample_rate_);
                break;
            case METALLIC_NOISE_RING_MOD:
                metallic_noise_ = new RingModNoise();
                metallic_noise_.Init(sample_rate_);
                break;
        }
    }

    public void SetResonance(boolean pResonance) {
        resonance = pResonance;
    }

    /**
     * Make the hihat ring out infinitely.
     *
     * @param sustain True = infinite sustain.
     */
    public void SetSustain(boolean sustain) {
        sustain_ = sustain;
    }

    /**
     * Set how much accent to use
     *
     * @param accent Works 0-1.
     */
    public void SetAccent(float accent) {
        accent_ = fclamp(accent, 0.f, 1.f);
    }

    /**
     * Set the hihat tone's root frequency
     *
     * @param f0 Freq in Hz
     */
    public void SetFreq(float f0) {
        f0 /= sample_rate_;
        f0_ = fclamp(f0, 0.f, 1.f);
    }

    /**
     * Set the overall brightness / darkness of the hihat.
     *
     * @param tone Works from 0-1.
     */
    public void SetTone(float tone) {
        tone_ = fclamp(tone, 0.f, 1.f);
    }

    /**
     * Set the length of the hihat decay
     *
     * @param decay Works > 0. Tuned for 0-1.
     */
    public void SetDecay(float decay) {
        decay_ = fmax(decay, 0.f);
        decay_ *= 1.7;
        decay_ -= 1.2;
    }

    /**
     * Sets the mix between tone and noise
     *
     * @param noisiness 1 = just noise. 0 = just tone.
     */
    public void SetNoisiness(float noisiness) {
        noisiness_ = fclamp(noisiness, 0.f, 1.f);
        noisiness_ *= noisiness_;
    }

    private float SemitonesToRatio(float in) {
        return powf(2.f, in * kOneTwelfth);
    }

    private float sample_rate_;
    private float accent_, f0_, tone_, decay_, noisiness_;
    private boolean sustain_;
    private boolean trig_;

    private float envelope_;
    private float noise_clock_;
    private float noise_sample_;
    private float sustain_gain_;

    private boolean resonance;
    private MetallicNoiseSource metallic_noise_;
    private VCA vca;
    private final Svf noise_coloration_svf_ = new Svf();
    private final Svf hpf_ = new Svf();

    private interface VCA {

        float operator(float s, float gain);
    }

    /**
     * @author Ben Sergentanis
     * @brief Swing type VCA
     * @date Jan 2021
     *         <p>
     *         Ported from pichenettes/eurorack/plaits/dsp/drums/hihat.h  to an independent module.  Original code
     *         written by Emilie Gillet in 2016.
     */
    public static class SwingVCA implements VCA {
        public float operator(float s, float gain) {
            s *= s > 0.0f ? 10.0f : 0.1f;
            s = s / (1.0f + fabsf(s));
            return (s + 1.0f) * gain;
        }
    }

    /**
     * @author Ben Sergentanis
     * @brief Linear type VCA
     * @date Jan 2021
     *         <p>
     *         Ported from pichenettes/eurorack/plaits/dsp/drums/hihat.h  to an independent module.  Original code
     *         written by Emilie Gillet in 2016.
     */
    public static class LinearVCA implements VCA {
        public float operator(float s, float gain) {
            return s * gain;
        }
    }

    private interface MetallicNoiseSource {
        void Init(float sample_rate);
        float Process(float f0);
    }

    /**
     * @author Ben Sergentanis
     * @brief 808 style "metallic noise" with 6 square oscillators.
     * @date Jan 2021
     *         <p>
     *         Ported from pichenettes/eurorack/plaits/dsp/drums/hihat.h  to an independent module.  Original code
     *         written by Emilie Gillet in 2016.
     */
    public static class SquareNoise implements MetallicNoiseSource {
        public void Init(float sample_rate) {
            for (int i = 0; i < 6; i++) {
                phase_[i] = 0;
            }
        }

        public float Process(float f0) {
            final float[] ratios = {// Nominal f0: 414 Hz
                                    1.0f, 1.304f, 1.466f, 1.787f, 1.932f, 2.536f};

            int[] increment = new int[6];
            int[] phase = new int[6];
            for (int i = 0; i < 6; ++i) {
                float f = f0 * ratios[i];
                if (f >= 0.499f) {
                    f = 0.499f;
                }
                increment[i] = (int) (f * 4294967296.0f);
                phase[i] = phase_[i];
            }

            phase[0] += increment[0];
            phase[1] += increment[1];
            phase[2] += increment[2];
            phase[3] += increment[3];
            phase[4] += increment[4];
            phase[5] += increment[5];
            int noise = 0;
            noise += (phase[0] >> 31);
            noise += (phase[1] >> 31);
            noise += (phase[2] >> 31);
            noise += (phase[3] >> 31);
            noise += (phase[4] >> 31);
            noise += (phase[5] >> 31);

            for (int i = 0; i < 6; ++i) {
                phase_[i] = phase[i];
            }

            return 0.33f * (float) (noise) - 1.0f;
        }

        private final int[] phase_ = new int[6];
    }

    /**
     * @author Ben Sergentanis
     * @brief Ring mod style metallic noise generator.
     * @date Jan 2021
     *         <p>
     *         Ported from pichenettes/eurorack/plaits/dsp/drums/hihat.h  to an independent module.  Original code
     *         written by Emilie Gillet in 2016.
     */
    public static class RingModNoise implements MetallicNoiseSource {
        public void Init(float sample_rate) {
            sample_rate_ = sample_rate;

            for (int i = 0; i < 6; ++i) {
                oscillator_[i] = new Oscillator();
                oscillator_[i].Init(sample_rate_);
            }
        }

        public float Process(float f0) {
            final float ratio = f0 / (0.01f + f0);
            final float f1a = 200.0f / sample_rate_ * ratio;
            final float f1b = 7530.0f / sample_rate_ * ratio;
            final float f2a = 510.0f / sample_rate_ * ratio;
            final float f2b = 8075.0f / sample_rate_ * ratio;
            final float f3a = 730.0f / sample_rate_ * ratio;
            final float f3b = 10500.0f / sample_rate_ * ratio;

            float out = ProcessPair(oscillator_[0], oscillator_[1], f1a, f1b);
            out += ProcessPair(oscillator_[2], oscillator_[3], f2a, f2b);
            out += ProcessPair(oscillator_[4], oscillator_[5], f3a, f3b);

            return out;
        }

        private float ProcessPair(Oscillator osc_0, Oscillator osc_1, float f1, float f2) {
            osc_0.SetWaveform(Oscillator.WAVE_FORM.WAVE_SQUARE);
            osc_0.SetFreq(f1 * sample_rate_);
            float temp_1 = osc_0.Process();

            osc_1.SetWaveform(Oscillator.WAVE_FORM.WAVE_SAW);
            osc_1.SetFreq(f2 * sample_rate_);
            float temp_2 = osc_1.Process();

            return temp_1 * temp_2;
        }

        private final Oscillator[] oscillator_ = new Oscillator[6];
        private float sample_rate_;
    }
}
