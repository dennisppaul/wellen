package wellen.tests;

import processing.core.PApplet;
import wellen.dsp.DSP;
import wellen.dsp.Wavetable;

public class TestDSPPauseAndResume extends PApplet {

    private Wavetable mWavetable;

    public void settings() {
        size(640, 480);
    }

    public void setup() {

        mWavetable = new Wavetable(1024);
        Wavetable.sine(mWavetable.get_wavetable());

        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);

        if (DSP.is_paused()) {
            fill(0);
            circle(width * 0.5f, height * 0.5f, 100);
        }
    }

    public void mousePressed() {
        DSP.pause(!DSP.is_paused());
    }

    public void mouseMoved() {
        final float mNewFrequency = map(mouseX, 0, width, 55, 880);
        final float mNewAmplitude = map(mouseY, 0, height, 0.0f, 0.9f);
        mWavetable.set_frequency(mNewFrequency);
        mWavetable.set_amplitude(mNewAmplitude);
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = mWavetable.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestDSPPauseAndResume.class.getName());
    }
}