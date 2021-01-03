package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Vocoder;
import wellen.Wavetable;
import wellen.Wellen;

public class ExampleDSP18Vocoder extends PApplet {

    /*
     * this example demonstrate how to use a vocoder.
     *
     * a vocoder superimposes a modulator signal ( e.g a human voice ) onto a carrier signal ( preferably a signal with
     * a lot of harmonics e.g sawtooth oscillator or white noise or some instrument )
     *
     * this vocoder algorithm is an adaptation of [voclib](https://github.com/blastbay/voclib) by Philip Bennefall.
     */

    private Vocoder mVocoder;
    private Wavetable mVocoderCarrierOsc;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wellen.dumpAudioInputAndOutputDevices();

        mVocoderCarrierOsc = new Wavetable();
        Wavetable.fill(mVocoderCarrierOsc.get_wavetable(), Wellen.WAVESHAPE_SAWTOOTH);
        mVocoderCarrierOsc.set_frequency(55);
        mVocoderCarrierOsc.set_amplitude(1.0f);

        mVocoder = new Vocoder(24, 4, Wellen.DEFAULT_SAMPLING_RATE, 1);
        mVocoder.set_volume(8);
        DSP.start(this, 1, 1);
    }

    public void draw() {
        background(255);
        stroke(0);
        final int mBufferSize = DSP.get_buffer_size();
        DSP.draw_buffer(g, width, height);
    }

    public void mouseMoved() {
        mVocoder.set_formant_shift(map(mouseX, 0, width, 0.25f, 2.5f));
        mVocoder.set_reaction_time(map(mouseY, 0, height, 0.002f, 0.1f));
    }

    public void keyPressed() {
        if (key == '1') {
            mVocoderCarrierOsc.set_frequency(22.5f);
        }
        if (key == '2') {
            mVocoderCarrierOsc.set_frequency(55.0f);
        }
        if (key == '3') {
            mVocoderCarrierOsc.set_frequency(110.0f);
        }
        if (key == '4') {
            mVocoderCarrierOsc.set_frequency(220.0f);
        }
    }

    public void audioblock(float[] pOutputSamples, float[] pInputSamples) {
        for (int i = 0; i < pInputSamples.length; i++) {
            float mCarrier = mVocoderCarrierOsc.output();
            pOutputSamples[i] = mVocoder.process(mCarrier, pInputSamples[i]);
        }
        /* note, there is also a faster audio block processing method available `process(float[], float[], float[])` */
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP18Vocoder.class.getName());
    }
}