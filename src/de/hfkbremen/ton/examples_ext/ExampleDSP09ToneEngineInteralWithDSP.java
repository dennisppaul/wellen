package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.DSP;
import de.hfkbremen.ton.Ton;
import de.hfkbremen.ton.ToneEngineInternal;
import processing.core.PApplet;

public class ExampleDSP09ToneEngineInteralWithDSP extends PApplet {

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
        Ton.dumpAudioInputAndOutputDevices();
        Ton.dumpMidiInputDevices();
        Ton.dumpMidiOutputDevices();

        mToneEngine = Ton.start(Ton.TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT);
        DSP.start(this);
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, Ton.is_playing() ? 100 : 5, Ton.is_playing() ? 100 : 5);
        DSP.draw_buffer(g, width, height);
    }

    public void mousePressed() {
        int mNote = 53;
        Ton.instrument(0).note_on(mNote, 40);
        Ton.instrument(1).note_on(mNote + 7, 30);
        Ton.instrument(2).note_on(mNote + 12, 30);
    }

    public void mouseReleased() {
        Ton.instrument(0).note_off();
        Ton.instrument(1).note_off();
        Ton.instrument(2).note_off();
    }

    public void audioblock(float[] pSamples) {
        mToneEngine.audioblock(pSamples);
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
        PApplet.main(ExampleDSP09ToneEngineInteralWithDSP.class.getName());
    }
}
