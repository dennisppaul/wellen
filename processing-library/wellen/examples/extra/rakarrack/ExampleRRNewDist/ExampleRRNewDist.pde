import wellen.*; 

import wellen.extra.rakarrack.*;

static final float[] mFreqNotes = {1.0f, 1.0f, 1.19661538f, 1.34315385f};

ADSR mADSR;

final float mBaseFrequency = 2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE;

boolean mEnableDistortion = true;

int mFreqNotesCounter = mFreqNotes.length - 1;

float mFreqOffset = 1;

final float mMasterVolume = 0.85f;

RRNewDist mNewDist;

final Wavetable mVCO1 = new Wavetable();

final Wavetable mVCO2 = new Wavetable();

void settings() {
    size(640, 480);
}

void setup() {
    Wavetable.triangle(mVCO1.get_wavetable());
    mVCO1.set_frequency(mBaseFrequency);
    mVCO1.set_amplitude(0.75f);
    Wavetable.triangle(mVCO2.get_wavetable());
    mVCO2.set_frequency(mBaseFrequency + mFreqOffset);
    mVCO2.set_amplitude(0.75f);
    mADSR = new ADSR();
    mNewDist = new RRNewDist();
    DSP.start(this, 2);
    Beat.start(this, 120 * 8);
}

void draw() {
    background(255);
    DSP.draw_buffers(g, width, height);
}

void mouseMoved() {
    mVCO1.set_amplitude(map(mouseY, 0, height, 0.0f, 1.0f));
    mVCO2.set_amplitude(map(mouseY, 0, height, 0.0f, 1.0f));
    mFreqOffset = map(mouseX, 0, width, 0.0f, 3.0f);
}

void beat(int pBeat) {
    if (pBeat % 2 == 0) {
        mADSR.start();
    } else if (pBeat % 2 == 1) {
        mADSR.stop();
    }
    if (pBeat % 16 == 0) {
        mFreqNotesCounter++;
        mFreqNotesCounter %= mFreqNotes.length;
    }
    if (pBeat % 8 == 0) {
        mVCO1.set_frequency(mBaseFrequency * mFreqNotes[mFreqNotesCounter]);
        mVCO2.set_frequency(mBaseFrequency * mFreqNotes[mFreqNotesCounter] + mFreqOffset);
    }
    if (pBeat % 8 == 4) {
        mVCO1.set_frequency(mBaseFrequency * mFreqNotes[mFreqNotesCounter] * 2);
        mVCO2.set_frequency((mBaseFrequency * mFreqNotes[mFreqNotesCounter] + mFreqOffset) * 2);
    }
}

void keyPressed() {
    switch (key) {
        case 'q':
            Wavetable.fill(mVCO2.get_wavetable(), Wellen.WAVEFORM_SINE);
            break;
        case 'w':
            Wavetable.fill(mVCO2.get_wavetable(), Wellen.WAVEFORM_TRIANGLE);
            break;
        case 'e':
            Wavetable.fill(mVCO2.get_wavetable(), Wellen.WAVEFORM_SAWTOOTH);
            break;
        case 'r':
            Wavetable.fill(mVCO2.get_wavetable(), Wellen.WAVEFORM_SQUARE);
            break;
        case 'a':
            Wavetable.fill(mVCO1.get_wavetable(), Wellen.WAVEFORM_SINE);
            break;
        case 's':
            Wavetable.fill(mVCO1.get_wavetable(), Wellen.WAVEFORM_TRIANGLE);
            break;
        case 'd':
            Wavetable.fill(mVCO1.get_wavetable(), Wellen.WAVEFORM_SAWTOOTH);
            break;
        case 'f':
            Wavetable.fill(mVCO1.get_wavetable(), Wellen.WAVEFORM_SQUARE);
            break;
        case '1':
            mNewDist.setpreset(RRNewDist.PRESET_NEW_DIST_1);
            break;
        case '2':
            mNewDist.setpreset(RRNewDist.PRESET_NEW_DIST_2);
            break;
        case '3':
            mNewDist.setpreset(RRNewDist.PRESET_NEW_DIST_3);
            break;
        case '0':
            mEnableDistortion = !mEnableDistortion;
            break;
    }
}

void audioblock(float[] pOutputSignalLeft, float[] pOutputSignalRight) {
    for (int i = 0; i < pOutputSignalLeft.length; i++) {
        final float a = mVCO1.output();
        final float b = mVCO2.output();
        final float mADSRValue = mADSR.output();
        if (mousePressed) {
            pOutputSignalLeft[i] = a + b;
            pOutputSignalLeft[i] *= 0.5f;
            pOutputSignalLeft[i] *= mADSRValue;
        } else {
            pOutputSignalLeft[i] = a * mADSRValue;
            pOutputSignalRight[i] = b * mADSRValue;
        }
    }
    if (mEnableDistortion) {
        if (mousePressed) {
            mNewDist.out(pOutputSignalLeft);
        } else {
            mNewDist.out(pOutputSignalLeft, pOutputSignalRight);
        }
    }
    for (int i = 0; i < pOutputSignalLeft.length; i++) {
        pOutputSignalLeft[i] = Wellen.clamp(pOutputSignalLeft[i]);
        pOutputSignalLeft[i] *= mMasterVolume;
        if (mousePressed) {
            pOutputSignalRight[i] = pOutputSignalLeft[i];
        } else {
            pOutputSignalRight[i] = Wellen.clamp(pOutputSignalRight[i]);
            pOutputSignalRight[i] *= mMasterVolume;
        }
    }
}
