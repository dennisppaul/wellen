package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.DSP;
import wellen.FMSynthesis;
import wellen.Wavetable;
import wellen.Wellen;

public class ExampleDSP20FMSynthesis extends PApplet {

    /*
     * this example demonstrate how to use simple FM Synthesis with two oscillators. the carrier oscillator usually
     * defines the pitch while the modulator modifies the carrier.
     */

    private FMSynthesis mFMSynthesis;
    private final float mVisuallyStableFrequency =
    (float) Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable mCarrier = new Wavetable(2048);
        mCarrier.interpolate_samples(true);
        Wavetable.fill(mCarrier.get_wavetable(), Wellen.OSC_SINE);
        mCarrier.set_frequency(2.0f * mVisuallyStableFrequency);

        Wavetable mModulator = new Wavetable(2048);
        mModulator.interpolate_samples(true);
        Wavetable.fill(mModulator.get_wavetable(), Wellen.OSC_SINE);
        mModulator.set_frequency(2.0f * mVisuallyStableFrequency);

        mFMSynthesis = new FMSynthesis(mCarrier, mModulator);
        mFMSynthesis.set_amplitude(0.33f);

        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mouseDragged() {
        mFMSynthesis.get_carrier().set_frequency(map(mouseY, 0, height, 0, 4.0f * mVisuallyStableFrequency));
    }

    public void mouseMoved() {
        mFMSynthesis.set_modulation_depth(map(mouseX, 0, width, 0, 20));
        mFMSynthesis.get_modulator().set_frequency(map(mouseY, 0, height, 0, 4.0f * mVisuallyStableFrequency));
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mFMSynthesis.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP20FMSynthesis.class.getName());
    }
}