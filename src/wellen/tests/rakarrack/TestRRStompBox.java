package wellen.tests.rakarrack;

import processing.core.PApplet;
import wellen.ADSR;
import wellen.Beat;
import wellen.DSP;
import wellen.Wavetable;
import wellen.Wellen;

import static wellen.Wellen.DEFAULT_AUDIOBLOCK_SIZE;
import static wellen.Wellen.clamp;
import static wellen.tests.rakarrack.RRStompBox.PRESET_CLASSIC_DISTORTION;
import static wellen.tests.rakarrack.RRStompBox.PRESET_FUZZ;
import static wellen.tests.rakarrack.RRStompBox.PRESET_GRUNGER;
import static wellen.tests.rakarrack.RRStompBox.PRESET_HARD_DISTORTION;
import static wellen.tests.rakarrack.RRStompBox.PRESET_MID_ELVE;
import static wellen.tests.rakarrack.RRStompBox.PRESET_MORBIND_IMPALEMENT;
import static wellen.tests.rakarrack.RRStompBox.PRESET_ODIE;
import static wellen.tests.rakarrack.RRStompBox.PRESET_RATTY;

public class TestRRStompBox extends PApplet {

    private ADSR mADSR;
    private final float mBaseFrequency = 4.0f * Wellen.DEFAULT_SAMPLING_RATE / DEFAULT_AUDIOBLOCK_SIZE;
    private boolean mEnableDistortion = true;
    private float mFreqOffset = 5;
    private final float mMasterVolume = 0.5f;
    private RRStompBox mStompBox;
    private final Wavetable mVCO1 = new Wavetable(512);
    private final Wavetable mVCO2 = new Wavetable(512);

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable.sine(mVCO1.get_wavetable());
        mVCO1.set_frequency(mBaseFrequency);
        mVCO1.set_amplitude(0.75f);

        Wavetable.sine(mVCO2.get_wavetable());
        mVCO2.set_frequency(mBaseFrequency + mFreqOffset);
        mVCO2.set_amplitude(0.75f);

        mADSR = new ADSR();

        mStompBox = new RRStompBox(new float[DEFAULT_AUDIOBLOCK_SIZE], new float[DEFAULT_AUDIOBLOCK_SIZE]);

        DSP.start(this);
        Beat.start(this, 120 * 8);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mouseMoved() {
        mVCO1.set_amplitude(map(mouseY, 0, height, 0.0f, 1.0f));
        mVCO2.set_amplitude(map(mouseY, 0, height, 0.0f, 1.0f));
        mFreqOffset = map(mouseX, 0, width, 0.0f, 10.0f);
    }

    public void beat(int pBeat) {
        if (pBeat % 2 == 0) {
            mADSR.start();
        } else if (pBeat % 2 == 1) {
            mADSR.stop();
        }
        if (pBeat % 8 == 0) {
            mVCO1.set_frequency(mBaseFrequency);
            mVCO2.set_frequency(mBaseFrequency + mFreqOffset);
        }
        if (pBeat % 8 == 3) {
            mVCO1.set_frequency(mBaseFrequency * 2);
            mVCO2.set_frequency((mBaseFrequency + mFreqOffset) * 2);
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
            case '9':
                mEnableDistortion = !mEnableDistortion;
                break;
        }
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            final float a = mVCO1.output();
            final float b = mVCO2.output();
            pOutputSignal[i] = a + b;
            pOutputSignal[i] *= 0.5f;
            final float mADSRValue = mADSR.output();
            pOutputSignal[i] *= mADSRValue;
        }
        if (mEnableDistortion) {
            mStompBox.out(pOutputSignal, new float[DEFAULT_AUDIOBLOCK_SIZE]);
        }
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = clamp(pOutputSignal[i]);
            pOutputSignal[i] *= mMasterVolume;
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestRRStompBox.class.getName());
    }
}
