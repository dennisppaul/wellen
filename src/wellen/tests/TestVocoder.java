package wellen.tests;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Vocoder;
import wellen.Wavetable;
import wellen.Wellen;

public class TestVocoder extends PApplet {

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
        DSP.start(this, 1, 1);
    }

    public void draw() {
        background(255);
        stroke(0);
        final int mBufferSize = DSP.get_buffer_size();
        DSP.draw_buffer(g, width, height);
    }

    public void audioblock(float[] pOutputSamples, float[] pInputSamples) {
        float[] mCarrierBuffer = new float[Wellen.DEFAULT_AUDIOBLOCK_SIZE];
        for (int i = 0; i < mCarrierBuffer.length; i++) {
            mCarrierBuffer[i] = mVocoderCarrierOsc.output();
        }
        mVocoder.voclib_process(mCarrierBuffer, pInputSamples, pOutputSamples, Wellen.DEFAULT_AUDIOBLOCK_SIZE);
    }

    public void mouseMoved() {
        mVocoder.voclib_set_formant_shift(map(mouseX, 0, width, 0.25f, 2.25f));
        mVocoder.voclib_set_reaction_time(map(mouseY, 0, height, 0.002f, 0.102f));
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
        if (key == '5') {
            mVocoderCarrierOsc.set_frequency(440.0f);
        }
        if (key == '0') {
            mVocoder.voclib_reset_history();
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestVocoder.class.getName());
    }
}