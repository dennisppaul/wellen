import wellen.*; 

import wellen.extra.rakarrack.*;

ADSR mADSR;

final float mBaseFrequency = 2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE;

RREchotron mEchotron;

boolean mEnableEchotron = true;

boolean mIsPlaying = false;

final float mMasterVolume = 0.8f;

final Wavetable mVCO = new Wavetable();

void settings() {
    size(640, 480);
}

void setup() {
    Wavetable.triangle(mVCO.get_wavetable());
    mVCO.set_frequency(mBaseFrequency);
    mVCO.set_amplitude(0.5f);
    mADSR = new ADSR();
    mEchotron = new RREchotron();
    DSP.start(this, 2);
    Beat.start(this, 120 * 4);
}

void draw() {
    background(255);
    DSP.draw_buffer_stereo(g, width, height);
}

void beat(int pBeat) {
    if (random(1) > 0.4f) {
        if (mIsPlaying) {
            mADSR.stop();
        } else {
            mADSR.start();
            final float mFifth = (random(1) > 0.4f) ? 1 : 2.0f / 3.0f;
            mVCO.set_frequency(mBaseFrequency * (int) random(1, 5) / mFifth);
        }
        mIsPlaying = !mIsPlaying;
    }
}

void keyPressed() {
    switch (key) {
        case 'q':
            Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVESHAPE_SINE);
            break;
        case 'w':
            Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVESHAPE_TRIANGLE);
            break;
        case 'e':
            Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVESHAPE_SAWTOOTH);
            break;
        case 'r':
            Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVESHAPE_SQUARE);
            break;
        case '1':
            mEchotron.setpreset(RREchotron.PRESET_SUMMER);
            break;
        case '2':
            mEchotron.setpreset(RREchotron.PRESET_AMBIENCE);
            break;
        case '3':
            mEchotron.setpreset(RREchotron.PRESET_ARRANJER);
            break;
        case '4':
            mEchotron.setpreset(RREchotron.PRESET_SUCTION);
            break;
        case '5':
            mEchotron.setpreset(RREchotron.PRESET_SUCFLANGE);
            break;
        case '0':
            mEnableEchotron = !mEnableEchotron;
            break;
    }
}

void audioblock(float[] pOutputSignalLeft, float[] pOutputSignalRight) {
    for (int i = 0; i < pOutputSignalLeft.length; i++) {
        pOutputSignalLeft[i] = mVCO.output();
        final float mADSRValue = mADSR.output();
        pOutputSignalLeft[i] *= mADSRValue;
        pOutputSignalRight[i] = pOutputSignalLeft[i];
    }
    if (mEnableEchotron) {
        mEchotron.out(pOutputSignalLeft, pOutputSignalRight);
    }
    for (int i = 0; i < pOutputSignalLeft.length; i++) {
        pOutputSignalLeft[i] = Wellen.clamp(pOutputSignalLeft[i]);
        pOutputSignalLeft[i] *= mMasterVolume;
        pOutputSignalRight[i] = Wellen.clamp(pOutputSignalRight[i]);
        pOutputSignalRight[i] *= mMasterVolume;
    }
}
