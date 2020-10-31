import de.hfkbremen.ton.*; 
import controlP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 


void settings() {
    size(640, 480);
}
void setup() {}
void draw() {
    background(Ton.isPlaying() ? 255 : 0);
}
void mousePressed() {
    int mNote = 45 + (int) random(0, 12);
    Ton.noteOn(mNote, 100);
}
void mouseReleased() {
    Ton.noteOff();
}
