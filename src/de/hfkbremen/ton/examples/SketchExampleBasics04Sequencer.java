package de.hfkbremen.ton.examples;

import de.hfkbremen.ton.Beat;
import de.hfkbremen.ton.Note;
import de.hfkbremen.ton.Scale;
import de.hfkbremen.ton.Sequencer;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

/**
 * this examples shows how to use the sequencer to repeatedly play a pattern..
 */
public class SketchExampleBasics04Sequencer extends PApplet {

    private static final int OFF = -1;

    private final Sequencer<Integer> mSequence = new Sequencer<>(
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
        textFont(createFont("Roboto Mono", 11));
        Beat.start(this, 120 * 4);
    }

    public void draw() {
        background(255);
        noStroke();
        fill(0);
        text(nf(mSequence.current(), 2), 10, 20);
        if (mSequence.current() != OFF) {
            float mScale = (mSequence.current() - 18) / 36.0f + 0.1f;
            ellipse(width * 0.5f, height * 0.5f, width * mScale, width * mScale);
        }
    }

    public void beat(int pBeat) {
        int mStep = mSequence.step();
        if (mStep != OFF) {
            int mNote = Scale.note(Scale.HALF_TONE, Note.NOTE_C4, mStep);
            Ton.noteOn(mNote, 100);
        } else {
            Ton.noteOff();
        }
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleBasics04Sequencer.class.getName());
    }
}
