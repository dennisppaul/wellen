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

import processing.core.PApplet;

/**
 * supplies a series of note constants and methods to convert note values to frequencies and vice versa.
 */
public abstract class Note {

    public static final int NOTE_A0 = 9;
    public static final int NOTE_A1 = 21;
    public static final int NOTE_A2 = 33;
    public static final int NOTE_A3 = 45;
    public static final int NOTE_A4 = 57;
    public static final int NOTE_A5 = 69;
    public static final int NOTE_A6 = 81;
    public static final int NOTE_A7 = 93;
    public static final int NOTE_A8 = 105;
    public static final int NOTE_A9 = 117;
    public static final int NOTE_C1 = 12;
    public static final int NOTE_C2 = 24;
    public static final int NOTE_C3 = 36;
    public static final int NOTE_C4 = 48;
    public static final int NOTE_C5 = 60;
    public static final int NOTE_C6 = 72;
    public static final int NOTE_C7 = 84;
    public static final int NOTE_C8 = 96;
    public static final int NOTE_C9 = 108;
    public static final int NOTE_C10 = 120;
    private static final int NOTE_OFFSET = (69 - 12);

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

    public static int frequency_to_note(int pFreq) {
        return frequency_to_note(pFreq, 440, NOTE_OFFSET);
    }

    public static int frequency_to_note(int pFreq, float pBaseFreq, int pOffset) {
        return (int) (Math.round(12 * log2(pFreq / pBaseFreq)) + pOffset);
    }

    private static double log2(double num) {
        return (Math.log(num) / Math.log(2));
    }
}
