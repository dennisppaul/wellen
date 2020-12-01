import de.hfkbremen.ton.*; 
import netP5.*; 
import oscP5.*; 

float mFreq = 344.53125f;

int mCounter = 0;

float mDetune = 1.1f;

void settings() {
    size(640, 480);
}

void setup() {
    DSP.start(this, 2);
}

void draw() {
    background(255);
    stroke(0);
    final int mBufferSize = DSP.buffer_size();
    if (DSP.buffer_left() != null && DSP.buffer_right() != null) {
        for (int i = 0; i < mBufferSize; i++) {
            final float x = map(i, 0, mBufferSize, 0, width);
            point(x, map(DSP.buffer_left()[i], -1, 1, 0, height * 0.5f));
            point(x, map(DSP.buffer_right()[i], -1, 1, height * 0.5f, height));
        }
    }
}

void mouseMoved() {
    mFreq = map(mouseX, 0, width, 86.1328125f, 344.53125f);
    mDetune = map(mouseY, 0, height, 1.0f, 1.5f);
}

void audioblock(float[] pSamplesLeft, float[] pSamplesRight) {
    for (int i = 0; i < pSamplesLeft.length; i++) {
        mCounter++;
        float mLeft = 0.5f * sin(2 * PI * mFreq * mCounter / DSP.sample_rate());
        float mRight = 0.5f * sin(2 * PI * mFreq * mDetune * mCounter / DSP.sample_rate());
        pSamplesLeft[i] = mLeft * 0.7f + mRight * 0.3f;
        pSamplesRight[i] = mRight * 0.7f + mLeft * 0.3f;
    }
}
