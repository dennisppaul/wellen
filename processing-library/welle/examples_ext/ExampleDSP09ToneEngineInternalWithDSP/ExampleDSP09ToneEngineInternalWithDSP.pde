import welle.*; 
import netP5.*; 
import oscP5.*; 

final float[] mDelayBuffer = new float[4096];

final int mDelayOffset = 512;

final float mDecay = 0.9f;

final float mMix = 0.7f;

ToneEngineInternal mToneEngine;

int mDelayID = 0;

void settings() {
    size(640, 480);
}

void setup() {
    mToneEngine = Tone.start(Tone.TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT);
    DSP.start(this);
}

void draw() {
    background(255);
    fill(0);
    ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
    DSP.draw_buffer(g, width, height);
}

void mousePressed() {
    int mNote = 53;
    Tone.instrument(0).note_on(mNote, 40);
    Tone.instrument(1).note_on(mNote + 7, 30);
    Tone.instrument(2).note_on(mNote + 12, 30);
}

void mouseReleased() {
    Tone.instrument(0).note_off();
    Tone.instrument(1).note_off();
    Tone.instrument(2).note_off();
}

void audioblock(float[] pSamples) {
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
