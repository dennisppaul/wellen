package wellen.tests;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Wavetable;
import wellen.Wellen;

public class TestWavetableJitter extends PApplet {

    private Wavetable mWavetable;
    private Wavetable mWavetableRef;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mWavetable = new Wavetable(512);
        mWavetable.enable_jitter(true);
        mWavetable.set_jitter_range(0.5f);
        mWavetable.set_frequency(4.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
        mWavetable.set_amplitude(0.25f);
        Wavetable.sine(mWavetable.get_wavetable());

        mWavetableRef = new Wavetable(512);
        mWavetableRef.set_frequency(2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
        mWavetableRef.set_amplitude(0.25f);
        Wavetable.sine(mWavetableRef.get_wavetable());

        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mousePressed() {
        mWavetable.enable_jitter(false);
    }

    public void mouseReleased() {
        mWavetable.enable_jitter(true);
    }

    public void mouseMoved() {
        mWavetable.set_jitter_range(map(mouseX, 0, width, 0.0f, 2.0f));
    }

    public void audioblock(float[] pOutputSamples) {
        for (int i = 0; i < pOutputSamples.length; i++) {
            pOutputSamples[i] = mWavetable.output() + mWavetableRef.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestWavetableJitter.class.getName());
    }
}
