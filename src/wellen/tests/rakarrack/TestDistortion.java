package wellen.tests.rakarrack;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Wavetable;
import wellen.Wellen;

import static wellen.Wellen.DEFAULT_AUDIOBLOCK_SIZE;
import static wellen.tests.rakarrack.RRDistortion.PRESET_DISTORSION_1;
import static wellen.tests.rakarrack.RRDistortion.PRESET_DISTORSION_2;
import static wellen.tests.rakarrack.RRDistortion.PRESET_DISTORSION_3;
import static wellen.tests.rakarrack.RRDistortion.PRESET_GUITAR_AMP;
import static wellen.tests.rakarrack.RRDistortion.PRESET_OVERDRIVE_1;
import static wellen.tests.rakarrack.RRDistortion.PRESET_OVERDRIVE_2;

public class TestDistortion extends PApplet {

    private RRAnalogFilter mAnalogFilter;
    private final float mBaseFrequency = 4.0f * Wellen.DEFAULT_SAMPLING_RATE / DEFAULT_AUDIOBLOCK_SIZE;
    private RRDistortion mDistortion;
    private final float mMasterVolume = 0.3f;
    private final Wavetable mVCO1 = new Wavetable(512);
    private final Wavetable mVCO2 = new Wavetable(512);
    private int mWaveShapeDrive = 90;
    private int mWaveShapeType = 0;
    private RRWaveshaper mWaveshaper;

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

        mDistortion = new RRDistortion(new float[DEFAULT_AUDIOBLOCK_SIZE], new float[DEFAULT_AUDIOBLOCK_SIZE]);
        mAnalogFilter = new RRAnalogFilter(RRAnalogFilter.TYPE_LPF_1_POLE, 30, 1, 0);
        mWaveshaper = new RRWaveshaper();

        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mouseMoved() {
        mVCO1.set_amplitude(map(mouseY, 0, height, 0.0f, 1.0f));
        mVCO2.set_amplitude(map(mouseX, 0, width, 0.0f, 1.0f));
        mAnalogFilter.setq(map(mouseY, 0, height, 0.0f, 4.0f));
        mAnalogFilter.setfreq(map(mouseX, 0, width, 0.0f, 10000.0f));
//        mWaveShapeDrive = (int) map(mouseX, 0, width, 0.0f, 127.0f);
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
                mWaveShapeType = 0;
                mDistortion.setpreset(PRESET_OVERDRIVE_1);
                break;
            case '2':
                mAnalogFilter.settype(RRAnalogFilter.TYPE_HPF_1_POLE);
                mWaveShapeType = 1;
                mDistortion.setpreset(PRESET_OVERDRIVE_2);
                break;
            case '3':
                mAnalogFilter.settype(RRAnalogFilter.TYPE_LPF_2_POLE);
                mWaveShapeType = 2;
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
        // see http://www.vttoth.com/CMS/index.php/technical-notes/68
        for (int i = 0; i < pOutputSignal.length; i++) {
            final float a = mVCO1.output();
            final float b = mVCO2.output();
            pOutputSignal[i] = a + b;
            pOutputSignal[i] *= 0.5f;
            pOutputSignal[i] *= mMasterVolume;
        }
        final float[] mTemp = new float[DEFAULT_AUDIOBLOCK_SIZE];
        mDistortion.out(pOutputSignal, mTemp);
//        mAnalogFilter.filterout(pOutputSignal);
//        mWaveshaper.waveshapesmps(pOutputSignal.length,
//                                  pOutputSignal,
//                                  mWaveShapeType,
//                                  mWaveShapeDrive,
//                                  true);
    }

    public static void main(String[] args) {
        PApplet.main(TestDistortion.class.getName());
    }
}
