package wellen.examples.extra.rakarrack;

import processing.core.PApplet;
import wellen.ADSR;
import wellen.Beat;
import wellen.DSP;
import wellen.Wavetable;
import wellen.Wellen;
import wellen.extra.rakarrack.RRChorus;

public class ExampleRRChorus extends PApplet {
    //@add import wellen.extra.rakarrack.*;

    private ADSR mADSR;
    private final float mBaseFrequency = 4.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE;
    private RRChorus mChorus;
    private boolean mEnableChorus = true;
    private boolean mIsPlaying = false;
    private final float mMasterVolume = 0.75f;
    private final Wavetable mVCO = new Wavetable();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable.triangle(mVCO.get_wavetable());
        mVCO.set_frequency(mBaseFrequency);
        mVCO.set_amplitude(0.75f);
        mADSR = new ADSR();
        mChorus = new RRChorus();

        DSP.start(this, 2);
        Beat.start(this, 120 * 4);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer_stereo(g, width, height);
    }

    public void beat(int pBeat) {
        if (random(1) > (mIsPlaying ? 0.4f : 0.2f)) {
            if (mIsPlaying) {
                mADSR.stop();
            } else {
                mADSR.start();
                mVCO.set_frequency(mBaseFrequency * (int) random(1, 5));
            }
            mIsPlaying = !mIsPlaying;
        }
    }

    public void keyPressed() {
        switch (key) {
            case 'q':
                Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVESHAPE_SINE);
                break;
            case 'w':
                Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVESHAPE_TRIANGLE);
                break;
            case 'e':
                Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVESHAPE_SAWTOOTH);
                break;
            case 'r':
                Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVESHAPE_SQUARE);
                break;
            case '1':
                mChorus.setpreset(RRChorus.PRESET_CHORUS_1);
                break;
            case '2':
                mChorus.setpreset(RRChorus.PRESET_CHORUS_2);
                break;
            case '3':
                mChorus.setpreset(RRChorus.PRESET_CHORUS_3);
                break;
            case '4':
                mChorus.setpreset(RRChorus.PRESET_CELESTE_1);
                break;
            case '5':
                mChorus.setpreset(RRChorus.PRESET_CELESTE_2);
                break;
            case '6':
                mChorus.setpreset(RRChorus.PRESET_FLANGE_1);
                break;
            case '7':
                mChorus.setpreset(RRChorus.PRESET_FLANGE_2);
                break;
            case '8':
                mChorus.setpreset(RRChorus.PRESET_FLANGE_3);
                break;
            case '9':
                mChorus.setpreset(RRChorus.PRESET_FLANGE_4);
                break;
            case '0':
                mChorus.setpreset(RRChorus.PRESET_FLANGE_5);
                break;
            case ' ':
                mEnableChorus = !mEnableChorus;
                break;
        }
    }

    public void audioblock(float[] pOutputSignalLeft, float[] pOutputSignalRight) {
        for (int i = 0; i < pOutputSignalLeft.length; i++) {
            pOutputSignalLeft[i] = mVCO.output();
            final float mADSRValue = mADSR.output();
            pOutputSignalLeft[i] *= mADSRValue;
            pOutputSignalRight[i] = pOutputSignalLeft[i];
        }
        if (mEnableChorus) {
            mChorus.out(pOutputSignalLeft, pOutputSignalRight);
        }
        for (int i = 0; i < pOutputSignalLeft.length; i++) {
            pOutputSignalLeft[i] = Wellen.clamp(pOutputSignalLeft[i]);
            pOutputSignalLeft[i] *= mMasterVolume;
            pOutputSignalRight[i] = Wellen.clamp(pOutputSignalRight[i]);
            pOutputSignalRight[i] *= mMasterVolume;
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleRRChorus.class.getName());
    }
}
