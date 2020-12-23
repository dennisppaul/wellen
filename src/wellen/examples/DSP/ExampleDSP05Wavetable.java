package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Wavetable;
import wellen.Wellen;

public class ExampleDSP05Wavetable extends PApplet {

    /*
     * this example demonstrates how to use a *wavetable* ( a chunk of memory ) and play it back at different
     * frequencies and amplitudes. in this example a wavetable is used to emulate an oscillator (VCO) with different
     * wave shapes.
     */

    private final Wavetable mWavetable = new Wavetable(512);

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable.sine(mWavetable.get_wavetable());
        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mouseDragged() {
        mWavetable.set_frequency(2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
        mWavetable.set_amplitude(0.25f);
    }

    public void mouseMoved() {
        mWavetable.set_frequency(map(mouseX, 0, width, 55, 220));
        mWavetable.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                Wavetable.sine(mWavetable.get_wavetable());
                break;
            case '2':
                Wavetable.triangle(mWavetable.get_wavetable());
                break;
            case '3':
                Wavetable.sawtooth(mWavetable.get_wavetable());
                break;
            case '4':
                Wavetable.square(mWavetable.get_wavetable());
                break;
            case '5':
                randomize(mWavetable.get_wavetable());
                break;
        }
    }

    public void audioblock(float[] pOutputSamples) {
        for (int i = 0; i < pOutputSamples.length; i++) {
            pOutputSamples[i] = mWavetable.output();
        }
    }

    private void randomize(float[] pWavetable) {
        for (int i = 0; i < pWavetable.length; i++) {
            pWavetable[i] = random(-1, 1);
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP05Wavetable.class.getName());
    }
}