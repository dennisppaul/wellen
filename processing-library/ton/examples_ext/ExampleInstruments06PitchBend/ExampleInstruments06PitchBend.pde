import de.hfkbremen.ton.*; 
import controlP5.*; 
import netP5.*; 
import oscP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 

void settings() {
    size(640, 480);
}

void setup() {
    background(255);
}

void draw() {
    background(Ton.isPlaying() ? 255 : 0);
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
