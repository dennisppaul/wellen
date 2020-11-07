package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.Instrument;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

public class SketchExampleInstruments03FrequencyAndAmplitude extends PApplet {

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Ton.init("jsyn-minimal");
        Ton.instrument().osc_type(Instrument.SAWTOOTH);
    }

    public void draw() {
        background(255);
        noStroke();
        fill(255 - 255 * Ton.instrument().get_amplitude());
        float mScale = map(Ton.instrument().get_frequency(), 110, 440, 0.5f, 0.2f);
        ellipse(width * 0.5f, height * 0.5f, width * mScale, width * mScale);
    }

    public void mouseMoved() {
        float mFreq = map(mouseX, 0, width, 110, 440);
        float mAmp = mouseY / (float) height;
        Ton.instrument().frequency(mFreq);
        Ton.instrument().amplitude(mAmp);
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleInstruments03FrequencyAndAmplitude.class.getName());
    }
}
