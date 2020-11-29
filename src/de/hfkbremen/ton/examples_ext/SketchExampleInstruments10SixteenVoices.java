package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.Beat;
import de.hfkbremen.ton.Note;
import de.hfkbremen.ton.Scale;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

public class SketchExampleInstruments10SixteenVoices extends PApplet {

    private int mBeatCount;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Ton.start();
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
        Ton.instrument(mInstrument);
        if (Ton.isPlaying()) {
            Ton.note_off();
        } else {
            final int mVelocity = (int) map(mInstrument, 0, 15, 16, 2);
            Ton.note_on(Scale.note(Scale.MAJOR_CHORD_7, Note.NOTE_C3, mInstrument), mVelocity);
        }
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleInstruments10SixteenVoices.class.getName());
    }
}
