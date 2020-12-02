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
    int mNote = 45 + (int) random(0, 12);
    Ton.instrument(0);
    Ton.instrument().set_pan(0.0f);
    Ton.note_on(mNote, 80);
    Ton.instrument(1);
    Ton.instrument().set_pan(0.2f);
    Ton.note_on(mNote + 7, 80);
    Ton.instrument(2);
    Ton.instrument().set_pan(-0.2f);
    Ton.note_on(mNote - 5, 80);
}

void mouseReleased() {
    Ton.instrument(0);
    Ton.note_off();
    Ton.instrument(1);
    Ton.note_off();
    Ton.instrument(2);
    Ton.note_off();
}
