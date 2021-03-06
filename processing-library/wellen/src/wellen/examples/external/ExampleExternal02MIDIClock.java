package wellen.examples.external;

import processing.core.PApplet;
import wellen.BeatMIDI;
import wellen.Tone;
import wellen.Wellen;

public class ExampleExternal02MIDIClock extends PApplet {

    /*
     * this example demonstrates how to use a beat triggered by an external MIDI beat clock ( e.g generated by an
     * external MIDI device or an internal MIDI application ).
     */

    private int mColor;
    private BeatMIDI mBeatMIDI;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wellen.dumpMidiInputDevices();
        mBeatMIDI = BeatMIDI.start(this, "Arturia KeyStep 37");
    }

    public void draw() {
        background(mBeatMIDI.running() ? mColor : 0);
    }

    public void beat(int pBeat) {
        /* MIDI clock runs at 24 pulses per quarter note (PPQ), therefore `pBeat % 12` is triggers eighth note. */
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

    public static void main(String[] args) {
        PApplet.main(ExampleExternal02MIDIClock.class.getName());
    }
}