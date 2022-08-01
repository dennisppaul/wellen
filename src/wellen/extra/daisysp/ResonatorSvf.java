package wellen.extra.daisysp;

// We render 4 modes simultaneously since there are enough registers to hold
// all state variables.

/**
 * @author Ported by Ben Sergentanis
 * @brief SVF for use in the Resonator Class
 * @date Jan 2021
 * <p>
 * Ported from pichenettes/eurorack/plaits/dsp/physical_modelling/resonator.h to an independent module.
 * <p>
 * Original code written by Emilie Gillet in 2016.
 */
public class ResonatorSvf {

    private static final float kPiPow3 = DSP.PI_F * DSP.PI_F * DSP.PI_F;
    private static final float kPiPow5 = kPiPow3 * DSP.PI_F * DSP.PI_F;
    private final int batch_size;
    private final float[] state_1_;
    private final float[] state_2_;

    public enum FilterMode {
        LOW_PASS,
        BAND_PASS,
        BAND_PASS_NORMALIZED,
        HIGH_PASS
    }

    public ResonatorSvf(int pBatchSize) {
        batch_size = pBatchSize;
        state_1_ = new float[batch_size];
        state_2_ = new float[batch_size];
    }

    public void Init() {
        for (int i = 0; i < batch_size; ++i) {
            state_1_[i] = state_2_[i] = 0.0f;
        }
    }

    public float Process(FilterMode mode, boolean add,
                         final float[] f,
                         final float[] q,
                         final float[] gain,
                         final float in) {
        float out = 0.0f;
        float[] g = new float[batch_size];
        float[] r = new float[batch_size];
        float[] r_plus_g = new float[batch_size];
        float[] h = new float[batch_size];
        float[] state_1 = new float[batch_size];
        float[] state_2 = new float[batch_size];
        float[] gains = new float[batch_size];
        for (int i = 0; i < batch_size; ++i) {
            g[i] = fasttan(f[i]);
            r[i] = 1.0f / q[i];
            h[i] = 1.0f / (1.0f + r[i] * g[i] + g[i] * g[i]);
            r_plus_g[i] = r[i] + g[i];
            state_1[i] = state_1_[i];
            state_2[i] = state_2_[i];
            gains[i] = gain[i];
        }

        float s_in = in;
        float s_out = 0.0f;
        for (int i = 0; i < batch_size; ++i) {
            final float hp
            = (s_in - r_plus_g[i] * state_1[i] - state_2[i]) * h[i];
            final float bp = g[i] * hp + state_1[i];
            state_1[i] = g[i] * hp + bp;
            final float lp = g[i] * bp + state_2[i];
            state_2[i] = g[i] * bp + lp;
            s_out += gains[i] * ((mode == FilterMode.LOW_PASS) ? lp : bp);
        }
        if (add) {
            out += s_out;
        } else {
            out = s_out;
        }

        for (int i = 0; i < batch_size; ++i) {
            state_1_[i] = state_1[i];
            state_2_[i] = state_2[i];
        }
        return out;
    }

    private static float fasttan(float f) {
        final float a = 3.260e-01f * kPiPow3;
        final float b = 1.823e-01f * kPiPow5;
        float f2 = f * f;
        return f * (DSP.PI_F + f2 * (a + b * f2));
    }
}
