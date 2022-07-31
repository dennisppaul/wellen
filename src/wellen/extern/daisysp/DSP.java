package wellen.extern.daisysp;

import java.util.Random;

/**
 * Helpful defines, functions, and other utilities for use in/with daisysp modules.
 */
public abstract class DSP {

    /**
     * PIs
     */
    public static final float PI_F = 3.1415927410125732421875f;
    public static final float TWOPI_F = (2.0f * PI_F);
    public static final float HALFPI_F = (PI_F * 0.5f);

    private static final Random randomNumberGenerator = new Random();

    public static float DSY_MIN(float in, float mn) {
        return in < mn ? in : mn;
    }

    public static float DSY_MAX(float in, float mx) {
        return in > mx ? in : mx;
    }

    public static float DSY_CLAMP(float in, float mn, float mx) {
        return DSY_MIN(DSY_MAX(in, mn), mx);
    }

    //Avoids division for random floats. e.g. rand() * kRandFrac
    //public static final float kRandFrac = 1.f / (float)RAND_MAX;
    // @TODO(see if this is used and necessary)

    //Convert from semitones to other units. e.g. 2 ^ (kOneTwelfth * x)
    public static final float kOneTwelfth = 1.f / 12.f;

    /**
     * efficient floating point min/max c/o stephen mccaul
     */
    public static float fmax(float a, float b) {
        float r;
//#ifdef __arm__
//            asm("vmaxnm.f32 %[d], %[n], %[m]" : [d] "=t"(r) : [n] "t"(a), [m] "t"(b) :);
//#else
        r = (a > b) ? a : b;
//#endif // __arm__
        return r;
    }

    public static float fmin(float a, float b) {
        float r;
//#ifdef __arm__
//            asm("vminnm.f32 %[d], %[n], %[m]" : [d] "=t"(r) : [n] "t"(a), [m] "t"(b) :);
//#else
        r = (a < b) ? a : b;
//#endif // __arm__
        return r;
    }

    /**
     * quick fp clamp
     */
    public static float fclamp(float in, float min, float max) {
        return fmin(fmax(in, min), max);
    }

    /**
     * From Musicdsp.org "Fast power and root estimates for 32bit floats) Original code by Stefan Stenzel These are
     * approximations
     */
    public static float fastpower(float f, int n) {
//        long *lp, l;
//        lp = ( long *)( & f);
//        l = *lp;
//        l -= 0x3F800000;
//        l <<= (n - 1);
//        l += 0x3F800000;
//        *lp = l;
//        return f;
        return powf(f, n);
    }

    public static float fastroot(float f, int n) {
//        long *lp, l;
//        lp = ( long *)( & f);
//        l = *lp;
//        l -= 0x3F800000;
//        l >>= (n = 1);
//        l += 0x3F800000;
//        *lp = l;
//        return f;
        return powf(f, 1.0f / n);
    }

    /**
     * From http://openaudio.blogspot.com/2017/02/faster-log10-and-pow.html No approximation, pow10f(x) gives a 90%
     * speed increase over powf(10.f, x)
     */
    public static float pow10f(float f) {
        return expf(2.302585092994046f * f);
    }

    /* Original code for fastlog2f by Dr. Paul Beckmann from the ARM community forum, adapted from the CMSIS-DSP library
    About 25% performance increase over std::log10f
    */
    public static float fastlog2f(float f) {
//        float frac;
//        int exp;
//        frac = frexpf(fabsf(f), & exp);
//        f = 1.23149591368684f;
//        f *= frac;
//        f += -4.11852516267426f;
//        f *= frac;
//        f += 6.02197014179219f;
//        f *= frac;
//        f += -3.13396450166353f;
//        f += exp;
//        return (f);
        return Math.abs(f);
    }

    public static float fastlog10f(float f) {
        return fastlog2f(f) * 0.3010299956639812f;
    }

    /**
     * Midi to frequency helper
     */
    public static float mtof(float m) {
        return powf(2, (m - 69.0f) / 12.0f) * 440.0f;
    }

    /**
     * one pole lpf out is passed by reference, and must be retained between calls to properly filter the signal coeff
     * can be calculated: coeff = 1.0 / (time * sample_rate) ; where time is in seconds
     */
//    public void fonepole(float &out, float in, float coeff) {
//        out += coeff * (in - out);
//    }
    public static float fonepole_return(float out, float in, float coeff) {
        out += coeff * (in - out);
        return out;
    }

    /**
     * Simple 3-point median filter c/o stephen mccaul
     */
    public static float median(float a, float b, float c) {
        return (b < a) ? (b < c) ? (c < a) ? c : a : b : (a < c) ? (c < b) ? c : b : a;
    }

    /**
     * Ported from pichenettes/eurorack/plaits/dsp/oscillator/oscillator.h
     */
    public static float ThisBlepSample(float t) {
        return 0.5f * t * t;
    }

    /**
     * Ported from pichenettes/eurorack/plaits/dsp/oscillator/oscillator.h
     */
    public static float NextBlepSample(float t) {
        t = 1.0f - t;
        return -0.5f * t * t;
    }

    /**
     * Ported from pichenettes/eurorack/plaits/dsp/oscillator/oscillator.h
     */
    public static float NextIntegratedBlepSample(float t) {
        final float t1 = 0.5f * t;
        final float t2 = t1 * t1;
        final float t4 = t2 * t2;
        return 0.1875f - t1 + 1.5f * t2 - t4;
    }

    /**
     * Ported from pichenettes/eurorack/plaits/dsp/oscillator/oscillator.h
     */
    public static float ThisIntegratedBlepSample(float t) {
        return NextIntegratedBlepSample(1.0f - t);
    }

    /**
     * Soft Limiting function ported extracted from pichenettes/stmlib
     */
    public static float SoftLimit(float x) {
        return x * (27.f + x * x) / (27.f + 9.f * x * x);
    }

    /**
     * Soft Clipping function extracted from pichenettes/stmlib
     */
    public static float SoftClip(float x) {
        if (x < -3.0f) {
            return -1.0f;
        } else if (x > 3.0f) {
            return 1.0f;
        } else {
            return SoftLimit(x);
        }
    }

    /**
     * Based on soft saturate from: [musicdsp.org](musicdsp.org/en/latest/Effects/42-soft-saturation.html) Bram de Jong
     * (2002-01-17) This still needs to be tested/fixed. Definitely does some weird stuff described as: x < a: f(x) = x
     * x > a: f(x) = a + (x-a)/(1+((x-a)/(1-a))^2) x > 1: f(x) = (a + 1)/2
     */
    public static float soft_saturate(float in, float thresh) {
        boolean flip;
        float val = 0;
        float out = 0;
        //val = fabsf(in);
        flip = val < 0.0f;
        val = flip ? -in : in;
        if (val < thresh) {
            out = in;
        } else if (val > 1.0f) {
            out = (thresh + 1.0f) / 2.0f;
            if (flip) {
                out *= -1.0f;
            }
        } else if (val > thresh) {
            float temp;
            temp = (val - thresh) / (1 - thresh);
            out = thresh + (val - thresh) / (1.0f + (temp * temp));
            if (flip) {
                out *= -1.0f;
            }
        }
        return out;
        //    return val < thresh
        //               ? val
        //               : val > 1.0f
        //                     ? (thresh + 1.0f) / 2.0f
        //                     : thresh
        //                           + (val - thresh)
        //                                 / (1.0f
        //                                    + (((val - thresh) / (1.0f - thresh))
        //                                       * ((val - thresh) / (1.0f - thresh))));
    }

    /* methods to replace c/c++ functions from `math.h` */

    public static float powf(float f, float n) {
        return (float) Math.pow(f, n);
    }

    public static float expf(float f) {
        return (float) Math.exp(f);
    }

    public static float sinf(float r) {
        return (float) Math.sin(r);
    }

    public static float sin(float r) {
        return (float) Math.sin(r);
    }

    public static float cosf(float r) {
        return (float) Math.cos(r);
    }

    public static float cos(float r) {
        return (float) Math.cos(r);
    }

    public static float fabs(float v) {
        return Math.abs(v);
    }

    public static int abs(int v) {
        return Math.abs(v);
    }

    public static float fabsf(float v) {
        return Math.abs(v);
    }

    public static float fmodf(float x, float y) {
        return x % y;
    }

    public static float randf() {
        return (float) Math.random();
    }

    public static int rand() {
        return randomNumberGenerator.nextInt();
    }

    public static float sqrtf(float v) {
        return (float) Math.sqrt(v);
    }

    public static float logf(float v) {
        return (float) Math.log(v);
    }
}
