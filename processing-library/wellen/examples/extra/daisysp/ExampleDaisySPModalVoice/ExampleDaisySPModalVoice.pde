import wellen.*; 
import wellen.dsp.*; 

import wellen.extra.daisysp.*;

ModalVoice mModalVoice;

void settings() {
    size(640, 480);
}

void setup() {
    mModalVoice = new ModalVoice();
    mModalVoice.Init(Wellen.DEFAULT_SAMPLING_RATE);
    DSP.start(this);
    Beat.start(this, 120);
}

void draw() {
    background(255);
    noStroke();
    fill(0);
    float mScale = 0.98f * height;
    circle(width * 0.5f, height * 0.5f, mScale);
    stroke(255);
    DSP.draw_buffers(g, width, height);
}

void mouseMoved() {
    mModalVoice.SetFreq(map(mouseX, 0, width, 0, 400));
    mModalVoice.SetAccent(map(mouseY, 0, height, 0, 0.3f));
}

void mouseDragged() {
    mModalVoice.SetStructure(map(mouseX, 0, width, 0, 0.6f));
    mModalVoice.SetBrightness(map(mouseY, 0, height, 0, 0.8f));
    mModalVoice.SetDamping(map(mouseY, 0, height, 0, 0.6f));
}

void beat(int pBeatCount) {
    mModalVoice.Trig();
}

void audioblock(float[] pOutputSignal) {
    for (int i = 0; i < pOutputSignal.length; i++) {
        pOutputSignal[i] = mModalVoice.Process();
    }
}
