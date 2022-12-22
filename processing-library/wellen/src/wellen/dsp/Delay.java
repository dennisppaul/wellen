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

package wellen.dsp;

import wellen.Wellen;

public class Delay implements DSPNodeProcess, DSPNodeProcessSignal {

    private final float fSampleRate;
    private float[] fEchoBuffer;
    private int fBufferPosition;
    private float fDecayRate;
    private float fNewEchoLength;

    /**
     * @param echo_length in seconds
     * @param sample_rate the sample rate in Hz.
     * @param decay_rate  the decay of the echo, a value between 0 and 1. 1 meaning no decay, 0 means immediate decay
     */
    public Delay(float echo_length, float decay_rate, float sample_rate) {
        fSampleRate = sample_rate;
        set_decay_rate(decay_rate);
        set_echo_length(echo_length);
        adaptyEchoLength();
    }

    public Delay() {
        this(1.0f, 0.75f, Wellen.DEFAULT_SAMPLING_RATE);
    }

    /**
     * @param echo_length A new echo buffer length in seconds.
     */
    public void set_echo_length(float echo_length) {
        fNewEchoLength = echo_length;
    }

    @Override
    public Signal process_signal(Signal pSignal) {
        return Signal.create(process(pSignal.mono()));
    }

    private void adaptyEchoLength() {
        if (fNewEchoLength != -1) {
            float[] mNewEchoBuffer = new float[(int) (fSampleRate * fNewEchoLength)];
            if (fEchoBuffer != null) {
                for (int i = 0; i < mNewEchoBuffer.length; i++) {
                    if (fBufferPosition >= fEchoBuffer.length) {
                        fBufferPosition = 0;
                    }
                    mNewEchoBuffer[i] = fEchoBuffer[fBufferPosition];
                    fBufferPosition++;
                }
            }
            fEchoBuffer = mNewEchoBuffer;
            fNewEchoLength = -1;
        }
    }

    /**
     * A decay, should be a value between zero and one.
     *
     * @param decay_rate the new decay (preferably between zero and one).
     */
    public void set_decay_rate(float decay_rate) {
        fDecayRate = decay_rate;
    }

    @Override
    public float process(float pSignal) {
        adaptyEchoLength();

        if (fBufferPosition >= fEchoBuffer.length) {
            fBufferPosition = 0;
        }
        pSignal = pSignal + fEchoBuffer[fBufferPosition] * fDecayRate;
        fEchoBuffer[fBufferPosition] = pSignal;
        fBufferPosition++;

        return 0;
    }
}
