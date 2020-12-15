import wellen.*; 

/*
 * this example demonstrates how to play *musical notes*.
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
    Tone.note_on(mNote, 100);
}

void mouseReleased() {
    Tone.note_off();
}
