package wellen.examples.basics;

import processing.core.PApplet;
import wellen.DSP;

public class ExampleBasics04DSP extends PApplet {

    /*
     * this example demonstrates how to perform digital signal processing (DSP) by continuously writing to an audio
     * buffer.
     */

    private float mFreq = 440.0f;
    private float mAmp = 0.5f;
    private int mCounter = 0;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        DSP.start(this);
    }

    public void draw() {
        background(255);
        stroke(0);
        DSP.draw_buffer(g, width, height);
    }

    public void mouseMoved() {
        mFreq = map(mouseX, 0, width, 55, 440);
        mAmp = map(mouseY, 0, height, 0, 1);
    }

    public void audioblock(float[] pSamples) {
        for (int i = 0; i < pSamples.length; i++) {
            mCounter++;
            pSamples[i] = mAmp * sin(2 * PI * mFreq * mCounter / DSP.get_sample_rate());
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleBasics04DSP.class.getName());
    }
}