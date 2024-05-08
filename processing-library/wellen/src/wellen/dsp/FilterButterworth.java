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
import static processing.core.PApplet.sqrt;
import static processing.core.PApplet.tan;
import static wellen.Wellen.FILTER_MODE_BAND_PASS;
import static wellen.Wellen.FILTER_MODE_BAND_REJECT;
import static wellen.Wellen.FILTER_MODE_HIGH_PASS;
import static wellen.Wellen.FILTER_MODE_LOW_PASS;

/**
 * collection of butterworth filters
 */
public class FilterButterworth implements DSPNodeProcess {

    private float del0;
    private float del1;
    private final float fSamplingRate;
    private float a, a1, a2, b1, b2, tanthe, costhe, sqrtan, tansq;
    private float bandwidth;
    private float frequency;
    private int mode;

    public FilterButterworth() {
        this(Wellen.DEFAULT_SAMPLING_RATE);
    }

    public FilterButterworth(float sampling_rate) {
        fSamplingRate = sampling_rate;
    }

    public float get_frequency() {
        return frequency;
    }

    public void set_frequency(float frequency) {
        this.frequency = frequency;
        update_coefficients();
    }

    public float get_bandwidth() {
        return bandwidth;
    }

    public void set_bandwidth(float bandwidth) {
        this.bandwidth = bandwidth;
        update_coefficients();
    }

    public int get_mode() {
        return mode;
    }

    public void set_mode(int mode) {
        this.mode = mode;
        update_coefficients();
    }

    public float process(float signal) {
        float w = signal - b1 * del0 - b2 * del1;
        signal = a * w + a1 * del0 + a2 * del1;
        del1 = del0;
        del0 = w;

        return signal;
    }

    private void update_coefficients() {
        switch (mode) {
            case FILTER_MODE_LOW_PASS:
                tanthe = 1.f / tan(PI * frequency / fSamplingRate);
                sqrtan = sqrt(2.f) * tanthe;
                tansq = tanthe * tanthe;
                a = 1.f / (1.f + sqrtan + tansq);
                a1 = 2.f * a;
                a2 = a;
                b1 = 2.f * (1.f - tansq) * a;
                b2 = (1.f - sqrtan + tansq) * a;
                break;
            case FILTER_MODE_HIGH_PASS:
                tanthe = tan(PI * frequency / fSamplingRate);
                sqrtan = sqrt(2.f) * tanthe;
                tansq = tanthe * tanthe;
                a = 1.f / (1.f + sqrtan + tansq);
                a1 = -2.f * a;
                a2 = a;
                b1 = 2.f * (tansq - 1.f) * a;
                b2 = (1.f - sqrtan + tansq) * a;
                break;
            case FILTER_MODE_BAND_REJECT:
                tanthe = tan(PI * bandwidth / fSamplingRate);
                costhe = 2.f * cos(2 * PI * frequency / fSamplingRate);
                a = 1.f / (1.f + tanthe);
                a1 = -costhe * a;
                a2 = a;
                b1 = -costhe * a;
                b2 = (1.f - tanthe) * a;
                break;
            case FILTER_MODE_BAND_PASS:
            default:
                tanthe = 1.f / tan(PI * bandwidth / fSamplingRate);
                costhe = 2.f * cos(2 * PI * frequency / fSamplingRate);
                a = 1.f / (1.f + tanthe);
                a1 = 0;
                a2 = -a;
                b1 = -tanthe * costhe * a;
                b2 = (tanthe - 1.f) * a;
        }
    }
}