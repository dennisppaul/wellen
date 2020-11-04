package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.BeatMIDI;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

public class SketchExampleEventBeatMIDIClock extends PApplet {

    private int mColor;

    private BeatMIDI mBeatMIDI;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Ton.dumpMidiInputDevices();
        mBeatMIDI = BeatMIDI.start(this, "Bus 1");
    }

    public void draw() {
        background(mBeatMIDI.running() ? mColor : 0);
    }

    public void beat(int pBeat) {
        if (pBeat % 24 == 0) {
            System.out.println("beat");
            mColor = color(random(127, 255),
                    random(127, 255),
                    random(127, 255));
        }
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleEventBeatMIDIClock.class.getName());
    }
}