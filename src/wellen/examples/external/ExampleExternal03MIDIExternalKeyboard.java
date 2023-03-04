package wellen.examples.external;

import processing.core.PApplet;
import wellen.EventReceiverMIDI;
import wellen.Tone;
import wellen.Wellen;

public class ExampleExternal03MIDIExternalKeyboard extends PApplet {

    /*
     * this example demonstrates how to receive MIDI events sent from an external MIDI device ( i.e a MIDI keyboard )
     * play the notes with the tone engine.
     */

    private int mNote = 0;
    private int mVelocity = 0;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wellen.dumpMidiInputDevices();
        EventReceiverMIDI.start(this, "Arturia KeyStep 37");
    }

    public void draw() {
        background(255);
        noStroke();
        fill(map(mVelocity, 0, 127, 255, 0));
        float mScale = Tone.is_playing() ? map(mNote, 24, 96, 5, height * 0.8f) : 5;
        ellipse(width * 0.5f, height * 0.5f, mScale, mScale);
    }

    public void midi_note_off(int channel, int pitch) {
        Tone.instrument(channel);
        mNote = pitch;
        mVelocity = 0;
        Tone.note_off(mNote);
    }

    public void midi_note_on(int channel, int pitch, int velocity) {
        Tone.instrument(channel);
        mNote = pitch;
        mVelocity = velocity;
        Tone.note_on(mNote, mVelocity);
    }

    public static void main(String[] args) {
        PApplet.main(ExampleExternal03MIDIExternalKeyboard.class.getName());
    }
}
