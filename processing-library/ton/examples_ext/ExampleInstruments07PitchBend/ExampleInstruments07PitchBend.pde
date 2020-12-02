import ton.*; 
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
    ellipse(width * 0.5f, height * 0.5f, Ton.is_playing() ? 100 : 5, Ton.is_playing() ? 100 : 5);
}

void mousePressed() {
    int mNote = Scale.note(Scale.MAJOR_CHORD_7, Note.NOTE_A3, (int) random(0, 10));
    Ton.note_on(mNote, 127);
}

void mouseReleased() {
    Ton.note_off();
    Ton.pitch_bend(8192);
}

void mouseDragged() {
    Ton.pitch_bend((int) map(mouseY, 0, height, 16383, 0));
}
