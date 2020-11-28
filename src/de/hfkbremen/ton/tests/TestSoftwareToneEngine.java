package de.hfkbremen.ton.tests;

import de.hfkbremen.ton.Ton;
import de.hfkbremen.ton.ToneEngineSoftware;
import processing.core.PApplet;

import static de.hfkbremen.ton.DSP.DEFAULT_AUDIOBLOCK_SIZE;

public class TestSoftwareToneEngine extends PApplet {

    private ToneEngineSoftware mToneEngine;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mToneEngine = new ToneEngineSoftware();
        Ton.set_engine(mToneEngine);
        mToneEngine.register_audioblock_callback(new MMasterEffect());
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, Ton.isPlaying() ? 100 : 5, Ton.isPlaying() ? 100 : 5);
    }

    public void mousePressed() {
        int mNote = 45 + (int) random(0, 12);
        Ton.instrument(0).note_on(mNote, 40);
        Ton.instrument(1).note_on(mNote + 7, 30);
        Ton.instrument(2).note_on(mNote + 12, 30);
    }

    public void mouseReleased() {
        Ton.instrument(0).note_off();
        Ton.instrument(1).note_off();
        Ton.instrument(2).note_off();
    }

    private static class MMasterEffect implements ToneEngineSoftware.AudioOutputCallback {

        float[] mDelayBuffer = new float[4096];
        int mDelayID = 0;
        int mDelayOffset = 512;
        float mDecay = 0.9f;
        float mMix = 0.9f;

        @Override
        public void audioblock(float[][] pOutputSamples) {
            for (int i = 0; i < DEFAULT_AUDIOBLOCK_SIZE; i++) {
                mDelayID++;
                mDelayID %= mDelayBuffer.length;
                int mOffsetID = mDelayID + mDelayOffset;
                mOffsetID %= mDelayBuffer.length;
                pOutputSamples[0][i] = pOutputSamples[0][i] * (1.0f - mMix) + mDelayBuffer[mOffsetID] * mMix;
                pOutputSamples[1][i] = pOutputSamples[0][i];
                mDelayBuffer[mDelayID] = pOutputSamples[0][i] * mDecay;
            }
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestSoftwareToneEngine.class.getName());
    }
}
