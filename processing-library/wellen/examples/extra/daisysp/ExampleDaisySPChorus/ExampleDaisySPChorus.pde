import wellen.*; 
import wellen.dsp.*; 

import wellen.extra.daisysp.*;

Pluck mPluck;

Chorus mChorus;

int mMIDINoteCounter = 0;

final int[] mMIDINotes = {36, 48, 39, 51};

void settings() {
    size(640, 480);
}

void setup() {
    mChorus = new Chorus();
    mChorus.Init(Wellen.DEFAULT_SAMPLING_RATE);
    mChorus.SetPan(0.25f, 0.75f);
    mPluck = new Pluck();
    mPluck.Init();
    mPluck.SetDecay(0.5f);
    mPluck.SetDamp(0.85f);
    DSP.start(this, 2);
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
    switch (keyCode) {
        case SHIFT:
            mChorus.SetFeedback(map(mouseX, 0, width, 0, 1), map(mouseY, 0, height, 0, 1));
            break;
        case ALT:
            mChorus.SetLfoDepth(map(mouseX, 0, width, 0, 1), map(mouseY, 0, height, 0, 1));
            break;
        case CONTROL:
            mChorus.SetLfoFreq(map(mouseX, 0, width, 0, 10), map(mouseY, 0, height, 0, 10));
            break;
        default:
            mChorus.SetDelay(map(mouseX, 0, width, 0, 1), map(mouseY, 0, height, 0, 1));
    }
}

void beat(int beatCount) {
    mPluck.Trig();
    mPluck.SetFreq(DaisySP.mtof(mMIDINotes[mMIDINoteCounter]));
    mMIDINoteCounter++;
    mMIDINoteCounter %= mMIDINotes.length;
}

void audioblock(float[] output_signalLeft, float[] output_signalRight) {
    for (int i = 0; i < output_signalLeft.length; i++) {
        mChorus.Process(mPluck.Process());
        output_signalLeft[i] = mChorus.GetLeft();
        output_signalRight[i] = mChorus.GetRight();
    }
}
