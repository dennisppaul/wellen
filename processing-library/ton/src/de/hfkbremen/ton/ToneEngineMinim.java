package de.hfkbremen.ton;

import ddf.minim.AudioOutput;
import ddf.minim.Minim;

import java.util.ArrayList;
import java.util.Timer;

public class ToneEngineMinim extends ToneEngine {

    private static final boolean USE_AMP_FRACTION = false;
    private final ArrayList<Instrument> mInstruments;
    private final Timer mTimer;
    private int mInstrumentID;
    private boolean mIsPlaying = false;
    private final Minim mMinim;

    public ToneEngineMinim() {
        mMinim = new Minim(this);
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
        if (USE_AMP_FRACTION) {
            velocity /= NUMBERS_OF_INSTRUMENTS;
        }
        if (mInstruments.get(instrument().ID()) instanceof InstrumentMinim) {
            InstrumentMinim mInstrument = (InstrumentMinim) mInstruments.get(instrument().ID());
            mInstrument.note_on(note, velocity);
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

    @Override
    public void replace_instrument(Instrument pInstrument) {
        mInstruments.set(pInstrument.ID(), pInstrument);
    }

    public Minim minim() {
        return mMinim;
    }
}
