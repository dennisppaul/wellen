package wellen.tests;

import processing.core.PApplet;
import wellen.Beat;
import wellen.Note;
import wellen.Sequencer;
import wellen.Tone;

public class AppSequenceRecorder extends PApplet {

    private static final int OFF = -1;
    private static final int NO_INPUT = -2;
    private final Sequencer<Integer> mSequence = new Sequencer<Integer>(
            OFF, OFF, OFF, OFF,
            OFF, OFF, OFF, OFF,
            OFF, OFF, OFF, OFF,
            OFF, OFF, OFF, OFF
    );
    private int mLastInput = NO_INPUT;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Beat.start(this, 120 * 2);
        noStroke();
    }

    public void draw() {
        background(255);
        translate(0, height * 0.5f);
        for (int i = 0; i < mSequence.data().length; i++) {
            if (mSequence.get_step() == (i + 1) % mSequence.data().length) {
                fill(0, 31);
                ellipse(i, 45);
            }
            if (mSequence.data()[i] != OFF) {
                fill(mSequence.data()[i] * 9 + 91, 255, 0);
            } else {
                fill(0, 15);
            }
            ellipse(i, 30);
        }
    }

    public void ellipse(int pPosition, float pSize) {
        final float x = map(pPosition, 0, mSequence.data().length - 1, 30, width - 30);
        ellipse(x, 0, pSize, pSize);
    }

    public void keyPressed() {
        switch (key) {
            case ' ':
                mLastInput = OFF;
                break;
            case '1':
                mLastInput = 0;
                break;
            case '2':
                mLastInput = 7;
                break;
            case '3':
                mLastInput = 12;
                break;
            case '4':
                mLastInput = 12 + 7;
                break;
            default:
                mLastInput = NO_INPUT;
        }
        /* record note */
        if (mLastInput != NO_INPUT) {
            mSequence.set_current(mLastInput);
            mLastInput = NO_INPUT;
        }
    }

    public void beat(int pBeat) {
        /* playback next note */
        int mStep = mSequence.step();
        if (mStep != OFF) {
            int mNote = Note.NOTE_C3 + mStep;
            Tone.note_on(mNote, 100);
        } else {
            Tone.note_off();
        }
    }


    public static void main(String[] args) {
        PApplet.main(AppSequenceRecorder.class.getName());
    }
}