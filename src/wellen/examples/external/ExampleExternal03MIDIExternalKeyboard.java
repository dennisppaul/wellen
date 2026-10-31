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
        /* the device name does not need to match exactly e.g "KeyStep" also finds "Arturia KeyStep 37" */
        EventReceiverMIDI.start(this, "Arturia KeyStep 37");
    }

    public void draw() {
        background(255);
        noStroke();
        fill(map(mVelocity, 0, 127, 255, 0));
        float mScale = Tone.is_playing() ? map(mNote, 24, 96, 5, height * 0.8f) : 5;
        ellipse(width * 0.5f, height * 0.5f, mScale, mScale);
    }

    /*
     * MIDI callbacks are called on the MIDI thread ( not on the thread that calls `draw()` ). therefore notes are played
     * with `Tone.note_on_instrument(...)` which does not change the currently selected instrument and is safe to call
     * from any thread. alternatively `EventReceiverMIDI.start(...).dispatch_on_draw_thread(true)` moves all callbacks
     * to the drawing thread ( at the cost of up to one frame latency ).
     */
    public void midi_note_off(int channel, int pitch, int velocity) {
        mNote = pitch;
        mVelocity = 0;
        Tone.note_off_instrument(channel, pitch);
    }

    public void midi_note_on(int channel, int pitch, int velocity) {
        mNote = pitch;
        mVelocity = velocity;
        Tone.note_on_instrument(channel, pitch, velocity);
    }

    public static void main(String[] args) {
        PApplet.main(ExampleExternal03MIDIExternalKeyboard.class.getName());
    }
}
