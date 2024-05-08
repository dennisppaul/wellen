/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2024 Dennis P Paul.
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

/**
 * interface implemented by classes that process stereo ( or more multi-channel ) signals.
 * <p>
 * note that the input signal is not guaranteed to be left unchanged.
 */
public interface DSPNodeProcessSignal {

    /**
     * @param pSignal input signal
     * @return processed signal
     */
    Signal process_signal(Signal pSignal);
}

