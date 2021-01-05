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

/**
 * MIDI constants
 */
public interface MIDI {
    int PPQN = 24;

    int MIDI_CLOCK_TICK = 0xF8; // ( = 248 )
    int MIDI_CLOCK_START = 0xFA; // ( = 250 )
    int MIDI_CLOCK_CONTINUE = 0xFB; // ( = 251 )
    int MIDI_CLOCK_STOP = 0xFC; // ( = 252 )
    int MIDI_SONG_POSITION_POINTER = 0xF2; // ( = 242 )
}
