import wellen.*; 

/*
 * this example demonstrate how to reroute the output of the internal tone engine to `DSP` to e.g apply an effect.
 * in order to achieve this the tone engine is started without an output device and is then called in `audioblock`
 * to produce the tone signals.
 */

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
    mToneEngine = Tone.start(Wellen.TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT);
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
    Tone.instrument(0).note_on(mNote, 80);
    Tone.instrument(1).note_on(mNote + 7, 60);
    Tone.instrument(2).note_on(mNote + 12, 60);
}

void mouseReleased() {
    Tone.instrument(0).note_off();
    Tone.instrument(1).note_off();
    Tone.instrument(2).note_off();
}

void audioblock(float[] pOutputSignal) {
    mToneEngine.audioblock(pOutputSignal); /* populate buffer with samples from tone engine */
    for (int i = 0; i < pOutputSignal.length; i++) {
        mDelayID++;
        mDelayID %= mDelayBuffer.length;
        int mOffsetID = mDelayID + mDelayOffset;
        mOffsetID %= mDelayBuffer.length;
        pOutputSignal[i] = pOutputSignal[i] * (1.0f - mMix) + mDelayBuffer[mOffsetID] * mMix;
        mDelayBuffer[mDelayID] = pOutputSignal[i] * mDecay;
    }
}
