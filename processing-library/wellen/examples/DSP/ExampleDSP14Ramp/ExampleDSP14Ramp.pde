import wellen.*; 

/*
 * this example demonstrates how to use envelopes as linear ramps.
 *
 * the ramp controls the frequency of the oscillator while the envelope controls the amplitude. note that there are
 * two variants of the ramp method: `ramp(float, float, float)` and `ramp_to(float, float)`.
 *
 * `ramp(float, float, float)` clears all current stages from the envelope and creates a ramp from start to end
 * value in specified duration.
 *
 * `ramp_to(float, float)` clears all current stages from the envelope and creates a ramp from the current value of
 * the envelope to an end value in specified duration.
 */

Envelope mRampFrequency;

Envelope mEnvelopeAmplitude;

Wavetable mWavetable;

final int NOTE_RANGE = 36;

final int BASE_NOTE = Note.NOTE_C1;

final float MIN_FREQ = Note.note_to_frequency(BASE_NOTE);

final float MAX_FREQ = Note.note_to_frequency(BASE_NOTE + NOTE_RANGE);

void settings() {
    size(640, 480);
}

void setup() {
    mRampFrequency = new Envelope();
    mRampFrequency.set_current_value(MIN_FREQ);
    mEnvelopeAmplitude = new Envelope();
    mEnvelopeAmplitude.add_stage(0.0f, 0.01f);
    mEnvelopeAmplitude.add_stage(0.2f, 0.24f);
    mEnvelopeAmplitude.add_stage(0.0f);
    mWavetable = new Wavetable();
    Wavetable.fill(mWavetable.get_wavetable(), Wellen.WAVESHAPE_SAWTOOTH);
    DSP.start(this);
    Beat.start(this, 120);
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

void beat(int pBeatCounter) {
    final float mFreq = Note.note_to_frequency(BASE_NOTE + (int) random(0, NOTE_RANGE + 1));
    mRampFrequency.ramp_to(mFreq, 0.25f);
    mRampFrequency.start();
    mEnvelopeAmplitude.start();
}

void audioblock(float[] pSamples) {
    for (int i = 0; i < pSamples.length; i++) {
        mWavetable.set_frequency(mRampFrequency.output());
        mWavetable.set_amplitude(mEnvelopeAmplitude.output());
        pSamples[i] = mWavetable.output();
    }
}
