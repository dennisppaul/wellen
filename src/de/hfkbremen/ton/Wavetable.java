package de.hfkbremen.ton;

import processing.core.PApplet;

public class Wavetable implements DSPNodeOutput {

    public static final int TYPE_SINE = 0;
    public static final int TYPE_TRIANGLE = 1;
    public static final int TYPE_SAWTOOTH = 2;
    public static final int TYPE_SQUARE = 3;
    private final int mSamplingRate;
    private final float[] mWavetable;
    private float mFrequency;
    private float mStepSize;
    private float mArrayPtr;
    private float mAmplitude;
    private boolean mInterpolateSamples = false;

    public Wavetable(int pWavetableSize) {
        this(pWavetableSize, DSP.DEFAULT_SAMPLING_RATE);
    }

    public Wavetable(int pWavetableSize, int pSamplingRate) {
        mWavetable = new float[pWavetableSize];
        mSamplingRate = pSamplingRate;
        mArrayPtr = 0;
        mAmplitude = 0.75f;
        set_frequency(220);
    }

    public float get_frequency() {
        return mFrequency;
    }

    public void set_frequency(float pFrequency) {
        if (mFrequency != pFrequency) {
            mFrequency = pFrequency;
            mStepSize = mFrequency * ((float) mWavetable.length / (float) mSamplingRate);
        }
    }

    public void interpolate(boolean pInterpolateSamples) {
        mInterpolateSamples = pInterpolateSamples;
    }

    public float get_amplitude() {
        return mAmplitude;
    }

    public void set_amplitude(float pAmplitude) {
        mAmplitude = pAmplitude;
    }

    public float[] wavetable() {
        return mWavetable;
    }

    public float output() {
        mArrayPtr += mStepSize;
        final int i = (int) mArrayPtr;
        final float mFrac = mArrayPtr - i;
        final int j = i % mWavetable.length;
        mArrayPtr = j + mFrac;

        if (mInterpolateSamples) {
            float mNextSample = mWavetable[(j + 1) % mWavetable.length];
            float mSample = mWavetable[j];
            float mInterpolatedSample = mSample * (1.0f - mFrac) + mNextSample * mFrac;
            return mInterpolatedSample * mAmplitude;
        } else {
            return mWavetable[j] * mAmplitude;
        }
    }

    public static void fill(float[] pWavetable, int pWavetableType) {
        switch (pWavetableType) {
            case TYPE_SINE:
                sine(pWavetable);
                break;
            case TYPE_TRIANGLE:
                triangle(pWavetable);
                break;
            case TYPE_SAWTOOTH:
                sawtooth(pWavetable);
                break;
            case TYPE_SQUARE:
                square(pWavetable);
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
}