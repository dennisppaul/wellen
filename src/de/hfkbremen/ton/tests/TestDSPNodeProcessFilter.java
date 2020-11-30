package de.hfkbremen.ton.tests;

import de.hfkbremen.ton.DSP;
import de.hfkbremen.ton.LowPassFilter;
import de.hfkbremen.ton.Ton;
import de.hfkbremen.ton.Wavetable;
import processing.core.PApplet;

public class TestDSPNodeProcessFilter extends PApplet {

    private final Wavetable mWavetable = new Wavetable(512);
    private final LowPassFilter mFilter = new LowPassFilter(Ton.DEFAULT_SAMPLING_RATE);

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable.sawtooth(mWavetable.wavetable());
        mWavetable.set_frequency(172.265625f);
        mWavetable.set_amplitude(0.55f);
        DSP.dumpAudioDevices();
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
                Wavetable.square(mWavetable.wavetable());
                break;
            case '5':
                randomize(mWavetable.wavetable());
                break;
        }
    }

    public void audioblock(float[] pOutputSamples) {
        for (int i = 0; i < pOutputSamples.length; i++) {
            pOutputSamples[i] = mFilter.process(mWavetable.output());
        }
    }

    private void randomize(float[] pWavetable) {
        for (int i = 0; i < pWavetable.length; i++) {
            pWavetable[i] = random(-1, 1);
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestDSPNodeProcessFilter.class.getName());
    }
}
