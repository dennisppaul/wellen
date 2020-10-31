package de.hfkbremen.ton.applications;

import de.hfkbremen.ton.Beat;
import de.hfkbremen.ton.Note;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

public class AppSixteenStepSequencer extends PApplet {

    private final int[] mSequence = new int[16];
    private int mBeatCount = 0;
    private Beat mBeat = new Beat(this, 240);

    public void settings() {
        size(1280, 720);
    }

    public void setup() {
        textFont(createFont("Helvetica", 11));
        for (int i = 0; i < mSequence.length; i++) {
            mSequence[i] = Note.NOTE_C3 + i * 3;
        }
    }

    public void draw() {
        background(50);

        /* draw sequencer */
        noStroke();
        for (int i = 0; i < mSequence.length; i++) {
            int mCurrentIndex = mBeatCount % mSequence.length;
            if (i == mCurrentIndex) {
                fill(255, 127, 0);
            } else {
                fill(127);
            }
            float x = 100 + i * 50;
            float y = 100;
            float mSize = 48;
            rect(x, y, mSize, mSize);
            fill(255);
            int mNote = mSequence[i];
            text(mNote, x, y);
        }
    }

    public void keyPressed() {
        if (key == ' ') {
            for (int i = 0; i < mSequence.length; i++) {
                mSequence[i] = (int) random(Note.NOTE_C3, Note.NOTE_C6);
            }
        }
    }

    public void beat(int pBeatCount) {
        mBeatCount = pBeatCount;
        int mIndex = mBeatCount % mSequence.length;
        int mNote = mSequence[mIndex];
        Ton.noteOn(mNote, 127);
    }

    public static void main(String[] args) {
        PApplet.main(AppSixteenStepSequencer.class.getName());
    }
}
