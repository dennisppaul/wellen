package wellen.examples.extra.rakarrack;

import processing.core.PApplet;
import wellen.ADSR;
import wellen.Beat;
import wellen.DSP;
import wellen.Wavetable;
import wellen.Wellen;
import wellen.extra.rakarrack.RREcho;

public class ExampleRREcho extends PApplet {
    //@add import wellen.extra.rakarrack.*;

    private ADSR mADSR;
    private final float mBaseFrequency = 4.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE;
    private RREcho mEcho;
    private boolean mEnableEcho = true;
    private boolean mIsPlaying = false;
    private final float mMasterVolume = 0.75f;
    private final Wavetable mVCO = new Wavetable(512);

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable.sine(mVCO.get_wavetable());
        mVCO.set_frequency(mBaseFrequency);
        mVCO.set_amplitude(0.5f);
        mADSR = new ADSR();
        mEcho = new RREcho();

        DSP.start(this, 2);
        Beat.start(this, 120 * 8);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
    }

    public void mouseMoved() {
        mEcho.Tempo2Delay((int) map(mouseX, 0, width, 10, 300));
    }

    public void beat(int pBeat) {
        if (random(1) > 0.8f) {
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
                Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVEFORM_SINE);
                break;
            case 'w':
                Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVEFORM_TRIANGLE);
                break;
            case 'e':
                Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVEFORM_SAWTOOTH);
                break;
            case 'r':
                Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVEFORM_SQUARE);
                break;
            case '1':
                mEcho.setpreset(RREcho.PRESET_ECHO_1);
                break;
            case '2':
                mEcho.setpreset(RREcho.PRESET_ECHO_2);
                break;
            case '3':
                mEcho.setpreset(RREcho.PRESET_ECHO_3);
                break;
            case '4':
                mEcho.setpreset(RREcho.PRESET_SIMPLE_ECHO);
                break;
            case '5':
                mEcho.setpreset(RREcho.PRESET_CANYON);
                break;
            case '6':
                mEcho.setpreset(RREcho.PRESET_PANNING_ECHO_1);
                break;
            case '7':
                mEcho.setpreset(RREcho.PRESET_PANNING_ECHO_2);
                break;
            case '8':
                mEcho.setpreset(RREcho.PRESET_PANNING_ECHO_3);
                break;
            case '9':
                mEcho.setpreset(RREcho.PRESET_FEEDBACK_ECHO);
                break;
            case '0':
                mEnableEcho = !mEnableEcho;
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
        if (mEnableEcho) {
            for (int i = 0; i < pOutputSignalLeft.length; i++) {
                float mGain = 3.0f;
                pOutputSignalLeft[i] *= mGain;
                pOutputSignalRight[i] *= mGain;
            }
            mEcho.out(pOutputSignalLeft, pOutputSignalRight);
        }
        for (int i = 0; i < pOutputSignalLeft.length; i++) {
            pOutputSignalLeft[i] = Wellen.clamp(pOutputSignalLeft[i]);
            pOutputSignalLeft[i] *= mMasterVolume;
            pOutputSignalRight[i] = Wellen.clamp(pOutputSignalRight[i]);
            pOutputSignalRight[i] *= mMasterVolume;
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleRREcho.class.getName());
    }
}

