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

import static wellen.Wellen.SIGNAL_MAX;
import static wellen.Wellen.SIGNAL_MIN;
import static wellen.Wellen.TWO_PI;
import static wellen.Wellen.WAVEFORM_NOISE;
import static wellen.Wellen.WAVEFORM_SAWTOOTH;
import static wellen.Wellen.WAVEFORM_SINE;
import static wellen.Wellen.WAVEFORM_SQUARE;
import static wellen.Wellen.WAVEFORM_TRIANGLE;
import static wellen.Wellen.random;

/**
 * generates a signal from a mathematical function ( e.g sine, triangle, saw or square waveform ) at different
 * frequencies and amplitudes.
 */
public class OscillatorFunction extends Oscillator {

    public static final float DEFAULT_AMPLITUDE = 0.75f;
    public static final float DEFAULT_FREQUENCY = 220.0f;
    private float mAmplitude;
    private double mFrequency;
    private float mOffset;
    private double mPhase;
    private final double mSamplingRate;
    private double mStepSize;
    private int mWaveform;

    public OscillatorFunction(int pSamplingRate) {
        mSamplingRate = pSamplingRate;
        mWaveform = WAVEFORM_SINE;
        set_frequency(DEFAULT_FREQUENCY);
        set_amplitude(DEFAULT_AMPLITUDE);
    }

    public OscillatorFunction() {
        this(Wellen.DEFAULT_SAMPLING_RATE);
    }

    private static double mod(double a, double b) {
        return a >= b ? (a - b * (int) (a / b)) : a;
    }

    public int get_waveform() {
        return mWaveform;
    }

    @Override
    public void set_waveform(int pWaveform) {
        mWaveform = pWaveform;
    }

    @Override
    public float get_amplitude() {
        return mAmplitude;
    }

    @Override
    public void set_amplitude(float pAmplitude) {
        mAmplitude = pAmplitude;
    }

    @Override
    public float get_offset() {
        return mOffset;
    }

    @Override
    public void set_offset(float pOffset) {
        mOffset = pOffset;
    }

    @Override
    public float get_frequency() {
        return (float) mFrequency;
    }

    @Override
    public void set_frequency(float pFrequency) {
        if (mFrequency != pFrequency) {
            mFrequency = pFrequency;
            mStepSize = mFrequency * (double) TWO_PI / mSamplingRate;
        }
    }

    @Override
    public float output() {
        double s;
        switch (mWaveform) {
            case WAVEFORM_SINE:
                s = process_sine();
                break;
            case WAVEFORM_TRIANGLE:
                s = process_triangle();
                break;
            case WAVEFORM_SAWTOOTH:
                s = process_sawtooth();
                break;
            case WAVEFORM_SQUARE:
                s = process_square();
                break;
            case WAVEFORM_NOISE:
                s = random(-1, 1);
                break;
            default:
                s = 0.0f;
        }
        s *= mAmplitude;
        s += mOffset;
        return (float) s;
    }

    private double process_sawtooth() {
        mPhase += mFrequency;
        mPhase = mod(mPhase, mSamplingRate);
        return (mPhase / (mSamplingRate / 2.0)) + SIGNAL_MIN;
    }

    private double process_sine() {
        mPhase += mStepSize;
        if (mPhase > TWO_PI) {
            mPhase -= TWO_PI;
        }
        return Math.sin(mPhase);
    }

    private double process_square() {
        mPhase += mFrequency;
        mPhase = mod(mPhase, mSamplingRate);
        return mPhase > (mSamplingRate / 2.0f) ? SIGNAL_MAX : SIGNAL_MIN;
    }

    private double process_triangle() {
        mPhase += mFrequency;
        mPhase = mod(mPhase, mSamplingRate);
        final double mPhaseShifted = mPhase - (mSamplingRate / 2.0);
        final double mPhaseShiftedAbs = mPhaseShifted > 0 ? mPhaseShifted : -mPhaseShifted;
        return (mPhaseShiftedAbs - (mSamplingRate / 4.0)) / (mSamplingRate / 4.0);
    }
}
