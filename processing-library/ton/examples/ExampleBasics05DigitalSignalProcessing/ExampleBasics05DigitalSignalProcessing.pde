import de.hfkbremen.ton.*; 
import controlP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 


float mFreq = 440.0f;
int mCounter = 0;
void settings() {
    size(640, 480);
}
void setup() {
    DSP.start(this);
}
void draw() {
    background(255);
    fill(0);
    noStroke();
    final float mScale = mFreq / 440.f * width * 0.25f;
    ellipse(width * 0.5f, height * 0.5f, mScale, mScale);
    stroke(0);
    final int mBufferSize = DSP.buffer_size();
    for (int i = 0; i < mBufferSize; i++) {
        final float x = map(i, 0, mBufferSize, 0, width);
        point(x, map(DSP.buffer_left()[i], -1, 1, 0, height * 0.5f));
        point(x, map(DSP.buffer_right()[i], -1, 1, height * 0.5f, height));
    }
}
void mouseMoved() {
    mFreq = map(mouseX, 0, width, 55, 440);
}
void audioblock(float[] pSamplesLeft, float[] pSamplesRight) {
    float mDetune = map(mouseY, 0, height, 0.9f, 1.1f);
    for (int i = 0; i < pSamplesLeft.length; i++) {
        mCounter++;
        pSamplesLeft[i] = 0.5f * sin(2 * PI * mFreq * mCounter / DSP.sample_rate());
        pSamplesRight[i] = 0.5f * sin(2 * PI * mFreq * mDetune * mCounter / DSP.sample_rate());
    }
}
