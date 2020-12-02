package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.EventReceiverMIDI;
import de.hfkbremen.ton.Ton;
import de.hfkbremen.ton.TonEvent;
import processing.core.PApplet;

/**
 * this examples demonstrates how to parse MIDI events sent from an external MIDI device ( i.e a MIDI keyboard ) and
 * parse the events to play notes with the tone engine.
 */
public class ExampleEvent03MIDIExternalKeyboard extends PApplet {

    private int mNote = 0;
    private int mVelocity = 0;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Ton.dumpMidiInputDevices();
        EventReceiverMIDI.start(this, "Arturia KeyStep 37");
    }

    public void draw() {
        background(255);
        noStroke();
        fill(map(mVelocity, 0, 127, 255, 0));
        float mScale = map(mNote, 24, 96, 5, height * 0.8f);
        ellipse(width * 0.5f, height * 0.5f, mScale, mScale);
    }

    public void event_receive(int pEvent, float[] pData) {
        /* parse event + data. see `Event` for all *defined* events. */
        if (pEvent == TonEvent.EVENT_NOTE_ON) {
            mNote = (int) pData[TonEvent.NOTE];
            mVelocity = (int) pData[TonEvent.VELOCITY];
            Ton.note_on(mNote, mVelocity);
        } else if (pEvent == TonEvent.EVENT_NOTE_OFF) {
            mNote = (int) pData[TonEvent.NOTE];
            mVelocity = 0;
            Ton.note_off(mNote);
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleEvent03MIDIExternalKeyboard.class.getName());
    }
}
