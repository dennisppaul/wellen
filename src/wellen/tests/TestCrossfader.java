package wellen.tests;

import processing.core.PApplet;
import wellen.Crossfader;
import wellen.DSP;
import wellen.Wavetable;
import wellen.Wellen;

public class TestCrossfader extends PApplet {

    private final Crossfader mCrossfaderA = new Crossfader();
    private final Crossfader mCrossfaderB = new Crossfader();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable mWavetableA = new Wavetable();
        Wavetable.fill(mWavetableA.get_wavetable(), Wellen.OSC_SINE);
        mWavetableA.set_frequency(2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
        mWavetableA.set_amplitude(0.35f);
        mCrossfaderA.signal_a = mWavetableA;

        Wavetable mWavetableB = new Wavetable(2048);
        Wavetable.fill(mWavetableB.get_wavetable(), Wellen.OSC_SINE);
        mWavetableB.set_frequency(1.33f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
        mWavetableB.set_amplitude(0.75f);
        mCrossfaderA.signal_b = mWavetableB;

        mCrossfaderB.signal_a = mCrossfaderA;
        Wavetable mWavetableC = new Wavetable(2048);
        Wavetable.fill(mWavetableC.get_wavetable(), Wellen.OSC_SINE);
        mWavetableC.set_frequency(1.66f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
        mWavetableC.set_amplitude(0.6f);
        mCrossfaderB.signal_b = mWavetableC;

        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mouseMoved() {
        mCrossfaderA.ratio = norm(mouseX, 0, width);
        mCrossfaderB.ratio = norm(mouseY, 0, height);
    }

    public void audioblock(float[] pOutputSamples) {
        for (int i = 0; i < pOutputSamples.length; i++) {
            pOutputSamples[i] = mCrossfaderB.output();
        }
    }


    public static void main(String[] args) {
        PApplet.main(TestCrossfader.class.getName());
    }
}