package de.hfkbremen.ton;

import ddf.minim.AudioOutput;
import ddf.minim.Minim;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static de.hfkbremen.ton.Note.note_to_frequency;
import static de.hfkbremen.ton.TonUtil.clamp127;

public class ToneEngineMinim extends ToneEngine {

    private static final boolean USE_AMP_FRACTION = false;
    private final ArrayList<Instrument> mInstruments;
    private final Timer mTimer;
    private int mInstrumentID;
    private boolean mIsPlaying = false;

    public ToneEngineMinim() {
        Minim mMinim = new Minim(this);
        AudioOutput mOut = mMinim.getLineOut(Minim.MONO, 2048);
        mTimer = new Timer();

        mInstruments = new ArrayList<>();
        for (int i = 0; i < NUMBERS_OF_INSTRUMENTS; i++) {
            mInstruments.add(new InstrumentMinim(mMinim, i));
            ((InstrumentMinim) mInstruments.get(i)).amplitude(1.0f);
        }
    }

    public void noteOn(int note, int velocity, float duration) {
        noteOn(note, velocity);
        TimerTask mTask = new NoteOffTask();
        mTimer.schedule(mTask, (long) (duration * 1000));
    }

    public void noteOn(int note, int velocity) {
        mIsPlaying = true;
        final float mFreq = note_to_frequency(clamp127(note));
        float mAmp = clamp127(velocity) / 127.0f;
        if (USE_AMP_FRACTION) {
            mAmp /= (float) NUMBERS_OF_INSTRUMENTS;
        }
        if (mInstruments.get(getInstrumentID()) instanceof InstrumentMinim) {
            InstrumentMinim mInstrument = (InstrumentMinim) mInstruments.get(getInstrumentID());
            mInstrument.noteOn(mFreq, mAmp);
        }
    }

    public void noteOff(int note) {
        noteOff();
    }

    public void noteOff() {
        if (mInstruments.get(getInstrumentID()) instanceof InstrumentMinim) {
            InstrumentMinim mInstrument = (InstrumentMinim) mInstruments.get(getInstrumentID());
            mInstrument.noteOff();
            mIsPlaying = false;
        }
    }

    public void control_change(int pCC, int pValue) {
    }

    public void pitch_bend(int pValue) {
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    public Instrument instrument(int pInstrumentID) {
        mInstrumentID = pInstrumentID;
        return instruments().get(mInstrumentID);
    }

    public Instrument instrument() {
        return instruments().get(mInstrumentID);
    }

    public ArrayList<? extends Instrument> instruments() {
        return mInstruments;
    }

    private int getInstrumentID() {
        return Math.max(mInstrumentID, 0) % mInstruments.size();
    }

    public class NoteOffTask extends TimerTask {

        public void run() {
            noteOff();
        }
    }
}
