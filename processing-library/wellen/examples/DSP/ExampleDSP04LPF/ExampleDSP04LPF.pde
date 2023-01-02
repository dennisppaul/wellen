import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to use a *Low-Pass Filter* (LPF) on a sawtooth oscillator in DSP.
 */

final LowPassFilter mFilter = new LowPassFilter();

final Wavetable mWavetable = new Wavetable();

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
    DSP.draw_buffers(g, width, height);
}

void mouseMoved() {
    mFilter.set_frequency(map(mouseX, 0, width, 1.0f, Wellen.DEFAULT_SAMPLING_RATE * 0.5f));
    mFilter.set_resonance(map(mouseY, 0, height, 0.0f, 0.97f));
}

void audioblock(float[] output_signal) {
    for (int i = 0; i < output_signal.length; i++) {
        output_signal[i] = mFilter.process(mWavetable.output());
    }
}
