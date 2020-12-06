package wellen.tests;

//import com.sun.media.sound.FFT;

import processing.core.PApplet;
import wellen.DSP;
import wellen.FFT;
import wellen.Tone;
import wellen.ToneEngineInternal;
import wellen.Wellen;

public class TestFFT extends PApplet {

    private static final int BUFFER_COLLECTOR_SIZE = 8;
    private float[] mSampleBuffer = new float[0];
    private FFT mFFT;
    private ToneEngineInternal mToneEngine;
    private int mNote = 53;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mToneEngine = Tone.start(Wellen.TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT);

        mFFT = new FFT(Wellen.DEFAULT_AUDIOBLOCK_SIZE * BUFFER_COLLECTOR_SIZE, Wellen.DEFAULT_SAMPLING_RATE);
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
        for (int i = 0; i < mSpectrum.length; i += BUFFER_COLLECTOR_SIZE) {
            float x = map(i, 0, mSpectrum.length, 10, width - 10);
            float y = map(mSpectrum[i], 0.0f, 150.0f, 10, height - 10);
            line(x, 10, x, y);
        }
    }

    public void mousePressed() {
        Tone.instrument(0).note_on(mNote, 40);
        Tone.instrument(1).note_on(mNote + 7, 30);
        Tone.instrument(2).note_on(mNote + 12, 30);
    }

    public void mouseReleased() {
        Tone.instrument(0).note_off();
        Tone.instrument(1).note_off();
        Tone.instrument(2).note_off();
    }

    public void mouseMoved() {
        mNote = (int) map(mouseX, 0, width, 24, 60);
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
