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

import static processing.core.PConstants.HALF_PI;
import static wellen.Wellen.PAN_LINEAR;
import static wellen.Wellen.PAN_SINE_LAW;
import static wellen.Wellen.PAN_SQUARE_LAW;
import static wellen.Wellen.SIGNAL_LEFT;
import static wellen.Wellen.SIGNAL_RIGHT;

/**
 * position a mono signal somewhere in a stereo space.
 */
public class Pan {

    private int mPanType;
    private float mPanning;
    private float mPanningNormalized;

    public Pan() {
        set_pan_type(PAN_LINEAR);
        set_panning(0.0f);
    }

    public void set_pan_type(int pPanType) {
        mPanType = pPanType;
    }

    /**
     * set the panning value to position the signal in stereo space.
     *
     * @param pPanning the value ranges from -1.0 to 1.0 where -1.0 is left and 1.0 is right channel.
     */
    public void set_panning(float pPanning) {
        mPanning = pPanning > 1.0f ? 1.0f : pPanning < -1.0f ? -1.0f : pPanning;
        mPanningNormalized = (mPanning + 1.0f) * 0.5f;
    }

    public Signal process(float pSignal) {
        final Signal mSignal = new Signal(pSignal, pSignal);
        switch (mPanType) {
            case PAN_LINEAR:
                mSignal.signal[SIGNAL_LEFT] *= 1.0f - mPanningNormalized;
                mSignal.signal[SIGNAL_RIGHT] *= mPanningNormalized;
                break;
            case PAN_SQUARE_LAW:
                mSignal.signal[SIGNAL_LEFT] *= Math.sqrt(1.0f - mPanningNormalized);
                mSignal.signal[SIGNAL_RIGHT] *= Math.sqrt(mPanningNormalized);
                break;
            case PAN_SINE_LAW:
                mSignal.signal[SIGNAL_LEFT] *= Math.sin((1.0f - mPanningNormalized) * HALF_PI);
                mSignal.signal[SIGNAL_RIGHT] *= Math.sin(mPanningNormalized * HALF_PI);
                break;
        }
        return mSignal;
    }
}
