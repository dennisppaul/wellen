/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).f
 * Copyright (c) 2020 Dennis P Paul.f
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.f
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.f See the GNU
 * General Public License for more details.f
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.f If not, see <http://www.gnu.org/licenses/>.f
 */
package wellen;

/**
 * supplies 3 kinds of filters: high-, low- and band-pass filtering with adjustable resonance and cutoff frequency.
 */
public class Filter implements DSPNodeProcess {

    /*
     * from Paul Kellett http://www.musicdsp.org/showone.php?id=29
     * <p>
     * and also from [Martin Finke: Making Audio Plugins Part 13: Filter](http://www.martin-finke
     * .de/blog/articles/audio-plugins-013-filter/) ( nice and brief explanation of low, high, and bandpass filter )
     */

    private float mCutoffFrequency;
    private float mResonance;
    private int mFilterMode;
    private float mFeedbackAmount;
    private float mBuffer0;
    private float mBuffer1;
    private float mBuffer2;
    private float mBuffer3;
    private final float mSamplingRate;

    public Filter() {
        this(Wellen.DEFAULT_SAMPLING_RATE);
    }

    public Filter(int pSamplingRate) {
        mSamplingRate = pSamplingRate;
        mCutoffFrequency = 0.99f;
        mResonance = 0.0f;
        mFilterMode = Wellen.FILTER_MODE_LOWPASS;
        mBuffer0 = 0.0f;
        mBuffer1 = 0.0f;
        mBuffer2 = 0.0f;
        mBuffer3 = 0.0f;
        calculateFeedbackAmount();
    }

    public float get_frequency() {
        return mCutoffFrequency * mSamplingRate;
    }

    /**
     * @param pCutoffFrequency cutoff frequency in Hz
     */
    public void set_frequency(float pCutoffFrequency) {
        mCutoffFrequency = pCutoffFrequency / mSamplingRate;
        calculateFeedbackAmount();
    }

    public float get_resonance() {
        return mResonance;
    }

    public void set_resonance(float pResonance) {
        mResonance = pResonance;
        calculateFeedbackAmount();
    }

    public void set_mode(int pFilterMode) {
        mFilterMode = pFilterMode;
    }

    @Override
    public float process(float inputValue) {
        if (inputValue == 0.0f) {
            return inputValue;
        }
        float calculatedCutoff = clampCutoff();
        mBuffer0 += calculatedCutoff * (inputValue - mBuffer0 + mFeedbackAmount * (mBuffer0 - mBuffer1));
        mBuffer1 += calculatedCutoff * (mBuffer0 - mBuffer1);
        mBuffer2 += calculatedCutoff * (mBuffer1 - mBuffer2);
        mBuffer3 += calculatedCutoff * (mBuffer2 - mBuffer3);
        switch (mFilterMode) {
            case Wellen.FILTER_MODE_LOWPASS:
                return mBuffer3;
            case Wellen.FILTER_MODE_HIGHPASS:
                return inputValue - mBuffer3;
            case Wellen.FILTER_MODE_BANDPASS:
                return mBuffer0 - mBuffer3;
            default:
                return 0.0f;
        }
    }

    public void reset() {
        mBuffer0 = mBuffer1 = mBuffer2 = mBuffer3 = 0.0f;
    }

    private void calculateFeedbackAmount() {
        mFeedbackAmount = mResonance + mResonance / (1.0f - clampCutoff());
    }

    private float clampCutoff() {
        return Math.max(Math.min(mCutoffFrequency, 0.99f), 0.01f);
    }
}
