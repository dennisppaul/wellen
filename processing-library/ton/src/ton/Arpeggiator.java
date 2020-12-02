package ton;

import java.util.Arrays;

import static ton.MIDI.PPQN;

public class Arpeggiator {

// @TODO(add a `note_off` mechanism)

    private final NoteStruct[] mPattern;
    private NoteStruct mCurrentNote;
    private int mStep;
    private int mBaseNote;
    private float mBaseVelocity;

    public Arpeggiator(int pLengthInQuarterNotes) {
        mPattern = new NoteStruct[pLengthInQuarterNotes];
        Arrays.fill(mPattern, new NoteStruct());
        reset();
    }

    public boolean step() {
        if (mStep < mPattern.length) {
            mCurrentNote = mPattern[mStep];
            mStep++;
            return trigger();
        } else {
            return false;
        }
    }

    public boolean trigger() {
        return mCurrentNote != null && mCurrentNote.active;
    }

    public int note() {
        return mCurrentNote != null ? mCurrentNote.note + mBaseNote : 0;
    }

    public int velocity() {
        return mCurrentNote != null ? (int) (mCurrentNote.velocity * mBaseVelocity) : 0;
    }

    public void reset() {
        mStep = 0;
        mCurrentNote = null;
    }

    public void play(int pNote, int pVelocity) {
        mBaseNote = pNote;
        mBaseVelocity = pVelocity;
        reset();
    }

    public boolean pattern(int pPosition, int pNote, float pVelocity) {
        return set(mPattern, pPosition, pNote, pVelocity);
    }

    public boolean pattern(int pPosition, int pScaler, int pNote, float pVelocity) {
        return set(mPattern, pPosition, pScaler, pNote, pVelocity);
    }

    public boolean pattern_4(int pPosition, int pNote, float pVelocity) {
        return set(mPattern, pPosition, PPQN, pNote, pVelocity);
    }

    public boolean pattern_8(int pPosition, int pNote, float pVelocity) {
        return set(mPattern, pPosition, PPQN / 2, pNote, pVelocity);
    }

    public boolean pattern_16(int pPosition, int pNote, float pVelocity) {
        return set(mPattern, pPosition, PPQN / 4, pNote, pVelocity);
    }

    public boolean pattern_32(int pPosition, int pNote, float pVelocity) {
        return set(mPattern, pPosition, PPQN / 8, pNote, pVelocity);
    }

    public static boolean set(NoteStruct[] pPattern, int pPosition, int pScaler, int pNote, float pVelocity) {
        return set(pPattern, pPosition * pScaler, pNote, pVelocity);
    }

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

    private static class NoteStruct {
        boolean active;
        int note;
        float velocity;

        public NoteStruct() {
            this(false, 0, 0.0f);
        }

        public NoteStruct(boolean pActive, int pNote, float pVelocity) {
            active = pActive;
            note = pNote;
            velocity = pVelocity;
        }

        public NoteStruct copy() {
            return new NoteStruct(active, note, velocity);
        }
    }
}
