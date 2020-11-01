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
    stroke(0);
    if (DSP.buffer() != null) {
        final int mBufferSize = DSP.buffer_size();
        for (int i = 0; i < mBufferSize; i++) {
            final float x = map(i, 0, mBufferSize, 0, width);
            point(x, map(DSP.buffer()[i], -1, 1, 0, height));
        }
    }
}
void mouseMoved() {
    mFreq = map(mouseX, 0, width, 55, 440);
}
void audioblock(float[] pSamples) {
    float mDetune = map(mouseY, 0, height, 1.0f, 1.5f);
    for (int i = 0; i < pSamples.length; i++) {
        mCounter++;
        pSamples[i] = 0.5f * sin(2 * PI * mFreq * mCounter / DSP.sample_rate());
    }
}
