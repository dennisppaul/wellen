import wellen.*; 
import wellen.dsp.*; 

import wellen.extra.daisysp.*;

OscillatorDaisy mOscillator;

void settings() {
    size(640, 480);
}

void setup() {
    mOscillator = new OscillatorDaisy();
    mOscillator.Init(Wellen.DEFAULT_SAMPLING_RATE);
    DSP.start(this);
}

void draw() {
    background(255);
    DSP.draw_buffers(g, width, height);
}

void mouseDragged() {
    mOscillator.SetFreq(2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
    mOscillator.SetAmp(0.25f);
}

void mouseMoved() {
    mOscillator.SetFreq(map(mouseX, 0, width, 55, 220));
    mOscillator.SetAmp(map(mouseY, 0, height, 0.0f, 0.9f));
}

void keyPressed() {
    switch (key) {
        case '1':
            mOscillator.SetWaveform(OscillatorDaisy.WAVE_FORM.WAVE_SIN);
            break;
        case '2':
            mOscillator.SetWaveform(OscillatorDaisy.WAVE_FORM.WAVE_TRI);
            break;
        case '3':
            mOscillator.SetWaveform(OscillatorDaisy.WAVE_FORM.WAVE_SAW);
            break;
        case '4':
            mOscillator.SetWaveform(OscillatorDaisy.WAVE_FORM.WAVE_RAMP);
            break;
        case '5':
            mOscillator.SetWaveform(OscillatorDaisy.WAVE_FORM.WAVE_SQUARE);
            break;
        case '6':
            mOscillator.SetWaveform(OscillatorDaisy.WAVE_FORM.WAVE_POLYBLEP_TRI);
            break;
        case '7':
            mOscillator.SetWaveform(OscillatorDaisy.WAVE_FORM.WAVE_POLYBLEP_SAW);
            break;
        case '8':
            mOscillator.SetWaveform(OscillatorDaisy.WAVE_FORM.WAVE_POLYBLEP_SQUARE);
            break;
    }
}

void audioblock(float[] output_signal) {
    for (int i = 0; i < output_signal.length; i++) {
        output_signal[i] = mOscillator.Process();
    }
}
