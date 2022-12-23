import wellen.*; 
import wellen.dsp.*; 

import wellen.extra.rakarrack.*;

ADSR mADSR;

final float mBaseFrequency = 4.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE;

RRChorus mChorus;

boolean mEnableChorus = true;

boolean mIsPlaying = false;

final float mMasterVolume = 0.75f;

final Wavetable mVCO = new Wavetable();

void settings() {
    size(640, 480);
}

void setup() {
    Wavetable.triangle(mVCO.get_wavetable());
    mVCO.set_frequency(mBaseFrequency);
    mVCO.set_amplitude(0.75f);
    mADSR = new ADSR();
    mChorus = new RRChorus();
    DSP.start(this, 2);
    Beat.start(this, 120 * 4);
}

void draw() {
    background(255);
    DSP.draw_buffers(g, width, height);
}

void beat(int pBeat) {
    if (random(1) > (mIsPlaying ? 0.4f : 0.2f)) {
        if (mIsPlaying) {
            mADSR.stop();
        } else {
            mADSR.start();
            mVCO.set_frequency(mBaseFrequency * (int) random(1, 5));
        }
        mIsPlaying = !mIsPlaying;
    }
}

void keyPressed() {
    switch (key) {
        case 'q':
            Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVEFORM_SINE);
            break;
        case 'w':
            Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVEFORM_TRIANGLE);
            break;
        case 'e':
            Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVEFORM_SAWTOOTH);
            break;
        case 'r':
            Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVEFORM_SQUARE);
            break;
        case '1':
            mChorus.setpreset(RRChorus.PRESET_CHORUS_1);
            break;
        case '2':
            mChorus.setpreset(RRChorus.PRESET_CHORUS_2);
            break;
        case '3':
            mChorus.setpreset(RRChorus.PRESET_CHORUS_3);
            break;
        case '4':
            mChorus.setpreset(RRChorus.PRESET_CELESTE_1);
            break;
        case '5':
            mChorus.setpreset(RRChorus.PRESET_CELESTE_2);
            break;
        case '6':
            mChorus.setpreset(RRChorus.PRESET_FLANGE_1);
            break;
        case '7':
            mChorus.setpreset(RRChorus.PRESET_FLANGE_2);
            break;
        case '8':
            mChorus.setpreset(RRChorus.PRESET_FLANGE_3);
            break;
        case '9':
            mChorus.setpreset(RRChorus.PRESET_FLANGE_4);
            break;
        case '0':
            mChorus.setpreset(RRChorus.PRESET_FLANGE_5);
            break;
        case ' ':
            mEnableChorus = !mEnableChorus;
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
    if (mEnableChorus) {
        mChorus.out(pOutputSignalLeft, pOutputSignalRight);
    }
    for (int i = 0; i < pOutputSignalLeft.length; i++) {
        pOutputSignalLeft[i] = Wellen.clamp(pOutputSignalLeft[i]);
        pOutputSignalLeft[i] *= mMasterVolume;
        pOutputSignalRight[i] = Wellen.clamp(pOutputSignalRight[i]);
        pOutputSignalRight[i] *= mMasterVolume;
    }
}
