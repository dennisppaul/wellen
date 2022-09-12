package wellen.tests.dsp;

public class ButterworthFilters {

    public static final int LOW_PASS = 0;
    public static final int HIGH_PASS = 1;
    public static final int BAND_REJECT = 2;
    public static final int BAND_PASS = 3;
    private final float[] del;
    private static final float pi = (float) Math.PI;
    private final float sr;

    public ButterworthFilters(float pSamplingFrequency) {
        del = new float[2];
        sr = pSamplingFrequency;
    }

    public float[] process(float[] pSignal, float freq, float bw, int mode) {

        double a, a1, a2, b1, b2, tanthe, costhe, sqrtan, tansq, w;

        switch (mode) {
            case LOW_PASS:
                tanthe = 1. / tan(pi * freq / sr);
                sqrtan = sqrt(2.) * tanthe;
                tansq = tanthe * tanthe;
                a = 1. / (1. + sqrtan + tansq);
                a1 = 2. * a;
                a2 = a;
                b1 = 2. * (1. - tansq) * a;
                b2 = (1. - sqrtan + tansq) * a;
                break;

            case HIGH_PASS:
                tanthe = tan(pi * freq / sr);
                sqrtan = sqrt(2.) * tanthe;
                tansq = tanthe * tanthe;
                a = 1. / (1. + sqrtan + tansq);
                a1 = -2. * a;
                a2 = a;
                b1 = 2. * (tansq - 1.) * a;
                b2 = (1. - sqrtan + tansq) * a;
                break;

            case BAND_REJECT:
                tanthe = tan(pi * bw / sr);
                costhe = 2. * cos(2 * pi * freq / sr);
                a = 1. / (1. + tanthe);
                a1 = -costhe * a;
                a2 = a;
                b1 = -costhe * a;
                b2 = (1. - tanthe) * a;
                break;

            case BAND_PASS:
            default:
                tanthe = 1. / tan(pi * bw / sr);
                costhe = 2. * cos(2 * pi * freq / sr);
                a = 1. / (1. + tanthe);
                a1 = 0;
                a2 = -a;
                b1 = -tanthe * costhe * a;
                b2 = (tanthe - 1.) * a;

        }

        for (int i = 0; i < pSignal.length; i++) {
            w = pSignal[i] - b1 * del[0] - b2 * del[1];
            pSignal[i] = (float) (a * w + a1 * del[0] + a2 * del[1]);
            del[1] = del[0];
            del[0] = (float) w;
        }

        return pSignal;
    }

    private static double cos(double r) {
        return Math.cos(r);
    }

    private static double tan(double r) {
        return Math.tan(r);
    }

    private static double sqrt(double r) {
        return Math.sqrt(r);
    }
}