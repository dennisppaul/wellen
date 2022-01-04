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

package wellen;

import static processing.core.PApplet.sin;
import static processing.core.PConstants.TWO_PI;

/**
 * FM synthesis with two oscillators, carrier ( e.g fundamental or *pitch* ) and modulator.
 * <p>
 * formula taken from [Anders Øland & Roger B. Dannenberg: FM Synthesis -- Introduction to Computer Music Carnegie
 * Mellon University](http://www.cs.cmu.edu/~music/icm-online/readings/fm-synthesis/fm_synthesis.pdf)
 *
 * <pre>
 * <code>
 * f(t) = A sin(2πCt + D sin(2πMt))
 * A :: amplitude
 * Ct :: carrier
 * Mt :: modulator
 * D :: modulation depth
 * </code>
 * </pre>
 */
public class FMSynthesis implements DSPNodeOutput {

    private Wavetable mCarrier;
    private Wavetable mModulator;
    private float mModulationDepth;
    private float mAmplitude;

    public FMSynthesis(Wavetable pCarrier, Wavetable pModulator) {
        mCarrier = pCarrier;
        mModulator = pModulator;
    }

    public FMSynthesis() {
        this(new Wavetable(), new Wavetable());
        mCarrier.interpolate_samples(true);
        Wavetable.fill(mCarrier.get_wavetable(), Wellen.OSC_SINE);

        mModulator.interpolate_samples(true);
        Wavetable.fill(mModulator.get_wavetable(), Wellen.OSC_SINE);
    }

    public Wavetable get_modulator() {
        return mModulator;
    }

    public void set_modulator(Wavetable pModulator) {
        mModulator = pModulator;
    }

    public Wavetable get_carrier() {
        return mCarrier;
    }

    public void set_carrier(Wavetable pCarrier) {
        mCarrier = pCarrier;
    }

    public float get_amplitude() {
        return mAmplitude;
    }

    public void set_amplitude(float pAmplitude) {
        mAmplitude = pAmplitude;
    }

    public float get_modulation_depth() {
        return mModulationDepth;
    }

    public void set_modulation_depth(float pModulationDepth) {
        mModulationDepth = pModulationDepth;
    }

    @Override
    public float output() {
        final float mCarrierSignal = mCarrier.output();
        final float mModulatorSignal = mModulator.output();
        final float mSignal = mAmplitude * sin(
        TWO_PI * mCarrierSignal + mModulationDepth * sin(TWO_PI * mModulatorSignal));
        return mSignal;
    }
}
