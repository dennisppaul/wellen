package wellen.examples.analysis;

import processing.core.PApplet;
import wellen.Wellen;
import wellen.analysis.FrequencyDistribution;
import wellen.analysis.Sonogram;
import wellen.dsp.DSP;
import wellen.dsp.FilterHighLowBandPass;
import wellen.dsp.Wavetable;

public class ExampleDSPAnalysis01SonogramFrequencyDistribution extends PApplet {
    //@add import wellen.analysis.*;

    /*
     * this example demonstrates how to visualize an audio signal as a sonogram anbd a frequency distribution.
     *
     * use number keys to change oscillator wave shapes and filter configuration. use mouse to change oscillator
     * frequency and amplitude. use mouse+`SHIFT` to change filter cutoff frequency and resonance.
     */

    private boolean fEnableFilter = false;
    private final FilterHighLowBandPass fFilter = new FilterHighLowBandPass();
    private FrequencyDistribution fFrequencyDistribution;
    private Sonogram fSonogram;
    private final Wavetable fWavetable = new Wavetable();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        fSonogram = new Sonogram(createGraphics(width, height));
        fFrequencyDistribution = new FrequencyDistribution(createGraphics(width, height));
        Wavetable.sine(fWavetable.get_wavetable());
        DSP.start(this);
    }

    public void draw() {
        background(0);
        fSonogram.draw();
        image(fSonogram.get_graphics(), 0, height * 0.25f, width * 0.5f, height * 0.5f);
        fFrequencyDistribution.draw();
        image(fFrequencyDistribution.get_graphics(), width * 0.5f + 1, height * 0.25f, width * 0.5f, height * 0.5f);
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = fWavetable.output();
            if (fEnableFilter) {
                output_signal[i] = fFilter.process(output_signal[i]);
            }
        }
        fSonogram.process(output_signal);
        fFrequencyDistribution.process(output_signal);
    }

    public void mouseMoved() {
        if (keyCode == SHIFT) {
            fFilter.set_frequency(map(mouseX, 0, width, 55, Wellen.DEFAULT_SAMPLING_RATE / 2.0f));
            fFilter.set_resonance(map(mouseY, 0, height, 0.0f, 0.99f));
        } else {
            fWavetable.set_frequency(map(mouseX, 0, width, 55, 440 * 4));
            fWavetable.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
        }
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                Wavetable.sine(fWavetable.get_wavetable());
                break;
            case '2':
                Wavetable.triangle(fWavetable.get_wavetable());
                break;
            case '3':
                Wavetable.sawtooth(fWavetable.get_wavetable());
                break;
            case '4':
                Wavetable.square(fWavetable.get_wavetable());
                break;
            case '5':
                randomize(fWavetable.get_wavetable());
                break;
            case '6':
                fFilter.set_mode(Wellen.FILTER_MODE_LOW_PASS);
                break;
            case '7':
                fFilter.set_mode(Wellen.FILTER_MODE_BAND_PASS);
                break;
            case '8':
                fFilter.set_mode(Wellen.FILTER_MODE_HIGH_PASS);
                break;
            case '9':
                fEnableFilter = !fEnableFilter;
                break;
        }
    }

    private void randomize(float[] wavetable) {
        for (int i = 0; i < wavetable.length; i++) {
            wavetable[i] = random(-1, 1);
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSPAnalysis01SonogramFrequencyDistribution.class.getName());
    }
}