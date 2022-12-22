package wellen.tests;

import processing.core.PApplet;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Wavetable;

import static wellen.Wellen.DEFAULT_AUDIOBLOCK_SIZE;
import static wellen.Wellen.clamp;

public class TestSignalMixing extends PApplet {

    private final float mBaseFrequency = 4.0f * Wellen.DEFAULT_SAMPLING_RATE / DEFAULT_AUDIOBLOCK_SIZE;
    private final float mMasterVolume = 0.3f;
    private final Wavetable mVCO1 = new Wavetable(512);
    private final Wavetable mVCO2 = new Wavetable(512);

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable.sine(mVCO1.get_wavetable());
        mVCO1.set_frequency(mBaseFrequency + 5);
        mVCO1.set_amplitude(0.75f);

        Wavetable.sine(mVCO2.get_wavetable());
        mVCO2.set_frequency(mBaseFrequency + 50);
        mVCO2.set_amplitude(0.75f);

        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
    }

    public void mouseMoved() {
        mVCO1.set_amplitude(map(mouseY, 0, height, 0.0f, 1.0f));
        mVCO2.set_amplitude(map(mouseX, 0, width, 0.0f, 1.0f));
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
        }
    }

    public void audioblock(float[] pOutputSignal) {
        // see http://www.vttoth.com/CMS/index.php/technical-notes/68
        for (int i = 0; i < pOutputSignal.length; i++) {
            final float a = mVCO1.output();
            final float b = mVCO2.output();
            pOutputSignal[i] = a + b;
            pOutputSignal[i] *= 0.5f;
            pOutputSignal[i] -= a * b;
            pOutputSignal[i] = clamp(pOutputSignal[i], -1.0f, 1.0f);
            pOutputSignal[i] *= mMasterVolume;
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestSignalMixing.class.getName());
    }
}
