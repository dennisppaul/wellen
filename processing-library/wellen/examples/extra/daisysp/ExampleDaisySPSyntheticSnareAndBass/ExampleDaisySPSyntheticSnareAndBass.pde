import wellen.*; 

import wellen.extra.daisysp.*;

int mBeatCount;

SyntheticBassDrum mBassDrum;

SyntheticSnareDrum mSnareDrum;

void settings() {
    size(640, 480);
}

void setup() {
    mBassDrum = new SyntheticBassDrum();
    mBassDrum.Init(Wellen.DEFAULT_SAMPLING_RATE);
    mSnareDrum = new SyntheticSnareDrum();
    mSnareDrum.Init(Wellen.DEFAULT_SAMPLING_RATE);
    DSP.start(this);
    Beat.start(this, 120);
}

void draw() {
    background(255);
    noStroke();
    fill(0);
    float mScale = 0.98f * height - (mBeatCount % 2) * 50;
    circle(width * 0.5f, height * 0.5f, mScale);
    stroke(255);
    DSP.draw_buffer(g, width, height);
}

void mouseMoved() {
    mBassDrum.SetDirtiness(map(mouseX, 0, width, 0, 1));
    mBassDrum.SetAccent(map(mouseY, 0, height, 0, 1));
    if (keyCode == SHIFT) {
        mSnareDrum.SetFreq(map(mouseX, 0, width, 0, 400));
        mSnareDrum.SetAccent(map(mouseY, 0, height, 0, 1));
    }
    if (keyCode == ALT) {
        mBassDrum.SetFreq(map(mouseX, 0, width, 0, 400));
        mBassDrum.SetAccent(map(mouseY, 0, height, 0, 1));
    }
}

void beat(int pBeatCount) {
    mBeatCount = pBeatCount;
    if (mBeatCount % 2 == 1) {
        mSnareDrum.Trig();
    } else {
        mBassDrum.Trig();
    }
}

void audioblock(float[] pOutputSignal) {
    for (int i = 0; i < pOutputSignal.length; i++) {
        pOutputSignal[i] = mSnareDrum.Process() + mBassDrum.Process();
    }
}
