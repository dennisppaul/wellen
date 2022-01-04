package wellen.tests.rr;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Wavetable;
import wellen.Wellen;
import wellen.effect.RRAnalogFilter;

import static wellen.Wellen.DEFAULT_AUDIOBLOCK_SIZE;

public class ExampleRRAnalogFilter extends PApplet {

    private RRAnalogFilter mAnalogFilter;
    private final float mBaseFrequency = 4.0f * Wellen.DEFAULT_SAMPLING_RATE / DEFAULT_AUDIOBLOCK_SIZE;
    private final float mMasterVolume = 0.75f;
    private final Wavetable mVCO1 = new Wavetable(512);
    private final Wavetable mVCO2 = new Wavetable(512);

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable.square(mVCO1.get_wavetable());
        mVCO1.set_frequency(mBaseFrequency);
        mVCO1.set_amplitude(0.75f);

        Wavetable.square(mVCO2.get_wavetable());
        mVCO2.set_frequency(mBaseFrequency * 0.99f);
        mVCO2.set_amplitude(0.75f);

        mAnalogFilter = new RRAnalogFilter(RRAnalogFilter.TYPE_LPF_1_POLE, 30, 1, 0);

        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mouseMoved() {
        mAnalogFilter.setq(map(mouseY, 0, height, 0.0f, 4.0f));
        mAnalogFilter.setfreq(map(mouseX, 0, width, 0.0f, 10000.0f));
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
                mAnalogFilter.settype(RRAnalogFilter.TYPE_LPF_1_POLE);
                break;
            case '2':
                mAnalogFilter.settype(RRAnalogFilter.TYPE_HPF_1_POLE);
                break;
            case '3':
                mAnalogFilter.settype(RRAnalogFilter.TYPE_LPF_2_POLE);
                break;
            case '4':
                mAnalogFilter.settype(RRAnalogFilter.TYPE_HPF_2_POLE);
                break;
            case '5':
                mAnalogFilter.settype(RRAnalogFilter.TYPE_BPF_2_POLE);
                break;
            case '6':
                mAnalogFilter.settype(RRAnalogFilter.TYPE_NOTCH_2_POLE);
                break;
            case '7':
                mAnalogFilter.settype(RRAnalogFilter.TYPE_PEAK_2_POLE);
                break;
        }
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            final float a = mVCO1.output();
            final float b = mVCO2.output();
            pOutputSignal[i] = a + b;
            pOutputSignal[i] *= 0.5f;
            pOutputSignal[i] *= mMasterVolume;
        }
        mAnalogFilter.filterout(pOutputSignal);
    }

    public static void main(String[] args) {
        PApplet.main(ExampleRRAnalogFilter.class.getName());
    }
}
