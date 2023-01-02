import wellen.*; 
import wellen.dsp.*; 

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
    DSP.draw_buffers(g, width, height);
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

void audioblock(float[] output_signal, float[] pInputSignal) {
    RRUtilities.memcpy(output_signal, pInputSignal, pInputSignal.length);
    if (mEnableCompressor) {
        mCompressor.out(output_signal, new float[Wellen.DEFAULT_AUDIOBLOCK_SIZE]);
    }
    for (int i = 0; i < output_signal.length; i++) {
        output_signal[i] = Wellen.clamp(output_signal[i]);
        output_signal[i] *= mMasterVolume;
    }
}
