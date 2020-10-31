package de.hfkbremen.ton.examples;

import de.hfkbremen.ton.DSP;
import processing.core.PApplet;

/**
 * this example demonstrates how to perform digital signal processing (DSP) by directly writing to an audio buffer.
 */
public class SketchExampleBasics05DigitalSignalProcessing extends PApplet {

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
        fill(0);
        noStroke();
        final float mScale = mFreq / 440.f * width * 0.25f;
        ellipse(width * 0.5f, height * 0.5f, mScale, mScale);

        stroke(0);
        final int mBufferSize = DSP.buffer_size();
        for (int i = 0; i < mBufferSize; i++) {
            final float x = map(i, 0, mBufferSize, 0, width);
            point(x, map(DSP.buffer_left()[i], -1, 1, 0, height * 0.5f));
            point(x, map(DSP.buffer_right()[i], -1, 1, height * 0.5f, height));
        }
    }

    public void mouseMoved() {
        mFreq = map(mouseX, 0, width, 55, 440);
    }

    public void audioblock(float[] pSamplesLeft, float[] pSamplesRight) {
        float mDetune = map(mouseY, 0, height, 0.9f, 1.1f);
        for (int i = 0; i < pSamplesLeft.length; i++) {
            mCounter++;
            pSamplesLeft[i] = 0.5f * sin(2 * PI * mFreq * mCounter / DSP.sample_rate());
            pSamplesRight[i] = 0.5f * sin(2 * PI * mFreq * mDetune * mCounter / DSP.sample_rate());
        }
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleBasics05DigitalSignalProcessing.class.getName());
    }
}