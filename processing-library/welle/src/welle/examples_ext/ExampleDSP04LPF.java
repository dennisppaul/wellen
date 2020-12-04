package welle.examples_ext;

import processing.core.PApplet;
import welle.DSP;
import welle.LowPassFilter;
import welle.Wavetable;
import welle.Welle;

/**
 * this example demonstrates how to use a *Low-Pass Filter* (LPF) on a sawtooth oscillator in DSP.
 */
public class ExampleDSP04LPF extends PApplet {

    private final Wavetable mWavetable = new Wavetable(512);
    private final LowPassFilter mFilter = new LowPassFilter(Welle.DEFAULT_SAMPLING_RATE);

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable.sawtooth(mWavetable.wavetable());
        mWavetable.set_frequency(2.0f * Welle.DEFAULT_SAMPLING_RATE / Welle.DEFAULT_AUDIOBLOCK_SIZE);
        mWavetable.set_amplitude(0.5f);
        Welle.dumpAudioInputAndOutputDevices();
        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mouseMoved() {
        mFilter.set_frequency(map(mouseX, 0, width, 1.0f, 5000.0f));
        mFilter.set_resonance(map(mouseY, 0, height, 0.0f, 0.97f));
    }

    public void audioblock(float[] pOutputSamples) {
        for (int i = 0; i < pOutputSamples.length; i++) {
            pOutputSamples[i] = mFilter.process(mWavetable.output());
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP04LPF.class.getName());
    }
}
