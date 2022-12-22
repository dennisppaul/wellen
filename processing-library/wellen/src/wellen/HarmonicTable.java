package wellen;

/**
 * "The Harmonic Table note-layout, or tonal array, is a key layout for musical instruments that offers interesting
 * advantages over the traditional keyboard layout."
 * <p>
 * {@literal @}REF(https://en.wikipedia.org/wiki/Harmonic_table_note_layout)
 */
public class HarmonicTable {

    public static final int UP = 0;
    public static final int UP_RIGHT = 1;
    public static final int DOWN_RIGHT = 2;
    public static final int DOWN = 3;
    public static final int DOWN_LEFT = 4;
    public static final int UP_LEFT = 5;
    public static final int RIGHT = 6;
    public static final int LEFT = 7;
    private static final int HALF_TONE = 1;
    private static final int MAJOR_THIRD = 4;
    private static final int MINOR_THIRD = 3;
    private static final int PERFECT_FIFTH = 7;
    private int mCurrentNote;

    public int step(int pDirection) {
        final int mStep;
        switch (pDirection) {
            case UP:
                mStep = PERFECT_FIFTH;
                break;
            case UP_RIGHT:
                mStep = MAJOR_THIRD;
                break;
            case DOWN_RIGHT:
                mStep = -MINOR_THIRD;
                break;
            case DOWN:
                mStep = -PERFECT_FIFTH;
                break;
            case DOWN_LEFT:
                mStep = -MAJOR_THIRD;
                break;
            case UP_LEFT:
                mStep = MINOR_THIRD;
                break;
            case RIGHT:
                mStep = HALF_TONE;
                break;
            case LEFT:
                mStep = -HALF_TONE;
                break;
            default:
                mStep = 0;
        }
        mCurrentNote += mStep;
        return mCurrentNote;
    }

    public int get_note() {
        return mCurrentNote;
    }

    public void set_note(int pNote) {
        mCurrentNote = pNote;
    }
}
