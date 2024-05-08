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

package wellen.tests.dsp.tarsos;

/**
 * introduces the effect of bit depth modification. samples are quantized to specified bit depth.
 */
public class BitCrusher {

    private int fBitDepth;
    private float fFactor;

    public BitCrusher() {
        set_bit_depth(16);
    }

    public int get_bit_depth() {
        return fBitDepth;
    }

    public void set_bit_depth(int pBitDepth) {
        fBitDepth = pBitDepth;
        fFactor = (float) Math.pow(2, fBitDepth) / 2.0f - 1;
    }

    public boolean process(float[] pBuffer) {
        for (int i = 0; i < pBuffer.length; i++) {
            pBuffer[i] = ((int) (pBuffer[i] * fFactor)) / fFactor;
        }
        return true;
    }
}
