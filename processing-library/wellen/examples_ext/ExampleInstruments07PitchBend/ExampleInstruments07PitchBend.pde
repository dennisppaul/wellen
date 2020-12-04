import wellen.*; 
import netP5.*; 
import oscP5.*; 

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
    int mNote = Scale.get_note(Scale.MAJOR_CHORD_7, Note.NOTE_A3, (int) random(0, 10));
    Tone.note_on(mNote, 127);
}

void mouseReleased() {
    Tone.note_off();
    Tone.pitch_bend(8192);
}

void mouseDragged() {
    Tone.pitch_bend((int) map(mouseY, 0, height, 16383, 0));
}
