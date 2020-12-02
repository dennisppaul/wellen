package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.DSP;
import processing.core.PApplet;

/**
 * this examples demonstrates how to create stereo sounds with DSP.
 */

public class ExampleDSP01StereoOutput extends PApplet {

    private float mFreq = 344.53125f;
    private int mCounter = 0;
    private float mDetune = 1.1f;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        DSP.start(this, 2);
    }

    public void draw() {
        background(255);
        stroke(0);
        final int mBufferSize = DSP.buffer_size();
        if (DSP.buffer_left() != null && DSP.buffer_right() != null) {
            for (int i = 0; i < mBufferSize; i++) {
                final float x = map(i, 0, mBufferSize, 0, width);
                point(x, map(DSP.buffer_left()[i], -1, 1, 0, height * 0.5f));
                point(x, map(DSP.buffer_right()[i], -1, 1, height * 0.5f, height));
            }
        }
    }

    public void mouseMoved() {
        mFreq = map(mouseX, 0, width, 86.1328125f, 344.53125f);
        mDetune = map(mouseY, 0, height, 1.0f, 1.5f);
    }

    public void audioblock(float[] pSamplesLeft, float[] pSamplesRight) {
        for (int i = 0; i < pSamplesLeft.length; i++) {
            mCounter++;
            float mLeft = 0.5f * sin(2 * PI * mFreq * mCounter / DSP.sample_rate());
            float mRight = 0.5f * sin(2 * PI * mFreq * mDetune * mCounter / DSP.sample_rate());
            pSamplesLeft[i] = mLeft * 0.7f + mRight * 0.3f;
            pSamplesRight[i] = mRight * 0.7f + mLeft * 0.3f;
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP01StereoOutput.class.getName());
    }
}
