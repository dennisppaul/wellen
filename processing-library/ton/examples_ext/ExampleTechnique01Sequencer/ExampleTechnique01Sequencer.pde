import de.hfkbremen.ton.*; 
import netP5.*; 
import oscP5.*; 

static final int OFF = -1;

final Sequencer<Integer> mSequence = new Sequencer<Integer>(
        0, OFF, 12, OFF,
        0, OFF, 12, OFF,
        0, OFF, 12, OFF,
        0, OFF, 12, OFF,
        3, 3, 15, 15,
        3, 3, 15, 15,
        5, 5, 17, 17,
        5, 5, 17, 17
);

void settings() {
    size(640, 480);
}

void setup() {
    Beat.start(this, 120 * 4);
}

void draw() {
    background(255);
    noStroke();
    fill(0);
    if (mSequence.current() != OFF) {
        float mNote = (mSequence.current() - 18) / 36.0f + 0.1f;
        ellipse(width * 0.5f, height * 0.5f, width * mNote, width * mNote);
    }
}

void beat(int pBeat) {
    int mStep = mSequence.step();
    if (mStep != OFF) {
        int mNote = Scale.note(Scale.HALF_TONE, Note.NOTE_C4, mStep);
        Ton.note_on(mNote, 100);
    } else {
        Ton.note_off();
    }
}
