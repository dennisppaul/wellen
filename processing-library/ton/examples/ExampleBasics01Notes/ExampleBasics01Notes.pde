import de.hfkbremen.ton.*; 
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
    ellipse(width * 0.5f, height * 0.5f, Ton.isPlaying() ? 100 : 5, Ton.isPlaying() ? 100 : 5);
}

void mousePressed() {
    int mNote = 45 + (int) random(0, 12);
    Ton.note_on(mNote, 100);
}

void mouseReleased() {
    Ton.note_off();
}
