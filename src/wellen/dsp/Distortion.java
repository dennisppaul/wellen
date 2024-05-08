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

package wellen.dsp;

import static wellen.Wellen.DISTORTION_BIT_CRUSHING;
import static wellen.Wellen.DISTORTION_FOLDBACK;
import static wellen.Wellen.DISTORTION_FOLDBACK_SINGLE;
import static wellen.Wellen.DISTORTION_FULL_WAVE_RECTIFICATION;
import static wellen.Wellen.DISTORTION_HALF_WAVE_RECTIFICATION;
import static wellen.Wellen.DISTORTION_HARD_CLIPPING;
import static wellen.Wellen.DISTORTION_INFINITE_CLIPPING;
import static wellen.Wellen.DISTORTION_SOFT_CLIPPING_ARC_TANGENT;
import static wellen.Wellen.DISTORTION_SOFT_CLIPPING_CUBIC;

/**
 * distorts a signal with different distortion strategies.
 */
public class Distortion implements DSPNodeProcess {

    private static final int MAX_NUM_OF_ITERATIONS = 16;
    private final boolean LOCK_GUARD = true;
    private float mAmplification;
    private int mBits;
    private float mClip;
    private int mDistortionType;
    private int mSteps;

    /**
     *
     */
    public Distortion() {
        set_clip(1.0f);
        set_amplification(1.0f);
        set_type(DISTORTION_HARD_CLIPPING);
        set_bits(8);
    }

    /**
     * @return amplification value
     */
    public float get_amplification() {
        return mAmplification;
    }

    /**
     * set pre-amplification value. amplification only affects some distortion types e.g `DISTORTION_HARD_CLIPPING`,
     * `DISTORTION_SOFT_CLIPPING_CUBIC`, `DISTORTION_SOFT_CLIPPING_ARC_TANGENT`.
     *
     * @param pAmplification the amplification factor. a value of `1.0f` has no effect on the signal.
     */
    public void set_amplification(float pAmplification) {
        mAmplification = pAmplification;
    }

    /**
     * @return clipping value
     */
    public float get_clip() {
        return mClip;
    }

    /**
     * @param pClip clipping value
     */
    public void set_clip(float pClip) {
        mClip = pClip;
    }

    /**
     * @return distortion type
     */
    public int get_type() {
        return mDistortionType;
    }

    /**
     * @param pDistortionType distortion type
     */
    public void set_type(int pDistortionType) {
        mDistortionType = pDistortionType;
    }

    /**
     * @return number of bits for `DISTORTION_BIT_CRUSHING`
     */
    public int get_bits() {
        return mBits;
    }

    /**
     * set the number of bits for `DISTORTION_BIT_CRUSHING`
     *
     * @param pBits number of bits to which the signal will be reduced to
     */
    public void set_bits(int pBits) {
        mBits = pBits;
        mSteps = (int) (Math.pow(2, mBits - 1));
    }

    /**
     * @param pSignal input signal
     * @return distorted signal
     */
    public float process(float pSignal) {
        // @TODO(check if it makes sense to always hard clip all distortion types to [-1.0, 1.0])
        final float mAmplifiedSignal = pSignal * mAmplification;
        switch (mDistortionType) {
            case DISTORTION_HARD_CLIPPING:
                // - Hard Clipping ( i.e `f(x) = x > t ? t : x < -t ? -t : x (t=threshold)` )
                return limit_clip(mAmplifiedSignal);
            case DISTORTION_FOLDBACK:
                return limit_foldback(mAmplifiedSignal);
            case DISTORTION_FOLDBACK_SINGLE:
                return limit_foldback_single(mAmplifiedSignal);
            case DISTORTION_FULL_WAVE_RECTIFICATION:
                // - Full-Wave Rectification ( i.e `f(x) = abs(x)` )
                return Math.abs(mAmplifiedSignal);
            case DISTORTION_HALF_WAVE_RECTIFICATION:
                // - Half-Wave Rectification ( i.e `f(x) = x < 0.0 ? 0.0 : x` )
                return mAmplifiedSignal < 0.0f ? 0.0f : mAmplifiedSignal;
            case DISTORTION_INFINITE_CLIPPING:
                // - Infinite Clipping ( i.e `f(x) = x < 0.0 ? -1.0 : x > 0.0 ? 1.0 : 0.0` || `Math.signum(x)` )
                return mAmplifiedSignal < 0.0f ? -mClip : mAmplifiedSignal > 0.0f ? mClip : 0.0f;
            case DISTORTION_SOFT_CLIPPING_CUBIC:
                // - Soft Clipping Cubic ( i.e `f(x) = x - s * pow(x, 3) (s=scaling_factor=[0,1]=default:0.33)` )
                return (float) (mAmplifiedSignal - mClip * Math.pow(mAmplifiedSignal, 3));
            case DISTORTION_SOFT_CLIPPING_ARC_TANGENT:
                // - Soft Clipping Arc Tangent ( i.e `f(x) = (2.0 / PI) * atan(a*x) (a=amount=[1,10])` )
                return (float) ((2.0 / Math.PI) * Math.atan(mClip * mAmplifiedSignal));
            case DISTORTION_BIT_CRUSHING:
                // - Bit Crushing ( i.e ` f(x) = floor(x * s) / s (s=steps=pow(2,bits-1))` )
                return (float) (Math.floor(mAmplifiedSignal * mSteps) / mSteps);
            default:
                return 0.0f;
        }
        // see [Hack Audio: Distortion Effects](https://www.hackaudio.com/digital-signal-processing/distortion-effects/)
    }

    private float limit_clip(float v) {
        if (v > mClip) {
            return mClip;
        } else if (v < -mClip) {
            return -mClip;
        } else {
            return v;
        }
    }

    private float limit_foldback(float v) {
        int i = 0;
        while (v > mClip || v < -mClip) {
            v = limit_foldback_single(v);
            if (LOCK_GUARD) {
                i++;
                if (i > MAX_NUM_OF_ITERATIONS) {
                    return 0.0f;
                }
            }
        }
        return v;
    }

    private float limit_foldback_single(float v) {
        if (v > mClip) {
            float w = 2 * mClip - v;
            return w;
        } else if (v < -mClip) {
            float w = -2 * mClip - v;
            return w;
        } else {
            return v;
        }
    }
}
