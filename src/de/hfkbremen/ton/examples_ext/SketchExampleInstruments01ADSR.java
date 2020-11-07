package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.Instrument;
import de.hfkbremen.ton.Note;
import de.hfkbremen.ton.Scale;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

/**
 * this examples shows how to use an instrument with an amplitude envelope ( ADSR ).
 */
public class SketchExampleInstruments01ADSR extends PApplet {
// @TODO update visual representation of ADSR

    private boolean mIsPlaying = false;
    private int mNote;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        background(255);

        /* set ADSR parameters for current instrument */
        println(Instrument.ADSR_DIAGRAM);
        Ton.instrument().attack(3.0f);
        Ton.instrument().decay(0.0f);
        Ton.instrument().sustain(1.0f);
        Ton.instrument().release(1.0f);
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
                Ton.instrument().attack(mAttack);
            }
            if (key == '2') {
                final float mDecay = 2.0f * (float) mouseX / width;
                Ton.instrument().decay(mDecay);
            }
            if (key == '3') {
                final float mSustain = (float) mouseX / width;
                Ton.instrument().sustain(mSustain);
            }
            if (key == '4') {
                final float mRelease = 2.0f * (float) mouseX / width;
                Ton.instrument().release(mRelease);
            }
        }

        /* draw ADSR */
        float mY = height / 2.0f;
        float mRadiusA = 10;
        float mRadiusB = 50;
        fill(255);
        ellipse(width * 0.2f,
                mY,
                mRadiusA + mRadiusB * Ton.instrument().get_attack(),
                mRadiusA + mRadiusB * Ton.instrument().get_attack());
        ellipse(width * 0.4f,
                mY,
                mRadiusA + mRadiusB * Ton.instrument().get_decay(),
                mRadiusA + mRadiusB * Ton.instrument().get_decay());
        ellipse(width * 0.6f,
                mY,
                mRadiusA + mRadiusB * Ton.instrument().get_sustain(),
                mRadiusA + mRadiusB * Ton.instrument().get_sustain());
        ellipse(width * 0.8f,
                mY,
                mRadiusA + mRadiusB * Ton.instrument().get_release(),
                mRadiusA + mRadiusB * Ton.instrument().get_release());
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
        PApplet.main(SketchExampleInstruments01ADSR.class.getName());
    }
}
