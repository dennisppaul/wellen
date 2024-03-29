package wellen.tests;

import processing.core.PApplet;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Wavetable;

public class TestWavetableInterpolation extends PApplet {

    private final Wavetable mWavetable = new Wavetable(16);

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wellen.dumpAudioInputAndOutputDevices();
        DSP.start(this);
        Wavetable.sine(mWavetable.get_wavetable());
        mWavetable.set_frequency(172.265625f);
        mWavetable.set_amplitude(0.25f);
        mWavetable.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
        mWavetable.set_interpolation(mousePressed ? Wellen.WAVESHAPE_INTERPOLATE_LINEAR :
                                             Wellen.WAVESHAPE_INTERPOLATE_NONE);
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
                Wavetable.fill(mWavetable.get_wavetable(),
                               Wellen.WAVEFORM_SQUARE); /* alternative way to fill wavetable */
                break;
            case '5':
                randomize(mWavetable.get_wavetable());
                break;
        }
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = mWavetable.output();
        }
    }

    private void randomize(float[] pWavetable) {
        for (int i = 0; i < pWavetable.length; i++) {
            pWavetable[i] = random(-1, 1);
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestWavetableInterpolation.class.getName());
    }
}