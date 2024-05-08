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

import wellen.Wellen;

import static processing.core.PApplet.PI;
import static processing.core.PApplet.cos;
import static wellen.Wellen.DEFAULT_FILTER_BANDWIDTH;
import static wellen.Wellen.DEFAULT_FILTER_FREQUENCY;

/**
 * Low-Pass Filter 1st order
 */
public class FilterBandPass implements DSPNodeProcess {

    private final float fSamplingRate;
    private float a;
    private float costh;
    private float del0;
    private float del1;
    private float fBandWidth;
    private float fFrequency;
    private float r;
    private float rr;
    private float rsq;

    public FilterBandPass() {
        this(Wellen.DEFAULT_SAMPLING_RATE);
    }

    public FilterBandPass(int sampling_rate) {
        fSamplingRate = sampling_rate;
        set_frequency(DEFAULT_FILTER_FREQUENCY);
        set_bandwidth(DEFAULT_FILTER_BANDWIDTH);
    }

    public float get_frequency() {
        return fFrequency;
    }

    public void set_frequency(float frequency) {
        fFrequency = frequency;
        update_coefficients();
    }

    public float get_bandwidth() {
        return fBandWidth;
    }

    public void set_bandwidth(float bandwidth) {
        fBandWidth = bandwidth;
        update_coefficients();
    }

    @Override
    public float process(float sig) {
        float w = a * sig + rr * costh * del0 - rsq * del1;
        sig = w - r * del1;
        del1 = del0;
        del0 = w;

        return sig;
    }

    private void update_coefficients() {
        r = 1.f - PI * (fBandWidth / fSamplingRate);
        rr = 2 * r;
        rsq = r * r;
        costh = (rr / (1.f + rsq)) * cos(2 * PI * fFrequency / fSamplingRate);
        a = (1 - r);
    }
}
