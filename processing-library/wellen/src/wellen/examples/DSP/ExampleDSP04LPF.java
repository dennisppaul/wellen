package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.LowPassFilter;
import wellen.dsp.Wavetable;

public class ExampleDSP04LPF extends PApplet {

    /*
     * this example demonstrates how to use a *Low-Pass Filter* (LPF) on a sawtooth oscillator in DSP.
     */

    private final Wavetable mWavetable = new Wavetable();
    private final LowPassFilter mFilter = new LowPassFilter();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable.fill(mWavetable.get_wavetable(), Wellen.OSC_SAWTOOTH);
        mWavetable.set_frequency(2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
        mWavetable.set_amplitude(0.33f);
        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
    }

    public void mouseMoved() {
        mFilter.set_frequency(map(mouseX, 0, width, 1.0f, Wellen.DEFAULT_SAMPLING_RATE * 0.5f));
        mFilter.set_resonance(map(mouseY, 0, height, 0.0f, 0.97f));
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mFilter.process(mWavetable.output());
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP04LPF.class.getName());
    }
}
