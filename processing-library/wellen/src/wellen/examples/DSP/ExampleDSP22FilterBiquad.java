package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.FilterBiquad;
import wellen.dsp.Wavetable;

public class ExampleDSP22FilterBiquad extends PApplet {

    /*
     * this example demonstrates how to use the biquad filter class.
     *
     * keys `1 – 7` select filter modes, mouse changes cutoff frequency, resonance and peak gain.
     */

    private final FilterBiquad mFilter = new FilterBiquad();
    private final Wavetable mWavetable = new Wavetable();

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
        DSP.draw_buffers(g, width, height);
    }

    public void keyPressed() {
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
                mFilter.set_mode(Wellen.FILTER_MODE_NOTCH);
                break;
            case '5':
                mFilter.set_mode(Wellen.FILTER_MODE_PEAK);
                break;
            case '6':
                mFilter.set_mode(Wellen.FILTER_MODE_LOWSHELF);
                break;
            case '7':
                mFilter.set_mode(Wellen.FILTER_MODE_HIGHSHELF);
                break;
        }
    }

    public void mouseMoved() {
        mFilter.set_frequency(map(mouseX, 0, width, 1.0f, Wellen.DEFAULT_SAMPLING_RATE * 0.5f));
        mFilter.set_resonance(map(mouseY, 0, height, 0.0f, 3.99f));
        mFilter.set_peak_gain(map(mouseY, 0, height, 0.0f, 10.0f));
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = mFilter.process(random(-0.25f, 0.25f));
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP22FilterBiquad.class.getName());
    }
}
