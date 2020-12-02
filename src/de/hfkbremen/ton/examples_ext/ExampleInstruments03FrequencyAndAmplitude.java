package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

/**
 * this examples demonstrates how to control the tone engine’s frequency and amplitude directly ( instead of setting it
 * by a note and its velocity ). in order to control frequency and amplitude directly the ADSR envolpe, normally
 * controlling the amplitude, needs to be disabled.
 */
public class ExampleInstruments03FrequencyAndAmplitude extends PApplet {

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        /* disable ADSR */
        Ton.instrument().enable_ADSR(false);
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
        Ton.instrument().set_frequency(mFreq);
        Ton.instrument().set_amplitude(mAmp);
    }

    public static void main(String[] args) {
        PApplet.main(ExampleInstruments03FrequencyAndAmplitude.class.getName());
    }
}
