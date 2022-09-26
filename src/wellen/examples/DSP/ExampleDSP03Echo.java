package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.dsp.DSP;

public class ExampleDSP03Echo extends PApplet {

    /*
     * this example demonstrates how to implement a basic echo effect in DSP.
     *
     * note that a microphone or some other line in must be available to run this example.
     */

    float[] mDelayBuffer = new float[4096];
    int mDelayID = 0;
    int mDelayOffset = 512;
    float mDecay = 0.9f;
    float mMix = 0.25f;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        DSP.start(this, 1, 1);
    }

    public void draw() {
        background(255);
        stroke(0);
        DSP.draw_buffers(g, width, height);
    }

    public void mouseMoved() {
        mMix = map(mouseX, 0, width, 0.2f, 0.95f);
        mDelayOffset = (int) map(mouseY, 0, height, 1, mDelayBuffer.length);
    }

    public void audioblock(float[] pOutputSignal, float[] pInputSignal) {
        for (int i = 0; i < pInputSignal.length; i++) {
            mDelayID++;
            mDelayID %= mDelayBuffer.length;
            int mOffsetID = mDelayID + mDelayOffset;
            mOffsetID %= mDelayBuffer.length;
            pOutputSignal[i] = pInputSignal[i] * (1.0f - mMix) + mDelayBuffer[mOffsetID] * mMix;
            mDelayBuffer[mDelayID] = pOutputSignal[i] * mDecay;
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP03Echo.class.getName());
    }
}
