package wellen.examples.external;

import processing.core.PApplet;
import wellen.MidiIn;
import wellen.MidiInListener;
import wellen.MidiOut;
import wellen.Tone;
import wellen.Wellen;

public class ExampleExternal11MIDIInputOutput extends PApplet {

    /*
     * this example demonstrates how to send + receive MIDI events.
     */


    private MidiOut mMidiOut;
    private MidiIn mMidiIn;
    private int mNote = 0;
    private int mVelocity = 0;


    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wellen.dumpMidiInputDevices();
        mMidiIn = new MidiIn("Arturia KeyStep 37");
        mMidiIn.addListener(new MIDIInput());
        mMidiOut = new MidiOut("Arturia KeyStep 37");
    }

    public void draw() {
        background(255);
        noStroke();
        fill(map(mVelocity, 0, 127, 255, 0));
        float mScale = Tone.is_playing() ? map(mNote, 24, 96, 5, height * 0.8f) : 5;
        ellipse(width * 0.5f, height * 0.5f, mScale, mScale);
    }

    public void mousePressed() {
        mMidiOut.sendNoteOn(10, 48, 100);
    }


    private class MIDIInput implements MidiInListener {
        public void receiveProgramChange(int channel, int number, int value) {
        }

        public void receiveControlChange(int channel, int number, int value) {
        }

        public void clock_tick() {
        }

        public void clock_start() {
        }

        public void clock_continue() {
        }

        public void clock_stop() {
        }

        public void clock_song_position_pointer(int pOffset16th) {
        }

        public void receiveNoteOff(int channel, int pitch) {
            Tone.instrument(channel);
            mNote = pitch;
            mVelocity = 0;
            Tone.note_off(mNote);
        }

        public void receiveNoteOn(int channel, int pitch, int velocity) {
            Tone.instrument(channel);
            mNote = pitch;
            mVelocity = velocity;
            Tone.note_on(mNote, mVelocity);
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleExternal11MIDIInputOutput.class.getName());
    }
}
