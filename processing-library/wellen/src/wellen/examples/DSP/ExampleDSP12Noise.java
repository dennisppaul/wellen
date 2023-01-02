package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Noise;

public class ExampleDSP12Noise extends PApplet {

    /*
     * this example demonstrates different noise generators.
     */

    private final Noise mNoise = new Noise();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mNoise.set_amplitude(0.25f);
        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
    }

    public void mouseMoved() {
        mNoise.set_step(map(mouseX, 0, width, 0.0f, 0.1f));
        mNoise.set_amplitude(map(mouseY, 0, height, 0.0f, 0.33f));
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                mNoise.set_type(Wellen.NOISE_WHITE);
                break;
            case '2':
                mNoise.set_type(Wellen.NOISE_GAUSSIAN_WHITE);
                break;
            case '3':
                mNoise.set_type(Wellen.NOISE_GAUSSIAN_WHITE2);
                break;
            case '4':
                mNoise.set_type(Wellen.NOISE_PINK);
                break;
            case '5':
                mNoise.set_type(Wellen.NOISE_PINK2);
                break;
            case '6':
                mNoise.set_type(Wellen.NOISE_PINK3);
                break;
            case '7':
                mNoise.set_type(Wellen.NOISE_SIMPLEX);
                break;
        }
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = mNoise.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP12Noise.class.getName());
    }
}