import ton.*; 
import netP5.*; 
import oscP5.*; 

int mNote;

void settings() {
    size(640, 480);
}

void setup() {
    Ton.start("osc", "127.0.0.1", "7001");
}

void draw() {
    background(Ton.is_playing() ? 255 : 0);
}

void mousePressed() {
    Ton.instrument(mouseX < width / 2.0 ? 0 : 1);
    mNote = 45 + (int) random(0, 12);
    Ton.note_on(mNote, 127);
}

void mouseReleased() {
    Ton.note_off(mNote);
}