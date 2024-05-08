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
import static wellen.Wellen.DEFAULT_FILTER_FREQUENCY;

/**
 * Balance Filter 1st order
 */
public class FilterBalance implements DSPNodeProcess {

    private final float fSamplingRate;
    private float cmp;
    private float coef;
    private float costh;
    private float del0;
    private float del1;
    private float fFrequency;

    public FilterBalance() {
        this(Wellen.DEFAULT_SAMPLING_RATE);
    }

    public FilterBalance(int sampling_rate) {
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

    public void set_comparator_signal(float comparator_signal) {
        cmp = comparator_signal;
    }

    @Override
    public float process(float sig) {
        del0 = ((sig < 0 ? -sig : sig) * (1 + coef) - del0 * coef);
        del1 = ((cmp < 0 ? -cmp : cmp) * (1 + coef) - del1 * coef);
        sig *= (del0 != 0) ? del1 / del0 : del1;

        return sig;
    }

    private void update_coefficients() {
        costh = 2.f - cos(2 * PI * fFrequency / fSamplingRate);
        coef = sqrt(costh * costh - 1.f) - costh;
    }
}
