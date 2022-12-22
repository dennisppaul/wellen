import wellen.*; 

import wellen.extra.daisysp.*;

Adsr mAdsr;

Oscillator mOscillator;

void settings() {
    size(640, 480);
}

void setup() {
    mAdsr = new Adsr();
    mAdsr.Init(Wellen.DEFAULT_SAMPLING_RATE);
    mOscillator = new Oscillator();
    mOscillator.Init(Wellen.DEFAULT_SAMPLING_RATE);
    mOscillator.SetFreq(220);
    mOscillator.SetAmp(0.75f);
    DSP.start(this);
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
    mAdsr.SetTime(Adsr.ADSR_SEG_ATTACK, map(mouseX, 0, width, 0, 2));
    mAdsr.SetSustainLevel(map(mouseY, 0, height, 0, 1));
}

void audioblock(float[] pOutputSignal) {
    for (int i = 0; i < pOutputSignal.length; i++) {
        pOutputSignal[i] = mOscillator.Process() * mAdsr.Process(mousePressed);
    }
}
