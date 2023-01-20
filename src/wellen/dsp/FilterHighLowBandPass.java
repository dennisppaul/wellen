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

/**
 * provides 3 kinds of filters: high-, low- and band-pass filtering with adjustable resonance and cutoff frequency.
 */
public class FilterHighLowBandPass implements DSPNodeProcess {

    /*
     * from Paul Kellett http://www.musicdsp.org/showone.php?id=29
     * <p>
     * and also from [Martin Finke: Making Audio Plugins Part 13: Filter](http://www.martin-finke
     * .de/blog/articles/audio-plugins-013-filter/) ( nice and brief explanation of low, high, and bandpass filter )
     */

    private float mBuffer0;
    private float mBuffer1;
    private float mBuffer2;
    private float mBuffer3;
    private float fCutoffFrequency;
    private float mFeedbackAmount;
    private int fFilterMode;
    private float fResonance;
    private final float fSamplingRate;

    /**
     *
     */
    public FilterHighLowBandPass() {
        this(Wellen.DEFAULT_SAMPLING_RATE);
    }

    /**
     * @param sampling_rate sampling rate in Hz
     */
    public FilterHighLowBandPass(int sampling_rate) {
        fSamplingRate = sampling_rate;
        fCutoffFrequency = 0.99f;
        fResonance = 0.0f;
        fFilterMode = Wellen.FILTER_MODE_LOW_PASS;
        mBuffer0 = 0.0f;
        mBuffer1 = 0.0f;
        mBuffer2 = 0.0f;
        mBuffer3 = 0.0f;
        calculateFeedbackAmount();
    }

    /**
     * @return cutoff frequency in Hz
     */
    public float get_frequency() {
        return fCutoffFrequency * fSamplingRate;
    }

    /**
     * @param pCutoffFrequency cutoff frequency in Hz
     */
    public void set_frequency(float pCutoffFrequency) {
        fCutoffFrequency = pCutoffFrequency / fSamplingRate;
        calculateFeedbackAmount();
    }

    /**
     * @return resonance
     */
    public float get_resonance() {
        return fResonance;
    }

    /**
     * @param pResonance resonance
     */
    public void set_resonance(float pResonance) {
        fResonance = pResonance;
        calculateFeedbackAmount();
    }

    /**
     * @param pFilterMode filter mode
     */
    public void set_mode(int pFilterMode) {
        fFilterMode = pFilterMode;
    }

    /**
     * @param inputValue input signal
     * @return filtered signal
     */
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
        switch (fFilterMode) {
            case Wellen.FILTER_MODE_LOW_PASS:
                return mBuffer3;
            case Wellen.FILTER_MODE_HIGH_PASS:
                return inputValue - mBuffer3;
            case Wellen.FILTER_MODE_BAND_PASS:
                return mBuffer0 - mBuffer3;
            default:
                return 0.0f;
        }
    }

    /**
     *
     */
    public void reset() {
        mBuffer0 = mBuffer1 = mBuffer2 = mBuffer3 = 0.0f;
    }

    private void calculateFeedbackAmount() {
        mFeedbackAmount = fResonance + fResonance / (1.0f - clampCutoff());
    }

    private float clampCutoff() {
        return Math.max(Math.min(fCutoffFrequency, 0.99f), 0.01f);
    }
}
