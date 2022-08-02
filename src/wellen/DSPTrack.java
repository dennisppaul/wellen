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

import static wellen.Wellen.NO_IN_OUTPOINT;

/**
 * a track allows to compose complex DSP modules. it is a container that may be managed by a {@link DSPComposition}
 * object.
 * <p>
 * a track must implement the method <pre><code>void&nbsp;output(Signal)</code></pre> which supplies an audio signal and
 * may implement the method <pre><code>void&nbsp;beat(int)</code></pre> which can be used to receive beat events.
 */
public abstract class DSPTrack implements DSPNodeOutputSignal {
    public float volume;
    public int in;
    public int out;

    public DSPTrack() {
        this(1.0f);
    }

    public DSPTrack(float pVolume) {
        volume = pVolume;
        in = NO_IN_OUTPOINT;
        out = NO_IN_OUTPOINT;
    }

    public void set_in_outpoint(int pIn, int pOut) {
        in = pIn;
        out = pOut;
    }

    public void beat(int pBeat) {
    }
}
