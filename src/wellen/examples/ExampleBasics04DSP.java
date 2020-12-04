package wellen.examples;

import processing.core.PApplet;
import wellen.DSP;

/**
 * this example demonstrates how to perform digital signal processing (DSP) by continuously writing to an audio buffer
 * directly.
 */
public class ExampleBasics04DSP extends PApplet {

    private float mFreq = 440.0f;
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
        if (DSP.get_buffer() != null) {
            final int mBufferSize = DSP.get_buffer_size();
            for (int i = 0; i < mBufferSize; i++) {
                final float x = map(i, 0, mBufferSize, 0, width);
                point(x, map(DSP.get_buffer()[i], -1, 1, 0, height));
            }
        }
    }

    public void mouseMoved() {
        mFreq = map(mouseX, 0, width, 55, 440);
    }

    public void audioblock(float[] pSamples) {
        for (int i = 0; i < pSamples.length; i++) {
            mCounter++;
            pSamples[i] = 0.5f * sin(2 * PI * mFreq * mCounter / DSP.get_sample_rate());
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleBasics04DSP.class.getName());
    }
}