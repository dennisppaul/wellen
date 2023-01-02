import wellen.*; 
import wellen.dsp.*; 

import wellen.extra.daisysp.*;

boolean mBeat;

Metro mMetro;

Pluck mPluck;

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
    DSP.draw_buffers(g, width, height);
}

void mouseMoved() {
    mMetro.SetFreq(map(mouseX, 0, width, 0, 16));
}

void audioblock(float[] output_signal) {
    for (int i = 0; i < output_signal.length; i++) {
        boolean mTrigger = mMetro.Process();
        if (mTrigger) {
            mBeat = true;
        }
        output_signal[i] = mPluck.Process(mTrigger);
    }
}
