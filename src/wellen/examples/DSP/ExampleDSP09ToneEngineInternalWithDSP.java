package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Tone;
import wellen.ToneEngineInternal;
import wellen.Wellen;

public class ExampleDSP09ToneEngineInternalWithDSP extends PApplet {

    /*
     * this example demonstrate how to reroute the output of the internal tone engine to `DSP` to e.g apply an effect.
     * in order to achieve this the tone engine is started without an output device and is then called in `audioblock`
     * to produce the tone signals.
     */

    private final float[] mDelayBuffer = new float[4096];
    private final int mDelayOffset = 512;
    private final float mDecay = 0.9f;
    private final float mMix = 0.7f;
    private ToneEngineInternal mToneEngine;
    private int mDelayID = 0;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mToneEngine = Tone.start(Wellen.TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT);
        DSP.start(this);
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
        DSP.draw_buffer(g, width, height);
    }

    public void mousePressed() {
        int mNote = 53;
        Tone.instrument(0).note_on(mNote, 80);
        Tone.instrument(1).note_on(mNote + 7, 60);
        Tone.instrument(2).note_on(mNote + 12, 60);
    }

    public void mouseReleased() {
        Tone.instrument(0).note_off();
        Tone.instrument(1).note_off();
        Tone.instrument(2).note_off();
    }

    public void audioblock(float[] pSamples) {
        mToneEngine.audioblock(pSamples); /* populate buffer with samples from tone engine */
        for (int i = 0; i < pSamples.length; i++) {
            mDelayID++;
            mDelayID %= mDelayBuffer.length;
            int mOffsetID = mDelayID + mDelayOffset;
            mOffsetID %= mDelayBuffer.length;
            pSamples[i] = pSamples[i] * (1.0f - mMix) + mDelayBuffer[mOffsetID] * mMix;
            mDelayBuffer[mDelayID] = pSamples[i] * mDecay;
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP09ToneEngineInternalWithDSP.class.getName());
    }
}
