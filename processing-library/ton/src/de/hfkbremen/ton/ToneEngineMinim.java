package de.hfkbremen.ton;

import ddf.minim.AudioOutput;
import ddf.minim.Minim;

import java.util.ArrayList;
import java.util.Timer;

import static de.hfkbremen.ton.Note.note_to_frequency;
import static de.hfkbremen.ton.Ton.clamp127;

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
            mInstruments.get(i).amplitude(1.0f);
        }
    }

    public void note_on(int note, int velocity) {
        mIsPlaying = true;
        final float mFreq = note_to_frequency(clamp127(note));
        float mAmp = clamp127(velocity) / 127.0f;
        if (USE_AMP_FRACTION) {
            mAmp /= (float) NUMBERS_OF_INSTRUMENTS;
        }
        if (mInstruments.get(instrument().ID()) instanceof InstrumentMinim) {
            InstrumentMinim mInstrument = (InstrumentMinim) mInstruments.get(instrument().ID());
            mInstrument.note_on(mFreq, mAmp);
        }
    }

    public void note_off(int note) {
        note_off();
    }

    public void note_off() {
        if (mInstruments.get(instrument().ID()) instanceof InstrumentMinim) {
            InstrumentMinim mInstrument = (InstrumentMinim) mInstruments.get(instrument().ID());
            mInstrument.note_off();
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
}
