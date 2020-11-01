package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

public class SketchExampleInstruments07OSCToneEngine extends PApplet {

    private int mNote;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Ton.init("osc", "127.0.0.1", "7001");
    }

    public void draw() {
        background(Ton.isPlaying() ? 255 : 0);
    }

    public void mousePressed() {
        Ton.instrument(mouseX < width / 2.0 ? 0 : 1);
        mNote = 45 + (int) random(0, 12);
        Ton.noteOn(mNote, 127);
    }

    public void mouseReleased() {
        Ton.noteOff(mNote);
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleInstruments07OSCToneEngine.class.getName());
    }
}
