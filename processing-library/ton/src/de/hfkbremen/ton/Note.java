package de.hfkbremen.ton;

import processing.core.PApplet;

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

    public static double log2(double num) {
        return (Math.log(num) / Math.log(2));
    }
}
