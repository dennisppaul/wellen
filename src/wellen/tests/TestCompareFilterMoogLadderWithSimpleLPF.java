package wellen.tests;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Filter;
import wellen.LowPassFilter;
import wellen.Wavetable;
import wellen.Wellen;

public class TestCompareFilterMoogLadderWithSimpleLPF extends PApplet {
    private final Wavetable mWavetable = new Wavetable();
    private final Filter mFilter = new Filter();
    private final LowPassFilter mMoggLadder = new LowPassFilter();

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
        DSP.draw_buffer(g, width, height);
    }

    public void keyPressed() {
        switch (key) {
            case '4':
                Wavetable.fill(mWavetable.get_wavetable(), Wellen.OSC_SAWTOOTH);
                break;
            case '5':
                Wavetable.fill(mWavetable.get_wavetable(), Wellen.OSC_SQUARE);
                break;
            case '6':
                mWavetable.set_frequency(2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
                break;
            case '7':
                mWavetable.set_frequency(1.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
                break;
        }
    }

    public void mouseDragged() {
        mMoggLadder.set_frequency(map(mouseX, 0, width, 1.0f, Wellen.DEFAULT_SAMPLING_RATE * 0.5f));
        mMoggLadder.set_resonance(map(mouseY, 0, height, 0.0f, 0.97f));
    }

    public void mouseMoved() {
        mFilter.set_frequency(map(mouseX, 0, width, 1.0f, Wellen.DEFAULT_SAMPLING_RATE * 0.5f));
        mFilter.set_resonance(map(mouseY, 0, height, 0.0f, 0.97f));
    }

    public void audioblock(float[] pOutputSamples) {
        for (int i = 0; i < pOutputSamples.length; i++) {
            if (mousePressed) {
                pOutputSamples[i] = mMoggLadder.process(mWavetable.output()) * 2.0f;
            } else {
                pOutputSamples[i] = mFilter.process(mWavetable.output());
            }
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestCompareFilterMoogLadderWithSimpleLPF.class.getName());
    }
}
