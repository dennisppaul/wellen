package wellen.tests;

import processing.core.PApplet;
import processing.core.PGraphics;
import wellen.Tone;
import wellen.ToneEngineInternal;

public class TestToneEngineInteralWithDSPPostProcess extends PApplet {

    private ToneEngineInternal mToneEngine;
    private MMasterEffect mPostProcessing;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mToneEngine = new ToneEngineInternal();
        Tone.set_engine(mToneEngine);
        mPostProcessing = new MMasterEffect();
        mToneEngine.register_audioblock_callback(mPostProcessing);
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
        draw_buffer(g, width, height);
    }

    public void mousePressed() {
        int mNote = 53;
        Tone.instrument(0).note_on(mNote, 40);
        Tone.instrument(1).note_on(mNote + 7, 30);
        Tone.instrument(2).note_on(mNote + 12, 30);
    }

    public void mouseReleased() {
        Tone.instrument(0).note_off();
        Tone.instrument(1).note_off();
        Tone.instrument(2).note_off();
    }

    private void draw_buffer(PGraphics g, int pWidth, int pHeight) {
        if (mPostProcessing.buffer != null) {
            final int mBufferSize = mPostProcessing.buffer.length;
            for (int i = 0; i < mBufferSize - 1; i++) {
                g.line(map(i, 0, mBufferSize, 0, pWidth),
                       map(mPostProcessing.buffer[i], -1, 1, 0, pHeight),
                       map(i + 1, 0, mBufferSize, 0, pWidth),
                       map(mPostProcessing.buffer[i + 1], -1, 1, 0, pHeight));
            }
        }
    }

    private static class MMasterEffect implements ToneEngineInternal.AudioOutputCallback {

        float[] mDelayBuffer = new float[4096];
        int mDelayID = 0;
        int mDelayOffset = 512;
        float mDecay = 0.9f;
        float mMix = 0.7f;
        float[] buffer = null;

        @Override
        public void audioblock(float[][] pOutputSamples) {
            for (int i = 0; i < pOutputSamples[0].length; i++) {
                mDelayID++;
                mDelayID %= mDelayBuffer.length;
                int mOffsetID = mDelayID + mDelayOffset;
                mOffsetID %= mDelayBuffer.length;
                pOutputSamples[0][i] = pOutputSamples[0][i] * (1.0f - mMix) + mDelayBuffer[mOffsetID] * mMix;
                pOutputSamples[1][i] = pOutputSamples[0][i];
                mDelayBuffer[mDelayID] = pOutputSamples[0][i] * mDecay;
            }
            buffer = pOutputSamples[0];
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestToneEngineInteralWithDSPPostProcess.class.getName());
    }
}
