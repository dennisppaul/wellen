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
    final int mBufferSize = DSP.buffer() != null ? DSP.buffer().length : 0;
    for (int i = 0; i < mBufferSize; i++) {
        point(map(i, 0, mBufferSize, 0, width),
              map(DSP.buffer()[i], -1, 1, 0, height));
    }
}
void mouseMoved() {
    mFreq = map(mouseX, 0, width, 55, 440);
}
void audioblock(float[] pSamples) {
    for (int i = 0; i < pSamples.length; i++) {
        pSamples[i] = 0.5f * sin(2 * PI * mFreq * mCounter++ / DSP.sample_rate());
    }
}
