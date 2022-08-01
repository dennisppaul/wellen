import wellen.*; 

import wellen.extra.daisysp.*;

Metro mMetro;

Pluck mPluck;

boolean mBeat;

void settings() {
    size(640, 480);
}

void setup() {
    mPluck = new Pluck();
    mPluck.Init();
    mMetro = new Metro();
    mMetro.Init(2, Wellen.DEFAULT_SAMPLING_RATE);
    DSP.start(this);
}

void draw() {
    background(255);
    noStroke();
    fill(0);
    float mScale = 0.98f * height - (mBeat ? 0 : 50);
    mBeat = false;
    circle(width * 0.5f, height * 0.5f, mScale);
    stroke(255);
    DSP.draw_buffer(g, width, height);
}

void mouseMoved() {
    mMetro.SetFreq(map(mouseX, 0, width, 0, 16));
}

void audioblock(float[] pOutputSignal) {
    for (int i = 0; i < pOutputSignal.length; i++) {
        boolean mTrigger = mMetro.Process();
        if (mTrigger) {
            mBeat = true;
        }
        pOutputSignal[i] = mPluck.Process(mTrigger);
    }
}
