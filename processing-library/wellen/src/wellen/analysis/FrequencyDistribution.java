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

public class FrequencyDistribution {
    private final wellen.FFT fFFT;
    private float fMaximumFrequency = 8800;
    private final PGraphics fGraphics;

    public FrequencyDistribution(PGraphics graphics) {
        fGraphics = graphics;
        fFFT = new wellen.FFT(Wellen.DEFAULT_AUDIOBLOCK_SIZE, Wellen.DEFAULT_SAMPLING_RATE);
        fFFT.window(FFT.HAMMING);
        fGraphics.beginDraw();
        fGraphics.stroke(0);
        fGraphics.noFill();
        fGraphics.endDraw();
    }

    public void draw() {
        fGraphics.beginDraw();
        fGraphics.background(255);
        final int LAST_FREQ_INDEX = fFFT.freqToIndex(fMaximumFrequency) + 1;
        fGraphics.beginShape();
        for (int i = 0; i < LAST_FREQ_INDEX; i++) {
            float x = pow(map(i, 0, LAST_FREQ_INDEX, 0, 1), 0.5f) * fGraphics.width;
            float y = map(fFFT.getSpectrum()[i], 0.0f, 100.0f, fGraphics.height, 0);
            fGraphics.vertex(x, y);
        }
        fGraphics.endShape();
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
