import wellen.*; 

import wellen.extra.rakarrack.*;

final float mBaseFrequency = 4.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE;

float mMasterVolume = 0.5f;

final Wavetable mVCO1 = new Wavetable(512);

final Wavetable mVCO2 = new Wavetable(512);

int mWaveshapeDrive = 90;

int mWaveshapeType = RRWaveShaper.TYPE_ARCTANGENT;

RRWaveShaper mWaveshaper;

void settings() {
    size(640, 480);
}

void setup() {
    Wavetable.sine(mVCO1.get_wavetable());
    mVCO1.set_frequency(mBaseFrequency);
    mVCO1.set_amplitude(0.75f);
    Wavetable.sine(mVCO2.get_wavetable());
    mVCO2.set_frequency(mBaseFrequency * 0.99f);
    mVCO2.set_amplitude(0.25f);
    mWaveshaper = new RRWaveShaper();
    DSP.start(this);
}

void draw() {
    background(255);
    DSP.draw_buffer(g, width, height);
}

void mouseMoved() {
    mWaveshapeDrive = (int) map(mouseX, 0, width, 0.0f, 127.0f);
    mMasterVolume = map(mouseY, 0, height, 0.0f, 1.0f);
}

void keyPressed() {
    switch (key) {
        case 'q':
            Wavetable.fill(mVCO2.get_wavetable(), Wellen.WAVESHAPE_SINE);
            break;
        case 'w':
            Wavetable.fill(mVCO2.get_wavetable(), Wellen.WAVESHAPE_TRIANGLE);
            break;
        case 'e':
            Wavetable.fill(mVCO2.get_wavetable(), Wellen.WAVESHAPE_SAWTOOTH);
            break;
        case 'r':
            Wavetable.fill(mVCO2.get_wavetable(), Wellen.WAVESHAPE_SQUARE);
            break;
        case 'a':
            Wavetable.fill(mVCO1.get_wavetable(), Wellen.WAVESHAPE_SINE);
            break;
        case 's':
            Wavetable.fill(mVCO1.get_wavetable(), Wellen.WAVESHAPE_TRIANGLE);
            break;
        case 'd':
            Wavetable.fill(mVCO1.get_wavetable(), Wellen.WAVESHAPE_SAWTOOTH);
            break;
        case 'f':
            Wavetable.fill(mVCO1.get_wavetable(), Wellen.WAVESHAPE_SQUARE);
            break;
        case '1':
            mWaveshapeType--;
            if (mWaveshapeType < 0) {
                mWaveshapeType += RRWaveShaper.NUM_WAVESHAPE_TYPES;
            }
            System.out.println("WAVESHAPE TYPE: " + mWaveshapeType);
            break;
        case '2':
            mWaveshapeType++;
            mWaveshapeType %= RRWaveShaper.NUM_WAVESHAPE_TYPES;
            System.out.println("WAVESHAPE TYPE: " + mWaveshapeType);
            break;
    }
}

void audioblock(float[] pOutputSignal) {
    for (int i = 0; i < pOutputSignal.length; i++) {
        final float a = mVCO1.output();
        final float b = mVCO2.output();
        pOutputSignal[i] = a + b;
        pOutputSignal[i] *= 0.5f;
    }
    mWaveshaper.waveshapesmps(pOutputSignal.length,
                              pOutputSignal,
                              mWaveshapeType,
                              mWaveshapeDrive,
                              true);
    for (int i = 0; i < pOutputSignal.length; i++) {
        pOutputSignal[i] *= mMasterVolume;
    }
}
