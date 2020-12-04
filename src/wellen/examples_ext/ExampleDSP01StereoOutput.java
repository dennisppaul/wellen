package wellen.examples_ext;

import processing.core.PApplet;
import wellen.DSP;

/**
 * this example demonstrates how to create stereo sounds with DSP. two slightly detuned sine waves are generated and
 * distributed to the left and right channel.
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
        final int mBufferSize = DSP.get_buffer_size();
        if (DSP.get_buffer_left() != null && DSP.get_buffer_right() != null) {
            for (int i = 0; i < mBufferSize; i++) {
                final float x = map(i, 0, mBufferSize, 0, width);
                point(x, map(DSP.get_buffer_left()[i], -1, 1, 0, height * 0.5f));
                point(x, map(DSP.get_buffer_right()[i], -1, 1, height * 0.5f, height));
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
            float mLeft = 0.5f * sin(2 * PI * mFreq * mCounter / DSP.get_sample_rate());
            float mRight = 0.5f * sin(2 * PI * mFreq * mDetune * mCounter / DSP.get_sample_rate());
            pSamplesLeft[i] = mLeft * 0.7f + mRight * 0.3f;
            pSamplesRight[i] = mLeft * 0.3f + mRight * 0.7f;
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP01StereoOutput.class.getName());
    }
}
