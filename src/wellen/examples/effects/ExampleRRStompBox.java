package wellen.examples.effects;

import processing.core.PApplet;
import wellen.ADSR;
import wellen.Beat;
import wellen.DSP;
import wellen.Wavetable;
import wellen.Wellen;
import wellen.rakarrack.RRStompBox;

import static wellen.Wellen.DEFAULT_AUDIOBLOCK_SIZE;
import static wellen.Wellen.clamp;
import static wellen.rakarrack.RRStompBox.PRESET_CLASSIC_DISTORTION;
import static wellen.rakarrack.RRStompBox.PRESET_FUZZ;
import static wellen.rakarrack.RRStompBox.PRESET_GRUNGER;
import static wellen.rakarrack.RRStompBox.PRESET_HARD_DISTORTION;
import static wellen.rakarrack.RRStompBox.PRESET_MID_ELVE;
import static wellen.rakarrack.RRStompBox.PRESET_MORBIND_IMPALEMENT;
import static wellen.rakarrack.RRStompBox.PRESET_ODIE;
import static wellen.rakarrack.RRStompBox.PRESET_RATTY;

public class ExampleRRStompBox extends PApplet {

    private static final float[] mFreqNotes = {1.0f, 1.0f, 1.19661538f, 1.34315385f};
    private ADSR mADSR;
    private final float mBaseFrequency = 2.0f * Wellen.DEFAULT_SAMPLING_RATE / DEFAULT_AUDIOBLOCK_SIZE;
    private boolean mEnableDistortion = true;
    private int mFreqNotesCounter = mFreqNotes.length - 1;
    private float mFreqOffset = 1;
    private final float mMasterVolume = 0.5f;
    private RRStompBox mStompBox;
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

        mStompBox = new RRStompBox();

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
                mStompBox.setpreset(PRESET_ODIE);
                break;
            case '2':
                mStompBox.setpreset(PRESET_GRUNGER);
                break;
            case '3':
                mStompBox.setpreset(PRESET_HARD_DISTORTION);
                break;
            case '4':
                mStompBox.setpreset(PRESET_RATTY);
                break;
            case '5':
                mStompBox.setpreset(PRESET_CLASSIC_DISTORTION);
                break;
            case '6':
                mStompBox.setpreset(PRESET_MORBIND_IMPALEMENT);
                break;
            case '7':
                mStompBox.setpreset(PRESET_MID_ELVE);
                break;
            case '8':
                mStompBox.setpreset(PRESET_FUZZ);
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
                mStompBox.out(pOutputSignalLeft);
            } else {
                mStompBox.out(pOutputSignalLeft, pOutputSignalRight);
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
        PApplet.main(ExampleRRStompBox.class.getName());
    }
}
