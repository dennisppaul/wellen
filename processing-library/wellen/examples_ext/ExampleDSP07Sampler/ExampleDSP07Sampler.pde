import wellen.*; 
import netP5.*; 
import oscP5.*; 

Sampler mSampler;

void settings() {
    size(640, 480);
}

void setup() {
    byte[] mData = SampleDataSNARE.data;
    // alternatively load data with `loadBytes("audio.raw")` ( raw format, 32bit float )
    mSampler = new Sampler();
    mSampler.load(mData);
    mSampler.loop(false);
    Wellen.dumpAudioInputAndOutputDevices();
    DSP.start(this);
}

void draw() {
    background(255);
    DSP.draw_buffer(g, width, height);
}

void mousePressed() {
    mSampler.rewind();
}

void mouseMoved() {
    mSampler.set_speed(map(mouseX, 0, width, 0, 32));
    mSampler.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
}

void keyPressed() {
    switch (key) {
        case 'l':
        case 'L':
            mSampler.loop(true);
            break;
        default:
            mSampler.loop(false);
    }
}

void audioblock(float[] pOutputSamples) {
    for (int i = 0; i < pOutputSamples.length; i++) {
        pOutputSamples[i] = mSampler.output();
    }
}
