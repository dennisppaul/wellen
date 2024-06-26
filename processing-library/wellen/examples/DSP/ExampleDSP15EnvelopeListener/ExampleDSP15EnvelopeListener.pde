import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to use envelope listeners with envelopes. envelope listeners are informed by the
 * envelope once it completed the last stage.
 *
 * in this example the end of envelope event is used to restart the envelope as well as randomly resetting amplitude
 * and frequency of the oscillator.
 */
final float MAX_FREQ = Note.note_to_frequency(48);
final float MIN_FREQ = Note.note_to_frequency(24);
Envelope mRampFrequency;
Wavetable mWavetable;
void settings() {
    size(640, 480);
}
void setup() {
    mRampFrequency = new Envelope();
    mRampFrequency.add_listener(new MEnvelopeListener());
    mRampFrequency.ramp(MIN_FREQ, MAX_FREQ, 0.0625f);
    mRampFrequency.start();
    mWavetable = new Wavetable();
    mWavetable.set_amplitude(0.25f);
    Wavetable.fill(mWavetable.get_wavetable(), Wellen.WAVEFORM_SAWTOOTH);
    DSP.start(this);
}
void draw() {
    final float mValue = mRampFrequency.get_current_value();
    final float x = frameCount % width;
    final float y = map(mValue, MIN_FREQ, MAX_FREQ, 10, height - 10);
    if (x == 1) {
        background(255);
    }
    point(x, y);
}
void audioblock(float[] output_signal) {
    for (int i = 0; i < output_signal.length; i++) {
        mWavetable.set_frequency(mRampFrequency.output());
        output_signal[i] = mWavetable.output();
    }
}
class MEnvelopeListener implements EnvelopeListener {
    void finished_envelope(Envelope pEnvelope) {
        mWavetable.set_amplitude(random(0.1f, 0.4f));
        mRampFrequency.ramp_to(random(MIN_FREQ, MAX_FREQ), 0.0625f);
        mRampFrequency.start();
    }
    void finished_stage(Envelope pEnvelope, int pStageID) {
    }
}
