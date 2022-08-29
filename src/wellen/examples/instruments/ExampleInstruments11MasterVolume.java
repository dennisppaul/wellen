package wellen.examples.instruments;

import processing.core.PApplet;
import wellen.Gain;
import wellen.Tone;
import wellen.ToneEngineDSP;

public class ExampleInstruments11MasterVolume extends PApplet {

    /*
     * this example demonstrates how to adjust the volume of tone output.
     */

    private final Gain mMasterVolume = new Gain();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        ToneEngineDSP mToneEngine = Tone.get_internal_engine();
        mToneEngine.add_effect(mMasterVolume);

        mMasterVolume.set_gain(1.5f);
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
    }

    public void mouseMoved() {
        mMasterVolume.set_gain(map(mouseY, 0, height, 3.0f, 0.0f));
    }

    public void mousePressed() {
        int mNote = 45 + (int) random(0, 12);
        Tone.note_on(mNote, 50);
    }

    public void mouseReleased() {
        Tone.note_off();
    }

    public static void main(String[] args) {
        PApplet.main(ExampleInstruments11MasterVolume.class.getName());
    }
}
