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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.f See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.f If not, see <http://www.gnu.org/licenses/>.
 */

package wellen.dsp;

import wellen.Wellen;

import static wellen.Wellen.FILTER_MODE_BAND_PASS;
import static wellen.Wellen.FILTER_MODE_HIGHSHELF;
import static wellen.Wellen.FILTER_MODE_HIGH_PASS;
import static wellen.Wellen.FILTER_MODE_LOWSHELF;
import static wellen.Wellen.FILTER_MODE_LOW_PASS;
import static wellen.Wellen.FILTER_MODE_NOTCH;
import static wellen.Wellen.FILTER_MODE_PEAK;

/**
 * biquad filter inspired by <a href="https://www.musicdsp.org/en/latest/_downloads/3e1dc886e7849251d6747b194d482272/Audio-EQ-Cookbook.txt">Audio-EQ-Cookbook.txt</a>
 */
public class FilterBiquad implements DSPNodeProcess {
    private static final int BQN = 3;
    private static final float M_PI = (float) Math.PI;
    private final float[] A = new float[BQN];
    private final float[] B = new float[BQN];
    private float Q;
    private final float[] X = new float[BQN];
    private final float[] Y = new float[BQN];
    private float fc;
    private int index;
    private final float mSamplingRate;
    private int mode;
    private float peakGain;

    public FilterBiquad() {
        this(Wellen.DEFAULT_SAMPLING_RATE);
    }

    public FilterBiquad(int pSamplingRate) {
        mSamplingRate = pSamplingRate;
        mode = FILTER_MODE_LOW_PASS;
        fc = 1000;
        Q = 0.7071f;
        peakGain = 0;
        calculate();
    }

    public float get_frequency() {
        return fc;
    }

    /**
     * @param pCutoffFrequency cutoff frequency in Hz
     */
    public void set_frequency(float pCutoffFrequency) {
        fc = pCutoffFrequency;
        calculate();
    }

    public float get_resonance() {
        return Q;
    }

    public void set_resonance(float pResonance) {
        Q = pResonance;
        calculate();
    }

    public float get_peak_gain() {
        return peakGain;
    }

    public void set_peak_gain(float pPeakGain) {
        peakGain = pPeakGain;
        calculate();
    }

    public void set_mode(int pFilterMode) {
        mode = pFilterMode;
        calculate();
    }

    public void process(float[] buffer) {
        for (int i = 0; i < buffer.length; i++) {
            int nm1 = buff_ix(index, -1);
            int nm2 = buff_ix(index, -2);

            X[index] = buffer[i];
            Y[index] = B[0] * X[index] + B[1] * X[nm1] + B[2] * X[nm2] - A[1] * Y[nm1] - A[2] * Y[nm2];
            buffer[i] = Y[index];

            index = buff_ix(index, 1);
        }
    }

    public void calculate() {
        float AA = pow(10.0f, peakGain / 40.0f);
        float w0 = 2.0f * M_PI * fc / mSamplingRate;
        float alpha = sin(w0) / (2.0f * Q);

        float cos_w0 = cos(w0);
        float sqrt_AA = sqrt(AA);

        switch (mode) {
            case FILTER_MODE_LOW_PASS:
                B[0] = (1.0f - cos_w0) / 2.0f;
                B[1] = 1.0f - cos_w0;
                B[2] = (1.0f - cos_w0) / 2.0f;
                A[0] = 1 + alpha;
                A[1] = -2.0f * cos_w0;
                A[2] = 1.0f - alpha;
                break;
            case FILTER_MODE_HIGH_PASS:
                B[0] = (1.0f + cos_w0) / 2.0f;
                B[1] = -(1.0f + cos_w0);
                B[2] = (1.0f + cos_w0) / 2.0f;
                A[0] = 1.0f + alpha;
                A[1] = -2.0f * cos_w0;
                A[2] = 1.0f - alpha;
                break;
            case FILTER_MODE_BAND_PASS: // (constant 0 dB peak gain)
                B[0] = alpha;
                B[1] = 0.0f;
                B[2] = -alpha;
                A[0] = 1.0f + alpha;
                A[1] = -2.0f * cos_w0;
                A[2] = 1.0f - alpha;
                break;
            case FILTER_MODE_NOTCH:
                B[0] = 1.0f;
                B[1] = -2.0f * cos_w0;
                B[2] = 1.0f;
                A[0] = 1.0f + alpha;
                A[1] = -2.0f * cos_w0;
                A[2] = 1.0f - alpha;
                break;
            case FILTER_MODE_PEAK:
                B[0] = 1.0f + alpha * AA;
                B[1] = -2.0f * cos_w0;
                B[2] = 1.0f - alpha * AA;
                A[0] = 1.0f + alpha / AA;
                A[1] = -2.0f * cos_w0;
                A[2] = 1.0f - alpha / AA;
                break;
            case FILTER_MODE_LOWSHELF:
                B[0] = AA * ((AA + 1.0f) - (AA - 1.0f) * cos_w0 + 2.0f * sqrt_AA * alpha);
                B[1] = 2.0f * AA * ((AA - 1) - (AA + 1.0f) * cos_w0);
                B[2] = AA * ((AA + 1.0f) - (AA - 1.0f) * cos_w0 - 2.0f * sqrt_AA * alpha);
                A[0] = (AA + 1.0f) + (AA - 1.0f) * cos_w0 + 2.0f * sqrt_AA * alpha;
                A[1] = -2.0f * ((AA - 1.0f) + (AA + 1.0f) * cos_w0);
                A[2] = (AA + 1.0f) + (AA - 1.0f) * cos_w0 - 2.0f * sqrt_AA * alpha;
                break;
            case FILTER_MODE_HIGHSHELF:
                B[0] = AA * ((AA + 1.0f) + (AA - 1.0f) * cos_w0 + 2.0f * sqrt_AA * alpha);
                B[1] = -2.0f * AA * ((AA - 1.0f) + (AA + 1.0f) * cos_w0);
                B[2] = AA * ((AA + 1.0f) + (AA - 1.0f) * cos_w0 - 2.0f * sqrt_AA * alpha);
                A[0] = (AA + 1.0f) - (AA - 1.0f) * cos_w0 + 2.0f * sqrt_AA * alpha;
                A[1] = 2.0f * ((AA - 1.0f) - (AA + 1.0f) * cos_w0);
                A[2] = (AA + 1.0f) - (AA - 1.0f) * cos_w0 - 2.0f * sqrt_AA * alpha;
                break;
            default:
                B[0] = 1.0f;
                B[1] = 0.0f;
                B[2] = 0.0f;
                A[0] = 1.0f;
                A[1] = 0.0f;
                A[2] = 0.0f;
        }

        float norm = A[0];
        for (int i = 0; i < BQN; i++) {
            A[i] /= norm;
            B[i] /= norm;
        }
    }

    public float process(float inputValue) {
        float[] buffer = {inputValue};
        process(buffer);
        return buffer[0];
    }

    private int buff_ix(int n, int k) {
        return (n + k + BQN) % BQN;
    }

    private float cos(float r) {
        return (float) Math.cos(r);
    }

    private float pow(float v, float w) {
        return (float) Math.pow(v, w);
    }

    private float sin(float r) {
        return (float) Math.sin(r);
    }

    private float sqrt(float v) {
        return (float) Math.sqrt(v);
    }
}
