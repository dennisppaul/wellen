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

import java.util.ArrayList;

import static wellen.Wellen.IGNORE_IN_OUTPOINTS;
import static wellen.Wellen.NO_IN_OUTPOINT;

/**
 * manages a collection of {@link DSPTrack} objects. each track is updated to process audio signals and updated via the
 * method <pre><code>void&nbsp;beat(int)</code></pre>.
 */
public class DSPComposition implements DSPNodeOutputSignal {
    private final ArrayList<DSPTrack> mTracks = new ArrayList<>();
    private int mBeat = IGNORE_IN_OUTPOINTS;

    public ArrayList<DSPTrack> tracks() {
        return mTracks;
    }

    public DSPTrack track(int pID) {
        return mTracks.get(pID);
    }

    public Signal output() {
        Signal s = new Signal();
        output(s);
        return s;
    }

    public void output(Signal pSignal) {
        for (DSPTrack c : mTracks) {
            if (mBeat == IGNORE_IN_OUTPOINTS || evaluate_in_outpoints(c, mBeat)) {
                final Signal s = new Signal();
                c.output(s);
                pSignal.signal[Wellen.SIGNAL_LEFT] += s.left() * c.volume;
                pSignal.signal[Wellen.SIGNAL_RIGHT] += s.right() * c.volume;
            }
        }
    }

    private boolean evaluate_in_outpoints(DSPTrack c, int mBeat) {
        return (c.in == NO_IN_OUTPOINT && c.out == NO_IN_OUTPOINT) || (c.in <= mBeat && (c.out >= mBeat || c.out == NO_IN_OUTPOINT));
    }

    public void beat(int pBeat) {
        mBeat = pBeat;
        for (DSPTrack c : mTracks) {
            if (evaluate_in_outpoints(c, mBeat)) {
                c.beat(pBeat);
            }
        }
    }
}
