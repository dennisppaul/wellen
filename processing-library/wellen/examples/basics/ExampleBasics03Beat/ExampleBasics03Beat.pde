import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to use a *beat*. once started the method `beat(int)` is called at a speed defined
 * in *beats per minute* (bpm) ( i.e quarter notes per minute ).
 */

int mBeatCount;

final int[] mNotes = {Note.NOTE_C3, Note.NOTE_C4, Note.NOTE_A2, Note.NOTE_A3};

void settings() {
    size(640, 480);
}

void setup() {
    /* it is advised to start the beat at the very end of the `setup` method */
    Beat.start(this, 120);
}

void draw() {
    background(255);
    noStroke();
    fill(0);
    float mScale = (mBeatCount % 2) * 0.25f + 0.25f;
    ellipse(width * 0.5f, height * 0.5f, width * mScale, width * mScale);
}

void beat(int beatCount) {
    mBeatCount = beatCount;
    int mNote = mNotes[mBeatCount % mNotes.length];
    Tone.note_on(mNote, 100);
}
