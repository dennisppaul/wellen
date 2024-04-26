import wellen.*; 
import wellen.dsp.*; 

import wellen.analysis.*;
/*
 * this example demonstrates how to detect a pitch from an input signal and play the detected pitch back through
 * an oscillator.
 */
float fInputAmplification = 1.0f;
final PitchDetection fPitchDetection = new PitchDetection();
final Wavetable fWavetable = new Wavetable();
void settings() {
    size(640, 480);
}
void setup() {
    fWavetable.set_waveform(10, Wellen.WAVEFORM_SQUARE);
    DSP.start(this, 1, 1);
}
void mouseMoved() {
    fInputAmplification = Wellen.clamp(map(mouseY, 0, height * 0.75f, 2.0f, 0.0f), 0, 2);
}
void draw() {
    background(255);
    fill(0);
    ellipse(width * 0.5f,
            height * 0.5f,
            fPitchDetection.is_pitched() ? 100 : 5,
            fPitchDetection.is_pitched() ? 100 : 5);
    DSP.draw_buffers(g, width, height);
}
void audioblock(float[] output_signal, float[] pInputSignal) {
    /* amplify audio signal */
    for (int i = 0; i < pInputSignal.length; i++) {
        pInputSignal[i] *= fInputAmplification;
    }
    /* detect pitch and set oscillator */
    fPitchDetection.process(pInputSignal);
    if (fPitchDetection.is_pitched()) {
        fWavetable.set_frequency(fPitchDetection.get_pitch());
        fWavetable.set_amplitude(0.25f, 32);
    } else {
        fWavetable.set_amplitude(0.0f, 32);
    }
    /* process output signal */
    for (int i = 0; i < output_signal.length; i++) {
        output_signal[i] = fWavetable.output();
    }
}
