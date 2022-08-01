import wellen.*; 

import wellen.extra.daisysp.*;

int mBeatCount;

HiHat mHiHat;

SyntheticBassDrum mBassDrum;

ReverbSc mReverb;

void settings() {
    size(640, 480);
}

void setup() {
    mHiHat = new HiHat();
    mHiHat.Init(Wellen.DEFAULT_SAMPLING_RATE);
    mHiHat.SetDecay(0.8f);
    mHiHat.SetNoisiness(0.6f);
    mBassDrum = new SyntheticBassDrum();
    mBassDrum.Init(Wellen.DEFAULT_SAMPLING_RATE);
    mReverb = new ReverbSc();
    mReverb.Init(Wellen.DEFAULT_SAMPLING_RATE);
    mReverb.SetFeedback(0.33f);
    mReverb.SetLpFreq(10000);
    DSP.start(this, 2);
    Beat.start(this, 150 * 4);
}

void draw() {
    background(255);
    noStroke();
    fill(0);
    float mScale = 0.98f * height - (mBeatCount % 4) * 50;
    circle(width * 0.5f, height * 0.5f, mScale);
    stroke(255);
    DSP.draw_buffer(g, width, height);
}

void keyPressed() {
    switch (key) {
        case '1':
            mHiHat.SetMetallicNoise(HiHat.METALLIC_NOISE_SQUARE);
            break;
        case '2':
            mHiHat.SetMetallicNoise(HiHat.METALLIC_NOISE_RING_MOD);
            break;
        case '3':
            mHiHat.SetVCA(HiHat.VCA_LINEAR);
            break;
        case '4':
            mHiHat.SetVCA(HiHat.VCA_SWING);
            break;
        case 'r':
            mHiHat.SetResonance(true);
            break;
        case 'R':
            mHiHat.SetResonance(false);
            break;
        case 's':
            mHiHat.SetSustain(true);
            break;
        case 'S':
            mHiHat.SetSustain(false);
            break;
    }
}

void mouseMoved() {
    if (keyCode == SHIFT) {
        mHiHat.SetFreq(map(mouseX, 0, width, 0, 1000));
        mHiHat.SetTone(map(mouseY, 0, height, 0, 1));
    }
    if (keyCode == ALT) {
        mHiHat.SetDecay(map(mouseX, 0, width, 0, 1));
        mHiHat.SetNoisiness(map(mouseY, 0, height, 0, 1));
    }
}

void beat(int pBeatCount) {
    mBeatCount = pBeatCount;
    if (mBeatCount % 13 != 0 && mBeatCount % 7 != 0) {
        if (mBeatCount % 4 == 0) {
            mHiHat.SetAccent(0.9f);
            mHiHat.Trig();
        } else {
            mHiHat.SetAccent(0.2f);
            mHiHat.Trig();
        }
    }
    if (mBeatCount % 4 == 0 || mBeatCount % 32 > 27) {
        mBassDrum.Trig();
    }
}

void audioblock(float[] pOutputSignalLeft, float[] pOutputSignalRight) {
    for (int i = 0; i < pOutputSignalLeft.length; i++) {
        float s = mHiHat.Process() + mBassDrum.Process() * 0.5f;
        mReverb.Process(s);
        pOutputSignalLeft[i] = mReverb.GetLeft();
        pOutputSignalRight[i] = mReverb.GetRight();
    }
}
