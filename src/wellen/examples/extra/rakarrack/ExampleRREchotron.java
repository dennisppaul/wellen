package wellen.examples.extra.rakarrack;

import processing.core.PApplet;
import wellen.Beat;
import wellen.Wellen;
import wellen.dsp.ADSR;
import wellen.dsp.DSP;
import wellen.dsp.Wavetable;
import wellen.extra.rakarrack.RREchotron;

public class ExampleRREchotron extends PApplet {
    //@add import wellen.extra.rakarrack.*;

    private ADSR mADSR;
    private final float mBaseFrequency = 2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE;
    private RREchotron mEchotron;
    private boolean mEnableEchotron = true;
    private boolean mIsPlaying = false;
    private final float mMasterVolume = 0.8f;
    private final Wavetable mVCO = new Wavetable();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable.triangle(mVCO.get_wavetable());
        mVCO.set_frequency(mBaseFrequency);
        mVCO.set_amplitude(0.5f);
        mADSR = new ADSR();
        mEchotron = new RREchotron();

        DSP.start(this, 2);
        Beat.start(this, 120 * 4);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
    }

    public void beat(int beat) {
        if (random(1) > 0.4f) {
            if (mIsPlaying) {
                mADSR.stop();
            } else {
                mADSR.start();
                final float mFifth = (random(1) > 0.4f) ? 1 : 2.0f / 3.0f;
                mVCO.set_frequency(mBaseFrequency * (int) random(1, 5) / mFifth);
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
                mEchotron.setpreset(RREchotron.PRESET_SUMMER);
                break;
            case '2':
                mEchotron.setpreset(RREchotron.PRESET_AMBIENCE);
                break;
            case '3':
                mEchotron.setpreset(RREchotron.PRESET_ARRANJER);
                break;
            case '4':
                mEchotron.setpreset(RREchotron.PRESET_SUCTION);
                break;
            case '5':
                mEchotron.setpreset(RREchotron.PRESET_SUCFLANGE);
                break;
            case '0':
                mEnableEchotron = !mEnableEchotron;
                break;
        }
    }

    public void audioblock(float[] output_signalLeft, float[] output_signalRight) {
        for (int i = 0; i < output_signalLeft.length; i++) {
            output_signalLeft[i] = mVCO.output();
            final float mADSRValue = mADSR.output();
            output_signalLeft[i] *= mADSRValue;
            output_signalRight[i] = output_signalLeft[i];
        }
        if (mEnableEchotron) {
            mEchotron.out(output_signalLeft, output_signalRight);
        }
        for (int i = 0; i < output_signalLeft.length; i++) {
            output_signalLeft[i] = Wellen.clamp(output_signalLeft[i]);
            output_signalLeft[i] *= mMasterVolume;
            output_signalRight[i] = Wellen.clamp(output_signalRight[i]);
            output_signalRight[i] *= mMasterVolume;
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleRREchotron.class.getName());
    }
}
