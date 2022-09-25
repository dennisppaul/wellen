package wellen.tests.dsp;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Wavetable;
import wellen.Wellen;
import wellen.analysis.FrequencyDistribution;
import wellen.analysis.Sonogram;

public class TestWavetablePulse extends PApplet {

    private final Wavetable fWavetable = new Wavetable();
    private Sonogram fSonogram;
    private FrequencyDistribution fFrequencyDistribution;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        fSonogram = new Sonogram(createGraphics(width, height));
        fFrequencyDistribution = new FrequencyDistribution(createGraphics(width, height));
        Wavetable.pulse(fWavetable.get_wavetable(), 0.5f);
        fWavetable.set_amplitude(0.4f);
        fWavetable.set_frequency(2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
        DSP.start(this);
        background(0);
    }

    public void draw() {
        fSonogram.draw();
        image(fSonogram.get_graphics(), 0, 0, width * 0.5f, height * 0.5f);
        fFrequencyDistribution.draw();
        image(fFrequencyDistribution.get_graphics(), width * 0.5f + 1, 0, width * 0.5f, height * 0.5f);

        translate(0, height * 0.5f + 1);
        fill(255);
        noStroke();
        rect(0, 0, width * 0.5f, height * 0.5f);
        stroke(0);
        DSP.draw_buffers(g, width * 0.5f, height * 0.5f);
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = fWavetable.output();
        }
        fSonogram.process(pOutputSignal);
        fFrequencyDistribution.process(pOutputSignal);
    }

    public void mouseMoved() {
        Wavetable.pulse(fWavetable.get_wavetable(), map(mouseX, 0, width, 0, 1));
        fWavetable.set_frequency(0.1f * ceil(map(mouseY,
                                                 0,
                                                 height,
                                                 1,
                                                 20)) * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
    }

    public static void main(String[] args) {
        PApplet.main(TestWavetablePulse.class.getName());
    }
}