import wellen.*; 
import wellen.dsp.*; 

import wellen.extra.daisysp.*;

PitchShifter fPitchShifter;

Sampler fSampler;

void settings() {
    size(640, 480);
}

void setup() {
    byte[] mData = SampleDataSNARE.data;
    fSampler = new Sampler();
    fSampler.load(mData);
    fSampler.set_loop_all();
    fSampler.play();
    fPitchShifter = new PitchShifter();
    fPitchShifter.Init(Wellen.DEFAULT_SAMPLING_RATE);
    DSP.start(this);
}

void draw() {
    background(255);
    DSP.draw_buffers(g, width, height);
    line(width * 0.5f, height * 0.5f + 5, width * 0.5f, height * 0.5f - 5);
}

void mousePressed() {
    fSampler.rewind();
}

void mouseMoved() {
    fPitchShifter.SetDelSize((int) map(mouseX, 0, width, 1, 16384));
    fPitchShifter.SetTransposition(map(mouseY, 0, height, 1.0f, 24.0f));
}

void keyPressed() {
    switch (key) {
        case 'l':
        case 'L':
            fSampler.enable_loop(!fSampler.is_looping());
            break;
    }
}

void audioblock(float[] output_signal) {
    for (int i = 0; i < output_signal.length; i++) {
        output_signal[i] = fPitchShifter.Process(fSampler.output());
    }
}
