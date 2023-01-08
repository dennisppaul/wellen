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

package wellen.analysis;

/*
 *      _______                       _____   _____ _____
 *     |__   __|                     |  __ \ / ____|  __ \
 *        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
 *        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/
 *        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |
 *        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|
 *
 * -----------------------------------------------------------
 *
 *  TarsosDSP is developed by Joren Six at
 *  The Royal Academy of Fine Arts & Royal Conservatory,
 *  University College Ghent,
 *  Hoogpoort 64, 9000 Ghent - Belgium
 *
 *  http://tarsos.0110.be/tag/TarsosDSP
 *  https://github.com/JorenSix/TarsosDSP
 *  http://tarsos.0110.be/releases/TarsosDSP/
 *
 */

import wellen.Wellen;

/**
 * <p>
 * Estimates the locations of percussive onsets using a simple method described in <a
 * href="http://arrow.dit.ie/cgi/viewcontent.cgi?article=1018&context=argcon" >"Drum Source Separation using Percussive
 * Feature Detection and Spectral Modulation"</a> by Dan Barry, Derry Fitzgerald, Eugene Coyle and Bob Lawlor, ISSC
 * 2005.
 * </p>
 * <p>
 * Implementation based on a <a href= "http://vamp-plugins.org/code-doc/PercussionOnsetDetector_8cpp-source.html" >VAMP
 * plugin example</a> by Chris Cannam at Queen Mary, London:
 * <pre>
 *  Centre for Digital Music, Queen Mary, University of London.
 *  Copyright 2006 Chris Cannam.
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use, copy,
 *  modify, merge, publish, distribute, sublicense, and/or sell copies
 *  of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR
 *  ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 *  CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *  Except as contained in this notice, the names of the Centre for
 *  Digital Music; Queen Mary, University of London; and Chris Cannam
 *  shall not be used in advertising or otherwise to promote the sale,
 *  use or other dealings in this Software without prior written
 *  authorization.
 * </pre>
 *
 * @author Joren Six
 * @author Chris Cannam
 */
public class BeatDetection {

    public static final double DEFAULT_SENSITIVITY = 20;
    public static final double DEFAULT_THRESHOLD = 8;
    private float dfMinus1;
    private float dfMinus2;
    private final float[] fCurrentMagnitudes;
    private final FFT fFFT;
    private final float[] fPriorMagnitudes;
    private final float fSampleRate;
    private double fSensitivity;
    private double fThreshold;
    private float fTimeStamp;

    public BeatDetection() {
        this(Wellen.DEFAULT_SAMPLING_RATE, Wellen.DEFAULT_AUDIOBLOCK_SIZE);
    }
    /**
     * Create a new percussion onset detector. With a default sensitivity and threshold.
     *
     * @param sampleRate The sample rate in Hz (used to calculate timestamps)
     * @param bufferSize The size of the buffer in samples.
     */
    public BeatDetection(float sampleRate, int bufferSize) {
        this(sampleRate, bufferSize, DEFAULT_SENSITIVITY, DEFAULT_THRESHOLD);
    }
    /**
     * Create a new percussion onset detector.
     *
     * @param sample_rate The sample rate in Hz (used to calculate timestamps)
     * @param buffer_size The size of the buffer in samples.
     * @param sensitivity Sensitivity of the peak detector applied to broadband detection function (%). In [0-100].
     * @param threshold   Energy rise within a frequency bin necessary to count toward broadband total (dB). In [0-20].
     */
    public BeatDetection(float sample_rate, int buffer_size, double sensitivity, double threshold) {
        fFFT = new FFT(buffer_size / 2);
        fThreshold = threshold;
        fSensitivity = sensitivity;
        fPriorMagnitudes = new float[buffer_size / 2];
        fCurrentMagnitudes = new float[buffer_size / 2];
        fSampleRate = sample_rate;
    }

    public float get_time_stamp() {
        return fTimeStamp;
    }

    public double get_sensitivity() {
        return fSensitivity;
    }

    /**
     * sensitivity of peak detector applied to broadband detection function
     *
     * @param pfSensitivity in percentage [0-100]
     */
    public void set_sensitivity(double pfSensitivity) {
        fSensitivity = pfSensitivity;
    }

    public double get_threshold() {
        return fThreshold;
    }

    /**
     * energy rise within a frequency bin necessary to count toward broadband total
     *
     * @param pfThreshold in dB [0-20]
     */
    public void set_threshold(double pfThreshold) {
        fThreshold = pfThreshold;
    }

    public float[] process(float[] signal_buffer) {
        float[] mSignalBuffer = new float[signal_buffer.length];
        System.arraycopy(signal_buffer, 0, mSignalBuffer, 0, signal_buffer.length);

        fFFT.forwardTransform(mSignalBuffer);
        fFFT.modulus(mSignalBuffer, fCurrentMagnitudes);
        // fFFT.forward(audioFloatBuffer);
        // fCurrentMagnitudes = fFFT.getSpectrum();

        int mBinsOverThreshold = 0;
        for (int i = 0; i < fCurrentMagnitudes.length; i++) {
            if (fPriorMagnitudes[i] > 0) {
                double diff = 10 * Math.log10(fCurrentMagnitudes[i] / fPriorMagnitudes[i]);
                if (diff >= fThreshold) {
                    mBinsOverThreshold++;
                }
            }
            fPriorMagnitudes[i] = fCurrentMagnitudes[i];
        }

        fTimeStamp = -1.0f;
        if (dfMinus2 < dfMinus1 && dfMinus1 >= mBinsOverThreshold && dfMinus1 > ((100.0f - fSensitivity) * signal_buffer.length) / 200) {
            final float mTimeStamp = (float) signal_buffer.length / fSampleRate;
            fTimeStamp = mTimeStamp;
        }

        dfMinus2 = dfMinus1;
        dfMinus1 = mBinsOverThreshold;

        return signal_buffer;
    }
}
