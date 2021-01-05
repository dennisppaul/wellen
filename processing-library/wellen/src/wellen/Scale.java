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
 * {@link wellen.Scale} supplies a collection of musical scales.
 */
public class Scale {

    public static final int ID_CHROMATIC = 0;
    public static final int ID_HALF_TONE = ID_CHROMATIC;
    public static final int ID_FIFTH = 1;
    public static final int ID_MINOR = 2;
    public static final int ID_MAJOR = 3;
    public static final int ID_CHORD_MINOR = 4;
    public static final int ID_CHORD_MAJOR = 5;
    public static final int ID_CHORD_MINOR_7TH = 6;
    public static final int ID_CHORD_MAJOR_7TH = 7;
    public static final int ID_CHORD_DOMINANT_7 = 8;
    public static final int ID_MINOR_PENTATONIC = 9;
    public static final int ID_MAJOR_PENTATONIC = 10;
    public static final int ID_OCTAVE = 11;
    public static final int ID_DIMINISHED_7 = 12;
    public static final int ID_FIBONACCI = 13;
    public static final int NUMBER_OF_SCALES = 14;
    public static final int[] CHROMATIC = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    public static final int[] HALF_TONE = CHROMATIC;
    public static final int[] FIFTH = {0, 7};
    public static final int[] MINOR = {0, 2, 3, 5, 7, 8, 10};
    public static final int[] MAJOR = {0, 2, 4, 5, 7, 9, 11};
    public static final int[] CHORD_MINOR = {0, 3, 7};
    public static final int[] CHORD_MAJOR = {0, 4, 7};
    public static final int[] CHORD_MINOR_7TH = {0, 3, 7, 10};
    public static final int[] CHORD_MAJOR_7TH = {0, 4, 7, 11};
    public static final int[] CHORD_DOMINANT_7 = {0, 4, 7, 10};
    public static final int[] MINOR_PENTATONIC = {0, 3, 5, 7, 10};
    public static final int[] MAJOR_PENTATONIC = {0, 4, 5, 7, 11};
    public static final int[] OCTAVE = {0};
    public static final int[] DIMINISHED_7 = {0, 3, 6, 9};
    public static final int[] FIBONACCI = {1 - 1, 1 - 1, 2 - 1, 3 - 1, 5 - 1, 8 - 1};
    private static final int[][] ALL_SCALES = new int[NUMBER_OF_SCALES][];

    static {
        ALL_SCALES[ID_HALF_TONE] = HALF_TONE;
        ALL_SCALES[ID_CHROMATIC] = CHROMATIC;
        ALL_SCALES[ID_FIFTH] = FIFTH;
        ALL_SCALES[ID_MINOR] = MINOR;
        ALL_SCALES[ID_MAJOR] = MAJOR;
        ALL_SCALES[ID_CHORD_MINOR] = CHORD_MINOR;
        ALL_SCALES[ID_CHORD_MAJOR] = CHORD_MAJOR;
        ALL_SCALES[ID_CHORD_MINOR_7TH] = CHORD_MINOR_7TH;
        ALL_SCALES[ID_CHORD_MAJOR_7TH] = CHORD_MAJOR_7TH;
        ALL_SCALES[ID_CHORD_MAJOR_7TH] = CHORD_DOMINANT_7;
        ALL_SCALES[ID_MINOR_PENTATONIC] = MINOR_PENTATONIC;
        ALL_SCALES[ID_MAJOR_PENTATONIC] = MAJOR_PENTATONIC;
        ALL_SCALES[ID_OCTAVE] = OCTAVE;
        ALL_SCALES[ID_DIMINISHED_7] = DIMINISHED_7;
        ALL_SCALES[ID_FIBONACCI] = FIBONACCI;
    }

    public static int[] id(int pScaleID) {
        return ALL_SCALES[pScaleID];
    }

    public static int get_note(int[] pScale, int pBaseNote, int pNoteStepOffset) {
        if (pNoteStepOffset >= 0) {
            final int i = pNoteStepOffset % pScale.length;
            final int mOctave = pNoteStepOffset / pScale.length;
            return pBaseNote + mOctave * 12 + pScale[i];
        } else {
            /* @TODO this looks ridiculous */
            final int mOctave = (int) Math.ceil(Math.abs((float) pNoteStepOffset / pScale.length)) - 1;
            final int i = ((pScale.length - 1) - Math.abs((pNoteStepOffset + 1) % pScale.length));
            final int mOffset = 12 - pScale[i];
            return pBaseNote - mOffset - mOctave * 12;
        }
    }
}
