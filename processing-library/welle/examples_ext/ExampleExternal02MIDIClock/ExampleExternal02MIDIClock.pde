import welle.*; 
import netP5.*; 
import oscP5.*; 

int mColor;

BeatMIDI mBeatMIDI;

void settings() {
    size(640, 480);
}

void setup() {
    Welle.dumpMidiInputDevices();
    mBeatMIDI = BeatMIDI.start(this, "Arturia KeyStep 37");
}

void draw() {
    background(mBeatMIDI.running() ? mColor : 0);
}

void beat(int pBeat) {
    /* MIDI clock runs at 24 pulses per quarter note (PPQ). `pBeat % 12` is there for 0 every eigth note. */
    if (pBeat % 12 == 6) {
        mColor = color(random(127, 255),
                random(127, 255),
                random(127, 255));
        int mOffset = 4 * ((pBeat / 24) % 8);
        Tone.note_on(36 + mOffset, 90);
        System.out.println(mBeatMIDI.bpm());
    } else {
        Tone.note_off();
    }
}
