package wellen.extern.daisysp;

/**
 * author Ported by Ben Sergentanis brief Resonant Body Simulation date Jan 2021 Ported from
 * pichenettes/eurorack/plaits/dsp/physical_modelling/resonator.h \n to an independent module. \n Original code written
 * by Emilie Gillet in 2016. \n
 */
public class Resonator {

    private int resolution_;
    private float frequency_, brightness_, structure_, damping_;

    private static final int kMaxNumModes = 24;
    private static final int kModeBatchSize = 4;
    private static final float ratiofrac_ = 1.f / 12.f;
    private static final float stiff_frac_ = 1.f / 64.f;
    private static final float stiff_frac_2 = 1.f / .6f;

    private float sample_rate_;

    private final float[] mode_amplitude_ = new float[kMaxNumModes];
    //    private    ResonatorSvf<kModeBatchSize> mode_filters_[kMaxNumModes / kModeBatchSize];
    private final ResonatorSvf[] mode_filters_ = new ResonatorSvf[kMaxNumModes / kModeBatchSize];


    /**
     * Initialize the module \param position    Offset the phase of the amplitudes. 0-1 \param resolution Quality vs
     * speed scalar \param sample_rate Samplerate of the audio engine being run.
     */
    public void Init(float position, int resolution, float sample_rate) {
        sample_rate_ = sample_rate;

        SetFreq(440.f);
        SetStructure(.5f);
        SetBrightness(.5f);
        SetDamping(.5f);

        resolution_ = (int) DSP.fmin(resolution, kMaxNumModes);

        for (int i = 0; i < resolution; ++i) {
            mode_amplitude_[i] = DSP.cos(position * DSP.TWOPI_F) * 0.25f;
        }

        for (int i = 0; i < kMaxNumModes / kModeBatchSize; ++i) {
            mode_filters_[i] = new ResonatorSvf(kModeBatchSize);
            mode_filters_[i].Init();
        }
    }

    /**
     * Get the next sample_rate \param in The signal to excited the resonant body
     */
    public float Process(final float in) {
        //convert Hz to cycles / sample
        float out = 0.f;

        float stiffness = CalcStiff(structure_);
        float f0 = frequency_ * NthHarmonicCompensation(3, stiffness);
        float brightness = brightness_;

        float harmonic = f0;
        float stretch_factor = 1.0f;

        float input = damping_ * 79.7f;
        float q_sqrt = DSP.powf(2.f, input * ratiofrac_);

        float q = 500.0f * q_sqrt * q_sqrt;
        brightness *= 1.0f - structure_ * 0.3f;
        brightness *= 1.0f - damping_ * 0.3f;
        float q_loss = brightness * (2.0f - brightness) * 0.85f + 0.15f;

        float[] mode_q = new float[kModeBatchSize];
        float[] mode_f = new float[kModeBatchSize];
        float[] mode_a = new float[kModeBatchSize];
        int batch_counter = 0;

        int mBatchProcCounter = 0;
        ResonatorSvf batch_processor = mode_filters_[mBatchProcCounter];

        for (int i = 0; i < resolution_; ++i) {
            float mode_frequency = harmonic * stretch_factor;
            if (mode_frequency >= 0.499f) {
                mode_frequency = 0.499f;
            }
            final float mode_attenuation = 1.0f - mode_frequency * 2.0f;

            mode_f[batch_counter] = mode_frequency;
            mode_q[batch_counter] = 1.0f + mode_frequency * q;
            mode_a[batch_counter] = mode_amplitude_[i] * mode_attenuation;
            ++batch_counter;

            if (batch_counter == kModeBatchSize) {
                batch_counter = 0;
                out += batch_processor.Process(ResonatorSvf.FilterMode.BAND_PASS, true, mode_f, mode_q, mode_a, in);
                mBatchProcCounter++;
                mBatchProcCounter %= mode_filters_.length;
                batch_processor = mode_filters_[mBatchProcCounter];
            }

            stretch_factor += stiffness;
            if (stiffness < 0.0f) {
                // Make sure that the partials do not fold back into negative frequencies.
                stiffness *= 0.93f;
            } else {
                // This helps adding a few extra partials in the highest frequencies.
                stiffness *= 0.98f;
            }
            harmonic += f0;
            q *= q_loss;
        }

        return out;
    }

    /**
     * Resonator frequency. \param freq Frequency in Hz.
     */
    public void SetFreq(float freq) {
        frequency_ = freq / sample_rate_;
    }

    /**
     * Changes the general charater of the resonator (stiffness, brightness) \param structure Works best from 0-1
     */
    public void SetStructure(float structure) {
        structure_ = DSP.fmax(DSP.fmin(structure, 1.f), 0.f);
    }

    /**
     * Set the brighness of the resonator \param brightness Works best 0-1
     */
    public void SetBrightness(float brightness) {
        brightness_ = DSP.fmax(DSP.fmin(brightness, 1.f), 0.f);
    }

    /**
     * How long the resonant body takes to decay. \param damping Works best 0-1
     */
    public void SetDamping(float damping) {
        damping_ = DSP.fmax(DSP.fmin(damping, 1.f), 0.f);
    }

    private float CalcStiff(float sig) {
        if (sig < .25f) {
            sig = .25f - sig;
            sig = -sig * .25f;
        } else if (sig < .3f) {
            sig = 0.f;
        } else if (sig < .9f) {
            sig -= .3f;
            sig *= stiff_frac_2;
        } else {
            sig -= .9f;
            sig *= 10; // div by .1
            sig *= sig;
            sig = 1.5f - DSP.cos(sig * DSP.PI_F) * .5f;
        }
        return sig;
    }


    private static float NthHarmonicCompensation(int n, float stiffness) {
        float stretch_factor = 1.0f;
        for (int i = 0; i < n - 1; ++i) {
            stretch_factor += stiffness;
            if (stiffness < 0.0f) {
                stiffness *= 0.93f;
            } else {
                stiffness *= 0.98f;
            }
        }
        return 1.0f / stretch_factor;
    }
}
