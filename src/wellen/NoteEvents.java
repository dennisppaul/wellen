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

/**
 * {@code NoteEvents} is a class that generates and handles note events. a common way to use this class is to call this mehtod continuously
 * in the {@code beat(int)} method:
 *
 * <pre>
 *     public void beat(int pBeat) {
 *     }
 * </pre>
 */
public class NoteEvents {
    public static final int NO_EVENT = -1;
    public static final float FULL_NOTE = 0.25f;
    public static final float HALF_NOTE = 0.5f;
    public static final int QUARTER_NOTE = 1;
    public static final int EIGHTH_NOTE = 2;
    public static final int SIXTEENTH_NOTE = 4;
    public static final int THIRTYSECOND_NOTE = 8;
    private int fPPQ;

    public NoteEvents(int PPQ) {
        fPPQ = PPQ;
    }

    public NoteEvents() {
        this(24);
    }

    /**
     * @return the number of beats per quarter note.
     */
    public int get_PPQ() {
        return fPPQ;
    }

    /**
     * set the *pulses per quarter note* (PPQ) value. the default value is 24 which means that a *quarter note event*
     * occurs every 24 beats e.g at 24, 48, 72, ...
     *
     * @param PPQ *pulses per quarter note* (PPQ) value
     */
    public void set_PPQ(int PPQ) {
        fPPQ = PPQ;
    }

    public boolean is_phase(int beat, int phase, int offset) {
        return (beat % phase) == offset;
    }

    public boolean is_note(int beat, float note) {
        return is_phase(beat, get_divider(note), 0);
    }

    public boolean is_quarter(int beat) {
        return is_note(beat, QUARTER_NOTE);
    }

    public boolean is_eighth(int beat) {
        return is_note(beat, EIGHTH_NOTE);
    }

    public boolean is_sixteenth(int beat) {
        return is_note(beat, SIXTEENTH_NOTE);
    }

    public int get_note(int beat, float note) {
        final int mDivider = get_divider(note);
        final boolean mIsEvent = (beat % mDivider) == 0;
        return mIsEvent ? (beat / mDivider) : NO_EVENT;
    }

    public int get_phase(int beat, int phase, int offset) {
        final boolean mIsEvent = (beat % phase) == offset;
        return mIsEvent ? (beat / phase) : NO_EVENT;
    }

    private int get_divider(float note) {
        return (int) (fPPQ / note);
    }

    /* ------------------------------------------------------------------------------------------------------------- */

    public static void main(String[] args) {
        run_test();
    }

    private static void run_test() {
        NoteEvents mEvents = new NoteEvents();
        mEvents.set_PPQ(24);
        System.out.println("PPQ " + mEvents.get_PPQ());
        printNoteEvents("SIXTEENTH ", mEvents, SIXTEENTH_NOTE);
        printNoteEvents("EIGHTH    ", mEvents, EIGHTH_NOTE);
        printNoteEvents("QUARTER   ", mEvents, QUARTER_NOTE);
        printNoteEvents("HALF      ", mEvents, HALF_NOTE);
        printNoteEvents("FULL      ", mEvents, FULL_NOTE);
        printPhaseEvents("PHASE 3   ", mEvents, 3, 0);
        printPhaseEvents("PHASE 5   ", mEvents, 5, 0);
        printPhaseEvents("PHASE 7   ", mEvents, 7, 0);
        printPhaseEvents("PHASE 24+1",
                         mEvents,
                         mEvents.get_PPQ(),
                         1); // i.e quarter note ( 24 pulses ) with an offset of 1 pulse.
    }

    private static void printPhaseEvents(String event_name, NoteEvents events, int phase, int offset) {
        System.out.print(event_name + ": ");
        for (int i = 0; i < get_num_pulses(); i++) {
            printBar(events, i);
            int mPhaseEventCount = events.get_phase(i, phase, offset);
            System.out.print(mPhaseEventCount != NO_EVENT ? (mPhaseEventCount % 10) : "-");
        }
        System.out.println();
    }

    private static void printNoteEvents(String event_name, NoteEvents events, float note_type) {
        System.out.print(event_name + ": ");
        for (int i = 0; i < get_num_pulses(); i++) {
            printBar(events, i);
            System.out.print(events.is_note(i, note_type) ? (events.get_note(i, note_type) % 10) : "-");
        }
        System.out.println();
    }

    private static int get_num_pulses() {
        return 24 * 8;
    }

    private static void printBar(NoteEvents events, int beat) {
        if (events.is_note(beat, QUARTER_NOTE)) {
            System.out.print("|" + events.get_note(beat, QUARTER_NOTE) + "|");
        }
    }
}
