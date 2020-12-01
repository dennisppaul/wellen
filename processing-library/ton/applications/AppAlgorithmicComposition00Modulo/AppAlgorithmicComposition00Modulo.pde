import de.hfkbremen.ton.*; 
import netP5.*; 
import oscP5.*; 

boolean mPlaying = false;

void settings() {
    size(640, 480);
}

void setup() {
    Ton.instrument().set_osc_type(Ton.OSC_TRIANGLE);
    Beat.start(this, 120 * 4);
}

void draw() {
    background(255);
    fill(0);
    float mScale;
    if (mPlaying) {
        mScale = width * 0.1f;
    } else {
        mScale = width * 0.25f;
    }
    ellipse(width * 0.5f, height * 0.5f, mScale, mScale);
}

void beat(int pBeat) {
    mPlaying = true;
    if (pBeat % 32 == 0) {
        Ton.note_on(Note.NOTE_A4, 80);
    } else if (pBeat % 8 == 0) {
        Ton.note_on(Note.NOTE_A3, 110);
    } else if (pBeat % 2 == 0) {
        Ton.note_on(Note.NOTE_A2 + (pBeat % 4) * 3, 120);
    } else if (pBeat % 11 == 0) {
        Ton.note_on(Note.NOTE_C4, 90, 0.05f);
    } else if (pBeat % 13 == 0) {
        Ton.note_on(Note.NOTE_C5, 100, 0.1f);
    } else {
        mPlaying = false;
    }
}
