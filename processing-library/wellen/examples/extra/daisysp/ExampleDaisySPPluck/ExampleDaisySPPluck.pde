import wellen.*; 
import wellen.dsp.*; 

import wellen.extra.daisysp.*;

Pluck mPluck;

boolean mTrigger = false;

int mMIDINoteCounter = 0;

final int[] mMIDINotes = {36, 48, 39, 51};

void settings() {
    size(640, 480);
}

void setup() {
    mPluck = new Pluck();
    mPluck.Init();
    DSP.start(this);
    Beat.start(this, 240);
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
    mPluck.SetDecay(map(mouseX, 0, width, 0, 1));
    mPluck.SetDamp(map(mouseY, 0, height, 0, 1));
}

void keyPressed() {
    switch (key) {
        case '1':
            mPluck.SetMode(Pluck.PLUCK_MODE_RECURSIVE);
            break;
        case '2':
            mPluck.SetMode(Pluck.PLUCK_MODE_WEIGHTED_AVERAGE);
            break;
    }
}

void beat(int beatCount) {
    mTrigger = true;
    mPluck.SetFreq(DaisySP.mtof(mMIDINotes[mMIDINoteCounter]));
    mMIDINoteCounter++;
    mMIDINoteCounter %= mMIDINotes.length;
}

void audioblock(float[] output_signal) {
    for (int i = 0; i < output_signal.length; i++) {
        output_signal[i] = mPluck.Process(mTrigger);
        mTrigger = false;
    }
}
