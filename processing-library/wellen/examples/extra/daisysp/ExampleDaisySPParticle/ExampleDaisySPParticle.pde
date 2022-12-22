import wellen.*; 

import wellen.extra.daisysp.*;

Particle mOscillator;

void settings() {
    size(640, 480);
}

void setup() {
    mOscillator = new Particle();
    mOscillator.Init(Wellen.DEFAULT_SAMPLING_RATE);
    DSP.start(this);
}

void draw() {
    background(255);
    DSP.draw_buffers(g, width, height);
}

void mouseMoved() {
    switch (keyCode) {
        case CONTROL:
            mOscillator.SetFreq(map(mouseX, 0, width, 27.5f, 880));
            mOscillator.SetRandomFreq(map(mouseY, 0, height, 0.1f, 880));
            break;
        case SHIFT:
            mOscillator.SetGain(map(mouseX, 0, width, 0, 1));
            mOscillator.SetSpread(map(mouseY, 0, height, 0, 100));
            break;
        case ALT:
            mOscillator.SetResonance(map(mouseX, 0, width, 0, 1));
            mOscillator.SetDensity(map(mouseY, 0, height, 0, 3));
            break;
    }
}

void keyPressed() {
    switch (key) {
        case 's':
            mOscillator.SetSync(true);
            break;
        case 'S':
            mOscillator.SetSync(false);
            break;
    }
}

void keyReleased() {
    keyCode = 0;
}

void audioblock(float[] pOutputSignal) {
    for (int i = 0; i < pOutputSignal.length; i++) {
        pOutputSignal[i] = mOscillator.Process();
    }
}

void randomize(float[] pWavetable) {
    for (int i = 0; i < pWavetable.length; i++) {
        pWavetable[i] = random(-1, 1);
    }
}
