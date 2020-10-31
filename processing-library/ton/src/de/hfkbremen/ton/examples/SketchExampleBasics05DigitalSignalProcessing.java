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
        final int mBufferSize = DSP.buffer() != null ? DSP.buffer().length : 0;
        for (int i = 0; i < mBufferSize; i++) {
            point(map(i, 0, mBufferSize, 0, width),
                  map(DSP.buffer()[i], -1, 1, 0, height));
        }
    }

    public void mouseMoved() {
        mFreq = map(mouseX, 0, width, 55, 440);
    }

    public void audioblock(float[] pSamples) {
        for (int i = 0; i < pSamples.length; i++) {
            pSamples[i] = 0.5f * sin(2 * PI * mFreq * mCounter++ / DSP.sample_rate());
        }
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleBasics05DigitalSignalProcessing.class.getName());
    }
}