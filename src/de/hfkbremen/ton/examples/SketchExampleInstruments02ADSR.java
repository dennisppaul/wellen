package de.hfkbremen.ton.examples;

import de.hfkbremen.ton.Instrument;
import de.hfkbremen.ton.Note;
import de.hfkbremen.ton.Scale;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

/**
 * this examples shows how to use an instrument with an amplitude envelope ( ADSR ).
 */
public class SketchExampleInstruments02ADSR extends PApplet {

    private Instrument mInstrument;
    private boolean mIsPlaying = false;
    private int mNote;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        background(255);

        /* set ADSR parameters for current instrument */
        println(Instrument.ADSR_DIAGRAM);
        mInstrument = Ton.instrument();
        mInstrument.attack(3.0f);
        mInstrument.decay(0.0f);
        mInstrument.sustain(1.0f);
        mInstrument.release(1.0f);
    }

    public void draw() {
        if (mIsPlaying) {
            int mColor = (mNote - Note.NOTE_A2) * 5 + 50;
            background(mColor);
        } else {
            background(0);
        }

        /* adjust ADSR */
        if (keyPressed) {
            if (key == '1') {
                final float mAttack = 3.0f * (float) mouseX / width;
                mInstrument.attack(mAttack);
            }
            if (key == '2') {
                final float mDecay = 2.0f * (float) mouseX / width;
                mInstrument.decay(mDecay);
            }
            if (key == '3') {
                final float mSustain = (float) mouseX / width;
                mInstrument.sustain(mSustain);
            }
            if (key == '4') {
                final float mRelease = 2.0f * (float) mouseX / width;
                mInstrument.release(mRelease);
            }
        }

        /* draw ADSR */
        float mY = height / 2.0f;
        float mRadiusA = 10;
        float mRadiusB = 50;
        fill(255);
        ellipse(width * 0.2f,
                mY,
                mRadiusA + mRadiusB * mInstrument.get_attack(),
                mRadiusA + mRadiusB * mInstrument.get_attack());
        ellipse(width * 0.4f,
                mY,
                mRadiusA + mRadiusB * mInstrument.get_decay(),
                mRadiusA + mRadiusB * mInstrument.get_decay());
        ellipse(width * 0.6f,
                mY,
                mRadiusA + mRadiusB * mInstrument.get_sustain(),
                mRadiusA + mRadiusB * mInstrument.get_sustain());
        ellipse(width * 0.8f,
                mY,
                mRadiusA + mRadiusB * mInstrument.get_release(),
                mRadiusA + mRadiusB * mInstrument.get_release());
    }

    public void mousePressed() {
        mNote = Scale.note(Scale.MAJOR_CHORD_7, Note.NOTE_A2, (int) random(0, 10));
        Ton.noteOn(mNote, 127);
        mIsPlaying = true;
    }

    public void mouseReleased() {
        Ton.noteOff();
        mIsPlaying = false;
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleInstruments02ADSR.class.getName());
    }
}
