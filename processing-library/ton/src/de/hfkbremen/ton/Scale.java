package de.hfkbremen.ton;

public class Scale {

    public static final int ID_HALF_TONE = 0;
    public static final int ID_CHROMATIC = 1;
    public static final int ID_FIFTH = 2;
    public static final int ID_MINOR = 3;
    public static final int ID_MAJOR = 4;
    public static final int ID_MINOR_CHORD = 5;
    public static final int ID_MAJOR_CHORD = 6;
    public static final int ID_MINOR_CHORD_7 = 7;
    public static final int ID_MAJOR_CHORD_7 = 8;
    public static final int ID_MINOR_PENTATONIC = 9;
    public static final int ID_MAJOR_PENTATONIC = 10;
    public static final int ID_OCTAVE = 11;
    public static final int ID_DIMINISHED = 12;
    public static final int ID_FIBONACCI = 13;
    public static final int[] HALF_TONE = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    public static final int[] CHROMATIC = HALF_TONE;
    public static final int[] FIFTH = {0, 7};
    public static final int[] MINOR = {0, 2, 3, 5, 7, 8, 10};
    public static final int[] MAJOR = {0, 2, 4, 5, 7, 9, 11};
    public static final int[] MINOR_CHORD = {0, 3, 7};
    public static final int[] MAJOR_CHORD = {0, 4, 7};
    public static final int[] MINOR_CHORD_7 = {0, 3, 7, 11};
    public static final int[] MAJOR_CHORD_7 = {0, 4, 7, 11};
    public static final int[] MINOR_PENTATONIC = {0, 3, 5, 7, 10};
    public static final int[] MAJOR_PENTATONIC = {0, 4, 5, 7, 11};
    public static final int[] OCTAVE = {0};
    public static final int[] DIMINISHED = {0, 3, 6, 9};
    public static final int[] FIBONACCI = {1 - 1, 1 - 1, 2 - 1, 3 - 1, 5 - 1, 8 - 1};
    private static final int NUMBER_OF_SCALES = 14;
    private static final int[][] ALL_SCALES = new int[NUMBER_OF_SCALES][];

    static {
        ALL_SCALES[ID_HALF_TONE] = HALF_TONE;
        ALL_SCALES[ID_CHROMATIC] = CHROMATIC;
        ALL_SCALES[ID_FIFTH] = FIFTH;
        ALL_SCALES[ID_MINOR] = MINOR;
        ALL_SCALES[ID_MAJOR] = MAJOR;
        ALL_SCALES[ID_MINOR_CHORD] = MINOR_CHORD;
        ALL_SCALES[ID_MAJOR_CHORD] = MAJOR_CHORD;
        ALL_SCALES[ID_MAJOR_CHORD_7] = MAJOR_CHORD_7;
        ALL_SCALES[ID_MINOR_CHORD_7] = MINOR_CHORD_7;
        ALL_SCALES[ID_MINOR_PENTATONIC] = MINOR_PENTATONIC;
        ALL_SCALES[ID_MAJOR_PENTATONIC] = MAJOR_PENTATONIC;
        ALL_SCALES[ID_OCTAVE] = OCTAVE;
        ALL_SCALES[ID_DIMINISHED] = DIMINISHED;
        ALL_SCALES[ID_FIBONACCI] = FIBONACCI;
    }

    public static int[] id(int pScaleID) {
        return ALL_SCALES[pScaleID];
    }

    public static int note(int[] pScale, int pBaseNote, int pNoteStepOffset) {
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
