package wellen.tests;

import processing.core.PApplet;
import wellen.Note;
import wellen.Tone;
import wellen.ToneEngineDSP;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.tests.analysis.PitchProcessor;

public class TestPitchDetectors extends PApplet {

    private ToneEngineDSP mToneEngine;
    private PitchProcessor mPitchProcessor = new PitchProcessor();
    private int mNote = 24;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        textFont(createFont("Courier", 11));
        mToneEngine = Tone.start(Wellen.TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT);
        Tone.instrument(0).set_oscillator_type(Wellen.WAVEFORM_TRIANGLE);
        DSP.start(this, 1, 1);
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
        DSP.draw_buffers(g, width, height);
        float mY = 20;
        float mStep = 10;
        text("NOTE         : " + mNote, 20, mY += mStep);
        text("NOTE FREQ    : " + Note.note_to_frequency(mNote), 20, mY += mStep);
        text("DETECTOR     : " + mPitchProcessor.getDetectorName(), 20, mY += mStep);
        text("DETECTED FREQ: " + mPitchProcessor.getPitch(), 20, mY += mStep);
        text("DETECTED PROB: " + mPitchProcessor.getProbability(), 20, mY += mStep);
        text("IS PITCHED   : " + mPitchProcessor.isPitched(), 20, mY += mStep);
    }

    public void mousePressed() {
        if (mNote++ > 127) {
            mNote = 24;
        }
        Tone.instrument(0).note_on(mNote, 80);
    }

    public void mouseReleased() {
        Tone.instrument(0).note_off();
    }

    public void keyReleased() {
        switch (key) {
            case '1':
                mPitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.MPM);
                break;
            case '2':
                mPitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.DYNAMIC_WAVELET);
                break;
            case '3':
                mPitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN);
                break;
            case '4':
                mPitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.AMDF);
                break;
            case '5':
                mPitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.YIN);
                break;
        }
    }

    public void audioblock(float[] pOutputSignal, float[] pInputSignal) {
        mToneEngine.audioblock(pOutputSignal);
        mPitchProcessor.process(pInputSignal);
    }

    public static void main(String[] args) {
        PApplet.main(TestPitchDetectors.class.getName());
    }
}
