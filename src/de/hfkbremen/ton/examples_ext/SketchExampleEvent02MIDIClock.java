package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.BeatMIDI;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;
/**
 * this examples demonstrates
 */

public class SketchExampleEvent02MIDIClock extends PApplet {

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

    public static void main(String[] args) {
        PApplet.main(SketchExampleEvent02MIDIClock.class.getName());
    }
}