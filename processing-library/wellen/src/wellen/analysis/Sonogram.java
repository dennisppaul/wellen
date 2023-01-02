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

package wellen.analysis;

import processing.core.PGraphics;
import wellen.FFT;
import wellen.Wellen;

import java.lang.reflect.Array;

import static processing.core.PApplet.map;
import static processing.core.PApplet.pow;

public class Sonogram {
    private final FFT fFFT;
    private final PGraphics fGraphics;
    private float fMaximumFrequency = 8800;
    private int x = 0;

    public Sonogram(PGraphics graphics) {
        fGraphics = graphics;
        fFFT = new FFT(Wellen.DEFAULT_AUDIOBLOCK_SIZE, Wellen.DEFAULT_SAMPLING_RATE);
        fFFT.window(FFT.HAMMING);
        fGraphics.beginDraw();
        fGraphics.background(255);
        fGraphics.endDraw();
    }

    public void draw() {
        fGraphics.beginDraw();
        final int LAST_FREQ_INDEX = fFFT.freqToIndex(fMaximumFrequency) + 1;
        for (int i = 0; i < LAST_FREQ_INDEX; i++) {
            float y = pow(map(i, 0, LAST_FREQ_INDEX, 1, 0), 3) * fGraphics.height;
            float b = map(fFFT.getSpectrum()[i], 0.0f, 50.0f, 255, 0);
            fGraphics.stroke(b);
            fGraphics.line(x, 0, x, y);
        }
        x++;
        x %= fGraphics.width;
        fGraphics.endDraw();
    }

    public PGraphics get_graphics() {
        return fGraphics;
    }

    public void set_maximum_frequency(float maximum_frequency) {
        fMaximumFrequency = maximum_frequency;
    }

    public float[] process(float[] pSignal) {
        float[] mSignalCopy = new float[pSignal.length];
        System.arraycopy(pSignal, 0, mSignalCopy, 0, Array.getLength(pSignal));
        fFFT.forward(mSignalCopy);
        return pSignal;
    }
}
