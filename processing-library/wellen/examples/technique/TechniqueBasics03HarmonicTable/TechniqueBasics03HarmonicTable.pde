import wellen.*; 
import wellen.dsp.*; 


int mStep;

final HarmonicTable mHarmonicTable = new HarmonicTable();

int mBaseNote;

void settings() {
    size(640, 480);
}

void setup() {
    mBaseNote = Note.NOTE_C4;
    mStep = HarmonicTable.UP_RIGHT;
    Beat.start(this, 240);
}

void draw() {
    background(255);
    fill(0);
    ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
    drawStepDirection();
}

void beat(int pBeatCount) {
    mHarmonicTable.set_note(mBaseNote);
    Tone.instrument(0);
    mBaseNote = playNextNote(mStep);
    Tone.instrument(1);
    playNextNote(HarmonicTable.UP);
    Tone.instrument(2);
    playNextNote(HarmonicTable.DOWN_RIGHT);
    if (mBaseNote > Note.NOTE_C6 || mBaseNote < Note.NOTE_C2) {
        mBaseNote = Note.NOTE_C4;
    }
}

void mouseMoved() {
    PVector mMouse = PVector.sub(new PVector(mouseX, mouseY), new PVector(width * 0.5f, height * 0.5f));
    float mAngle = atan2(mMouse.y, mMouse.x) + PI;
    mAngle /= TWO_PI;
    mAngle += 5.0f / 6.0f;
    mAngle %= 1.0f;
    mAngle *= 6;
    mStep = floor(mAngle);
}

void drawStepDirection() {
    float mAngle = mStep / 6.0f * TWO_PI;
    mAngle -= PI * 0.5f;
    PVector p = PVector.fromAngle(mAngle);
    p.mult(55);
    noFill();
    stroke(0);
    line(width * 0.5f, height * 0.5f, width * 0.5f + p.x, height * 0.5f + p.y);
}

int playNextNote(int pDirection) {
    int mNote = mHarmonicTable.step(pDirection);
    Tone.note_on(mNote, 70, 0.1f);
    return mNote;
}
