package de.hfkbremen.ton.applications;

import de.hfkbremen.ton.Beat;
import de.hfkbremen.ton.Instrument;
import de.hfkbremen.ton.InstrumentJSynOscillator;
import de.hfkbremen.ton.Note;
import de.hfkbremen.ton.Scale;
import de.hfkbremen.ton.Ton;
import de.hfkbremen.ton.ToneEngineJSyn;
import processing.core.PApplet;

import java.util.ArrayList;

public class AppAlgorithmicComposition04FunctionSineWaves extends PApplet {

    private static final int X = -1;
    private static final int O = -2;
    private static final int INSTRUMENT_BASE = 0;
    private static final int INSTRUMENT_FLUTE = 1;
    private static final int INSTRUMENT_NOISE = 2;
    private final int[] mBaseSequence = {0, O, X, 0,
                                         X, X, 0, X,
                                         X, 0, X, X,
                                         7, O, X, 12};
    private float mTime;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Beat.start(this, 240);
        Ton.instrument(INSTRUMENT_BASE).osc_type(Instrument.TRIANGLE);
        Ton.instrument(INSTRUMENT_FLUTE).osc_type(Instrument.SAWTOOTH);

        ArrayList<Instrument> m = (ArrayList<Instrument>) Ton.instruments();
        m.set(INSTRUMENT_NOISE, new InstrumentJSynOscillator((ToneEngineJSyn) Ton.instance(), INSTRUMENT_NOISE));

        Ton.instrument(INSTRUMENT_NOISE).osc_type(Instrument.NOISE);
        Ton.instrument(INSTRUMENT_NOISE).note_on(1, 127);
        Ton.instrument(INSTRUMENT_NOISE).sustain(1.0f);
        Ton.instrument(INSTRUMENT_NOISE).amplitude(0.0f);
    }

    public void draw() {
        background(255);

        mTime += 1.0f / frameRate;
        playNoise(mTime);
    }

    public void beat(int pBeatCount) {
        playBaseSequence(pBeatCount);
        if (pBeatCount % 2 == 0) {
            playMelody(pBeatCount / 2 - 1, 1.0f);
        } else {
            playMelody(pBeatCount / 2, 0.33f);
        }
    }

    private void playNoise(float pBeatCount) {
        float r = pBeatCount;
        r *= 0.5;
        float mAmplitude = abs(sin(r * r + sin(r * 0.3f) * TWO_PI)) * abs(sin(r / 20.0f));
        Ton.instrument(INSTRUMENT_NOISE).amplitude(map(mAmplitude, 0, 1.0f, 0.001f, 0.03f));
    }

    private void playMelody(int pBeatCount, float pVelocityScale) {
        Ton.instrument(INSTRUMENT_FLUTE);
        int mNote = getNote(pBeatCount);
        int mVelocity = (int) (getVelocity(pBeatCount) * pVelocityScale);
        Ton.note_on(mNote, mVelocity, 0.1f);
    }

    private int getVelocity(int pBeatCount) {
        float r = pBeatCount % 18; /* 18 beats == 1 phase */
        r /= 18.0f;
        r *= TWO_PI;
        float mVelocity = sin(r) * 0.5f + 0.5f;
        mVelocity *= 20;
        mVelocity += 3;
        return (int) mVelocity;
    }

    private int getNote(int pBeatCount) {
        float r = pBeatCount % 32; /* 32 beats == 1 phase */
        r /= 32.0f;
        r *= TWO_PI;
        float mNoteStep = sin(r) * 0.5f + 0.5f;
        mNoteStep *= 5;
        int mNote = Scale.note(Scale.FIFTH, Note.NOTE_C3, (int) mNoteStep);
        return mNote;
    }

    private void playBaseSequence(int pBeatCount) {
        Ton.instrument(INSTRUMENT_BASE);
        int mCounter = pBeatCount % mBaseSequence.length;
        int mStep = mBaseSequence[mCounter];
        if (mStep == X) {
            Ton.note_off();
        } else if (mStep == O) {
            /* do nothing, continue playing note */
        } else {
            int mNote = Scale.note(Scale.HALF_TONE, Note.NOTE_C2, mStep);
            Ton.note_on(mNote, 110);
        }
    }

    public static void main(String[] args) {
        PApplet.main(AppAlgorithmicComposition04FunctionSineWaves.class.getName());
    }
}
