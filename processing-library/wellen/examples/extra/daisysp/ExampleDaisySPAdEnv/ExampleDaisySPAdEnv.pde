import wellen.*; 
import wellen.dsp.*; 

import wellen.extra.daisysp.*;

AdEnv mAdEnv;

Adsr mAdsr;

Oscillator mOscillator;

void settings() {
    size(640, 480);
}

void setup() {
    mAdEnv = new AdEnv();
    mAdEnv.Init(Wellen.DEFAULT_SAMPLING_RATE);
    mAdEnv.SetTime(AdEnv.ADENV_SEG_ATTACK, 0.5f);
    mAdEnv.SetTime(AdEnv.ADENV_SEG_DECAY, 0.5f);
    mAdEnv.SetMin(110);
    mAdEnv.SetMax(880);
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

void mousePressed() {
    mAdEnv.Trigger();
}

void mouseMoved() {
    mAdEnv.SetMin(map(mouseX, 0, width, 55, 880));
    mAdEnv.SetMax(map(mouseY, 0, height, 55, 880));
}

void audioblock(float[] output_signal) {
    for (int i = 0; i < output_signal.length; i++) {
        mOscillator.SetFreq(mAdEnv.Process());
        output_signal[i] = mOscillator.Process() * mAdsr.Process(mousePressed);
    }
}
