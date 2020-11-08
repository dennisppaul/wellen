package de.hfkbremen.ton.applications;

import de.hfkbremen.ton.DSP;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

public class AppDSPwithJSynToneEngine extends PApplet {

    float[] mDelayBuffer = new float[4096];
    int mDelayID = 0;
    int mDelayOffset = 512;
    float mDecay = 0.9f;
    float mMix = 0.25f;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        /* by sending the output of `Ton` to a virtual soundcard ( e.g *Blackhole* in MacOS ) the generated sounds
         * can be used as an input for `DSP`. this presents a pipeline to apply e.g filters and effects ( e.g an echo )
         * to internally generated sounds.
         */
        DSP.dumpAudioDevices();
        final int OUTPUT_BUILT_IN_OUTPUT = 2;
        final int INPUT_BACKHOLE_2CH = 3;
        Ton.start("jsyn", INPUT_BACKHOLE_2CH, 2);
        DSP.start(this, OUTPUT_BUILT_IN_OUTPUT, 1, INPUT_BACKHOLE_2CH, 1);
    }

    public void draw() {
        background(255);
        stroke(0);
        final int mBufferSize = DSP.buffer_size();
        if (DSP.buffer() != null) {
            for (int i = 0; i < mBufferSize; i++) {
                final float x = map(i, 0, mBufferSize, 0, width);
                point(x, map(DSP.buffer()[i], -1, 1, 0, height));
            }
        }
    }

    public void mouseMoved() {
        mMix = map(mouseX, 0, width, 0.2f, 0.95f);
        mDelayOffset = (int) map(mouseY, 0, height, 1, mDelayBuffer.length);
    }

    public void keyPressed() {
        Ton.note_on(48, 100);
    }

    public void keyReleased() {
        Ton.note_off();
    }

    public void audioblock(float[] pOutputSamples, float[] pInputSamples) {
        for (int i = 0; i < pInputSamples.length; i++) {
            mDelayID++;
            mDelayID %= mDelayBuffer.length;
            int mOffsetID = mDelayID + mDelayOffset;
            mOffsetID %= mDelayBuffer.length;
            pOutputSamples[i] = pInputSamples[i] * (1.0f - mMix) + mDelayBuffer[mOffsetID] * mMix;
            mDelayBuffer[mDelayID] = pOutputSamples[i] * mDecay;
        }
    }

    public static void main(String[] args) {
        PApplet.main(AppDSPwithJSynToneEngine.class.getName());
    }
}
