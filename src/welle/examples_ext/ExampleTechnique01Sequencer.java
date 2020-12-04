package welle.examples_ext;

import processing.core.PApplet;
import welle.Beat;
import welle.Note;
import welle.Scale;
import welle.Sequencer;
import welle.Tone;

/**
 * this example demonstrates how to use `Sequencer` to repeatedly play a predefined pattern of notes.
 */
public class ExampleTechnique01Sequencer extends PApplet {

    private static final int OFF = -1;

    private final Sequencer<Integer> mSequence = new Sequencer<Integer>(
            0, OFF, 12, OFF,
            0, OFF, 12, OFF,
            0, OFF, 12, OFF,
            0, OFF, 12, OFF,
            3, 3, 15, 15,
            3, 3, 15, 15,
            5, 5, 17, 17,
            5, 5, 17, 17
    );

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Beat.start(this, 120 * 4);
    }

    public void draw() {
        background(255);
        noStroke();
        fill(0);
        if (mSequence.current() != OFF) {
            float mNote = (mSequence.current() - 18) / 36.0f + 0.1f;
            ellipse(width * 0.5f, height * 0.5f, width * mNote, width * mNote);
        }
    }

    public void beat(int pBeat) {
        int mStep = mSequence.step();
        if (mStep != OFF) {
            int mNote = Scale.note(Scale.HALF_TONE, Note.NOTE_C4, mStep);
            Tone.note_on(mNote, 100);
        } else {
            Tone.note_off();
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleTechnique01Sequencer.class.getName());
    }
}
