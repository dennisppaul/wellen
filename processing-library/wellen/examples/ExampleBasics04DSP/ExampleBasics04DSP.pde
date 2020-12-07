import wellen.*; 
import netP5.*; 
import oscP5.*; 

float mFreq = 440.0f;

float mAmp = 0.5f;

int mCounter = 0;

void settings() {
    size(640, 480);
}

void setup() {
    DSP.start(this);
}

void draw() {
    background(255);
    stroke(0);
    if (DSP.get_buffer() != null) {
        final int mBufferSize = DSP.get_buffer_size();
        for (int i = 0; i < mBufferSize; i++) {
            final float x = map(i, 0, mBufferSize, 0, width);
            point(x, map(DSP.get_buffer()[i], -1, 1, 0, height));
        }
    }
}

void mouseMoved() {
    mFreq = map(mouseX, 0, width, 55, 440);
    mAmp = map(mouseY, 0, height, 0, 1);
}

void audioblock(float[] pSamples) {
    for (int i = 0; i < pSamples.length; i++) {
        mCounter++;
        pSamples[i] = mAmp * sin(2 * PI * mFreq * mCounter / DSP.get_sample_rate());
    }
}
