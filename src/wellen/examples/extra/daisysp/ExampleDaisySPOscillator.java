package wellen.examples.extra.daisysp;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Wellen;
import wellen.extra.daisysp.Oscillator;

public class ExampleDaisySPOscillator extends PApplet {

    private Oscillator mOscillator;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mOscillator = new Oscillator();
        mOscillator.Init(Wellen.DEFAULT_SAMPLING_RATE);
        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mouseDragged() {
        mOscillator.SetFreq(2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
        mOscillator.SetAmp(0.25f);
    }

    public void mouseMoved() {
        mOscillator.SetFreq(map(mouseX, 0, width, 55, 220));
        mOscillator.SetAmp(map(mouseY, 0, height, 0.0f, 0.9f));
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                mOscillator.SetWaveform(Oscillator.WAVE_FORM.WAVE_SIN);
                break;
            case '2':
                mOscillator.SetWaveform(Oscillator.WAVE_FORM.WAVE_TRI);
                break;
            case '3':
                mOscillator.SetWaveform(Oscillator.WAVE_FORM.WAVE_SAW);
                break;
            case '4':
                mOscillator.SetWaveform(Oscillator.WAVE_FORM.WAVE_RAMP);
                break;
            case '5':
                mOscillator.SetWaveform(Oscillator.WAVE_FORM.WAVE_SQUARE);
                break;
            case '6':
                mOscillator.SetWaveform(Oscillator.WAVE_FORM.WAVE_POLYBLEP_TRI);
                break;
            case '7':
                mOscillator.SetWaveform(Oscillator.WAVE_FORM.WAVE_POLYBLEP_SAW);
                break;
            case '8':
                mOscillator.SetWaveform(Oscillator.WAVE_FORM.WAVE_POLYBLEP_SQUARE);
                break;
        }
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mOscillator.Process();
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDaisySPOscillator.class.getName());
    }
}
