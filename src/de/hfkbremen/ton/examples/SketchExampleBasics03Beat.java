package de.hfkbremen.ton.examples;

import de.hfkbremen.ton.Beat;
import de.hfkbremen.ton.Note;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

/**
 * this examples shows how to use a beat. once instantiated the beat object calls the `beat(int)` at a defined *beats
 * per minute* (bpm).
 */
public class SketchExampleBasics03Beat extends PApplet {

    private final int[] mNotes = {Note.NOTE_C3, Note.NOTE_C4, Note.NOTE_A2, Note.NOTE_A3};
    private int mBeatCount;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Beat.start(this, 120);
    }

    public void draw() {
        background(255);
        noStroke();
        fill(0);
        float mScale = (mBeatCount % 2) * 0.25f + 0.25f;
        ellipse(width * 0.5f, height * 0.5f, width * mScale, width * mScale);
    }

    public void beat(int pBeatCount) {
        mBeatCount = pBeatCount;
        int mNote = mNotes[mBeatCount % mNotes.length];
        Ton.noteOn(mNote, 100);
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleBasics03Beat.class.getName());
    }
}
