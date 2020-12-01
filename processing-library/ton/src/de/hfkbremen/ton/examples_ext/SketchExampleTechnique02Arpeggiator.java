package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.Arpeggiator;
import de.hfkbremen.ton.Beat;
import de.hfkbremen.ton.Note;
import de.hfkbremen.ton.Scale;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

/**
 * this examples demonstrates how to use the [arpeggiator](https://en.wikipedia.org/wiki/Synthesizer#Arpeggiator). the
 * method `play` prepares a series of notes defined by the *pattern* to be played sequentially.
 */
public class SketchExampleTechnique02Arpeggiator extends PApplet {

    private int mColor;
    private Beat mBeat;
    private Arpeggiator mArpeggiator;
    private boolean mToggle;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Ton.dumpMidiInputDevices();
        mBeat = Beat.start(this, 120 * 24);
        /* the pattern is composed of 8 notes with a length of 1/32 ( 8 * (1/32) = (1/4) ) i.e the pattern has a
         * length of 1/4 which means 24 pulses ( or ticks ) when synced with a MIDI clock.
         */
        mArpeggiator = new Arpeggiator(24);
        mArpeggiator.pattern(0 * 3, 0, 0.8f);
        mArpeggiator.pattern(1 * 3, 0, 0.6f);
        mArpeggiator.pattern(2 * 3, 3, 0.4f);
        mArpeggiator.pattern(3 * 3, 5, 0.3f);
        mArpeggiator.pattern(4 * 3, 4, 0.2f);
        mArpeggiator.pattern(6 * 3, 5, 0.1f);
    }

    public void draw() {
        background(255);
        if (mToggle) {
            fill(0);
            ellipse(width * 0.5f, height * 0.5f, 100, 100);
        }
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                mArpeggiator.play(0, 100);
                break;
            case '2':
                mArpeggiator.play(1, 100);
                break;
            case '3':
                mArpeggiator.play(3, 100);
                break;
            case '4':
                mArpeggiator.play(4, 100);
                break;
            case '5':
                mArpeggiator.play(5, 100);
                break;
        }
    }

    public void beat(int pBeat) {
        if (pBeat % 24 == 0) {
            mToggle = !mToggle;
        }
        /* step through the arpeggiator at clock speed i.e 24 steps ( or pulses ) per quarter note */
        if (mArpeggiator.step()) {
            int mNote = Scale.note(Scale.MINOR_PENTATONIC, Note.NOTE_C3, mArpeggiator.note());
            Ton.note_on(mNote, mArpeggiator.velocity());
        } else {
            Ton.note_off();
        }
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleTechnique02Arpeggiator.class.getName());
    }
}




