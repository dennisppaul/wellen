import wellen.*; 

import wellen.extra.daisysp.*;

StringVoice mStringVoice;

int mMIDINoteCounter = 0;

final int[] mMIDINotes = {36, 48, 39, 51};

void settings() {
    size(640, 480);
}

void setup() {
    mStringVoice = new StringVoice();
    mStringVoice.Init(Wellen.DEFAULT_SAMPLING_RATE);
    DSP.start(this);
    Beat.start(this, 240);
}

void draw() {
    background(0);
    noStroke();
    fill(255);
    float mScale = 0.98f * height;
    circle(width * 0.5f, height * 0.5f, mScale);
    stroke(0);
    DSP.draw_buffer(g, width, height);
}

void mouseMoved() {
    if (keyCode == SHIFT) {
        mStringVoice.SetAccent(map(mouseX, 0, width, 0, 1));
        mStringVoice.SetStructure(map(mouseY, 0, height, 0, 1));
    }
    if (keyCode == ALT) {
        mStringVoice.SetBrightness(map(mouseX, 0, width, 0, 1));
        mStringVoice.SetDamping(map(mouseY, 0, height, 0, 1));
    }
}

void keyPressed() {
    switch (key) {
        case 's':
            mStringVoice.SetSustain(true);
            break;
        case 'S':
            mStringVoice.SetSustain(false);
            break;
    }
}

void beat(int pBeatCount) {
    mStringVoice.Trig();
    mStringVoice.SetFreq(DaisySP.mtof(mMIDINotes[mMIDINoteCounter]));
    mMIDINoteCounter++;
    mMIDINoteCounter %= mMIDINotes.length;
}

void audioblock(float[] pOutputSignal) {
    for (int i = 0; i < pOutputSignal.length; i++) {
        pOutputSignal[i] = mStringVoice.Process();
    }
}
