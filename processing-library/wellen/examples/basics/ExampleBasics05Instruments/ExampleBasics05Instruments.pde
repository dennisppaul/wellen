import wellen.*; 

/*
 * this example demonstrates how to use *instruments* to play multiple notes at the same time.
 */

void settings() {
    size(640, 480);
}

void setup() {
}

void draw() {
    background(255);
    fill(0);
    ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
}

void mousePressed() {
    int mNote = 45 + (int) random(0, 12);
    Tone.instrument(0);
    Tone.instrument().set_pan(0.0f);
    Tone.note_on(mNote, 80);
    Tone.instrument(1);
    Tone.instrument().set_pan(0.2f);
    Tone.note_on(mNote + 7, 80);
    Tone.instrument(2);
    Tone.instrument().set_pan(-0.2f);
    Tone.note_on(mNote - 5, 80);
}

void mouseReleased() {
    Tone.instrument(0);
    Tone.note_off();
    Tone.instrument(1);
    Tone.note_off();
    Tone.instrument(2);
    Tone.note_off();
}
