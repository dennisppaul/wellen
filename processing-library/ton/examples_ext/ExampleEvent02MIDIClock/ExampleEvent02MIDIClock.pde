			 import de.hfkbremen.ton.*; 
import controlP5.*; 
import netP5.*; 
import oscP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 

			 
		int mColor;
BeatMIDI mBeatMIDI;
void settings() {
    size(640, 480);
}
void setup() {
    Ton.dumpMidiInputDevices();
    mBeatMIDI = BeatMIDI.start(this, "Arturia KeyStep 37");
}
void draw() {
    background(mBeatMIDI.running() ? mColor : 0);
}
void beat(int pBeat) {
    /* MIDI clock runs at 24 pulses per quarter note (PPQ). `pBeat % 12` is there for 0 every eigth note. */
    if (pBeat % 12 == 0) {
        mColor = color(random(127, 255),
                random(127, 255),
                random(127, 255));
        int mOffset = 4 * ((pBeat / 24) % 8);
        Ton.note_on(36 + mOffset, 90);
        System.out.println(mBeatMIDI.bpm());
    } else {
        Ton.note_off();
    }
}
