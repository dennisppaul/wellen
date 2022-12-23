import wellen.*; 
import wellen.dsp.*; 

import wellen.extra.daisysp.*;
// TODO(does not sound good … might be something fishy with the random functions)

Drip mPluck;

void settings() {
    size(640, 480);
}

void setup() {
    mPluck = new Drip();
    mPluck.Init(Wellen.DEFAULT_SAMPLING_RATE, 0.1f);
    DSP.start(this);
    Beat.start(this, 60);
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

void beat(int pBeatCount) {
    mPluck.Trig();
}

void audioblock(float[] pOutputSignal) {
    for (int i = 0; i < pOutputSignal.length; i++) {
        pOutputSignal[i] = mPluck.Process();
    }
}
