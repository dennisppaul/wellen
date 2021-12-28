package wellen.tests;

import processing.core.PApplet;
import wellen.DSP;
import wellen.FFT;
import wellen.Filter;
import wellen.Wavetable;
import wellen.Wellen;

public class AppSonogram extends PApplet {
    private final Filter mFilter = new Filter();
    private final Wavetable mWavetable = new Wavetable();
    private int x = 0;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable.sine(mWavetable.get_wavetable());
        DSP.start(this);
        background(255);
    }

    public void draw() {
        final int LAST_FREQ_INDEX = FFT.instance().freqToIndex(8800) + 1; // FFT.get_spectrum().length
        for (int i = 0; i < LAST_FREQ_INDEX; i++) {
            float y = map(i, 0, LAST_FREQ_INDEX, height, 0);
            float b = map(FFT.get_spectrum()[i], 0.0f, 10.0f, 255, 0);
            stroke(b);
            line(x, 0, x, y);
        }
        x++;
        x %= width;
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mFilter.process(mWavetable.output());
        }
        FFT.perform_forward_transform(pOutputSignal);
    }

    public void mouseMoved() {
        if (keyCode == SHIFT) {
            mFilter.set_frequency(map(mouseX, 0, width, 55, Wellen.DEFAULT_SAMPLING_RATE / 2.0f));
            mFilter.set_resonance(map(mouseY, 0, height, 0.0f, 0.99f));
        } else {
            mWavetable.set_frequency(map(mouseX, 0, width, 55, 440 * 4));
            mWavetable.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
        }
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                Wavetable.sine(mWavetable.get_wavetable());
                break;
            case '2':
                Wavetable.triangle(mWavetable.get_wavetable());
                break;
            case '3':
                Wavetable.sawtooth(mWavetable.get_wavetable());
                break;
            case '4':
                Wavetable.square(mWavetable.get_wavetable());
                break;
            case '5':
                randomize(mWavetable.get_wavetable());
                break;
            case '6':
                mFilter.set_mode(Wellen.FILTER_MODE_LOWPASS);
                break;
            case '7':
                mFilter.set_mode(Wellen.FILTER_MODE_BANDPASS);
                break;
            case '8':
                mFilter.set_mode(Wellen.FILTER_MODE_HIGHPASS);
                break;
        }
    }

    private void randomize(float[] pWavetable) {
        for (int i = 0; i < pWavetable.length; i++) {
            pWavetable[i] = random(-1, 1);
        }
    }

    public static void main(String[] args) {
        PApplet.main(AppSonogram.class.getName());
    }
}