package wellen.tests.dsp;

public class Filters {

    public static final int BAND_PASS = 3;
    public static final int HIGH_PASS = 1;
    public static final int LOW_PASS = 0;
    public static final int RESONATOR = 2;
    private static final float pi = (float) Math.PI;
    private final float[] del;
    private final float sr;

    public Filters(float pSamplingFrequency) {
        del = new float[2];
        sr = pSamplingFrequency;
    }

    private static double acos(double r) {
        return Math.acos(r);
    }

    private static double cos(double r) {
        return Math.cos(r);
    }

    private static double sin(double r) {
        return Math.sin(r);
    }

    private static double sqrt(double r) {
        return Math.sqrt(r);
    }

    public float[] process(float[] pSignal, float freq, float bw, int mode) {
        switch (mode) {
            case LOW_PASS:
                return lowpass(pSignal, freq);
            case HIGH_PASS:
                return highpass(pSignal, freq);
            case BAND_PASS:
                return bandpass(pSignal, freq, bw);
            case RESONATOR:
                return resonator(pSignal, freq, bw);
            default:
                return pSignal;
        }
    }

    public float[] lowpass(float[] sig, float freq) {

        double costh, coef;
        costh = 2. - cos(2 * pi * freq / sr);
        coef = sqrt(costh * costh - 1.) - costh;

        for (int i = 0; i < sig.length; i++) {
            sig[i] = (float) (sig[i] * (1 + coef) - del[0] * coef);
            del[0] = sig[i];
        }

        return sig;
    }

    public float[] highpass(float[] sig, float freq) {

        double costh, coef;
        costh = 2. - cos(2 * pi * freq / sr);
        coef = costh - sqrt(costh * costh - 1.);

        for (int i = 0; i < sig.length; i++) {
            sig[i] = (float) (sig[i] * (1 - coef) - del[0] * coef);
            del[0] = sig[i];
        }

        return sig;
    }

    public float[] resonator(float[] sig, float freq, float bw) {

        double r, rsq, rr, costh, a;
        rr = 2 * (r = 1. - pi * (bw / sr));
        rsq = r * r;
        costh = (rr / (1. + rsq)) * cos(2 * pi * freq / sr);
        a = (1 - rsq) * sin(acos(costh));

        for (int i = 0; i < sig.length; i++) {
            sig[i] = (float) (sig[i] * a + rr * costh * del[0] - rsq * del[1]);
            del[1] = del[0];
            del[0] = sig[i];
        }

        return sig;
    }

    public float[] bandpass(float[] sig, float freq, float bw) {

        double r, rsq, rr, costh, a, w;
        rr = 2 * (r = 1. - pi * (bw / sr));
        rsq = r * r;
        costh = (rr / (1. + rsq)) * cos(2 * pi * freq / sr);
        a = (1 - r);

        for (int i = 0; i < sig.length; i++) {
            w = a * sig[i] + rr * costh * del[0] - rsq * del[1];
            sig[i] = (float) (w - r * del[1]);
            del[1] = del[0];
            del[0] = (float) w;
        }

        return sig;
    }

    public float[] balance(float[] sig, float[] cmp, float freq) {

        double costh, coef;
        costh = 2. - cos(2 * pi * freq / sr);
        coef = sqrt(costh * costh - 1.) - costh;

        for (int i = 0; i < sig.length; i++) {
            del[0] = (float) ((sig[i] < 0 ? -sig[i] : sig[i]) * (1 + coef) - del[0] * coef);
            del[1] = (float) ((cmp[i] < 0 ? -cmp[i] : cmp[i]) * (1 + coef) - del[1] * coef);
            sig[i] *= (float) (del[0] > 0 ? del[1] / del[0] : del[1]);
        }

        return sig;
    }
}