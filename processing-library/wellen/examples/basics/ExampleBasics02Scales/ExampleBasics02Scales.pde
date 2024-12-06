import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to use *musical scales*. a selection of predefined scales is available in `Scale`,
 * however custom scales can also be created.
 */
int   mNote;
int[] mScale;
int   mStep;
void settings() {
    size(640, 480);
}
void setup() {
    Tone.preset(Wellen.INSTRUMENT_PRESET_SUB_SINE);
    mScale = Scale.CHORD_MINOR_7TH;
    mNote  = Note.NOTE_C4;
    fill(0);
}
void draw() {
    background(255);
    float mDiameter = map(mNote, Note.NOTE_C3, Note.NOTE_C4, height * 0.1f, height * 0.8f);
    ellipse(width * 0.5f, height * 0.5f, mDiameter, mDiameter);
}
void keyPressed() {
    if (key == ' ') {
        mStep++;
        mStep %= mScale.length + 1;
        mNote = Scale.get_note(mScale, Note.NOTE_C3, mStep);
        /*
         * note that this variant of `note_on` takes three parameters where the third parameter defines the
         * duration of the note in seconds. a `note_off` is automatically triggered after the duration.
         */
        Tone.note_on(mNote, 100, 0.25f);
    }
    if (key == '1') {
        mScale = Scale.HALF_TONE;
    }
    if (key == '2') {
        mScale = Scale.CHORD_MINOR_7TH;
    }
    if (key == '3') {
        mScale = Scale.MINOR_PENTATONIC;
    }
    if (key == '4') {
        mScale = new int[]{0, 2, 3, 6, 7, 8, 11}; // Nawa Athar
    }
}
    // Try retrieving the native window ID
    long windowID = Native.getWindowID(frame);
    System.out.println("Native Window ID: " + windowID);
    if (windowID == 0) {
        throw new IllegalStateException("Failed to retrieve native window ID.");
    }
    PApplet.main(ExampleBasics02Scales.class.getName());
}
