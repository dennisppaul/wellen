package wellen.examples.basics;

import processing.core.PApplet;
import wellen.Note;
import wellen.Scale;
import wellen.Tone;

public class ExampleBasics02Scales extends PApplet {

    /*
     * this example demonstrates how to use *musical scales*. a selection of predefined scales is available in `Scale`,
     * however custom scales can also be created.
     */

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
        float mDiameter = map(mNote, Note.NOTE_C4, Note.NOTE_C5, height * 0.1f, height * 0.8f);
        ellipse(width * 0.5f, height * 0.5f, mDiameter, mDiameter);
    }

    public void keyPressed() {
        if (key == ' ') {
            mStep++;
            mStep %= mScale.length + 1;
            mNote = Scale.get_note(mScale, Note.NOTE_C4, mStep);
            Tone.note_on(mNote, 100, 0.25f);
        }
        if (key == '1') {
            mScale = Scale.HALF_TONE;
        }
        if (key == '2') {
            mScale = Scale.CHORD_MINOR_7TH;
        }
        if (key == '3') {
            mScale = new int[]{0, 2, 3, 6, 7, 8, 11}; // Nawa Athar
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleBasics02Scales.class.getName());
    }
}
