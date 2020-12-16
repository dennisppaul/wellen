import wellen.*; 

/*
 * this example demonstrates how to use multiple tone engines ( i.e midi + internal ) at the same time.
 */

ToneEngineInternal mToneEngineInternal;

ToneEngineMIDI mToneEngineMIDI;

void settings() {
    size(640, 480);
}

void setup() {
    Wellen.dumpMidiOutputDevices();
    /* when working with multiple engines the use of `Tone.start(...)` is discouraged. */
    mToneEngineInternal = new ToneEngineInternal();
    mToneEngineMIDI = new ToneEngineMIDI("Bus 1");
}

void draw() {
    background(255);
    fill(0);
    ellipse(width * 0.33f,
            height * 0.5f,
            mToneEngineInternal.is_playing() ? 100 : 5,
            mToneEngineInternal.is_playing() ? 100 : 5);
    ellipse(width * 0.66f,
            height * 0.5f,
            mToneEngineMIDI.is_playing() ? 100 : 5,
            mToneEngineMIDI.is_playing() ? 100 : 5);
}

void mousePressed() {
    int mNote = 45 + (int) random(0, 12);
    mToneEngineInternal.note_on(mNote, 100);
    mToneEngineMIDI.note_on(mNote, 100);
}

void mouseReleased() {
    mToneEngineInternal.note_off();
    mToneEngineMIDI.note_off();
}
