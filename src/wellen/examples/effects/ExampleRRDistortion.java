package wellen.examples.effects;

import processing.core.PApplet;
import wellen.ADSR;
import wellen.Beat;
import wellen.DSP;
import wellen.Wavetable;
import wellen.Wellen;
import wellen.extern.rakarrack.RRDistortion;

import static wellen.Wellen.DEFAULT_AUDIOBLOCK_SIZE;
import static wellen.extern.rakarrack.RRDistortion.PRESET_DISTORSION_1;
import static wellen.extern.rakarrack.RRDistortion.PRESET_DISTORSION_2;
import static wellen.extern.rakarrack.RRDistortion.PRESET_DISTORSION_3;
import static wellen.extern.rakarrack.RRDistortion.PRESET_GUITAR_AMP;
import static wellen.extern.rakarrack.RRDistortion.PRESET_OVERDRIVE_1;
import static wellen.extern.rakarrack.RRDistortion.PRESET_OVERDRIVE_2;

public class ExampleRRDistortion extends PApplet {

    private ADSR mADSR;
    private final float mBaseFrequency = 4.0f * Wellen.DEFAULT_SAMPLING_RATE / DEFAULT_AUDIOBLOCK_SIZE;
    private RRDistortion mDistortion;
    private boolean mEnableDistortion = true;
    private float mFreqOffset = 5;
    private final float mMasterVolume = 0.3f;
    private final Wavetable mVCO1 = new Wavetable();
    private final Wavetable mVCO2 = new Wavetable();

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

        mDistortion = new RRDistortion();

        DSP.start(this);
        Beat.start(this, 120 * 4);
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
                mDistortion.setpreset(PRESET_OVERDRIVE_1);
                break;
            case '2':
                mDistortion.setpreset(PRESET_OVERDRIVE_2);
                break;
            case '3':
                mDistortion.setpreset(PRESET_DISTORSION_1);
                break;
            case '4':
                mDistortion.setpreset(PRESET_DISTORSION_2);
                break;
            case '5':
                mDistortion.setpreset(PRESET_DISTORSION_3);
                break;
            case '6':
                mDistortion.setpreset(PRESET_GUITAR_AMP);
                break;
            case '7':
                mEnableDistortion = !mEnableDistortion;
                break;
            case 'z':
                mDistortion.setoctave(0);
                break;
            case 'x':
                mDistortion.setoctave(63);
                break;
            case 'c':
                mDistortion.setoctave(91);
                break;
            case 'v':
                mDistortion.setoctave(126);
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
            pOutputSignal[i] *= mMasterVolume;
        }
        if (mEnableDistortion) {
            mDistortion.out(pOutputSignal);
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleRRDistortion.class.getName());
    }
}
