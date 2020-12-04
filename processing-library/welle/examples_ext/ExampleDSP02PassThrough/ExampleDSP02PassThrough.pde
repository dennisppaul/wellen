import welle.*; 
import netP5.*; 
import oscP5.*; 

void settings() {
    size(640, 480);
}

void setup() {
    Welle.dumpAudioInputAndOutputDevices();
    DSP.start(this, 1, 1);
}

void draw() {
    background(255);
    stroke(0);
    final int mBufferSize = DSP.buffer_size();
    if (DSP.buffer() != null) {
        for (int i = 0; i < mBufferSize; i++) {
            final float x = map(i, 0, mBufferSize, 0, width);
            point(x, map(DSP.buffer()[i], -1, 1, 0, height));
        }
    }
}

void audioblock(float[] pOutputSamples, float[] pInputSamples) {
    for (int i = 0; i < pInputSamples.length; i++) {
        pOutputSamples[i] = pInputSamples[i] * 0.25f;
    }
}
