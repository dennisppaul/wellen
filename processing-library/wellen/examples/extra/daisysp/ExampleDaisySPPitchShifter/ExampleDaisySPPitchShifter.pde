import wellen.*; 
import wellen.dsp.*; 

import wellen.extra.daisysp.*;

PitchShifter mPitchShifter;

Sampler mSampler;

void settings() {
    size(640, 480);
}

void setup() {
    byte[] mData = SampleDataSNARE.data;
    mSampler = new Sampler();
    mSampler.load(mData);
    mSampler.enable_loop(true);
    mSampler.start();
    mPitchShifter = new PitchShifter();
    mPitchShifter.Init(Wellen.DEFAULT_SAMPLING_RATE);
    DSP.start(this);
}

void draw() {
    background(255);
    DSP.draw_buffers(g, width, height);
    line(width * 0.5f, height * 0.5f + 5, width * 0.5f, height * 0.5f - 5);
}

void mousePressed() {
    mSampler.rewind();
}

void mouseMoved() {
    mPitchShifter.SetDelSize((int) map(mouseX, 0, width, 1, 16384));
    mPitchShifter.SetTransposition(map(mouseY, 0, height, 1.0f, 24.0f));
}

void keyPressed() {
    switch (key) {
        case 'l':
        case 'L':
            mSampler.enable_loop(!mSampler.is_looping());
            break;
    }
}

void audioblock(float[] output_signal) {
    for (int i = 0; i < output_signal.length; i++) {
        output_signal[i] = mPitchShifter.Process(mSampler.output());
    }
}
