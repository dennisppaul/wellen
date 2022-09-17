package wellen.tests;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Pan;
import wellen.Signal;
import wellen.Tone;
import wellen.ToneEngineDSP;
import wellen.Wellen;

public class TestPanning extends PApplet {

    private ToneEngineDSP mToneEngine;
    private final Pan mPan = new Pan();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mToneEngine = Tone.start(Wellen.TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT);
        DSP.start(this, 2);
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
        DSP.draw_buffers(g, width, height);
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                mPan.set_pan_type(Wellen.PAN_LINEAR);
                break;
            case '2':
                mPan.set_pan_type(Wellen.PAN_SQUARE_LAW);
                break;
            case '3':
                mPan.set_pan_type(Wellen.PAN_SINE_LAW);
                break;
        }
    }

    public void mousePressed() {
        int mNote = 53;
        Tone.instrument(0).note_on(mNote, 80);
    }

    public void mouseReleased() {
        Tone.instrument(0).note_off();
    }

    public void mouseDragged() {
        mPan.set_panning(map(mouseX, 0, width, -1.0f, 1.0f));
    }

    public void audioblock(float[] pOutputSignalLeft, float[] pOutputSignalRight) {
        float[] mOutputSignal = new float[Wellen.DEFAULT_AUDIOBLOCK_SIZE];
        mToneEngine.audioblock(mOutputSignal);
        for (int i = 0; i < mOutputSignal.length; i++) {
            Signal s = mPan.process(mOutputSignal[i]);
            pOutputSignalLeft[i] = s.signal[Wellen.SIGNAL_LEFT];
            pOutputSignalRight[i] = s.signal[Wellen.SIGNAL_RIGHT];
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestPanning.class.getName());
    }
}