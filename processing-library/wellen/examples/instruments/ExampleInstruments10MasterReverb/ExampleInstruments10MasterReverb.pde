import wellen.*; 

/*
 * this example demonstrates how to add a reverb effect to tone output.
 */

void settings() {
    size(640, 480);
}

void setup() {
    Tone.get_internal_engine().enable_reverb(true);
    Tone.get_internal_engine().get_reverb().set_roomsize(0.9f);
}

void draw() {
    background(255);
    fill(0);
    ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
}

void mousePressed() {
    int mNote = 45 + (int) random(0, 12);
    Tone.note_on(mNote, 100);
}

void mouseReleased() {
    Tone.note_off();
}
