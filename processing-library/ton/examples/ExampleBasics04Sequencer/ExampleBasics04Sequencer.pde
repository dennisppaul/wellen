import de.hfkbremen.ton.*; 
import controlP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 


static final int OFF = -1;
final Sequencer<Integer> mSequence = new Sequencer<>(
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
    textFont(createFont("Roboto Mono", 11));
    Beat.start(this, 120 * 4);
}
void draw() {
    background(255);
    noStroke();
    fill(0);
    text(nf(mSequence.current(), 2), 10, 20);
    if (mSequence.current() != OFF) {
        float mScale = (mSequence.current() - 18) / 36.0f + 0.1f;
        ellipse(width * 0.5f, height * 0.5f, width * mScale, width * mScale);
    }
}
void beat(int pBeat) {
    int mStep = mSequence.step();
    if (mStep != OFF) {
        int mNote = Scale.note(Scale.HALF_TONE, Note.NOTE_C4, mStep);
        Ton.noteOn(mNote, 100);
    } else {
        Ton.noteOff();
    }
}
