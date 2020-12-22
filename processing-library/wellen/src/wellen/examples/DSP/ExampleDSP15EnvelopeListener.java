package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Envelope;
import wellen.EnvelopeListener;
import wellen.Note;
import wellen.Wavetable;
import wellen.Wellen;

public class ExampleDSP15EnvelopeListener extends PApplet {

    /**
     * this example demonstrates how to use envelope listeners with envelopes. envelope listeners are informed by the
     * envelope once it completed the last stage.
     * <p>
     * in this example the end of envelope event is used to restart the envelope as well as randomly resetting amplitude
     * and frequency of the oscillator.
     */

    private Envelope mRampFrequency;
    private Wavetable mWavetable;
    private final float MIN_FREQ = Note.note_to_frequency(24);
    private final float MAX_FREQ = Note.note_to_frequency(48);

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mRampFrequency = new Envelope();
        mRampFrequency.add_listener(new MEnvelopeListener());
        mRampFrequency.ramp(MIN_FREQ, MAX_FREQ, 0.0625f);
        mRampFrequency.start();

        mWavetable = new Wavetable();
        mWavetable.set_amplitude(0.25f);
        Wavetable.fill(mWavetable.get_wavetable(), Wellen.WAVESHAPE_SAWTOOTH);

        DSP.start(this);
    }

    public void draw() {
        final float mValue = mRampFrequency.get_current_value();
        final float x = frameCount % width;
        final float y = map(mValue, MIN_FREQ, MAX_FREQ, 10, height - 10);
        if (x == 1) {
            background(255);
        }
        point(x, y);
    }

    public void audioblock(float[] pSamples) {
        for (int i = 0; i < pSamples.length; i++) {
            mWavetable.set_frequency(mRampFrequency.output());
            pSamples[i] = mWavetable.output();
        }
    }

    private class MEnvelopeListener implements EnvelopeListener {
        public void finished_envelope(Envelope pEnvelope) {
            mWavetable.set_amplitude(random(0.1f, 0.4f));
            mRampFrequency.ramp_to(random(MIN_FREQ, MAX_FREQ), 0.0625f);
            mRampFrequency.start();
        }

        public void finished_stage(Envelope pEnvelope, int pStageID) {
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP15EnvelopeListener.class.getName());
    }
}