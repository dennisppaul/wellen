package wellen.tests;

import processing.core.PApplet;
import wellen.DSP;
import wellen.FFT;
import wellen.Tone;
import wellen.ToneEngineInternal;
import wellen.Wellen;

public class TestFFT extends PApplet {

    private static final int BUFFER_COLLECTOR_SIZE = 2;
    private float[] mSampleBuffer = new float[0];
    private FFT mFFT;
    private ToneEngineInternal mToneEngine;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mToneEngine = Tone.start(Wellen.TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT);
        Tone.instrument(0).enable_ADSR(false);

        mFFT = new FFT(Wellen.DEFAULT_AUDIOBLOCK_SIZE * BUFFER_COLLECTOR_SIZE, Wellen.DEFAULT_SAMPLING_RATE);
        mFFT.window(FFT.HAMMING);
        DSP.start(this, 1, 1);
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 50 : 5, Tone.is_playing() ? 50 : 5);
        DSP.draw_buffer(g, width, height);

        float[] mExtremum = Wellen.get_extremum(DSP.get_buffer());
        ellipse(width * 0.5f, map(mExtremum[0], -1.0f, 1.0f, 0, height), 10, 10);
        ellipse(width * 0.5f, map(mExtremum[1], -1.0f, 1.0f, 0, height), 20, 20);

        float[] mSpectrum = mFFT.getSpectrum();
        final float mZoom = 6;
        for (int i = 0; i < mSpectrum.length / mZoom; i += BUFFER_COLLECTOR_SIZE) {
            float x = map(i, 0, mSpectrum.length / mZoom, 10, width - 10);
            float y = map(mSpectrum[i], 0.0f, 150.0f, height - 10, 10);
            line(x, height - 10, x, y);
        }
    }

    public void mouseMoved() {
        Tone.instrument(0).set_frequency(map(mouseX, 0, width, 440.0f / 8.0f, 440.0f * 8.0f));
        Tone.instrument(0).set_amplitude(map(mouseY, 0, height, 0.0f, 0.5f));
    }

    public void audioblock(float[] pOutputSamples, float[] pInputSamples) {
        mToneEngine.audioblock(pOutputSamples);
//        for (int i = 0; i < pInputSamples.length; i++) {
//            pOutputSamples[i] = pInputSamples[i] * 0.25f;
//        }
        mSampleBuffer = concat(mSampleBuffer, pOutputSamples);
        if (mSampleBuffer.length == Wellen.DEFAULT_AUDIOBLOCK_SIZE * BUFFER_COLLECTOR_SIZE) {
            mFFT.forward(mSampleBuffer);
            mSampleBuffer = new float[0];
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestFFT.class.getName());
    }
}
