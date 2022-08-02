package wellen.tests;

import processing.core.PApplet;
import wellen.DSP;
import wellen.OscillatorFunction;
import wellen.Wellen;

public class ExampleDSPOscillatorFunction extends PApplet {

    private final OscillatorFunction mOscillator = new OscillatorFunction();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mOscillator.set_waveform(Wellen.WAVESHAPE_SINE);
        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mouseDragged() {
        mOscillator.set_frequency(2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
        mOscillator.set_amplitude(0.25f);
    }

    public void mouseMoved() {
        mOscillator.set_frequency(map(mouseX, 0, width, 55, 220));
        mOscillator.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                mOscillator.set_waveform(Wellen.WAVESHAPE_SINE);
                break;
            case '2':
                mOscillator.set_waveform(Wellen.WAVESHAPE_TRIANGLE);
                break;
            case '3':
                mOscillator.set_waveform(Wellen.WAVESHAPE_SAWTOOTH);
                break;
            case '4':
                mOscillator.set_waveform(Wellen.WAVESHAPE_SQUARE);
                break;
            case '5':
                mOscillator.set_waveform(Wellen.WAVESHAPE_NOISE);
                break;
        }
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mOscillator.output();
        }
    }

    private void randomize(float[] pWavetable) {
        for (int i = 0; i < pWavetable.length; i++) {
            pWavetable[i] = random(-1, 1);
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSPOscillatorFunction.class.getName());
    }
}
