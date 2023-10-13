import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to
 */

BeatDSP fBeat;

ADSR fADSR;

Wavetable fWavetable;

Waveshaper fWaveshaper;

int fWaveshaperForm = Wellen.WAVESHAPER_ATAN;

void settings() {
    size(640, 480);
}

void setup() {
    fBeat = BeatDSP.start(this);
    fBeat.set_bpm(480);
    fADSR = new ADSR();
    fADSR.set_adsr(0.01f, 0.05f, 0.0f, 0.0f);
    fWavetable = new Wavetable();
    fWavetable.set_waveform(4, Wellen.WAVEFORM_SAWTOOTH);
    fWavetable.set_frequency(110.0f);
    fWavetable.set_amplitude(0.5f);
    fWaveshaper = new Waveshaper();
    fWaveshaper.set_type(fWaveshaperForm);
    DSP.start(this);
}

void draw() {
    background(255);
    background(255);
    fill(0);
    circle(width * 0.5f, height * 0.5f, fWaveshaper.get_type() * 20 + 10);
    stroke(0);
    DSP.draw_buffers(g, width, height);
}

void keyPressed() {
    switch (key) {
        case '+':
            fWaveshaperForm++;
            if (fWaveshaperForm >= Wellen.NUM_OF_WAVESHAPER_FORMS) {
                fWaveshaperForm = Wellen.NUM_OF_WAVESHAPER_FORMS - 1;
            }
            fWaveshaper.set_type(fWaveshaperForm);
            break;
        case '-':
            fWaveshaperForm--;
            if (fWaveshaperForm < 0) {
                fWaveshaperForm = 0;
            }
            fWaveshaper.set_type(fWaveshaperForm);
            break;
    }
}

void mouseMoved() {
    fWaveshaper.set_amount(map(mouseX, 0, width, 0, 50));
    fWaveshaper.set_output_gain(map(mouseY, 0, height, 0, 1));
}

void audioblock(float[] output_signal) {
    for (int i = 0; i < output_signal.length; i++) {
        fBeat.tick();
        output_signal[i] = fWaveshaper.process(fWavetable.output() * fADSR.output());
    }
}

void beat(int beatCount) {
    fADSR.start();
    final int mModifier = (beatCount / 8) % 4;
    fWavetable.set_frequency(mModifier * 55 + 110);
}
