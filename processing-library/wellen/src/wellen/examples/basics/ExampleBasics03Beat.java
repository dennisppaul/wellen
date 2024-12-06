package wellen.examples.basics;

import processing.core.PApplet;
import wellen.Beat;
import wellen.Note;
import wellen.Tone;
import wellen.Wellen;

public class ExampleBasics03Beat extends PApplet {

    /*
     * this example demonstrates how to use a *beat*. once started the method `beat(int)` is called at a speed defined
     * in *beats per minute* (bpm) ( i.e quarter notes per minute ).
     */

    private       int   mBeatCount;
    /* define a bass line of 8 notes */
    private final int[] mNotes = {Note.NOTE_C3, Note.NOTE_C4,
                                  Note.NOTE_A2, Note.NOTE_A3,
                                  Note.NOTE_C3 + 3, Note.NOTE_C4 + 3,
                                  Note.NOTE_C3 + 2, Note.NOTE_C4 + 2};

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        noStroke();
        fill(0);

        Tone.preset(Wellen.INSTRUMENT_PRESET_FAT);

        /* it is advised to start the beat at the very end of the `setup` method */
        Beat.start(this, 160);
    }

    public void draw() {
        background(255);
        float mScale = (mBeatCount % 2) * 0.25f + 0.25f;
        ellipse(width * 0.5f, height * 0.5f, width * mScale, width * mScale);
    }

    public void beat(int beatCount) {
        mBeatCount = beatCount;
        int mNote = mNotes[mBeatCount % mNotes.length]; /* loop through notes */
        Tone.note_on(mNote, 100, 0.25f);
    }

    public static void main(String[] args) {
        PApplet.main(ExampleBasics03Beat.class.getName());
    }
}
