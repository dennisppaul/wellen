package de.hfkbremen.ton.tests;

import de.hfkbremen.ton.DSP;
import de.hfkbremen.ton.Ton;
import de.hfkbremen.ton.Wavetable;
import processing.core.PApplet;

public class TestWavetableInterpolation extends PApplet {

    private final Wavetable mWavetable = new Wavetable(16);

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        DSP.dumpAudioDevices();
        DSP.start(this);
        Wavetable.sine(mWavetable.wavetable());
        mWavetable.set_frequency(172.265625f);
        mWavetable.set_amplitude(0.25f);
        mWavetable.interpolate_samples(true);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
        mWavetable.interpolate_samples(!mousePressed);
    }

    public void audioblock(float[] pOutputSamples) {
        for (int i = 0; i < pOutputSamples.length; i++) {
            pOutputSamples[i] = mWavetable.output();
        }
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                Wavetable.sine(mWavetable.wavetable());
                break;
            case '2':
                Wavetable.triangle(mWavetable.wavetable());
                break;
            case '3':
                Wavetable.sawtooth(mWavetable.wavetable());
                break;
            case '4':
                Wavetable.fill(mWavetable.wavetable(), Ton.OSC_SQUARE); /* alternative way to fill wavetable */
                break;
            case '5':
                randomize(mWavetable.wavetable());
                break;
        }
    }

    private void randomize(float[] pWavetable) {
        for (int i = 0; i < pWavetable.length; i++) {
            pWavetable[i] = random(-1, 1);
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestWavetableInterpolation.class.getName());
    }
}