package wellen.tests;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Tone;
import wellen.ToneEngineInternal;
import wellen.Wellen;
import wellen.extern.rakarrack.RRRecognizer;

import static wellen.extern.rakarrack.RRUtilities.memcpy;

public class TestNoteRecognizer extends PApplet {

    private RRRecognizer mRecognize;
    private ToneEngineInternal mToneEngine;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mRecognize = new RRRecognizer(0.6f);
        mToneEngine = Tone.start(Wellen.TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT);
        mToneEngine.instrument().enable_ADSR(false);
        mToneEngine.instrument().set_oscillator_type(Wellen.WAVESHAPE_SINE);

        DSP.start(this, 2, 1);
        textFont(createFont("Courier", 10));
    }

    public void draw() {
        background(255);
        noStroke();
        fill(255 - 255 * Tone.instrument().get_amplitude());
        float mScale = map(Tone.instrument().get_frequency(), 110, 440, 0.5f, 0.2f);
        ellipse(width * 0.5f, height * 0.5f, width * mScale, width * mScale);

        fill(0);
        translate(10, 20);
        text("ORIGINAL FREQ : " + Tone.instrument().get_frequency(), 0, 10);
        text("ACTUAL FREQ   : " + mRecognize.get_frequency(), 0, 20);
        text("NEAREST FREQ  : " + mRecognize.get_nearest_frequency(), 0, 30);
        text("MIDI NOTE     : " + mRecognize.get_MIDI_note(), 0, 40);
        text("NOTE          : " + mRecognize.get_note(), 0, 50);
        text("NOTE STR      : " + mRecognize.get_note_string() + mRecognize.get_octave(), 0, 60);
    }

    public void mouseDragged() {
        mouseMoved();
    }

    public void mouseMoved() {
        float mFreq = map(mouseX, 0, width, 55, 440);
        float mAmp = map(mouseY, 0, height, 0, 1);
        Tone.instrument().set_frequency(mFreq);
        Tone.instrument().set_amplitude(mAmp);
    }

    public void audioblock(float[] pOutputSignalLeft, float[] pOutputSignalRight, float[] pInputSignal) {
        if (!mousePressed) {
            mToneEngine.audioblock(pOutputSignalLeft, pOutputSignalRight);
        } else {
            memcpy(pOutputSignalLeft, pInputSignal, pInputSignal.length);
            memcpy(pOutputSignalRight, pInputSignal, pInputSignal.length);
        }
        mRecognize.schmittFloat(pOutputSignalLeft, pOutputSignalRight);
    }

    public static void main(String[] args) {
        PApplet.main(TestNoteRecognizer.class.getName());
    }
}