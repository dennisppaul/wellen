import wellen.*; 

/*
 * this example demonstrates how to adjust the volume of tone output.
 */

final Gain mMasterVolume = new Gain();

void settings() {
    size(640, 480);
}

void setup() {
    ToneEngineInternal mToneEngine = Tone.get_internal_engine();
    mToneEngine.add_effect(mMasterVolume);
    mMasterVolume.set_gain(1.5f);
}

void draw() {
    background(255);
    fill(0);
    ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
}

void mouseMoved() {
    mMasterVolume.set_gain(map(mouseY, 0, height, 3.0f, 0.0f));
}

void mousePressed() {
    int mNote = 45 + (int) random(0, 12);
    Tone.note_on(mNote, 50);
}

void mouseReleased() {
    Tone.note_off();
}
