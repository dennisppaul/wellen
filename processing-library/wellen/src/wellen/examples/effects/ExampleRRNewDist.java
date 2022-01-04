package wellen.examples.effects;

import processing.core.PApplet;
import wellen.ADSR;
import wellen.Beat;
import wellen.DSP;
import wellen.Wavetable;
import wellen.Wellen;
import wellen.effect.RRNewDist;

import static wellen.Wellen.DEFAULT_AUDIOBLOCK_SIZE;
import static wellen.Wellen.clamp;
import static wellen.effect.RRNewDist.PRESET_NEW_DIST_1;
import static wellen.effect.RRNewDist.PRESET_NEW_DIST_2;
import static wellen.effect.RRNewDist.PRESET_NEW_DIST_3;

public class ExampleRRNewDist extends PApplet {

    private static final float[] mFreqNotes = {1.0f, 1.0f, 1.19661538f, 1.34315385f};
    private ADSR mADSR;
    private final float mBaseFrequency = 2.0f * Wellen.DEFAULT_SAMPLING_RATE / DEFAULT_AUDIOBLOCK_SIZE;
    private boolean mEnableDistortion = true;
    private int mFreqNotesCounter = mFreqNotes.length - 1;
    private float mFreqOffset = 1;
    private final float mMasterVolume = 0.85f;
    private RRNewDist mNewDist;
    private final Wavetable mVCO1 = new Wavetable();
    private final Wavetable mVCO2 = new Wavetable();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable.triangle(mVCO1.get_wavetable());
        mVCO1.set_frequency(mBaseFrequency);
        mVCO1.set_amplitude(0.75f);

        Wavetable.triangle(mVCO2.get_wavetable());
        mVCO2.set_frequency(mBaseFrequency + mFreqOffset);
        mVCO2.set_amplitude(0.75f);

        mADSR = new ADSR();

        mNewDist = new RRNewDist();

        DSP.start(this, 2);
        Beat.start(this, 120 * 8);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer_stereo(g, width, height);
    }

    public void mouseMoved() {
        mVCO1.set_amplitude(map(mouseY, 0, height, 0.0f, 1.0f));
        mVCO2.set_amplitude(map(mouseY, 0, height, 0.0f, 1.0f));
        mFreqOffset = map(mouseX, 0, width, 0.0f, 3.0f);
    }

    public void beat(int pBeat) {
        if (pBeat % 2 == 0) {
            mADSR.start();
        } else if (pBeat % 2 == 1) {
            mADSR.stop();
        }
        if (pBeat % 16 == 0) {
            mFreqNotesCounter++;
            mFreqNotesCounter %= mFreqNotes.length;
        }
        if (pBeat % 8 == 0) {
            mVCO1.set_frequency(mBaseFrequency * mFreqNotes[mFreqNotesCounter]);
            mVCO2.set_frequency(mBaseFrequency * mFreqNotes[mFreqNotesCounter] + mFreqOffset);
        }
        if (pBeat % 8 == 4) {
            mVCO1.set_frequency(mBaseFrequency * mFreqNotes[mFreqNotesCounter] * 2);
            mVCO2.set_frequency((mBaseFrequency * mFreqNotes[mFreqNotesCounter] + mFreqOffset) * 2);
        }
    }

    public void keyPressed() {
        switch (key) {
            case 'q':
                Wavetable.fill(mVCO2.get_wavetable(), Wellen.WAVESHAPE_SINE);
                break;
            case 'w':
                Wavetable.fill(mVCO2.get_wavetable(), Wellen.WAVESHAPE_TRIANGLE);
                break;
            case 'e':
                Wavetable.fill(mVCO2.get_wavetable(), Wellen.WAVESHAPE_SAWTOOTH);
                break;
            case 'r':
                Wavetable.fill(mVCO2.get_wavetable(), Wellen.WAVESHAPE_SQUARE);
                break;
            case 'a':
                Wavetable.fill(mVCO1.get_wavetable(), Wellen.WAVESHAPE_SINE);
                break;
            case 's':
                Wavetable.fill(mVCO1.get_wavetable(), Wellen.WAVESHAPE_TRIANGLE);
                break;
            case 'd':
                Wavetable.fill(mVCO1.get_wavetable(), Wellen.WAVESHAPE_SAWTOOTH);
                break;
            case 'f':
                Wavetable.fill(mVCO1.get_wavetable(), Wellen.WAVESHAPE_SQUARE);
                break;
            case '1':
                mNewDist.setpreset(PRESET_NEW_DIST_1);
                break;
            case '2':
                mNewDist.setpreset(PRESET_NEW_DIST_2);
                break;
            case '3':
                mNewDist.setpreset(PRESET_NEW_DIST_3);
                break;
            case '0':
                mEnableDistortion = !mEnableDistortion;
                break;
        }
    }

    public void audioblock(float[] pOutputSignalLeft, float[] pOutputSignalRight) {
        for (int i = 0; i < pOutputSignalLeft.length; i++) {
            final float a = mVCO1.output();
            final float b = mVCO2.output();
            final float mADSRValue = mADSR.output();
            if (mousePressed) {
                pOutputSignalLeft[i] = a + b;
                pOutputSignalLeft[i] *= 0.5f;
                pOutputSignalLeft[i] *= mADSRValue;
            } else {
                pOutputSignalLeft[i] = a * mADSRValue;
                pOutputSignalRight[i] = b * mADSRValue;
            }
        }
        if (mEnableDistortion) {
            if (mousePressed) {
                mNewDist.out(pOutputSignalLeft);
            } else {
                mNewDist.out(pOutputSignalLeft, pOutputSignalRight);
            }
        }
        for (int i = 0; i < pOutputSignalLeft.length; i++) {
            pOutputSignalLeft[i] = clamp(pOutputSignalLeft[i]);
            pOutputSignalLeft[i] *= mMasterVolume;
            if (mousePressed) {
                pOutputSignalRight[i] = pOutputSignalLeft[i];
            } else {
                pOutputSignalRight[i] = clamp(pOutputSignalRight[i]);
                pOutputSignalRight[i] *= mMasterVolume;
            }
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleRRNewDist.class.getName());
    }
}
