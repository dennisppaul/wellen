/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2023 Dennis P Paul.
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

import wellen.Wellen;

import static processing.core.PApplet.PI;
import static processing.core.PApplet.cos;
import static processing.core.PApplet.sqrt;
import static wellen.Wellen.DEFAULT_FILTER_FREQUENCY;

/**
 * High-Pass Filter 1st order
 */
public class FilterHighPass implements DSPNodeProcess {

    private final float fSamplingRate;
    private float coef;
    private float costh;
    private float del;
    private float fFrequency;

    public FilterHighPass() {
        this(Wellen.DEFAULT_SAMPLING_RATE);
    }

    public FilterHighPass(int sampling_rate) {
        fSamplingRate = sampling_rate;
        set_frequency(DEFAULT_FILTER_FREQUENCY);
    }

    public float get_frequency() {
        return fFrequency;
    }

    public void set_frequency(float frequency) {
        fFrequency = frequency;
        update_coefficients();
    }

    @Override
    public float process(float sig) {
        sig = sig * (1 - coef) - del * coef;
        del = sig;
        return sig;
    }


    private void update_coefficients() {
        costh = 2.f - cos(2 * PI * fFrequency / fSamplingRate);
        coef = costh - sqrt(costh * costh - 1.f);
    }
}
