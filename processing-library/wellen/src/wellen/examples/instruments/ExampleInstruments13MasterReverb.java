package wellen.examples.instruments;

import processing.core.PApplet;
import wellen.Tone;
import wellen.ToneEngineInternal;

public class ExampleInstruments13MasterReverb extends PApplet {

    /*
     * this example demonstrates how to add a reverb effect to tone output.
     * @deprecated note that this mechanism will slowly be replaced by the more
     * flexible master effects.
     */

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        ToneEngineInternal mToneEngine = Tone.get_internal_engine();
        mToneEngine.enable_reverb(true);
        mToneEngine.get_reverb().set_roomsize(0.9f);
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
    }

    public void mousePressed() {
        int mNote = 45 + (int) random(0, 12);
        Tone.note_on(mNote, 100);
    }

    public void mouseReleased() {
        Tone.note_off();
    }

    public static void main(String[] args) {
        PApplet.main(ExampleInstruments13MasterReverb.class.getName());
    }
}
