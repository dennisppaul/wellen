package wellen.examples.extra.rakarrack;

import processing.core.PApplet;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Wavetable;
import wellen.extra.rakarrack.RRWaveShaper;

public class ExampleRRWaveshaper extends PApplet {
    //@add import wellen.extra.rakarrack.*;

    private final float mBaseFrequency = 4.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE;
    private float mMasterVolume = 0.5f;
    private final Wavetable mVCO1 = new Wavetable(512);
    private final Wavetable mVCO2 = new Wavetable(512);
    private int mWaveshapeDrive = 90;
    private int mWaveshapeType = RRWaveShaper.TYPE_ARCTANGENT;
    private RRWaveShaper mWaveshaper;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable.sine(mVCO1.get_wavetable());
        mVCO1.set_frequency(mBaseFrequency);
        mVCO1.set_amplitude(0.75f);

        Wavetable.sine(mVCO2.get_wavetable());
        mVCO2.set_frequency(mBaseFrequency * 0.99f);
        mVCO2.set_amplitude(0.25f);

        mWaveshaper = new RRWaveShaper();

        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
    }

    public void mouseMoved() {
        mWaveshapeDrive = (int) map(mouseX, 0, width, 0.0f, 127.0f);
        mMasterVolume = map(mouseY, 0, height, 0.0f, 1.0f);
    }

    public void keyPressed() {
        switch (key) {
            case 'q':
                Wavetable.fill(mVCO2.get_wavetable(), Wellen.WAVEFORM_SINE);
                break;
            case 'w':
                Wavetable.fill(mVCO2.get_wavetable(), Wellen.WAVEFORM_TRIANGLE);
                break;
            case 'e':
                Wavetable.fill(mVCO2.get_wavetable(), Wellen.WAVEFORM_SAWTOOTH);
                break;
            case 'r':
                Wavetable.fill(mVCO2.get_wavetable(), Wellen.WAVEFORM_SQUARE);
                break;
            case 'a':
                Wavetable.fill(mVCO1.get_wavetable(), Wellen.WAVEFORM_SINE);
                break;
            case 's':
                Wavetable.fill(mVCO1.get_wavetable(), Wellen.WAVEFORM_TRIANGLE);
                break;
            case 'd':
                Wavetable.fill(mVCO1.get_wavetable(), Wellen.WAVEFORM_SAWTOOTH);
                break;
            case 'f':
                Wavetable.fill(mVCO1.get_wavetable(), Wellen.WAVEFORM_SQUARE);
                break;
            case '1':
                mWaveshapeType--;
                if (mWaveshapeType < 0) {
                    mWaveshapeType += RRWaveShaper.NUM_WAVESHAPE_TYPES;
                }
                System.out.println("WAVESHAPE TYPE: " + mWaveshapeType);
                break;
            case '2':
                mWaveshapeType++;
                mWaveshapeType %= RRWaveShaper.NUM_WAVESHAPE_TYPES;
                System.out.println("WAVESHAPE TYPE: " + mWaveshapeType);
                break;
        }
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            final float a = mVCO1.output();
            final float b = mVCO2.output();
            output_signal[i] = a + b;
            output_signal[i] *= 0.5f;
        }
        mWaveshaper.waveshapesmps(output_signal.length, output_signal, mWaveshapeType, mWaveshapeDrive, true);
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] *= mMasterVolume;
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleRRWaveshaper.class.getName());
    }
}
