/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2020 Dennis P Paul.
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

package wellen;

import static wellen.Wellen.DISTORTION_ARC_HYPERBOLIC;
import static wellen.Wellen.DISTORTION_ARC_TANGENT;
import static wellen.Wellen.DISTORTION_CLIP;
import static wellen.Wellen.DISTORTION_FOLDBACK;
import static wellen.Wellen.DISTORTION_FOLDBACK_SINGLE;

/**
 * distorts a signal with different distortion strategies.
 */
public class Distortion implements DSPNodeProcess {

    private static final int MAX_NUM_OF_ITERATIONS = 16;
    private float mClip;
    private float mAmplification;
    private int mDistortionType;
    private final boolean LOCK_GUARD = true;

    public Distortion() {
        set_clip(1.0f);
        set_amplification(1.0f);
        set_type(DISTORTION_CLIP);
    }

    public float get_amplification() {
        return mAmplification;
    }

    public void set_amplification(float pAmplification) {
        mAmplification = pAmplification;
    }

    public float get_clip() {
        return mClip;
    }

    public void set_clip(float pClip) {
        mClip = pClip;
    }

    public int get_type() {
        return mDistortionType;
    }

    public void set_type(int pDistortionType) {
        mDistortionType = pDistortionType;
    }

    public float process(float s) {
        switch (mDistortionType) {
            case DISTORTION_CLIP:
                return limit_clip(s * mAmplification);
            case DISTORTION_FOLDBACK:
                return limit_foldback(s * mAmplification);
            case DISTORTION_FOLDBACK_SINGLE:
                return limit_clip(limit_foldback_single(s * mAmplification));
            case DISTORTION_ARC_TANGENT:
                return (float) (Math.atan(s * mAmplification)) * mClip;
            case DISTORTION_ARC_HYPERBOLIC:
                return (float) Math.tanh(s * mAmplification) * mClip;
            default:
                return 0.0f;
        }
        // see [Hack Audio: Distortion Effects](https://www.hackaudio.com/digital-signal-processing/distortion-effects/)
        // - Full-Wave Rectification ( i.e `f(x) = abs(x)` )
        // - Half-Wave Rectification ( i.e `f(x) = x < 0.0 ? 0.0 : x` )
        // - Infinite Clipping ( i.e `f(x) = x < 0.0 ? -1.0 : x > 0.0 ? 1.0 : 0.0` || `Math.signum(0.0f)` )
        // - Hard Clipping ( i.e `f(x) = x > t ? t : x < -t ? -t : x (t=threshold)` )
        // - Soft Clipping Cubic ( i.e `f(x) = x - s * pow(x, 3) (s=scaling_factor=[0,1]=default:0.33)` )
        // - Soft Clipping Arc Tangent ( i.e `f(x) = (2.0 / PI) * atan(a*x) (a=amount=[1,10])` )
        // - Bit Crushing ( i.e ` f(x) = floor(x * s) / s (s=steps=pow(2,bits-1))` ) ( instead of `floor use `round` )
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
