package de.hfkbremen.ton.applications;

import de.hfkbremen.ton.Beat;
import de.hfkbremen.ton.Instrument;
import de.hfkbremen.ton.Note;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

public class AppAlgorithmicCompositionModulo extends PApplet {

    private int mBeatCounter = 0;

    public void beat(int pBeat) {
        mBeatCounter++;
        play();
    }

    public void draw() {
        background(255);
    }

    public void play() {
        if (mBeatCounter % 2 == 0) {
            Ton.noteOn(Note.NOTE_A2 + (mBeatCounter % 4) * 3, 100);
        }
        if (mBeatCounter % 8 == 0) {
            Ton.noteOn(Note.NOTE_A3, 100);
        }
        if (mBeatCounter % 32 == 0) {
            Ton.noteOn(Note.NOTE_A4, 100);
        }
        if (mBeatCounter % 11 == 0) {
            Ton.noteOn(Note.NOTE_C4, 100);
        }
        if (mBeatCounter % 13 == 0) {
            Ton.noteOn(Note.NOTE_C5, 100);
        }
    }

    public void settings() {
        size(1280, 720);
    }

    public void setup() {
        background(255);

        /* set ADSR parameters for current instrument */
        Instrument mInstrument = Ton.instrument();
        mInstrument.attack(0.01f);
        mInstrument.decay(0.1f);
        mInstrument.sustain(0.0f);
        mInstrument.release(0.01f);
        mInstrument.osc_type(Instrument.TRIANGLE);

        Beat.start(this, 120 * 4);
        Ton.instrument().osc_type(Instrument.SAWTOOTH);
    }

    public static void main(String[] args) {
        PApplet.main(AppAlgorithmicCompositionModulo.class.getName());
    }
}
