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

import wellen.Wellen;

/**
 * detectes the pitch of a signal.
 * <p>
 * it uses the YIN algorithm described in the paper <a
 * href="http://recherche.ircam.fr/equipes/pcm/cheveign/ps/2002_JASA_YIN_proof.pdf">Alain de Cheveigné + Hideki
 * Kawahara:YIN, a fundamental frequency estimator for speech and music</a>.
 * <p>
 * Fair Use Disclaimer: this implementation was taken, with minor modifications, from a project called <a
 * href="https://github.com/JorenSix/TarsosDSP">TarsosDSP</a> by Joren Six released under the GPL-3.0 license. the
 * project seems to be really well written. i therefore think it is fair to use the existing source code and modify it.
 */
public final class PitchDetection {
    /*
     * An implementation of the AUBIO_YIN pitch tracking algorithm. See <a href=
     * "http://recherche.ircam.fr/equipes/pcm/cheveign/ps/2002_JASA_YIN_proof.pdf" >the YIN paper.</a> Implementation
     *  based
     * on <a href="http://aubio.org">aubio</a>
     *
     * @author Joren Six
     * @author Paul Brossier
     */

    /**
     * The default YIN threshold value. Should be around 0.10~0.15. See YIN paper for more information.
     */
    private static final double DEFAULT_THRESHOLD = 0.20;
    private float fPitch;
    private boolean fPitched;
    private float fProbability;
    /**
     * The audio sample rate. Most audio has a sample rate of 44.1kHz.
     */
    private final float fSampleRate;
    /**
     * The actual YIN threshold.
     */
    private final double fThreshold;
    /**
     * The buffer that stores the calculated values. It is exactly half the size of the input buffer.
     */
    private final float[] fYINBuffer;

    /**
     * Create a new pitch detector for a stream with the defined sample rate. Processes the audio in blocks of the
     * defined size.
     */
    public PitchDetection() {
        this(Wellen.DEFAULT_SAMPLING_RATE, Wellen.DEFAULT_AUDIOBLOCK_SIZE, DEFAULT_THRESHOLD);
    }

    /**
     * Create a new pitch detector for a stream with the defined sample rate. Processes the audio in blocks of the
     * defined size.
     *
     * @param audioSampleRate The sample rate of the audio stream. E.g. 44.1 kHz.
     * @param bufferSize      The size of a buffer. E.g. 1024.
     */
    public PitchDetection(final float audioSampleRate, final int bufferSize) {
        this(audioSampleRate, bufferSize, DEFAULT_THRESHOLD);
    }

    /**
     * Create a new pitch detector for a stream with the defined sample rate. Processes the audio in blocks of the
     * defined size.
     *
     * @param audioSampleRate The sample rate of the audio stream. E.g. 44.1 kHz.
     * @param bufferSize      The size of a buffer. E.g. 1024.
     * @param yinThreshold    The parameter that defines which peaks are kept as possible pitch candidates. See the YIN
     *                        paper for more details.
     */
    public PitchDetection(final float audioSampleRate, final int bufferSize, final double yinThreshold) {
        fSampleRate = audioSampleRate;
        fThreshold = yinThreshold;
        fYINBuffer = new float[bufferSize / 2];
    }

    /**
     * The main flow of the YIN algorithm. Returns a pitch value in Hz or -1 if no pitch is detected.
     *
     * @return a pitch value in Hz or -1 if no pitch is detected.
     */
    public float[] process(final float[] audioBuffer) {
        final int tauEstimate;
        final float pitchInHertz;

        // step 2
        difference(audioBuffer);

        // step 3
        cumulativeMeanNormalizedDifference();

        // step 4
        tauEstimate = absoluteThreshold();

        // step 5
        if (tauEstimate != -1) {
            final float betterTau = parabolicInterpolation(tauEstimate);

            // step 6
            // TODO Implement optimization for the AUBIO_YIN algorithm.
            // 0.77% => 0.5% error rate,
            // using the data of the YIN paper
            // bestLocalEstimate()

            // conversion to Hz
            pitchInHertz = fSampleRate / betterTau;
        } else {
            // no pitch found
            pitchInHertz = -1;
        }
        fPitch = pitchInHertz;
        return audioBuffer;
    }

    public float get_pitch() {
        return fPitch;
    }

    public float get_probability() {
        return fProbability;
    }

    public boolean is_pitched() {
        return fPitched;
    }

    /**
     * Implements step 4 of the AUBIO_YIN paper.
     */
    private int absoluteThreshold() {
        // Uses another loop construct
        // than the AUBIO implementation
        int tau;
        // first two positions in yinBuffer are always 1
        // So start at the third (index 2)
        for (tau = 2; tau < fYINBuffer.length; tau++) {
            if (fYINBuffer[tau] < fThreshold) {
                while (tau + 1 < fYINBuffer.length && fYINBuffer[tau + 1] < fYINBuffer[tau]) {
                    tau++;
                }
                // found tau, exit loop and return
                // store the probability
                // From the YIN paper: The threshold determines the list of
                // candidates admitted to the set, and can be interpreted as the
                // proportion of aperiodic power tolerated
                // within a periodic signal.
                //
                // Since we want the periodicity and and not aperiodicity:
                // periodicity = 1 - aperiodicity
                fProbability = 1 - fYINBuffer[tau];
                break;
            }
        }

        // if no pitch found, tau => -1
        if (tau == fYINBuffer.length || fYINBuffer[tau] >= fThreshold) {
            tau = -1;
            fProbability = 0;
            fPitched = false;
        } else {
            fPitched = true;
        }

        return tau;
    }

    /**
     * The cumulative mean normalized difference function as described in step 3 of the YIN paper. <br>
     * <code>
     * yinBuffer[0] == yinBuffer[1] = 1
     * </code>
     */
    private void cumulativeMeanNormalizedDifference() {
        int tau;
        fYINBuffer[0] = 1;
        float runningSum = 0;
        for (tau = 1; tau < fYINBuffer.length; tau++) {
            runningSum += fYINBuffer[tau];
            fYINBuffer[tau] *= tau / runningSum;
        }
    }

    /**
     * Implements the difference function as described in step 2 of the YIN paper.
     */
    private void difference(final float[] audioBuffer) {
        int index, tau;
        float delta;
        for (tau = 0; tau < fYINBuffer.length; tau++) {
            fYINBuffer[tau] = 0;
        }
        for (tau = 1; tau < fYINBuffer.length; tau++) {
            for (index = 0; index < fYINBuffer.length; index++) {
                delta = audioBuffer[index] - audioBuffer[index + tau];
                fYINBuffer[tau] += delta * delta;
            }
        }
    }

    /**
     * Implements step 5 of the AUBIO_YIN paper. It refines the estimated tau value using parabolic interpolation. This
     * is needed to detect higher frequencies more precisely. See
     * <a href="http://fizyka.umk.pl/nrbook/c10-2.pdf">(dead link)</a> and for more
     * background <a
     * href="http://sfb649.wiwi.hu-berlin.de/fedc_homepage/xplore/tutorials/xegbohtmlnode62.html">Minimization of a
     * Function: One-dimensional Case </a>
     *
     * @param tauEstimate The estimated tau value.
     * @return A better, more precise tau value.
     */
    private float parabolicInterpolation(final int tauEstimate) {
        final float betterTau;
        final int x0;
        final int x2;

        if (tauEstimate < 1) {
            x0 = tauEstimate;
        } else {
            x0 = tauEstimate - 1;
        }
        if (tauEstimate + 1 < fYINBuffer.length) {
            x2 = tauEstimate + 1;
        } else {
            x2 = tauEstimate;
        }
        if (x0 == tauEstimate) {
            if (fYINBuffer[tauEstimate] <= fYINBuffer[x2]) {
                betterTau = tauEstimate;
            } else {
                betterTau = x2;
            }
        } else if (x2 == tauEstimate) {
            if (fYINBuffer[tauEstimate] <= fYINBuffer[x0]) {
                betterTau = tauEstimate;
            } else {
                betterTau = x0;
            }
        } else {
            float s0, s1, s2;
            s0 = fYINBuffer[x0];
            s1 = fYINBuffer[tauEstimate];
            s2 = fYINBuffer[x2];
            // fixed AUBIO implementation, thanks to Karl Helgason:
            // (2.0f * s1 - s2 - s0) was incorrectly multiplied with -1
            betterTau = tauEstimate + (s2 - s0) / (2 * (2 * s1 - s2 - s0));
        }
        return betterTau;
    }
}
