package de.hfkbremen.ton.examples;

import de.hfkbremen.ton.Note;
import de.hfkbremen.ton.Scale;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

/**
 * this examples shows how to use musical scales. a selection of predefined scales is available in `Scale`, however
 * custom scales can also be created.
 */
public class SketchExampleBasics02Scales extends PApplet {

    private int mNote;
    private int mStep;
    private int[] mScale;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mScale = Scale.HALF_TONE;
        mNote = Note.NOTE_C4;
        fill(0);
    }

    public void draw() {
        background(255);
        float mScale = map(mNote, Note.NOTE_C4, Note.NOTE_C5, height * 0.1f, height * 0.8f);
        ellipse(width * 0.5f, height * 0.5f, mScale, mScale);
    }

    public void keyPressed() {
        if (key == ' ') {
            mStep++;
            mStep %= mScale.length + 1;
            mNote = Scale.note(mScale, Note.NOTE_C4, mStep);
            Ton.note_on(mNote, 100, 0.25f);
        }
        if (key == '1') {
            mScale = Scale.HALF_TONE;
        }
        if (key == '2') {
            mScale = Scale.MINOR_CHORD_7;
        }
        if (key == '3') {
            mScale = new int[]{0, 2, 3, 6, 7, 8, 11}; // Nawa Athar
        }
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleBasics02Scales.class.getName());
    }
}
