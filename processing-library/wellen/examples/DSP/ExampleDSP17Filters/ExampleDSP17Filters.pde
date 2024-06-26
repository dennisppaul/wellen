import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to use the filter class as low-pass, high-pass and band-pass filter.
 *
 * keys `1 – 3` select filter modes, keys `4 + 5` change the oscillator waveform and keys `6 + 7` change the
 * oscillator frequency.
 */
final FilterHighLowBandPass mFilter = new FilterHighLowBandPass();
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
void keyPressed() {
    switch (key) {
        case '1':
            mFilter.set_mode(Wellen.FILTER_MODE_LOW_PASS);
            break;
        case '2':
            mFilter.set_mode(Wellen.FILTER_MODE_HIGH_PASS);
            break;
        case '3':
            mFilter.set_mode(Wellen.FILTER_MODE_BAND_PASS);
            break;
        case '4':
            Wavetable.fill(mWavetable.get_wavetable(), Wellen.OSC_SAWTOOTH);
            break;
        case '5':
            Wavetable.fill(mWavetable.get_wavetable(), Wellen.OSC_SQUARE);
            break;
        case '6':
            mWavetable.set_frequency(2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
            break;
        case '7':
            mWavetable.set_frequency(1.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
            break;
    }
}
void mouseMoved() {
    mFilter.set_frequency(map(mouseX, 0, width, 1.0f, Wellen.DEFAULT_SAMPLING_RATE * 0.5f));
    mFilter.set_resonance(map(mouseY, 0, height, 0.0f, 0.99f));
}
void audioblock(float[] output_signal) {
    for (int i = 0; i < output_signal.length; i++) {
        output_signal[i] = mousePressed ? mWavetable.output() : mFilter.process(mWavetable.output());
    }
}
