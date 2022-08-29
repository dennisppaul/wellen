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

    private final Svf filt1 = new Svf();
    private final Svf filt2 = new Svf();
    private final Svf filt3 = new Svf();
    float formant1, formant2, formant3;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mOsc.set_frequency(140);
        mOsc.set_amplitude(0.33f);
        mOsc.set_waveform(Wellen.WAVESHAPE_SAWTOOTH);

        final float mResonance = 0.6f;
        filt1.Init(Wellen.DEFAULT_SAMPLING_RATE);
        filt1.SetRes(mResonance);
        filt1.SetDrive(0.8f);

        filt2.Init(Wellen.DEFAULT_SAMPLING_RATE);
        filt2.SetRes(mResonance + 0.2f);
        filt2.SetDrive(0.7f);

        filt3.Init(Wellen.DEFAULT_SAMPLING_RATE);
        filt3.SetRes(mResonance + 0.4f);
        filt3.SetDrive(0.5f);

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

            // from https://www.soundonsound.com/techniques/formant-synthesis
            switch (key) {
                case 'E': // leap
                    formant1 = 270;
                    formant2 = 2300;
                    formant3 = 3000;
                    break;
                case 'o': // loop
                    formant1 = 300;
                    formant2 = 870;
                    formant3 = 2350;
                    break;
                case 'i': // lip
                    formant1 = 400;
                    formant2 = 2000;
                    formant3 = 2550;
                    break;
                case 'e': // let
                    formant1 = 530;
                    formant2 = 1850;
                    formant3 = 2500;
                    break;
                case 'u': // lug
                    formant1 = 640;
                    formant2 = 1200;
                    formant3 = 2400;
                    break;
                case 'a': // lap
                    formant1 = 660;
                    formant2 = 1700;
                    formant3 = 2400;
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
            filt3.SetFreq(formant3);
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
            filt3.Process(pOutputSignal[i]);

            float band1 = filt1.Band();
            float band2 = filt2.Band();
            float band3 = filt3.Band();

            pOutputSignal[i] = (band1 + band2 + band3);
            pOutputSignal[i] *= 0.5f;
            pOutputSignal[i] *= mADSR.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestVowelizer.class.getName());
    }
}
