/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2022 Dennis P Paul.
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

public class Gain implements EffectMono, EffectStereo {
    private float mGain;

    public Gain() {
        mGain = 1.0f;
    }

    public float get_gain() {
        return mGain;
    }

    public void set_gain(float pGain) {
        mGain = pGain;
    }

    @Override
    public void out(float[] pOutputSignalLeft, float[] pOutputSignalRight) {
        for (int i = 0; i < pOutputSignalLeft.length; i++) {
            pOutputSignalLeft[i] *= mGain;
            if (pOutputSignalRight != null) {
                pOutputSignalRight[i] *= mGain;
            }
        }
    }

    @Override
    public void out(float[] pOutputSignal) {
        out(pOutputSignal, null);
    }
}
