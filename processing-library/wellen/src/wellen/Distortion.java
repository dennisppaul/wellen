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

public class Distortion implements DSPNodeProcess {

    private static final int MAX_NUM_OF_ITERATIONS = 16;
    private float mClip;
    private float mAmplification;
    private TYPE mDistortionType;
    private final boolean LOCK_GUARD = true;

    public enum TYPE {
        CLIP, FOLDBACK, FOLDBACK_SINGLE
    }

    public Distortion() {
        set_clip(1.0f);
        set_amplification(1.0f);
        set_type(TYPE.CLIP);
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

    public TYPE get_type() {
        return mDistortionType;
    }

    public void set_type(TYPE pDistortionType) {
        mDistortionType = pDistortionType;
    }

    public float process(float s) {
        switch (mDistortionType) {
            case CLIP:
                return limit_clip(s * mAmplification);
            case FOLDBACK:
                return limit_foldback(s * mAmplification);
            case FOLDBACK_SINGLE:
                return limit_clip(limit_foldback_single(s * mAmplification));
            default:
                return 0.0f;
        }
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
