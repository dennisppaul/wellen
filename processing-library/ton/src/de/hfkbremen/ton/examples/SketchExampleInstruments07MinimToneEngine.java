package de.hfkbremen.ton.examples;

import de.hfkbremen.ton.Instrument;
import de.hfkbremen.ton.Note;
import de.hfkbremen.ton.Scale;
import de.hfkbremen.ton.ToneEngineMinim;
import processing.core.PApplet;

/**
 * this examples shows how to use the minim library for sound synthesis ( note that the basic concept of sound synthesis
 * is very similar to the jsyn library ) note: if running this sketch from processing PDE add `import ddf.minim.*;` to
 * the imports.
 */
public class SketchExampleInstruments07MinimToneEngine extends PApplet {

    private ToneEngineMinim mToneEngine;

    private Instrument mInstrument;

    private boolean mIsPlaying = false;

    private int mNote;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        background(255);
        mToneEngine = new ToneEngineMinim();

        /* select instrument #2 */
        mToneEngine.instrument(2);

        /* set ADSR parameters for current instrument */
        mInstrument = mToneEngine.instrument();
        mInstrument.attack(0.5f);
        mInstrument.decay(1.0f);
        mInstrument.sustain(1.0f);
        mInstrument.release(0.5f);
    }

    public void draw() {
        if (mIsPlaying) {
            int mColor = (mNote - Note.NOTE_A2) * 5 + 50;
            background(mColor);
        } else {
            background(255);
        }

        /* set get_attack for current instrument */
        final float mAttack = (float) mouseX / width;
        mInstrument.attack(mAttack);
    }

    public void keyPressed() {
        if (key == ' ') {
            if (mIsPlaying) {
                mToneEngine.noteOff();
            } else {
                mNote = Scale.note(Scale.MAJOR_CHORD_7, Note.NOTE_A2, (int) random(0, 10));
                mToneEngine.noteOn(mNote, 127);
            }
            mIsPlaying = !mIsPlaying;
        }
        if (key == '1') {
            mInstrument.osc_type(Instrument.SINE);
        }
        if (key == '2') {
            mInstrument.osc_type(Instrument.TRIANGLE);
        }
        if (key == '3') {
            mInstrument.osc_type(Instrument.SAWTOOTH);
        }
        if (key == '4') {
            mInstrument.osc_type(Instrument.SQUARE);
        }
        if (key == '5') {
            mInstrument.osc_type(Instrument.NOISE);
        }
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleInstruments07MinimToneEngine.class.getName());
    }
}
