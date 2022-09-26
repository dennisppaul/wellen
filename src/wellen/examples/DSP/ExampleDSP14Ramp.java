package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.Beat;
import wellen.Note;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Envelope;
import wellen.dsp.Wavetable;

public class ExampleDSP14Ramp extends PApplet {

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

    private Envelope mRampFrequency;
    private Envelope mEnvelopeAmplitude;
    private Wavetable mWavetable;

    private final int NOTE_RANGE = 36;
    private final int BASE_NOTE = Note.NOTE_C1;
    private final float MIN_FREQ = Note.note_to_frequency(BASE_NOTE);
    private final float MAX_FREQ = Note.note_to_frequency(BASE_NOTE + NOTE_RANGE);

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mRampFrequency = new Envelope();
        mRampFrequency.set_current_value(MIN_FREQ);

        mEnvelopeAmplitude = new Envelope();
        mEnvelopeAmplitude.add_stage(0.0f, 0.01f);
        mEnvelopeAmplitude.add_stage(0.2f, 0.24f);
        mEnvelopeAmplitude.add_stage(0.0f);

        mWavetable = new Wavetable();
        Wavetable.fill(mWavetable.get_wavetable(), Wellen.WAVEFORM_SAWTOOTH);

        DSP.start(this);
        Beat.start(this, 120);
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

    public void beat(int pBeatCounter) {
        final float mFreq = Note.note_to_frequency(BASE_NOTE + (int) random(0, NOTE_RANGE + 1));
        mRampFrequency.ramp_to(mFreq, 0.25f);
        mRampFrequency.start();
        mEnvelopeAmplitude.start();
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            mWavetable.set_frequency(mRampFrequency.output());
            mWavetable.set_amplitude(mEnvelopeAmplitude.output());
            pOutputSignal[i] = mWavetable.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP14Ramp.class.getName());
    }
}