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

import static processing.core.PApplet.pow;

public abstract class MidiUtilities {

    private static final float MIDI_NOTE_CONVERSION_BASE_FREQUENCY = 440.0f;
    private static final int NOTE_OFFSET = 69;
    private static final String notes = "C C#D D#E F F#G G#A A#B ";

    public static void emit_MIDI_note_constants() {
        for (int noteNum = 21; noteNum < 128; noteNum++) {
            if (noteNum % 12 == 0) {
                System.out.println();
            }
            if (!note_to_string(noteNum).contains("#")) {
                System.out.println("int NOTE_" + note_to_string(noteNum) + " = " + noteNum + ";");
            }
        }
    }

    public static void emit_MIDI_note_table() {
        for (int noteNum = 21; noteNum < 128; noteNum++) {
            System.out.println(noteNum + "\t" + note_to_string(noteNum) + "\t" + "0");
        }
    }

    public static float note_to_frequency(int pMidiNote) {
        return MIDI_NOTE_CONVERSION_BASE_FREQUENCY * pow(2, ((pMidiNote - NOTE_OFFSET) / 12.0f));
    }

    public static String note_to_string(int pMidiNote) {
        // pMidiNote = processing.core.PApplet.max(processing.core.PApplet.min(pMidiNote, 127), 21);
        final int octv = pMidiNote / 12 - 1;
        final String nt = notes.substring((pMidiNote % 12) * 2, (pMidiNote % 12) * 2 + 2).trim();
        return nt + octv;
    }

    public static void main(String[] args) {
        emit_MIDI_note_table();
    }
}
