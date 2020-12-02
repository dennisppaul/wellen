package ton.examples_ext;

import ton.BeatMIDI;
import ton.Ton;
import processing.core.PApplet;

/**
 * this examples shows how to use a beat triggered by an external MIDI beat clock.
 */
public class ExampleExternal02MIDIClock extends PApplet {

    private int mColor;
    private BeatMIDI mBeatMIDI;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Ton.dumpMidiInputDevices();
        mBeatMIDI = BeatMIDI.start(this, "Arturia KeyStep 37");
    }

    public void draw() {
        background(mBeatMIDI.running() ? mColor : 0);
    }

    public void beat(int pBeat) {
        /* MIDI clock runs at 24 pulses per quarter note (PPQ). `pBeat % 12` is there for 0 every eigth note. */
        if (pBeat % 12 == 6) {
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

    public static void main(String[] args) {
        PApplet.main(ExampleExternal02MIDIClock.class.getName());
    }
}