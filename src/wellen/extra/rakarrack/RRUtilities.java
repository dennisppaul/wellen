/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2024 Dennis P Paul.
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package wellen.extra.rakarrack;

import static wellen.Wellen.DEFAULT_AUDIOBLOCK_SIZE;
import static wellen.Wellen.DEFAULT_SAMPLING_RATE;

public class RRUtilities {
    public static final float ATTACK = 0.175f;
    public static final float DENORMAL_GUARD = 1e-18f;
    public static final float D_NOTE = 1.059463f;
    public static final float D_NOTE_SQRT = 1.029302f;
    public static final float D_PI = 6.283185f;
    public static final int FF_MAX_FORMANTS = 12;
    public static final int FF_MAX_SEQUENCE = 8;
    public static final int FF_MAX_VOWELS = 6;
    public static final float LOG_10 = 2.302585f;
    public static final float LOG_2 = 0.693147f;
    public static final float LOG_D_NOTE = 0.057762f;
    public static final float MAX_CHORUS_DELAY = 250.0f;    //ms;
    public static final int MAX_DELAY = 2;
    public static final int MAX_FILTER_STAGES = 5;
    public static final int PERIOD = DEFAULT_AUDIOBLOCK_SIZE;
    public static final float PI = 3.141598f;
    public static final float SAMPLE_RATE = DEFAULT_SAMPLING_RATE;
    public static final float cSAMPLE_RATE = 1.0f / SAMPLE_RATE;
    public static final int fPERIOD = PERIOD;
    public static final float[] freqs = new float[12];
    public static final float[] lfreqs = new float[12];
    public static float CRUNCH_GAIN = 100.0f;       //Typical voltage gain for most OD stompboxes
    public static float DIV_THC_CONST = 0.0016666f; // 1/600 (approximately)
    public static float DIV_TLC_CONST = 0.002f;     // 1/300
    public static float Thc = 0.6365834f;           //Thi - sqrt(Thi/600)
    public static float Thi = 0.67f;                //High threshold for limiting onset
    public static float Tlc = -0.6139445f;          //Tlo + sqrt(Tlo/500)
    public static float Tlo = -0.65f;               //Low threshold for limiting onset
    public static float aFreq;

    static {
        update_freqs(440.0f);
    }

    public static int F2I(float f) {
        return ((f > 0) ? ((int) (f)) : ((int) (f - 1.0f)));
    }

    public static float RND() {
        return (float) Math.random();
    }

    public static float RND1() {
        return (float) Math.random();
    }

    public static float abs(float v) {
        return Math.abs(v);
    }

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

    public static float fmod(float v, float w) {
        return fmodf(v, w);
    }

    public static float fmodf(float v, float w) {
        // @note(`Math.IEEEremainder(v, w)` creates artifacts)
        return v % w;
    }

    public static float log(float v) {
        return (float) Math.log(v);
    }

    public static float logf(float v) {
        return (float) Math.log(v);
    }

    public static int lrintf(float x) {
        // @note(this function is not returning a long integer)
        return Math.round(x);
    }

    public static void memcpy(float[] dest, float[] src, int n) {
        System.arraycopy(src, 0, dest, 0, n);
    }

    public static void memset(float[] dest, float value, int n) {
        for (int i = 0; i < n; i++) {
            dest[i] = value;
        }
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

    public static void update_freqs(float val) {
        aFreq = val;
        freqs[0] = aFreq;
        lfreqs[0] = logf(freqs[0]);
        for (int i = 1; i < 12; i++) {
            freqs[i] = freqs[i - 1] * D_NOTE;
            lfreqs[i] = lfreqs[i - 1] + LOG_D_NOTE;
        }
    }

    public static final class SampleStereo {
        public float left;
        public float right;
    }
}
