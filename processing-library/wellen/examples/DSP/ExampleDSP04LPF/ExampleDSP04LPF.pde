import wellen.*; 

/*
 * this example demonstrates how to use a *Low-Pass Filter* (LPF) on a sawtooth oscillator in DSP.
 */

final Wavetable mWavetable = new Wavetable();

final LowPassFilter mFilter = new LowPassFilter();

void settings() {
    size(640, 480);
}

void setup() {
    Wavetable.fill(mWavetable.get_wavetable(), Wellen.OSC_SAWTOOTH);
    mWavetable.set_frequency(2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
    mWavetable.set_amplitude(0.33f);
    DSP.start(this);
}

void draw() {
    background(255);
    DSP.draw_buffer(g, width, height);
}

void mouseMoved() {
    mFilter.set_frequency(map(mouseX, 0, width, 1.0f, Wellen.DEFAULT_SAMPLING_RATE * 0.5f));
    mFilter.set_resonance(map(mouseY, 0, height, 0.0f, 0.97f));
}

void audioblock(float[] pOutputSamples) {
    for (int i = 0; i < pOutputSamples.length; i++) {
        pOutputSamples[i] = mFilter.process(mWavetable.output());
    }
}
