package wellen.tests;

import processing.core.PApplet;
import wellen.Beat;
import wellen.Note;
import wellen.Tone;

public class AppStepSequencerRandom extends PApplet {

    private static final int OFF = -1;
    private final int[] mSequence = new int[16];
    private int mBeatCount = 0;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        textFont(createFont("Roboto Mono", 14));
        textAlign(CENTER);
        noStroke();
        populateSequence();
        Beat.start(this, 120 * 4);
    }

    public void draw() {
        background(255);
        for (int i = 0; i < mSequence.length; i++) {
            int mCurrentIndex = mBeatCount % mSequence.length;
            if (i == mCurrentIndex) {
                fill(0);
            } else {
                fill(191);
            }
            float mSize = 34.56f;
            float x = mSize + i * (mSize + 1);
            float y = (height - mSize) * 0.5f;
            rect(x, y, mSize, mSize);
            fill(255);
            int mNote = mSequence[i];
            text(mNote, x + mSize * 0.5f, y + mSize * 0.5f + 5);
        }
    }

    public void keyPressed() {
        if (key == ' ') {
            populateSequence();
        }
    }

    public void beat(int pBeatCount) {
        mBeatCount = pBeatCount;
        int mIndex = mBeatCount % mSequence.length;
        int mNote = mSequence[mIndex];
        if (mNote != OFF) {
            Tone.note_on(mNote, 100);
        } else {
            Tone.note_off();
        }
    }

    private void populateSequence() {
        for (int i = 0; i < mSequence.length; i++) {
            if (random(1) > 0.5f) {
                mSequence[i] = OFF;
            } else {
                mSequence[i] = (int) random(Note.NOTE_C3,
                        Note.NOTE_C6);
            }
        }
    }

    public static void main(String[] args) {
        PApplet.main(AppStepSequencerRandom.class.getName());
    }
}
