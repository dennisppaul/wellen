package welle.examples_ext;

import processing.core.PApplet;
import welle.Beat;
import welle.Note;
import welle.Scale;
import welle.Tone;

/**
 * this examples demonstrates how to play all 16 instruments at the same time.
 */
public class ExampleInstruments06SixteenVoices extends PApplet {

    private int mBeatCount;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Tone.start();
        for (int i = 0; i < Tone.instruments().size(); i++) {
            final float mPan = 2.0f * i / Tone.instruments().size() - 1.0f;
            Tone.instruments().get(i).set_pan(mPan);
        }
        Beat.start(this, 120 * 3);
    }

    public void draw() {
        background(255);
        noStroke();
        fill(0);
        float mScale = (mBeatCount % 32) * 0.025f + 0.25f;
        ellipse(width * 0.5f, height * 0.5f, width * mScale, width * mScale);
    }

    public void beat(int pBeatCount) {
        mBeatCount = pBeatCount;
        int mInstrument = 15 - pBeatCount % 16;
        Tone.instrument(mInstrument);
        if (Tone.is_playing()) {
            Tone.note_off();
        } else {
            final int mVelocity = (int) map(mInstrument, 0, 15, 16, 2);
            Tone.note_on(Scale.get_note(Scale.MAJOR_CHORD_7, Note.NOTE_C3, mInstrument), mVelocity);
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleInstruments06SixteenVoices.class.getName());
    }
}
