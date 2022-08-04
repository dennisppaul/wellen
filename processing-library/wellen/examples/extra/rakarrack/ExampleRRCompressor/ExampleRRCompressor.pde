import wellen.*; 

import wellen.extra.rakarrack.*;

RRCompressor mCompressor;

boolean mEnableCompressor = true;

final float mMasterVolume = 0.5f;

void settings() {
    size(640, 480);
}

void setup() {
    mCompressor = new RRCompressor();
    DSP.start(this, 1, 1);
}

void draw() {
    background(255);
    DSP.draw_buffer(g, width, height);
}

void keyPressed() {
    switch (key) {
        case '1':
            mCompressor.setpreset(RRCompressor.PRESET_2_TO_1);
            break;
        case '2':
            mCompressor.setpreset(RRCompressor.PRESET_4_TO_1);
            break;
        case '3':
            mCompressor.setpreset(RRCompressor.PRESET_8_TO_1);
            break;
        case '4':
            mCompressor.setpreset(RRCompressor.PRESET_FINAL_LIMITER);
            break;
        case '5':
            mCompressor.setpreset(RRCompressor.PRESET_HARMONIC_ENHANCER);
            break;
        case '6':
            mCompressor.setpreset(RRCompressor.PRESET_BAND_COMP_BAND);
            break;
        case '7':
            mCompressor.setpreset(RRCompressor.PRESET_END_COMP_BAND);
            break;
        case ' ':
            mEnableCompressor = !mEnableCompressor;
            System.out.println("+++ " + (mEnableCompressor ? "enables" : "disabled") + " compressor.");
            break;
    }
}

void audioblock(float[] pOutputSignal, float[] pInputSignal) {
    RRUtilities.memcpy(pOutputSignal, pInputSignal, pInputSignal.length);
    if (mEnableCompressor) {
        mCompressor.out(pOutputSignal, new float[Wellen.DEFAULT_AUDIOBLOCK_SIZE]);
    }
    for (int i = 0; i < pOutputSignal.length; i++) {
        pOutputSignal[i] = Wellen.clamp(pOutputSignal[i]);
        pOutputSignal[i] *= mMasterVolume;
    }
}
