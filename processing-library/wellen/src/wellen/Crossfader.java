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

public class Crossfader implements DSPNodeOutput {
    public float ratio = 0.5f;
    public DSPNodeOutput signal_a;
    public DSPNodeOutput signal_b;

    public float output() {
        if (signal_a != null && signal_b != null) {
            final float mSignalA = signal_a.output();
            final float mSignalB = signal_b.output();
            return mSignalA + (mSignalB - mSignalA) * ratio;
        } else {
            return 0.0f;
        }
    }
}