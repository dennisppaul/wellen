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
 * {@code Grid} is a class that structures a continously increasing beat count into a temporal <em>grid</em> by
 * generating repeating events.
 * <p>
 * the class can, for example, be used to create an event every quarter note. this behavior is usually achieved via the
 * {@code beat(int)} method:
 * <pre><code>
 *     public void beat(int beat) {
 *         if (mNoteEvents.event(beat, QUARTER_NOTE)) {
 *             // do something every quarter note
 *         }
 *     }
 * </code></pre>
 * <p>
 * the class uses the concept of *Pulses per Quarter Note* (PPQN) ( as used e.g in MIDI clock @see <a
 * href="https://en.wikipedia.org/wiki/Pulses_per_quarter_note">Pulses per Quarter Note @ Wikipedia</a> ). PPQN is the
 * number of pulses that make up a quarter note. the default value is 24 which results into a quarter note being
 * occuring every 24 pulese ( or beats ).
 * <p>
 * the schematic drawing below shows events counted quarter, half, whole, etcetera notes:
 * <pre><code>
 * WHOLE     : |0-----------------------|------------------------|------------------------|------------------------
 * HALF      : |0-----------------------|------------------------|1-----------------------|------------------------
 * QUARTER   : |0-----------------------|1-----------------------|2-----------------------|3-----------------------
 * EIGHTH    : |0-----------1-----------|2-----------3-----------|4-----------5-----------|6-----------7-----------
 * SIXTEENTH : |0-----1-----2-----3-----|4-----5-----6-----7-----|8-----9-----0-----1-----|2-----3-----4-----5-----
 * THIRTYSEC : |0--1--2--3--4--5--6--7--|8--9--0--1--2--3--4--5--|6--7--8--9--0--1--2--3--|4--5--6--7--8--9--0--1--
 * ( PPQN 24 )
 * </code></pre>
 * note, that the <code>|</code> is generated every quarter note to make the events more visible.
 * <p>
 * the schematic drawing below shows events generated based on pulses ( or beats ). the approach is somewhat more raw
 * and ignores the concept of PPQN. it however is usefull when more fine grained grids are required ( e.g when
 * implementing quarter triplets in this case with a phase of 8 ). it can also apply an offset to the beat count.
 * <pre><code>
 * PHASE 3   : |0--1--2--3--4--5--6--7--|8--9--0--1--2--3--4--5--|6--7--8--9--0--1--2--3--|4--5--6--7--8--9--0--1--
 * PHASE 5   : |0----1----2----3----4---|-5----6----7----8----9--|--0----1----2----3----4-|---5----6----7----8----9
 * PHASE 7   : |0------1------2------3--|----4------5------6-----|-7------8------9------0-|-----1------2------3----
 * PHASE 8   : |0-------1-------2-------|3-------4-------5-------|6-------7-------8-------|9-------0-------1-------
 * PHASE 24>1: |-0----------------------|-1----------------------|-2----------------------|-3----------------------
 * ( PPQN 24 )
 * </code></pre>
 * <p>
 * See also {@link wellen.Loop} and {@link wellen.Pattern} for related classes implementing a related concept.
 */
public class Grid {
    private int fPPQN;

    public Grid(int PPQN) {
        fPPQN = PPQN;
    }

    public Grid() {
        this(24);
    }

    private static int get_num_pulses() {
        return 24 * 4;
    }

    private static void printBar(Grid events, int beat) {
        if (events.event(beat, Wellen.NOTE_QUARTER)) {
            System.out.print("|");
//            System.out.print("|" + events.event_count(beat, Wellen.NOTE_QUARTER) + "|");
        }
    }

    private static void printNoteEvents(String event_name, Grid events, float note_type) {
        System.out.print(event_name + ": ");
        for (int i = 0; i < get_num_pulses(); i++) {
            printBar(events, i);
            System.out.print(events.event(i, note_type) ? (events.event_count(i, note_type) % 10) : "-");
        }
        System.out.println();
    }

    private static void printPhaseEvents(String event_name, Grid events, int phase, int offset) {
        System.out.print(event_name + ": ");
        for (int i = 0; i < get_num_pulses(); i++) {
            printBar(events, i);
            int mPhaseEventCount = events.event_phase_count(i, phase, offset);
            System.out.print(mPhaseEventCount != Wellen.NO_EVENT ? (mPhaseEventCount % 10) : "-");
        }
        System.out.println();
    }

    private static void run_test() {
        Grid mEvents = new Grid();
        mEvents.set_PPQN(24);
        printNoteEvents("WHOLE     ", mEvents, Wellen.NOTE_WHOLE);
        printNoteEvents("HALF      ", mEvents, Wellen.NOTE_HALF);
        printNoteEvents("QUARTER   ", mEvents, Wellen.NOTE_QUARTER);
        printNoteEvents("EIGHTH    ", mEvents, Wellen.NOTE_EIGHTH);
        printNoteEvents("SIXTEENTH ", mEvents, Wellen.NOTE_SIXTEENTH);
        printNoteEvents("THIRTYSEC ", mEvents, Wellen.NOTE_THIRTYSECOND);
        System.out.println("( PPQN " + mEvents.get_PPQN() + " )");
        printPhaseEvents("PHASE 3   ", mEvents, 3, 0);
        printPhaseEvents("PHASE 5   ", mEvents, 5, 0);
        printPhaseEvents("PHASE 7   ", mEvents, 7, 0);
        printPhaseEvents("PHASE 8   ", mEvents, 8, 0);
        printPhaseEvents("PHASE 24>1",
                         mEvents,
                         mEvents.get_PPQN(),
                         1); // i.e quarter note ( 24 pulses ) with an offset of 1 pulse.
        System.out.println("( PPQN " + mEvents.get_PPQN() + " )");
    }

    /**
     * @return the number of beats per quarter note.
     */
    public int get_PPQN() {
        return fPPQN;
    }

    /**
     * set the *pulses per quarter note* (PPQN) value. the default value is 24 which means that a *quarter note event*
     * occurs every 24 beats e.g at 24, 48, 72, ...
     *
     * @param PPQN *pulses per quarter note* (PPQN) value
     */
    public void set_PPQN(int PPQN) {
        fPPQN = PPQN;
    }

    public boolean event_phase(int beat, int phase, int offset) {
        return (beat % phase) == offset;
    }

    public boolean event(int beat, float note) {
        return event_phase(beat, getDivider(note), 0);
    }

    public boolean is_whole(int beat) {
        return event(beat, Wellen.NOTE_WHOLE);
    }

    public boolean is_half(int beat) {
        return event(beat, Wellen.NOTE_HALF);
    }

    public boolean is_quarter(int beat) {
        return event(beat, Wellen.NOTE_QUARTER);
    }

    /* ------------------------------------------------------------------------------------------------------------- */

    public boolean is_eighth(int beat) {
        return event(beat, Wellen.NOTE_EIGHTH);
    }

    public boolean is_sixteenth(int beat) {
        return event(beat, Wellen.NOTE_SIXTEENTH);
    }

    public int event_count(int beat, float note) {
        final int mDivider = getDivider(note);
        final boolean mIsEvent = (beat % mDivider) == 0;
        return mIsEvent ? (beat / mDivider) : Wellen.NO_EVENT;
    }

    public int event_phase_count(int beat, int phase, int offset) {
        final boolean mIsEvent = (beat % phase) == offset;
        return mIsEvent ? (beat / phase) : Wellen.NO_EVENT;
    }

    private int getDivider(float note) {
        return (int) (fPPQN / note);
    }

    public static void main(String[] args) {
        run_test();
    }
}
