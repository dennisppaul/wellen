package wellen.tests;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Tone;
import wellen.Wellen;

/**
 * @deprecated do not use this method. it is just a proof of concept. depending on system configurations the output can
 * distorted. in order to post-process interal sounds from `ToneEngineInternal` see example
 * `ExampleDSP09ToneEngineInteralWithDSP` instead.
 */
public class TestRerouteInteralToneEngineWithVirtualSoundCardForDSP extends PApplet {

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
        Wellen.dumpAudioInputAndOutputDevices();
        final int OUTPUT_BUILT_IN_OUTPUT = 2;
        final int INPUT_BACKHOLE_2CH = 3;
        Tone.start(Wellen.TONE_ENGINE_INTERNAL, INPUT_BACKHOLE_2CH, 2);
        DSP.start(this, OUTPUT_BUILT_IN_OUTPUT, 1, INPUT_BACKHOLE_2CH, 1);
    }

    public void draw() {
        background(255);
        stroke(0);
        final int mBufferSize = DSP.get_buffer_size();
        if (DSP.get_buffer() != null) {
            for (int i = 0; i < mBufferSize; i++) {
                final float x = map(i, 0, mBufferSize, 0, width);
                point(x, map(DSP.get_buffer()[i], -1, 1, 0, height));
            }
        }
    }

    public void mouseMoved() {
        mMix = map(mouseX, 0, width, 0.2f, 0.95f);
        mDelayOffset = (int) map(mouseY, 0, height, 1, mDelayBuffer.length);
    }

    public void keyPressed() {
        Tone.note_on(48, 100);
    }

    public void keyReleased() {
        Tone.note_off();
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
        PApplet.main(TestRerouteInteralToneEngineWithVirtualSoundCardForDSP.class.getName());
    }
}
