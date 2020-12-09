import wellen.*; 
import netP5.*; 
import oscP5.*; 

void settings() {
    size(640, 480);
}

void setup() {
    Wellen.dumpAudioInputAndOutputDevices();
    DSP.start(this, 1, 1);
}

void draw() {
    background(255);
    stroke(0);
    final int mBufferSize = DSP.get_buffer_size();
    DSP.draw_buffer(g, width, height);
}

void audioblock(float[] pOutputSamples, float[] pInputSamples) {
    for (int i = 0; i < pInputSamples.length; i++) {
        pOutputSamples[i] = pInputSamples[i] * 0.25f;
    }
}
