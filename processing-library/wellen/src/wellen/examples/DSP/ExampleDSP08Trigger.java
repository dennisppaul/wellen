package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.Note;
import wellen.Tone;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Trigger;
import wellen.dsp.Wavetable;

public class ExampleDSP08Trigger extends PApplet {

    /*
     * this example demonstrates how to use a trigger to convert alternating signals into events ( `trigger` ). the
     * trigger is continuously fed a signal, whenever the signal cross the zero point an event is triggered. the trigger
     * can be configured to detect *rising-edge* ( signal previously had a negative value ), *falling-edge* ( signal
     * previously had a positive value ) signals or both.
     *
     * it is common to use a low-frequency oscillator (LFO) to generate the signal for the trigger.
     */

    private int mBeatCount;
    private final int[] mNotes = {Note.NOTE_C3, Note.NOTE_C4, Note.NOTE_F3 - 1, Note.NOTE_F4 - 1, Note.NOTE_A2,
                                  Note.NOTE_A3, Note.NOTE_F4 - 1, Note.NOTE_F3 - 1};
    private float mSignal;
    private Trigger mTrigger;
    private Wavetable mWavetable;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mTrigger = new Trigger(this);
        mTrigger.trigger_falling_edge(true);
        mTrigger.trigger_rising_edge(true);

        mWavetable = new Wavetable(64); /* use wavetable as LFO */
        Wavetable.sine(mWavetable.get_wavetable());
        mWavetable.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR); /* interpolate between samples to remove
         *steps* from the signal */
        mWavetable.set_frequency(1.0f / 3.0f); /* set phase duration to 3SEC */

        Tone.start();
        DSP.start(this); /* DSP is only used to create trigger events */
    }

    public void draw() {
        background(255);
        noStroke();
        fill(0);
        float mScale = (mBeatCount % 2) * 0.25f + 0.25f;
        ellipse(width * 0.5f, height * 0.5f, width * mScale, width * mScale);
        /* draw current signal */
        stroke(255);
        ellipse(width * 0.5f, map(mSignal, -1.0f, 1.0f, 0, height), 10, 10);
    }

    public void mouseMoved() {
        /* set oscillation speed a value between 0.1SEC and 5SEC */
        mWavetable.set_frequency(1.0f / map(mouseX, 0, width, 0.1f, 5.0f));
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            mSignal = mWavetable.output();
            mTrigger.input(mSignal);
        }
    }

    public void trigger() {
        mBeatCount++;
        int mNote = mNotes[mBeatCount % mNotes.length];
        Tone.note_on(mNote, 100, 0.1f);
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP08Trigger.class.getName());
    }
}