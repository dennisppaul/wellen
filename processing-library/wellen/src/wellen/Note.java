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

package wellen;

import processing.core.PApplet;

/**
 * supplies a series of note constants and methods to convert note values to frequencies and vice versa.
 */
public abstract class Note implements MIDI {


    private static final int NOTE_OFFSET = 69;

    public static int frequency_to_note(float pFreq) {
        return frequency_to_note(pFreq, 440, NOTE_OFFSET);
    }

    public static int frequency_to_note(float pFreq, float pBaseFreq, int pOffset) {
        return (int) Math.round(12 * log2(pFreq / pBaseFreq)) + pOffset;
    }

    public static float frequency_to_note_f(float pFreq) {
        return frequency_to_note_f(pFreq, 440.0f, NOTE_OFFSET);
    }

    public static float frequency_to_note_f(float pFreq, float pBaseFreq, float pOffset) {
        return (float) (12f * log2(pFreq / pBaseFreq)) + pOffset;
    }

    public static float note_to_frequency(int pMidiNote, float pBaseFreq) {
        return pBaseFreq * (float) Math.pow(2.0, (pMidiNote / 12.0));
    }

    public static float note_to_frequency(int pMidiNote) {
        return note_to_frequency(pMidiNote - NOTE_OFFSET, 440); // A4 440 Hz
    }

    public static String note_to_string(int noteNum) {
        final String notes = "C C#D D#E F F#G G#A A#B ";
        int octave;
        String note;

        octave = noteNum / 12 - 1;
        note = notes.substring((noteNum % 12) * 2, (noteNum % 12) * 2 + 2);
        return PApplet.trim(note) + octave;
    }

    private static double log2(double num) {
        return (Math.log(num) / Math.log(2));
    }
}
