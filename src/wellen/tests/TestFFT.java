package wellen.tests;

import processing.core.PApplet;
import wellen.FFT;
import wellen.Tone;
import wellen.ToneEngineDSP;
import wellen.Wellen;
import wellen.dsp.DSP;

public class TestFFT extends PApplet {

    //    private static final int BUFFER_COLLECTOR_SIZE = 1;
//    private float[] mSampleBuffer = new float[0];
//    private FFT mFFT;
    private ToneEngineDSP mToneEngine;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mToneEngine = Tone.start(Wellen.TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT);
        Tone.instrument(0).enable_ADSR(false);
        Tone.instrument(1).enable_ADSR(false);

//        mFFT = new FFT(Wellen.DEFAULT_AUDIOBLOCK_SIZE * BUFFER_COLLECTOR_SIZE, Wellen.DEFAULT_SAMPLING_RATE);
//        mFFT.window(FFT.HAMMING);
        DSP.start(this, 1, 1);
    }

    public void draw() {
        background(255);
        fill(0);
        stroke(0);
        ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 50 : 5, Tone.is_playing() ? 50 : 5);
        DSP.draw_buffers(g, width, height);

        float[] mExtremum = Wellen.get_extremum(DSP.get_output_buffer());
        ellipse(width * 0.5f, map(mExtremum[0], -1.0f, 1.0f, 0, height), 10, 10);
        ellipse(width * 0.5f, map(mExtremum[1], -1.0f, 1.0f, 0, height), 20, 20);

//        float[] mSpectrum = mFFT.getSpectrum();
//        mFFT.specSize();
//        mFFT.getBand(0):
//        final float mZoom = 6;
//        stroke(0);
//        for (int i = 0; i < mSpectrum.length / mZoom; i += BUFFER_COLLECTOR_SIZE) {
//            float x = map(i, 0, mSpectrum.length / mZoom, 10, width - 10);
//            float y = map(mSpectrum[i], 0.0f, 150.0f, height - 10, 10);
//            line(x, height - 10, x, y);
//        }
//        stroke(255, 127, 0);
//        for (int i = 0; i < 64; i++) {
//            float mFreq = i * 27.5f;
//            float mAmp = mFFT.getFreq(mFreq);
//            float x = map(i, 1, 32, 10, width - 10) + 1;
//            float y = map(mAmp, 0.0f, 150.0f, height - 10, 10);
//            line(x, height - 10, x, y);
//        }

        for (int i = 0; i < FFT.get_spectrum().length; i++) {
            float x = map(i, 0, FFT.get_spectrum().length, 10, width - 10);
            float y = map(FFT.get_spectrum()[i], 0.0f, 150.0f, height - 10, 10);
            line(x, height - 10, x, y);
        }

        stroke(255, 127, 0);
        for (int i = 0; i < 64; i++) {
            float mFreq = i * 27.5f;
            float mAmp = FFT.get_frequency(mFreq);
            float x = map(i, 1, 32, 10, width - 10) + 1;
            float y = map(mAmp, 0.0f, 150.0f, height - 10, 10);
            line(x, height - 10, x, y);
        }
    }

    public void mouseMoved() {
        Tone.instrument(0).set_frequency(map(mouseX, 0, width, 440.0f / 8.0f, 440.0f * 8.0f));
        Tone.instrument(0).set_amplitude(map(mouseY, 0, height, 0.0f, 0.5f));
        Tone.instrument(1).set_frequency(Tone.instrument(0).get_frequency() * 2);
        Tone.instrument(1).set_amplitude(Tone.instrument(0).get_amplitude() * 0.5f);
    }

    public void audioblock(float[] output_signal, float[] pInputSignal) {
        mToneEngine.audioblock(output_signal);
        FFT.perform_forward_transform(output_signal);

//        for (int i = 0; i < pInputSignal.length; i++) {
//            output_signal[i] = pInputSignal[i] * 0.25f;
//        }

//        mSampleBuffer = concat(mSampleBuffer, output_signal);
//        if (mSampleBuffer.length == Wellen.DEFAULT_AUDIOBLOCK_SIZE * BUFFER_COLLECTOR_SIZE) {
//            mFFT.forward(mSampleBuffer);
//            mSampleBuffer = new float[0];
//        }
    }

    public static void main(String[] args) {
        PApplet.main(TestFFT.class.getName());
    }
}
