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

import processing.core.PApplet;
import wellen.Wellen;

import java.util.Arrays;

import static java.lang.Math.PI;

/**
 * plays back a chunk of samples ( i.e arbitrary, single-cycle waveform like sine, triangle, saw or square waves ) at
 * different frequencies and amplitudes.
 */
public class Wavetable extends Oscillator {

    public static final float DEFAULT_AMPLITUDE = 0.75f;
    public static final float DEFAULT_FREQUENCY = 220.0f;
    private float mAmplitude;
    private float mArrayPtr;
    private float mDesiredAmplitude;
    private float mDesiredAmplitudeFraction;
    private int mDesiredAmplitudeSteps;
    private float mDesiredFrequency;
    private float mDesiredFrequencyFraction;
    private int mDesiredFrequencySteps;
    private boolean mEnableJitter;
    private float mFrequency;
    private int mInterpolationType;
    private float mJitterRange;
    private float mOffset;
    private float mPhaseOffset;
    private final int mSamplingRate;
    private float mSignal;
    private float mStepSize;
    private final float[] mWavetable;

    public Wavetable() {
        this(Wellen.DEFAULT_WAVETABLE_SIZE, Wellen.DEFAULT_SAMPLING_RATE);
    }

    public Wavetable(int wavetable_size) {
        this(wavetable_size, Wellen.DEFAULT_SAMPLING_RATE);
    }

    public Wavetable(int wavetable_size, int sampling_rate) {
        mWavetable = new float[wavetable_size];
        mSamplingRate = sampling_rate;
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

    public static void fill(float[] wavetable, int waveform) {
        // @TODO(add some more interesting waveforms like pulse )
        switch (waveform) {
            case Wellen.WAVEFORM_SINE:
                sine(wavetable);
                break;
            case Wellen.WAVEFORM_TRIANGLE:
                triangle(wavetable);
                break;
            case Wellen.WAVEFORM_SAWTOOTH:
                sawtooth(wavetable);
                break;
            case Wellen.WAVEFORM_SQUARE:
                square(wavetable);
                break;
            case Wellen.WAVEFORM_NOISE:
                noise(wavetable);
                break;
            default:
                sine(wavetable);
                System.out.println("+++ " + Wavetable.class.getSimpleName() + ".fill / could not find wave form: " + waveform + " ( using SINE )");
        }
    }

    public static void fill(float[] wavetable, int harmonics, int waveform) {
        switch (waveform) {
            case Wellen.WAVEFORM_TRIANGLE:
                triangle(wavetable, harmonics);
                break;
            case Wellen.WAVEFORM_SAWTOOTH:
                sawtooth(wavetable, harmonics);
                break;
            case Wellen.WAVEFORM_SQUARE:
                square(wavetable, harmonics);
                break;
            default:
                sine(wavetable);
                System.out.println("+++ " + Wavetable.class.getSimpleName() + ".fill / could not find wave form: " + waveform + " ( using SINE )");
        }
    }

    public static void noise(float[] wavetable) {
        for (int i = 0; i < wavetable.length; i++) {
            wavetable[i] = (float) (Math.random() * 2.0 - 1.0);
        }
    }

    public static void pulse(float[] wavetable, float pulse_width) {
        final int mThreshold = (int) (wavetable.length * pulse_width);
        for (int i = 0; i < wavetable.length; i++) {
            if (i < mThreshold) {
                wavetable[i] = 1.0f;
            } else {
                wavetable[i] = -1.0f;
            }
        }
    }

    public static float[] sawtooth(float[] wavetable, int harmonics) {
        Arrays.fill(wavetable, 0.0f);
        float[] amps = new float[harmonics];
        for (int i = 0; i < harmonics; i++) {
            amps[i] = 1.f / (i + 1);
        }
        return fourier_table(wavetable, harmonics, amps, -0.25f);
    }

    public static void sawtooth(float[] wavetable, boolean is_ramp_up) {
        final float mSign = is_ramp_up ? -1.0f : 1.0f;
        for (int i = 0; i < wavetable.length; i++) {
            wavetable[i] = mSign * (2.0f * ((float) i / (float) (wavetable.length - 1)) - 1.0f);
        }
    }

    public static void sawtooth(float[] wavetable) {
        sawtooth(wavetable, true);
    }

    public static void sine(float[] wavetable) {
        for (int i = 0; i < wavetable.length; i++) {
            wavetable[i] = PApplet.sin(2.0f * PApplet.PI * ((float) i / (float) (wavetable.length)));
        }
    }

    public static float[] square(float[] wavetable, int harmonics) {
        Arrays.fill(wavetable, 0.0f);
        float[] amps = new float[harmonics];
        for (int i = 0; i < harmonics; i += 2) {
            amps[i] = 1.f / (i + 1);
        }
        return fourier_table(wavetable, harmonics, amps, -0.25f);
    }

    public static void square(float[] wavetable) {
        for (int i = 0; i < wavetable.length / 2; i++) {
            wavetable[i] = 1.0f;
            wavetable[i + wavetable.length / 2] = -1.0f;
        }
    }

    public static float[] triangle(float[] wavetable, int harmonics) {
        Arrays.fill(wavetable, 0.0f);
        float[] amps = new float[harmonics];
        for (int i = 0; i < harmonics; i += 2) {
            amps[i] = 1.f / ((i + 1) * (i + 1));
        }
        return fourier_table(wavetable, harmonics, amps, 0);
    }

    public static void triangle(float[] wavetable) {
        final int q = wavetable.length / 4;
        final float qf = wavetable.length * 0.25f;
        for (int i = 0; i < q; i++) {
            wavetable[i] = i / qf;
            //noinspection PointlessArithmeticExpression
            wavetable[i + (q * 1)] = (qf - i) / qf;
            wavetable[i + (q * 2)] = -i / qf;
            wavetable[i + (q * 3)] = -(qf - i) / qf;
        }
    }

    private static float[] fourier_table(float[] pWavetable, int pHarmonics, float[] pAmps, float pPhase) {
        float a;
        double w;
        pPhase *= (float) PI * 2;
        for (int i = 0; i < pHarmonics; i++) {
            for (int n = 0; n < pWavetable.length; n++) {
                a = (pAmps != null) ? pAmps[i] : 1.f;
                w = (i + 1) * (n * 2 * PI / pWavetable.length);
                pWavetable[n] += (float) (a * Math.cos(w + pPhase));
            }
        }
        normalise_table(pWavetable);
        return pWavetable;
    }

    private static void normalise_table(float[] pWavetable) {
        int n;
        float max = 0.f;
        for (n = 0; n < pWavetable.length; n++) {
            max = Math.max(pWavetable[n], max);
        }
        if (max > 0) {
            for (n = 0; n < pWavetable.length; n++) {
                pWavetable[n] /= max;
            }
        }
    }

    @Override
    public void set_waveform(int waveform) {
        fill(mWavetable, waveform);
    }

    public void set_waveform(int harmonics, int waveform) {
        fill(mWavetable, harmonics, waveform);
    }

    @Override
    public float get_frequency() {
        return mFrequency;
    }

    @Override
    public void set_frequency(float frequency) {
        if (mFrequency != PApplet.abs(frequency)) {
            mFrequency = PApplet.abs(frequency);
            mStepSize = computeStepSize();
        }
    }

    /**
     * alternative version of `set_frequency` which takes a second paramter that specifies the duration in samples from
     * current to new value. this can prevent unwanted artifacts ( e.g when quickly changing values ). it can also be
     * used to create glissando or portamento effects.
     *
     * @param frequency                         destination frequency
     * @param interpolation_duration_in_samples duration of interpolation in samples
     */
    public void set_frequency(float frequency, int interpolation_duration_in_samples) {
        if (interpolation_duration_in_samples > 0) {
            mDesiredFrequency = frequency;
            mDesiredFrequencySteps = interpolation_duration_in_samples;
            final float mFrequencyDelta = mDesiredFrequency - mFrequency;
            mDesiredFrequencyFraction = mFrequencyDelta / interpolation_duration_in_samples;
        } else {
            set_frequency(frequency);
        }
    }

    @Override
    public float get_offset() {
        return mOffset;
    }

    @Override
    public void set_offset(float offset) {
        mOffset = offset;
    }

    @Override
    public float get_amplitude() {
        return mAmplitude;
    }

    /**
     * @param amplitude amplitude
     */
    @Override
    public void set_amplitude(float amplitude) {
        mAmplitude = amplitude;
        mDesiredAmplitudeSteps = 0;
    }

    /**
     * alternative version of `set_amplitude` which takes a second paramter that specifies the duration in samples from
     * current to new value. this can prevents unwanted artifacts ( e.g crackling noise when changing amplitude quickly
     * especially on smoother wave shape like sine waves ). it can also be used to create glissando or portamento
     * effects.
     *
     * @param amplitude                         destination amplitude
     * @param interpolation_duration_in_samples duration of interpolation in samples
     */
    public void set_amplitude(float amplitude, int interpolation_duration_in_samples) {
        if (interpolation_duration_in_samples > 0) {
            mDesiredAmplitude = amplitude;
            mDesiredAmplitudeSteps = interpolation_duration_in_samples;
            final float mAmplitudeDelta = mDesiredAmplitude - mAmplitude;
            mDesiredAmplitudeFraction = mAmplitudeDelta / interpolation_duration_in_samples;
        } else {
            set_amplitude(amplitude);
        }
    }

    public float[] get_wavetable() {
        return mWavetable;
    }

    public void enable_jitter(boolean enable_jitter) {
        mEnableJitter = enable_jitter;
    }

    public float get_phase_offset() {
        return mPhaseOffset;
    }

    public void set_phase_offset(float phase_offset) {
        mPhaseOffset = phase_offset < 0 ? 1 + phase_offset : phase_offset;
    }

    public float get_jitter_range() {
        return mJitterRange;
    }

    public void set_jitter_range(float jitter_range) {
        mJitterRange = jitter_range;
    }

    public void reset() {
        mSignal = 0.0f;
        mArrayPtr = 0.0f;
    }

    public float current() {
        return mSignal;
    }

    public void set_interpolation(int interpolation_type) {
        mInterpolationType = interpolation_type;
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

    private float computeStepSize() {
        return mFrequency * ((float) mWavetable.length / (float) mSamplingRate);
    }

    private float next_sample() {
        final float mOutput = mWavetable[(int) (mArrayPtr)];
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
}