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

package wellen;

import java.util.Arrays;

import static wellen.MIDI.PPQN;

/**
 * creates a sequence of notes based on an input pattern.
 */
public class Arpeggiator {

// @TODO(add a `note_off` mechanism)

    private int mBaseNote;
    private float mBaseVelocity;
    private NoteStruct mCurrentNote;
    private NoteStruct[] mPattern;
    private int mStep;

    /**
     * @param pLengthInQuarterNotes length of the pattern in quarter notes
     */
    public Arpeggiator(int pLengthInQuarterNotes) {
        mPattern = new NoteStruct[pLengthInQuarterNotes];
        Arrays.fill(mPattern, new NoteStruct());
        reset();
    }

    /**
     * @param pPattern  pattern to set
     * @param pPosition position in pattern
     * @param pScaler   position scaler in pattern
     * @param pNote     note in pattern
     * @param pVelocity velocity in pattern
     * @return true if note was set
     */
    public static boolean set(NoteStruct[] pPattern, int pPosition, int pScaler, int pNote, float pVelocity) {
        return set(pPattern, pPosition * pScaler, pNote, pVelocity);
    }

    /**
     * @param pPattern  pattern to set
     * @param pPosition position in pattern
     * @param pNote     note in pattern
     * @param pVelocity velocity in pattern
     * @return true if note was set
     */
    public static boolean set(NoteStruct[] pPattern, int pPosition, int pNote, float pVelocity) {
        NoteStruct ns = new NoteStruct();
        ns.active = true;
        ns.note = pNote;
        ns.velocity = pVelocity;
        if (pPosition >= 0 && pPosition < pPattern.length) {
            pPattern[pPosition] = ns;
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the current note
     */
    public boolean step() {
        if (mStep < mPattern.length) {
            mCurrentNote = mPattern[mStep];
            mStep++;
            return trigger();
        } else {
            return false;
        }
    }

    /**
     * @param pPattern pattern to replace current pattern
     */
    public void replace_pattern(NoteStruct[] pPattern) {
        mPattern = pPattern;
        reset();
    }

    /**
     * clear current pattern
     */
    public void clear_pattern() {
        for (int i = 0; i < mPattern.length; i++) {
            Arrays.fill(mPattern, new NoteStruct());
        }
    }

    /**
     * @return true if a note is currently playing and if is active
     */
    public boolean trigger() {
        return mCurrentNote != null && mCurrentNote.active;
    }

    /**
     * @return current note
     */
    public int note() {
        return mCurrentNote != null ? mCurrentNote.note + mBaseNote : 0;
    }

    /**
     * @return current velocity
     */
    public int velocity() {
        return mCurrentNote != null ? (int) (mCurrentNote.velocity * mBaseVelocity) : 0;
    }

    /**
     * reset to beginning of pattern
     */
    public void reset() {
        mStep = 0;
        mCurrentNote = null;
    }

    /**
     * @param pNote     set base note
     * @param pVelocity set base velocity
     */
    public void play(int pNote, int pVelocity) {
        mBaseNote = pNote;
        mBaseVelocity = pVelocity;
        reset();
    }

    /**
     * set note in pattern
     *
     * @param pPosition position in pattern
     * @param pNote     note in pattern
     * @param pVelocity velocity in pattern
     * @return true if note was set
     */
    public boolean pattern(int pPosition, int pNote, float pVelocity) {
        return set(mPattern, pPosition, pNote, pVelocity);
    }

    /**
     * set note in pattern
     *
     * @param pPosition position in pattern
     * @param pScaler   scaler in pattern
     * @param pNote     note in pattern
     * @param pVelocity velocity in pattern
     * @return true if note was set
     */
    public boolean pattern(int pPosition, int pScaler, int pNote, float pVelocity) {
        return set(mPattern, pPosition, pScaler, pNote, pVelocity);
    }

    /**
     * @param pPosition position in pattern
     * @param pNote     note in pattern
     * @param pVelocity velocity in pattern
     * @return true if note was set
     */
    public boolean pattern_4(int pPosition, int pNote, float pVelocity) {
        return set(mPattern, pPosition, PPQN, pNote, pVelocity);
    }

    /**
     * @param pPosition position in pattern
     * @param pNote     note in pattern
     * @param pVelocity velocity in pattern
     * @return true if note was set
     */
    public boolean pattern_8(int pPosition, int pNote, float pVelocity) {
        return set(mPattern, pPosition, PPQN / 2, pNote, pVelocity);
    }

    /**
     * @param pPosition position in pattern
     * @param pNote     note in pattern
     * @param pVelocity velocity in pattern
     * @return true if note was set
     */
    public boolean pattern_16(int pPosition, int pNote, float pVelocity) {
        return set(mPattern, pPosition, PPQN / 4, pNote, pVelocity);
    }

    /**
     * @param pPosition position in pattern
     * @param pNote     note in pattern
     * @param pVelocity velocity in pattern
     * @return true if note was set
     */
    public boolean pattern_32(int pPosition, int pNote, float pVelocity) {
        return set(mPattern, pPosition, PPQN / 8, pNote, pVelocity);
    }

    /**
     * data structure for storing note information.
     */
    public static class NoteStruct {
        boolean active;
        int note;
        float velocity;

        /**
         *
         */
        public NoteStruct() {
            this(false, 0, 0.0f);
        }
        /**
         * @param pActive   set actice state
         * @param pNote     set note
         * @param pVelocity set velocity
         */
        public NoteStruct(boolean pActive, int pNote, float pVelocity) {
            active = pActive;
            note = pNote;
            velocity = pVelocity;
        }

        /**
         * @return return copy of note structure
         */
        public NoteStruct copy() {
            return new NoteStruct(active, note, velocity);
        }
    }
}
