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
    public void out(float[] output_signalLeft, float[] output_signalRight) {
        for (int i = 0; i < output_signalLeft.length; i++) {
            output_signalLeft[i] *= mGain;
            if (output_signalRight != null) {
                output_signalRight[i] *= mGain;
            }
        }
    }

    @Override
    public void out(float[] output_signal) {
        out(output_signal, null);
    }
}
