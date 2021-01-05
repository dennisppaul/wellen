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
 * interface for listeners of {@link wellen.MidiIn}
 */
public interface MidiInListener {
    void receiveProgramChange(int channel, int number, int value);
    void receiveControlChange(int channel, int number, int value);
    void receiveNoteOff(int channel, int pitch);
    void receiveNoteOn(int channel, int pitch, int velocity);
    void clock_tick();
    void clock_start();
    void clock_continue();
    void clock_stop();
    void clock_song_position_pointer(int pOffset16th);
}