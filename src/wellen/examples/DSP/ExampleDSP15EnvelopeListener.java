package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.EnvelopeListener;
import wellen.Note;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Envelope;
import wellen.dsp.Wavetable;

public class ExampleDSP15EnvelopeListener extends PApplet {

    /*
     * this example demonstrates how to use envelope listeners with envelopes. envelope listeners are informed by the
     * envelope once it completed the last stage.
     *
     * in this example the end of envelope event is used to restart the envelope as well as randomly resetting amplitude
     * and frequency of the oscillator.
     */

    private final float MAX_FREQ = Note.note_to_frequency(48);
    private final float MIN_FREQ = Note.note_to_frequency(24);
    private Envelope mRampFrequency;
    private Wavetable mWavetable;

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
        Wavetable.fill(mWavetable.get_wavetable(), Wellen.WAVEFORM_SAWTOOTH);

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

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            mWavetable.set_frequency(mRampFrequency.output());
            output_signal[i] = mWavetable.output();
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