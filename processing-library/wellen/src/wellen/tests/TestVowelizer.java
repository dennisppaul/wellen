package wellen.tests;

import processing.core.PApplet;
import wellen.ADSR;
import wellen.DSP;
import wellen.Oscillator;
import wellen.OscillatorFunction;
import wellen.Wellen;
import wellen.extra.daisysp.Svf;

public class TestVowelizer extends PApplet {

    private final Oscillator mOsc = new OscillatorFunction();
    private final ADSR mADSR = new ADSR();
    private boolean mIsKeyPressed = false;

    private Svf filt1 = new Svf();
    private Svf filt2 = new Svf();
    float formant1, formant2;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mOsc.set_frequency(140);
        mOsc.set_amplitude(0.33f);
        mOsc.set_waveform(Wellen.WAVESHAPE_SAWTOOTH);

        filt1.Init(Wellen.DEFAULT_SAMPLING_RATE);
        filt1.SetRes(0.85f);
        filt1.SetDrive(0.8f);

        filt2.Init(Wellen.DEFAULT_SAMPLING_RATE);
        filt2.SetRes(0.85f);
        filt2.SetDrive(0.8f);

        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mouseMoved() {
        mOsc.set_frequency(map(mouseX, 0, width, 1, 110));
    }

    public void keyPressed() {
        if (!mIsKeyPressed) {
            mIsKeyPressed = true;

            switch (key) {
                case 'a':
                    formant1 = 200;
                    formant2 = 500;
                    break;
                case 'e':
                    formant1 = 300;
                    formant2 = 1000;
                    break;
                case 'i':
                    formant1 = 400;
                    formant2 = 1500;
                    break;
                case 'o':
                    formant1 = 450;
                    formant2 = 2000;
                    break;
                case 'u':
                    formant1 = 500;
                    formant2 = 2500;
                    break;
                case '1':
                    mOsc.set_waveform(Wellen.WAVESHAPE_SQUARE);
                    break;
                case '2':
                    mOsc.set_waveform(Wellen.WAVESHAPE_SAWTOOTH);
                    break;
                case '3':
                    mOsc.set_waveform(Wellen.WAVESHAPE_NOISE);
                    break;
            }
            filt1.SetFreq(formant1);
            filt2.SetFreq(formant2);
            mADSR.start();
        }
    }

    public void keyReleased() {
        if (mIsKeyPressed) {
            mIsKeyPressed = false;
            mADSR.stop();
        }
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mOsc.output();

            filt1.Process(pOutputSignal[i]);
            filt2.Process(pOutputSignal[i]);

            float band1 = filt1.Band();
            float band2 = filt2.Band();

            pOutputSignal[i] = (band1 + band2);
            pOutputSignal[i] *= 0.5f;
            pOutputSignal[i] *= mADSR.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestVowelizer.class.getName());
    }
}
