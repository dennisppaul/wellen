import wellen.*; 
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
    DSP.draw_buffer_stereo(g, width, height);
}

void mouseMoved() {
    mFreq = map(mouseX, 0, width, 86.1328125f, 344.53125f);
    mDetune = map(mouseY, 0, height, 1.0f, 1.5f);
}

void audioblock(float[] pOutputSamplesLeft, float[] pOutputSamplesRight) {
    for (int i = 0; i < pOutputSamplesLeft.length; i++) {
        mCounter++;
        float mLeft = 0.5f * sin(2 * PI * mFreq * mCounter / DSP.get_sample_rate());
        float mRight = 0.5f * sin(2 * PI * mFreq * mDetune * mCounter / DSP.get_sample_rate());
        pOutputSamplesLeft[i] = mLeft * 0.7f + mRight * 0.3f;
        pOutputSamplesRight[i] = mLeft * 0.3f + mRight * 0.7f;
    }
}
