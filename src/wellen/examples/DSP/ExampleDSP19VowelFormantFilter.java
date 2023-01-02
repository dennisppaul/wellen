package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.Wellen;
import wellen.dsp.ADSR;
import wellen.dsp.DSP;
import wellen.dsp.Oscillator;
import wellen.dsp.OscillatorFunction;
import wellen.dsp.VowelFormantFilter;

public class ExampleDSP19VowelFormantFilter extends PApplet {

    /*
     * this example demonstrates how to use the vowel format filter. it shapes a spectral rich signal ( e.g square or
     * sawtooth or even white noise ) into a sound that resembles a vowel formed by a human vocal cords.
     *
     * keys `1 – 3` select signal shapes, keys `a, e, i, o, u` select vowels, mouse changes the frequency of the
     * oscillator.
     */

    private final ADSR mADSR = new ADSR();
    private final VowelFormantFilter mFormantFilter = new VowelFormantFilter();
    private boolean mIsKeyPressed = false;
    private final Oscillator mOsc = new OscillatorFunction();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mOsc.set_frequency(55);
        mOsc.set_amplitude(0.33f);
        mOsc.set_waveform(Wellen.WAVEFORM_SQUARE);

        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
    }

    public void mousePressed() {
        mADSR.start();
    }

    public void mouseReleased() {
        mADSR.stop();
    }

    public void mouseDragged() {
        mFormantFilter.lerp_vowel(VowelFormantFilter.VOWEL_I, VowelFormantFilter.VOWEL_O, map(mouseY, 0, height, 0, 1));
        mouseMoved();
    }

    public void mouseMoved() {
        mOsc.set_frequency(map(mouseX, 0, width, 1, 110));
    }

    public void keyPressed() {
        if (!mIsKeyPressed) {
            mIsKeyPressed = true;

            switch (key) {
                case 'a':
                    mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_A);
                    break;
                case 'e':
                    mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_E);
                    break;
                case 'i':
                    mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_I);
                    break;
                case 'o':
                    mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_O);
                    break;
                case 'u':
                    mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_U);
                    break;
                case '1':
                    mOsc.set_waveform(Wellen.WAVEFORM_SQUARE);
                    break;
                case '2':
                    mOsc.set_waveform(Wellen.WAVEFORM_SAWTOOTH);
                    break;
                case '3':
                    mOsc.set_waveform(Wellen.WAVEFORM_NOISE);
                    break;
            }

            mADSR.start();
        }
    }

    public void keyReleased() {
        if (mIsKeyPressed) {
            mIsKeyPressed = false;
            mADSR.stop();
        }
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = mOsc.output();
            output_signal[i] = mFormantFilter.process(output_signal[i]);
            output_signal[i] *= 0.5f;
            output_signal[i] *= mADSR.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP19VowelFormantFilter.class.getName());
    }
}
