import wellen.*; 
import netP5.*; 
import oscP5.*; 

final int[] mNotes = {Note.NOTE_C3, Note.NOTE_C4, Note.NOTE_A2, Note.NOTE_A3};

int mBeatCount;

BeatDSP mBeat;

float mSignal;

void settings() {
    size(640, 480);
}

void setup() {
    Tone.start();
    mBeat = BeatDSP.start(this); /* create beat before `DSP.start` */
    DSP.start(this); /* DSP is only used to create beat events */
}

void draw() {
    background(255);
    fill(0);
    ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
}

void mouseMoved() {
    mBeat.set_bpm(map(mouseX, 0, width, 1, 480));
}

void audioblock(float[] pOutputSamples) {
    for (int i = 0; i < pOutputSamples.length; i++) {
        mBeat.tick();
    }
}

void beat(int pBeatCount) {
    int mNote = 45 + (int) random(0, 12);
    Tone.note_on(mNote, 100, 0.1f);
}
