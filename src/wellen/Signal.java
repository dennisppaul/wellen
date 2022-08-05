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

import static wellen.Wellen.SIGNAL_LEFT;
import static wellen.Wellen.SIGNAL_RIGHT;

public class Signal {

    public final float[] signal;

    public static Signal create(float pSignalLeft, float pSignalRight) {
        return new Signal(pSignalLeft, pSignalRight);
    }

    public static Signal create(float pSignal) {
        return new Signal(pSignal, pSignal);
    }

    public Signal(int pChannels) {
        signal = new float[pChannels];
    }

    public Signal(float pLeft, float pRight) {
        this(2);
        left(pLeft);
        right(pRight);
    }

    public Signal() {
        this(2);
    }

    public Signal(Signal pSignal) {
        this(pSignal.signal.length);
        System.arraycopy(pSignal.signal, 0, signal, 0, signal.length);
    }

    public float[] signal() {
        return signal;
    }

    public void set(float pSignal) {
        if (signal.length > 0) {
            signal[SIGNAL_LEFT] = pSignal;
        }
        if (signal.length > 1) {
            signal[SIGNAL_RIGHT] = pSignal;
        }
    }

    public void set(float pSignalLeft, float pSignalRight) {
        if (signal.length > 0) {
            signal[SIGNAL_LEFT] = pSignalLeft;
        }
        if (signal.length > 1) {
            signal[SIGNAL_RIGHT] = pSignalRight;
        }
    }

    public int num_channels() {
        return signal.length;
    }


    public float left() {
        if (signal.length < 1) {
            return 0;
        }
        return signal[SIGNAL_LEFT];
    }

    public float right() {
        if (signal.length < 2) {
            return 0;
        }
        return signal[SIGNAL_RIGHT];
    }

    public void left(float pSignal) {
        if (signal.length < 1) {
            return;
        }
        signal[SIGNAL_LEFT] = pSignal;
    }

    public void right(float pSignal) {
        if (signal.length < 2) {
            return;
        }
        signal[SIGNAL_RIGHT] = pSignal;
    }

    public void left_mult(float pSignal) {
        if (signal.length < 1) {
            return;
        }
        signal[SIGNAL_LEFT] *= pSignal;
    }

    public void right_mult(float pSignal) {
        if (signal.length < 2) {
            return;
        }
        signal[SIGNAL_RIGHT] *= pSignal;
    }

    public void left_add(float pSignal) {
        if (signal.length < 1) {
            return;
        }
        signal[SIGNAL_LEFT] += pSignal;
    }

    public void right_add(float pSignal) {
        if (signal.length < 2) {
            return;
        }
        signal[SIGNAL_RIGHT] += pSignal;
    }
}
