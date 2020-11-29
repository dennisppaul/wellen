package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.Instrument;
import de.hfkbremen.ton.Note;
import de.hfkbremen.ton.Scale;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

/**
 * this examples shows how to use different oscillators in an instrument. note that this only works with some tone
 * engines e.g `jsyn` or `minim`
 */
public class SketchExampleInstruments02Oscillators extends PApplet {

    private Instrument mInstrument;
    private boolean mIsPlaying = false;
    private int mNote;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        background(255);

        /* select instrument #2 */
        mInstrument = Ton.instrument(2);
    }

    public void draw() {
        background(255);
        fill(0);
        float mSize = map(mNote, 33, 69, 80, 320);
        ellipse(width * 0.5f, height * 0.5f, Ton.isPlaying() ? mSize : 5, Ton.isPlaying() ? mSize : 5);
    }

    public void keyPressed() {
        if (key == ' ') {
            if (mIsPlaying) {
                Ton.note_off();
            } else {
                mNote = Scale.note(Scale.MAJOR_CHORD_7, Note.NOTE_A2, (int) random(0, 10));
                Ton.note_on(mNote, 127);
            }
            mIsPlaying = !mIsPlaying;
        }
        if (key == '1') {
            mInstrument.osc_type(Ton.OSC_SINE);
        }
        if (key == '2') {
            mInstrument.osc_type(Ton.OSC_TRIANGLE);
        }
        if (key == '3') {
            mInstrument.osc_type(Ton.OSC_SAWTOOTH);
        }
        if (key == '4') {
            mInstrument.osc_type(Ton.OSC_SQUARE);
        }
        if (key == '5') {
            mInstrument.osc_type(Ton.OSC_NOISE);
        }
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleInstruments02Oscillators.class.getName());
    }
}
