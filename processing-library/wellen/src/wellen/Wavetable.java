/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2020 Dennis P Paul.
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

/**
 * plays back a chunk of samples ( e.g waveforms like sine, triangle, saw or square waves ) at different frequencies.
 */
public class Wavetable implements DSPNodeOutput {

    public static final float DEFAULT_FREQUENCY = 220.0f;
    public static final float DEFAULT_AMPLITUDE = 0.75f;
    private final int mSamplingRate;
    private final float[] mWavetable;
    private int mPhaseOffset;
    private float mFrequency;
    private float mStepSize;
    private float mDesiredStepSize;
    private float mArrayPtr;
    private float mAmplitude;
    private float mCurrentAmplitude;
    private boolean mInterpolateSamples;
    private float mInterpolateFrequencyChangeFactor;
    /* @TODO(consider replacing this with a linear ramp mechanism at some point) */
    private float mInterpolateAmplitudeChangeFactor;
    private float mInterpolateFrequencyDelta;
    private boolean mEnableJitter;
    private float mJitterRange;

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
        mInterpolateSamples = false;
        mInterpolateFrequencyChangeFactor = 0.0f;
        mInterpolateAmplitudeChangeFactor = 0.0f;
        mJitterRange = 0.0f;
        mEnableJitter = false;
        mAmplitude = DEFAULT_AMPLITUDE;
        mPhaseOffset = 0;
        set_frequency(DEFAULT_FREQUENCY);
    }

    public static void fill(float[] pWavetable, int pWavetableType) {
        switch (pWavetableType) {
            case Wellen.WAVESHAPE_SINE:
                sine(pWavetable);
                break;
            case Wellen.WAVESHAPE_TRIANGLE:
                triangle(pWavetable);
                break;
            case Wellen.WAVESHAPE_SAWTOOTH:
                sawtooth(pWavetable);
                break;
            case Wellen.WAVESHAPE_SQUARE:
                square(pWavetable);
                break;
            case Wellen.WAVESHAPE_NOISE:
                noise(pWavetable);
                break;
        }
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

    public float get_frequency() {
        return mFrequency;
    }

    public void set_frequency(float pFrequency) {
        if (mFrequency != PApplet.abs(pFrequency)) {
            mFrequency = PApplet.abs(pFrequency);
            if (mInterpolateFrequencyChangeFactor > 0.0f) {
                mDesiredStepSize = computeStepSize();
                mInterpolateFrequencyDelta = mDesiredStepSize - mStepSize;
                mInterpolateFrequencyDelta *= mInterpolateFrequencyChangeFactor;
            } else {
                mStepSize = computeStepSize();
            }
        }
    }

    public void interpolate_samples(boolean pInterpolateSamples) {
        mInterpolateSamples = pInterpolateSamples;
    }

    public void interpolate_frequency_change(float pInterpolateFrequencyChangeFactor) {
        mInterpolateFrequencyChangeFactor = pInterpolateFrequencyChangeFactor;
    }

    public void interpolate_amplitude_change(float pInterpolateAmplitudeChangeFactor) {
        mInterpolateAmplitudeChangeFactor = pInterpolateAmplitudeChangeFactor;
    }

    public float get_amplitude() {
        return mAmplitude;
    }

    public void set_amplitude(float pAmplitude) {
        mAmplitude = pAmplitude;
    }

    public float[] get_wavetable() {
        return mWavetable;
    }

    public void enable_jitter(boolean pEnableJitter) {
        mEnableJitter = pEnableJitter;
    }

    public int get_phase_offset() {
        return mPhaseOffset;
    }

    public void set_phase_offset(int pPhaseOffset) {
        mPhaseOffset = pPhaseOffset;
    }

    public float get_jitter_range() {
        return mJitterRange;
    }

    public void set_jitter_range(float pJitterRange) {
        mJitterRange = pJitterRange;
    }

    public float output() {
        if (mInterpolateFrequencyChangeFactor > 0.0f) {
            if (mStepSize != mDesiredStepSize) {
                mStepSize += mInterpolateFrequencyDelta;
                final float mDelta = mDesiredStepSize - mStepSize;
                if (Math.abs(mDelta) < 0.1f) {
                    mStepSize = mDesiredStepSize;
                }
            }
        }
        mArrayPtr += mStepSize * (mEnableJitter ? (Wellen.random(-mJitterRange, mJitterRange) + 1.0f) : 1.0f);
        final int i = (int) mArrayPtr;
        final float mFrac = mArrayPtr - i; /* store fractional part */
        int j = i % mWavetable.length; /* wrap pointer to array size */
        mArrayPtr = j + mFrac;
        j = (j + mPhaseOffset) % mWavetable.length; /* apply phase offset */

        final float mTmpAmplitude;
        if (mInterpolateAmplitudeChangeFactor > 0.0f) {
            mCurrentAmplitude += (mAmplitude - mCurrentAmplitude) * mInterpolateAmplitudeChangeFactor;
            mTmpAmplitude = mCurrentAmplitude;
        } else {
            mTmpAmplitude = mAmplitude;
        }

        if (mInterpolateSamples) {
            final float mNextSample = mWavetable[(j + 1) % mWavetable.length];
            final float mSample = mWavetable[j];
            final float mInterpolatedSample = mSample * (1.0f - mFrac) + mNextSample * mFrac;
            return mInterpolatedSample * mTmpAmplitude;
        } else {
            return mWavetable[j] * mTmpAmplitude;
        }
    }

    private float computeStepSize() {
        return mFrequency * ((float) mWavetable.length / (float) mSamplingRate);
    }
}