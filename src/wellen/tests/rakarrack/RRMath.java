package wellen.tests.rakarrack;

import static wellen.Wellen.DEFAULT_AUDIOBLOCK_SIZE;

public class RRMath {
    public static final float DENORMAL_GUARD = 1e-18f;
    public static final float SAMPLE_RATE = 48000;
    public static final float LOG_2 = 0.693147f;
    public static final float LOG_10 = 2.302585f;
    public static final float D_PI = 6.283185f;
    public static final float PI = 3.141598f;
    public static final int FF_MAX_FORMANTS = 12;
    public static final int FF_MAX_VOWELS = 6;
    public static final int FF_MAX_SEQUENCE = 8;
    public static final int PERIOD = DEFAULT_AUDIOBLOCK_SIZE;
    public static float Thi = 0.67f;                //High threshold for limiting onset
    public static float Tlo = -0.65f;               //Low threshold for limiting onset
    public static float Tlc = -0.6139445f;          //Tlo + sqrt(Tlo/500)
    public static float Thc = 0.6365834f;           //Thi - sqrt(Thi/600)
    public static float CRUNCH_GAIN = 100.0f;       //Typical voltage gain for most OD stompboxes
    public static float DIV_TLC_CONST = 0.002f;     // 1/300
    public static float DIV_THC_CONST = 0.0016666f; // 1/600 (approximately)

    public static float asinf(float v) {
        return (float) Math.asin(v);
    }

    public static float atanf(float v) {
        return (float) Math.atan(v);
    }

    public static float cosf(float v) {
        return (float) Math.cos(v);
    }

    public static float dB2rap(float dB) {
        return expf(dB * LOG_10 / 20.0f);
    }

    public static float expf(float v) {
        return (float) Math.exp(v);
    }

    public static float fabs(float v) {
        return Math.abs(v);
    }

    public static float fabsf(float v) {
        return Math.abs(v);
    }

    public static float floorf(float v) {
        return (float) Math.floor(v);
    }

    public static float logf(float v) {
        return (float) Math.log(v);
    }

    public static float powf(float v, float w) {
        return (float) Math.pow(v, w);
    }

    public static float random() {
        return (float) Math.random();
    }

    public static float rap2dB(float rap) {
        return (20.0f * logf(rap) / LOG_10);
    }

    public static float sinf(float v) {
        return (float) Math.sin(v);
    }

    public static float sqrt(float v) {
        return (float) Math.sqrt(v);
    }

    public static float sqrtf(float v) {
        return (float) Math.sqrt(v);
    }
}
