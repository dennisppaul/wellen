package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

/**
 * this example demonstrates how to use the built-in low-frequency oscillators (LFOs) to change the sound
 * characteristics of an instrument.
 */
public class ExampleInstruments04LFOs extends PApplet {

    private boolean mToggleLFOParameterSelect = true;
    private boolean mEnableFrequencyLFO = false;
    private boolean mEnableAmplitudeLFO = false;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Ton.start();
    }

    public void draw() {
        background(255);
        noStroke();
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, Ton.is_playing() ? 100 : 5, Ton.is_playing() ? 100 : 5);

        ellipse(40, height * 0.5f, mEnableFrequencyLFO ? 20 : 5, mEnableFrequencyLFO ? 20 : 5);
        ellipse(80, height * 0.5f, mEnableAmplitudeLFO ? 20 : 5, mEnableAmplitudeLFO ? 20 : 5);
        stroke(0);
        noFill();
        ellipse(mToggleLFOParameterSelect ? 40 : 80, height * 0.5f, 25, 25);
    }

    public void mousePressed() {
        int mNote = 45 + (int) random(0, 12);
        Ton.note_on(mNote, 100);
    }

    public void mouseReleased() {
        Ton.note_off();
    }

    public void mouseDragged() {
        if (mToggleLFOParameterSelect) {
            Ton.instrument().set_frequency_LFO_amplitude(map(mouseY, 0, height, 0.0f, 50.0f));
            Ton.instrument().set_frequency_LFO_frequency(map(mouseX, 0, width, 0.0f, 50.0f));
        } else {
            Ton.instrument().set_amplitude_LFO_amplitude(map(mouseY, 0, height, 0.0f, 1.0f));
            Ton.instrument().set_amplitude_LFO_frequency(map(mouseX, 0, width, 0.0f, 50.0f));
        }
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                mEnableFrequencyLFO = !mEnableFrequencyLFO;
                Ton.instrument().enable_frequency_LFO(mEnableFrequencyLFO);
                break;
            case '2':
                mEnableAmplitudeLFO = !mEnableAmplitudeLFO;
                Ton.instrument().enable_amplitude_LFO(mEnableAmplitudeLFO);
                break;
            case ' ':
                mToggleLFOParameterSelect = !mToggleLFOParameterSelect;
                break;
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleInstruments04LFOs.class.getName());
    }
}
