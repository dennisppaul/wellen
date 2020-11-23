import de.hfkbremen.ton.*; 
import controlP5.*; 
import netP5.*; 
import oscP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 

int mBeatCount;

void settings() {
    size(640, 480);
}

void setup() {
    Beat.start(this, 120 * 3);
}

void draw() {
    background(255);
    noStroke();
    fill(0);
    float mScale = (mBeatCount % 2) * 0.25f + 0.25f;
    ellipse(width * 0.5f, height * 0.5f, width * mScale, width * mScale);
}

void beat(int pBeatCount) {
    int mInstrument = 15 - pBeatCount % 16;
    Ton.instrument(mInstrument);
    if (Ton.isPlaying()) {
        Ton.note_off();
    } else {
        final int mVelocity = (int) map(mInstrument, 0, 15, 16, 2);
        Ton.note_on(Scale.note(Scale.MAJOR_CHORD_7, Note.NOTE_C3, mInstrument), mVelocity);
    }
}
