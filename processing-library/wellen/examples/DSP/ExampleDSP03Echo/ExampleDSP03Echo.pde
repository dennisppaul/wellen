import wellen.*; 

/*
 * this example demonstrates how to implement a basic echo effect in DSP.
 */
float[] mDelayBuffer = new float[4096];
int mDelayID = 0;
int mDelayOffset = 512;
float mDecay = 0.9f;
float mMix = 0.25f;

void settings() {
    size(640, 480);
}

void setup() {
    DSP.start(this, 1, 1);
}

void draw() {
    background(255);
    stroke(0);
    DSP.draw_buffer(g, width, height);
}

void mouseMoved() {
    mMix = map(mouseX, 0, width, 0.2f, 0.95f);
    mDelayOffset = (int) map(mouseY, 0, height, 1, mDelayBuffer.length);
}

void audioblock(float[] pOutputSamples, float[] pInputSamples) {
    for (int i = 0; i < pInputSamples.length; i++) {
        mDelayID++;
        mDelayID %= mDelayBuffer.length;
        int mOffsetID = mDelayID + mDelayOffset;
        mOffsetID %= mDelayBuffer.length;
        pOutputSamples[i] = pInputSamples[i] * (1.0f - mMix) + mDelayBuffer[mOffsetID] * mMix;
        mDelayBuffer[mDelayID] = pOutputSamples[i] * mDecay;
    }
}
