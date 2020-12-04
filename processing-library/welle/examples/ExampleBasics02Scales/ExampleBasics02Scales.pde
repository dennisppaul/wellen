import welle.*; 
import netP5.*; 
import oscP5.*; 

int mNote;

int mStep;

int[] mScale;

void settings() {
    size(640, 480);
}

void setup() {
    mScale = Scale.HALF_TONE;
    mNote = Note.NOTE_C4;
    fill(0);
}

void draw() {
    background(255);
    float mScale = map(mNote, Note.NOTE_C4, Note.NOTE_C5, height * 0.1f, height * 0.8f);
    ellipse(width * 0.5f, height * 0.5f, mScale, mScale);
}

void keyPressed() {
    if (key == ' ') {
        mStep++;
        mStep %= mScale.length + 1;
        mNote = Scale.note(mScale, Note.NOTE_C4, mStep);
        Tone.note_on(mNote, 100, 0.25f);
    }
    if (key == '1') {
        mScale = Scale.HALF_TONE;
    }
    if (key == '2') {
        mScale = Scale.MINOR_CHORD_7;
    }
    if (key == '3') {
        mScale = new int[]{0, 2, 3, 6, 7, 8, 11}; // Nawa Athar
    }
}
