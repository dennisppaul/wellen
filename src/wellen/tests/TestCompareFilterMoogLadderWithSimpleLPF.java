package wellen.tests;

import processing.core.PApplet;
import wellen.Beat;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.FilterHighLowBandPass;
import wellen.dsp.FilterMoogLadderLowPass;
import wellen.dsp.Wavetable;

public class TestCompareFilterMoogLadderWithSimpleLPF extends PApplet {
    private final FilterHighLowBandPass mFilter = new FilterHighLowBandPass();
    private int mFreqOffset = 0;
    private final FilterMoogLadderLowPass mMoggLadder = new FilterMoogLadderLowPass();
    private final Wavetable mWavetable = new Wavetable();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable.fill(mWavetable.get_wavetable(), Wellen.OSC_SAWTOOTH);
        mWavetable.set_frequency(2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
        mWavetable.set_amplitude(0.33f);
        DSP.start(this);
        Beat.start(this, 480);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
    }

    public void beat(int beatCounter) {
        if (beatCounter % 128 == 0) {
            mFreqOffset = 0;
        }
        if (beatCounter % 128 == 32) {
            mFreqOffset = 7;
        }
        if (beatCounter % 128 == 64) {
            mFreqOffset = 0;
        }
        if (beatCounter % 128 == 96) {
            mFreqOffset = 7 - 12;
        }
        if (beatCounter % 128 == 112) {
            mFreqOffset = 10 - 12;
        }
        float mFreqMult = (beatCounter % 4) + 1;
        mWavetable.set_frequency(mFreqMult * Wellen.DEFAULT_SAMPLING_RATE * ((12.0f + mFreqOffset) / 12.0f) / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                Wavetable.fill(mWavetable.get_wavetable(), Wellen.OSC_SAWTOOTH);
                break;
            case '2':
                Wavetable.fill(mWavetable.get_wavetable(), Wellen.OSC_SQUARE);
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

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            if (mousePressed) {
                output_signal[i] = mMoggLadder.process(mWavetable.output()) * 2.0f;
            } else {
                output_signal[i] = mFilter.process(mWavetable.output());
            }
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestCompareFilterMoogLadderWithSimpleLPF.class.getName());
    }
}
