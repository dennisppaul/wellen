import wellen.*; 


void settings() {
    size(640, 480);
}

void setup() {
    Wellen.dumpAudioInputAndOutputDevices();
    DSP.start(this);
    frameRate(120);
}

void draw() {
    background(255);
    DSP.draw_buffer(g, width, height);
}

void audioblock(float[] pOutputSamples) {
    for (int i = 0; i < pOutputSamples.length; i++) {
        pOutputSamples[i] = map(mouseY, 0, height, -1.0f, 1.0f);
    }
}
