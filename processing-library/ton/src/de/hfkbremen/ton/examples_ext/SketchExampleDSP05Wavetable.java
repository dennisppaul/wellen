package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.DSP;
import de.hfkbremen.ton.Wavetable;
import processing.core.PApplet;

/**
 * this examples demonstrates how to use a wavetable ( a chunk of memory ) and play it back at different frequencies and
 * amplitudes.
 */
public class SketchExampleDSP05Wavetable extends PApplet {

    private final Wavetable mWavetable = new Wavetable(512);

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        DSP.dumpAudioDevices();
        DSP.start(this);
        Wavetable.triangle(mWavetable.wavetable());
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mouseMoved() {
        mWavetable.set_frequency(map(mouseX, 0, width, 55, 220));
        mWavetable.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
    }

    public void mouseDragged() {
        mWavetable.set_frequency(172.265625f);
        mWavetable.set_amplitude(0.25f);
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                Wavetable.sine(mWavetable.wavetable());
                break;
            case '2':
                Wavetable.sawtooth(mWavetable.wavetable());
                break;
            case '3':
                Wavetable.triangle(mWavetable.wavetable());
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
            pOutputSamples[i] = mWavetable.process();
        }
    }

    private void randomize(float[] pWavetable) {
        for (int i = 0; i < pWavetable.length; i++) {
            pWavetable[i] = random(-1, 1);
        }
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleDSP05Wavetable.class.getName());
    }
}