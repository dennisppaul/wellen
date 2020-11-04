import de.hfkbremen.ton.*; 
import controlP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 


int mColor;
BeatMIDI mBeatMIDI;
void settings() {
    size(640, 480);
}
void setup() {
    Ton.dumpMidiInputDevices();
    mBeatMIDI = BeatMIDI.start(this, "Bus 1");
}
void draw() {
    background(mBeatMIDI.running() ? mColor : 0);
}
void beat(int pBeat) {
    if (pBeat % 24 == 0) {
        System.out.println("beat");
        mColor = color(random(127, 255),
                random(127, 255),
                random(127, 255));
    }
}
