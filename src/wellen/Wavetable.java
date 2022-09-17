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

import processing.core.PApplet;

import java.util.Arrays;

import static java.lang.Math.PI;

/**
 * plays back a chunk of samples ( i.e arbitrary, single-cycle waveform like sine, triangle, saw or square waves ) at
 * different frequencies and amplitudes.
 */
public class Wavetable extends Oscillator {

    public static final float DEFAULT_FREQUENCY = 220.0f;
    public static final float DEFAULT_AMPLITUDE = 0.75f;
    private float mOffset;
    private final int mSamplingRate;
    private final float[] mWavetable;
    private float mPhaseOffset;
    private float mFrequency;
    private float mStepSize;
    private float mAmplitude;
    private int mInterpolationType;
    private float mArrayPtr;
    private boolean mEnableJitter;
    private float mJitterRange;
    private float mSignal;

    private float mDesiredAmplitude;
    private float mDesiredAmplitudeFraction;
    private int mDesiredAmplitudeSteps;
    private float mDesiredFrequency;
    private float mDesiredFrequencyFraction;
    private int mDesiredFrequencySteps;

    public Wavetable() {
        this(Wellen.DEFAULT_WAVETABLE_SIZE, Wellen.DEFAULT_SAMPLING_RATE);
    }

    public Wavetable(int pWavetableSize) {
        this(pWavetableSize, Wellen.DEFAULT_SAMPLING_RATE);
    }

    public Wavetable(int pWavetableSize, int pSamplingRate) {
        mWavetable = new float[pWavetableSize];
        mSamplingRate = pSamplingRate;
        mArrayPtr = 0;
        mJitterRange = 0.0f;
        mEnableJitter = false;
        mAmplitude = DEFAULT_AMPLITUDE;
        mPhaseOffset = 0.0f;
        mInterpolationType = Wellen.WAVESHAPE_INTERPOLATE_NONE;
        mDesiredAmplitude = 0.0f;
        mDesiredAmplitudeFraction = 0.0f;
        mDesiredAmplitudeSteps = 0;
        set_frequency(DEFAULT_FREQUENCY);
    }

    @Override
    public void set_waveform(int pWaveform) {
        fill(mWavetable, pWaveform);
    }

    public void set_waveform(int pHarmonics, int pWaveform) {
        fill(mWavetable, pHarmonics, pWaveform);
    }

    public static void fill(float[] pWavetable, int pWaveform) {
        // @TODO(add some more interesting waveforms like pulse )
        switch (pWaveform) {
            case Wellen.WAVEFORM_SINE:
                sine(pWavetable);
                break;
            case Wellen.WAVEFORM_TRIANGLE:
                triangle(pWavetable);
                break;
            case Wellen.WAVEFORM_SAWTOOTH:
                sawtooth(pWavetable);
                break;
            case Wellen.WAVEFORM_SQUARE:
                square(pWavetable);
                break;
            case Wellen.WAVEFORM_NOISE:
                noise(pWavetable);
                break;
        }
    }

    public static void fill(float[] pWavetable, int pHarmonics, int pWaveform) {
        switch (pWaveform) {
            case Wellen.WAVEFORM_TRIANGLE:
                triangle(pWavetable, pHarmonics);
            case Wellen.WAVEFORM_SAWTOOTH:
                sawtooth(pWavetable, pHarmonics);
            case Wellen.WAVEFORM_SQUARE:
                square(pWavetable, pHarmonics);
        }
    }

    private static void normalise_table(float[] table) {
        int n;
        float max = 0.f;
        for (n = 0; n < table.length; n++) {
            max = Math.max(table[n], max);
        }
        if (max > 0) {
            for (n = 0; n < table.length; n++) {
                table[n] /= max;
            }
        }
    }

    private static float[] fourier_table(float[] table, int harms, float[] amps, float phase) {
        float a;
        double w;
        phase *= (float) PI * 2;
        for (int i = 0; i < harms; i++) {
            for (int n = 0; n < table.length; n++) {
                a = (amps != null) ? amps[i] : 1.f;
                w = (i + 1) * (n * 2 * PI / table.length);
                table[n] += (float) (a * Math.cos(w + phase));
            }
        }
        normalise_table(table);
        return table;
    }

    public static float[] sawtooth(float[] pWavetable, int pHarmonics) {
        Arrays.fill(pWavetable, 0.0f);
        float[] amps = new float[pHarmonics];
        for (int i = 0; i < pHarmonics; i++) {
            amps[i] = 1.f / (i + 1);
        }
        return fourier_table(pWavetable, pHarmonics, amps, -0.25f);
    }

    public static float[] square(float[] pWavetable, int pHarmonics) {
        Arrays.fill(pWavetable, 0.0f);
        float[] amps = new float[pHarmonics];
        for (int i = 0; i < pHarmonics; i += 2) {
            amps[i] = 1.f / (i + 1);
        }
        return fourier_table(pWavetable, pHarmonics, amps, -0.25f);
    }

    public static float[] triangle(float[] pWavetable, int pHarmonics) {
        Arrays.fill(pWavetable, 0.0f);
        float[] amps = new float[pHarmonics];
        for (int i = 0; i < pHarmonics; i += 2) {
            amps[i] = 1.f / ((i + 1) * (i + 1));
        }
        return fourier_table(pWavetable, pHarmonics, amps, 0);
    }

    public static void sine(float[] pWavetable) {
        for (int i = 0; i < pWavetable.length; i++) {
            pWavetable[i] = PApplet.sin(2.0f * PApplet.PI * ((float) i / (float) (pWavetable.length)));
        }
    }

    public static void sawtooth(float[] pWavetable) {
        for (int i = 0; i < pWavetable.length; i++) {
            pWavetable[i] = 2.0f * ((float) i / (float) (pWavetable.length - 1)) - 1.0f;
        }
    }

    public static void triangle(float[] pWavetable) {
        final int q = pWavetable.length / 4;
        final float qf = pWavetable.length * 0.25f;
        for (int i = 0; i < q; i++) {
            pWavetable[i] = i / qf;
            //noinspection PointlessArithmeticExpression
            pWavetable[i + (q * 1)] = (qf - i) / qf;
            pWavetable[i + (q * 2)] = -i / qf;
            pWavetable[i + (q * 3)] = -(qf - i) / qf;
        }
    }

    public static void square(float[] pWavetable) {
        for (int i = 0; i < pWavetable.length / 2; i++) {
            pWavetable[i] = 1.0f;
            pWavetable[i + pWavetable.length / 2] = -1.0f;
        }
    }

    public static void noise(float[] pWavetable) {
        for (int i = 0; i < pWavetable.length; i++) {
            pWavetable[i] = (float) (Math.random() * 2.0 - 1.0);
        }
    }

    @Override
    public float get_frequency() {
        return mFrequency;
    }

    @Override
    public void set_frequency(float pFrequency) {
        if (mFrequency != PApplet.abs(pFrequency)) {
            mFrequency = PApplet.abs(pFrequency);
            mStepSize = computeStepSize();
        }
    }

    /**
     * alternative version of `set_frequency` which takes a second paramter that specifies the duration in samples from
     * current to new value. this can prevent unwanted artifacts ( e.g when quickly changing values ). it can also be
     * used to create glissando or portamento effects.
     *
     * @param pFrequency                      destination frequency
     * @param pInterpolationDurationInSamples duration of interpolation in samples
     */
    public void set_frequency(float pFrequency, int pInterpolationDurationInSamples) {
        if (pInterpolationDurationInSamples > 0) {
            mDesiredFrequency = pFrequency;
            mDesiredFrequencySteps = pInterpolationDurationInSamples;
            final float mFrequencyDelta = mDesiredFrequency - mFrequency;
            mDesiredFrequencyFraction = mFrequencyDelta / pInterpolationDurationInSamples;
        } else {
            set_frequency(pFrequency);
        }
    }

    @Override
    public void set_offset(float pOffset) {
        mOffset = pOffset;
    }

    @Override
    public float get_offset() {
        return mOffset;
    }

    @Override
    public float get_amplitude() {
        return mAmplitude;
    }

    @Override
    public void set_amplitude(float pAmplitude) {
        mAmplitude = pAmplitude;
        mDesiredAmplitudeSteps = 0;
    }

    /**
     * alternative version of `set_amplitude` which takes a second paramter that specifies the duration in samples from
     * current to new value. this can prevents unwanted artifacts ( e.g crackling noise when changing amplitude quickly
     * especially on smoother wave shape like sine waves ). it can also be used to create glissando or portamento
     * effects.
     *
     * @param pAmplitude                      destination amplitude
     * @param pInterpolationDurationInSamples duration of interpolation in samples
     */
    public void set_amplitude(float pAmplitude, int pInterpolationDurationInSamples) {
        if (pInterpolationDurationInSamples > 0) {
            mDesiredAmplitude = pAmplitude;
            mDesiredAmplitudeSteps = pInterpolationDurationInSamples;
            final float mAmplitudeDelta = mDesiredAmplitude - mAmplitude;
            mDesiredAmplitudeFraction = mAmplitudeDelta / pInterpolationDurationInSamples;
        } else {
            set_amplitude(pAmplitude);
        }
    }

    public float[] get_wavetable() {
        return mWavetable;
    }

    public void enable_jitter(boolean pEnableJitter) {
        mEnableJitter = pEnableJitter;
    }

    public float get_phase_offset() {
        return mPhaseOffset;
    }

    public void set_phase_offset(float pPhaseOffset) {
        mPhaseOffset = pPhaseOffset < 0 ? 1 + pPhaseOffset : pPhaseOffset;
    }

    public float get_jitter_range() {
        return mJitterRange;
    }

    public void set_jitter_range(float pJitterRange) {
        mJitterRange = pJitterRange;
    }

    public void reset() {
        mSignal = 0.0f;
        mArrayPtr = 0.0f;
    }

    public float current() {
        return mSignal;
    }

    public void set_interpolation(int pInterpolationType) {
        mInterpolationType = pInterpolationType;
    }

    @Override
    public float output() {
        if (mDesiredAmplitudeSteps > 0) {
            mDesiredAmplitudeSteps--;
            if (mDesiredAmplitudeSteps == 0) {
                mAmplitude = mDesiredAmplitude;
            } else {
                mAmplitude += mDesiredAmplitudeFraction;
            }
        }

        if (mDesiredFrequencySteps > 0) {
            mDesiredFrequencySteps--;
            if (mDesiredFrequencySteps == 0) {
                set_frequency(mDesiredFrequency);
            } else {
                set_frequency(mFrequency + mDesiredFrequencyFraction);
            }
        }

        switch (mInterpolationType) {
            default:
                mSignal = next_sample();
                break;
            case Wellen.WAVESHAPE_INTERPOLATE_LINEAR:
                mSignal = next_sample_interpolate_linear();
                break;
            case Wellen.WAVESHAPE_INTERPOLATE_CUBIC:
                mSignal = next_sample_interpolate_cubic();
                break;
        }

        mSignal *= mAmplitude;
        mSignal += mOffset;
        return mSignal;
    }

    private void advance_array_ptr() {
        mArrayPtr += mStepSize * (mEnableJitter ? (Wellen.random(-mJitterRange, mJitterRange) + 1.0f) : 1.0f);
        while (mArrayPtr >= mWavetable.length) {
            mArrayPtr -= mWavetable.length;
        }
        while (mArrayPtr < 0) {
            mArrayPtr += mWavetable.length;
        }
    }

    private float next_sample() {
        final float mOutput = mWavetable[(int) (mArrayPtr)];
        advance_array_ptr();
        return mOutput;
    }

    private float next_sample_interpolate_linear() {
        final int mOffset = (int) (mPhaseOffset * mWavetable.length) % mWavetable.length;
        final float mArrayPtrOffset = mArrayPtr + mOffset;
        /* linear interpolation */
        final float mFrac = mArrayPtrOffset - (int) mArrayPtrOffset;
        final float a = mWavetable[(int) mArrayPtrOffset];
        final int p1 = (int) mArrayPtrOffset + 1;
        final float b = mWavetable[p1 >= mWavetable.length ? p1 - mWavetable.length : p1];
        final float mOutput = a + mFrac * (b - a);
        advance_array_ptr();
        return mOutput;
    }

    private float next_sample_interpolate_cubic() {
        final int mOffset = (int) (mPhaseOffset * mWavetable.length) % mWavetable.length;
        final float mArrayPtrOffset = mArrayPtr + mOffset;
        /* cubic interpolation */
        final float frac = mArrayPtrOffset - (int) mArrayPtrOffset;
        final float a = (int) mArrayPtrOffset > 0 ? mWavetable[(int) mArrayPtrOffset - 1] :
                mWavetable[mWavetable.length - 1];
        final float b = mWavetable[((int) mArrayPtrOffset) % mWavetable.length];
        final int p1 = (int) mArrayPtrOffset + 1;
        final float c = mWavetable[p1 >= mWavetable.length ? p1 - mWavetable.length : p1];
        final int p2 = (int) mArrayPtrOffset + 2;
        final float d = mWavetable[p2 >= mWavetable.length ? p2 - mWavetable.length : p2];
        final float tmp = d + 3.0f * b;
        final float fracsq = frac * frac;
        final float fracb = frac * fracsq;
        final float mOutput =
                (fracb * (-a - 3.f * c + tmp) / 6.f + fracsq * ((a + c) / 2.f - b) + frac * (c + (-2.f * a - tmp) / 6.f) + b);
        advance_array_ptr();
        return mOutput;
    }

    private float computeStepSize() {
        return mFrequency * ((float) mWavetable.length / (float) mSamplingRate);
    }

}