package wellen.examples.external;

import processing.core.PApplet;
import wellen.ToneEngineDSP;
import wellen.ToneEngineMIDI;
import wellen.Wellen;

public class ExampleExternal01ToneEngineMIDIWithToneEngineDSP extends PApplet {

    /*
     * this example demonstrates how to use multiple tone engines ( i.e midi + internal ) at the same time.
     */

    int mNote;
    private ToneEngineDSP mToneEngineDSP;
    private ToneEngineMIDI mToneEngineMIDI;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wellen.dumpMidiOutputDevices();
        /* when working with multiple engines the use of `Tone.start(...)` is discouraged. */
        mToneEngineDSP = new ToneEngineDSP();
        mToneEngineMIDI = new ToneEngineMIDI("Bus 1");
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.33f,
                height * 0.5f,
                mToneEngineDSP.is_playing() ? 100 : 5,
                mToneEngineDSP.is_playing() ? 100 : 5);
        ellipse(width * 0.66f,
                height * 0.5f,
                mToneEngineMIDI.is_playing() ? 100 : 5,
                mToneEngineMIDI.is_playing() ? 100 : 5);
    }

    public void mousePressed() {
        mNote = 45 + (int) random(0, 12);
        mToneEngineDSP.note_on(mNote, 100);
        mToneEngineMIDI.note_on(mNote, 100);
    }

    public void mouseReleased() {
        mToneEngineDSP.note_off();
        mToneEngineMIDI.note_off(mNote);
    }

    public static void main(String[] args) {
        PApplet.main(ExampleExternal01ToneEngineMIDIWithToneEngineDSP.class.getName());
    }
}
