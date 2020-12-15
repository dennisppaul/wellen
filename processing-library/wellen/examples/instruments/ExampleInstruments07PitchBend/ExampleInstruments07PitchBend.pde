import wellen.*; 

/*
 * this example shows how use pitch bending i.e offsetting the note’s frequency by a fraction of its frequency.
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
    Tone.note_on(Note.NOTE_A4, 127);
}

void mouseReleased() {
    Tone.note_off();
    Tone.pitch_bend(8192);
}

void mouseDragged() {
    Tone.pitch_bend((int) map(mouseY, 0, height, 16383, 0));
}
