package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Filter;
import wellen.Wavetable;
import wellen.Wellen;

public class ExampleDSP17Filters extends PApplet {

    /*
     * this example demonstrates how to use the filter class as low-pass, high-pass and band-pass filter.
     * <p>
     * pressing keys 1 – 3 select filter modes, keys 4 + 5 change the oscillator waveform and keys 6 + 7 change the
     * oscillator frequency.
     */

    private final Wavetable mWavetable = new Wavetable();
    private final Filter mFilter = new Filter();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable.fill(mWavetable.get_wavetable(), Wellen.OSC_SAWTOOTH);
        mWavetable.set_frequency(2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
        mWavetable.set_amplitude(0.33f);
        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                mFilter.set_mode(Wellen.FILTER_MODE_LOWPASS);
                break;
            case '2':
                mFilter.set_mode(Wellen.FILTER_MODE_HIGHPASS);
                break;
            case '3':
                mFilter.set_mode(Wellen.FILTER_MODE_BANDPASS);
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

    public void mouseMoved() {
        mFilter.set_frequency(map(mouseX, 0, width, 1.0f, Wellen.DEFAULT_SAMPLING_RATE * 0.5f));
        mFilter.set_resonance(map(mouseY, 0, height, 0.0f, 0.99f));
    }

    public void audioblock(float[] pOutputSamples) {
        for (int i = 0; i < pOutputSamples.length; i++) {
            pOutputSamples[i] = mousePressed ? mWavetable.output() : mFilter.process(mWavetable.output());
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP17Filters.class.getName());
    }
}