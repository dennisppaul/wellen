package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

/**
 * this examples demonstrates how to use the OSC tone engine to send OSC commands. the defined OSC address patterns can
 * be found in `ToneEngineOSC`
 */
public class SketchExampleInstruments04OSCToneEngine extends PApplet {

    private int mNote;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Ton.start("osc", "127.0.0.1", "7001");
    }

    public void draw() {
        background(Ton.isPlaying() ? 255 : 0);
    }

    public void mousePressed() {
        Ton.instrument(mouseX < width / 2.0 ? 0 : 1);
        mNote = 45 + (int) random(0, 12);
        Ton.note_on(mNote, 127);
    }

    public void mouseReleased() {
        Ton.note_off(mNote);
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleInstruments04OSCToneEngine.class.getName());
    }
}
