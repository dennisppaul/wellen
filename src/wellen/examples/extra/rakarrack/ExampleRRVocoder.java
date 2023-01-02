package wellen.examples.extra.rakarrack;

import processing.core.PApplet;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Wavetable;
import wellen.extra.rakarrack.RRUtilities;
import wellen.extra.rakarrack.RRVocoder;

public class ExampleRRVocoder extends PApplet {
    //@add import wellen.extra.rakarrack.*;

    private final float mBaseFrequency = 4.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE;
    private final float[] mCarrier = new float[Wellen.DEFAULT_AUDIOBLOCK_SIZE];
    private boolean mEnableVocoder = true;
    private final float mMasterVolume = 0.5f;
    private final Wavetable mVCO = new Wavetable(512);
    private RRVocoder mVocoder;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable.square(mVCO.get_wavetable());
        mVCO.set_frequency(55);
        mVCO.set_amplitude(0.75f);

        mVocoder = new RRVocoder(mCarrier, 32);
        DSP.start(this, 1, 1);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
    }

    public void mouseMoved() {
        mVCO.set_frequency(map(mouseY, 0, height, 0.1f, 1000.0f));
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                mVocoder.setpreset(RRVocoder.PRESET_VOCODER_1);
                break;
            case '2':
                mVocoder.setpreset(RRVocoder.PRESET_VOCODER_2);
                break;
            case '3':
                mVocoder.setpreset(RRVocoder.PRESET_VOCODER_3);
                break;
            case '4':
                mVocoder.setpreset(RRVocoder.PRESET_VOCODER_4);
                break;
            case ' ':
                mEnableVocoder = !mEnableVocoder;
                break;
        }
    }

    public void audioblock(float[] output_signal, float[] pInputSignal) {
        RRUtilities.memcpy(output_signal, pInputSignal, pInputSignal.length);
        for (int i = 0; i < mCarrier.length; i++) {
            mCarrier[i] = mVCO.output();
        }

        if (mEnableVocoder) {
            mVocoder.out(output_signal, new float[Wellen.DEFAULT_AUDIOBLOCK_SIZE]);
        }

        for (int i = 0; i < output_signal.length; i++) {
            if (mEnableVocoder) {
                output_signal[i] *= 32;
            }
            output_signal[i] = Wellen.clamp(output_signal[i]);
            output_signal[i] *= mMasterVolume;
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleRRVocoder.class.getName());
    }
}
