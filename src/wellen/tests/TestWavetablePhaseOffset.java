package wellen.tests;

import processing.core.PApplet;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Wavetable;

public class TestWavetablePhaseOffset extends PApplet {

    private final int WAVETABLE_SIZE = 512;
    private Wavetable mWavetable;
    private Wavetable mWavetableRef;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mWavetable = new Wavetable(WAVETABLE_SIZE);
        mWavetable.set_frequency(4.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
        mWavetable.set_amplitude(0.25f);
        Wavetable.sine(mWavetable.get_wavetable());

        mWavetableRef = new Wavetable(WAVETABLE_SIZE);
        mWavetableRef.set_frequency(2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
        mWavetableRef.set_amplitude(0.25f);
        Wavetable.sine(mWavetableRef.get_wavetable());

        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
    }

    public void mousePressed() {
        /* if phase offset is `WAVETABLE_SIZE/2` and frequencies are identical the wave cancel each other out */
        mWavetable.set_frequency(2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
    }

    public void mouseReleased() {
        mWavetable.set_frequency(4.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
    }

    public void mouseDragged() {
        mouseMoved();
    }

    public void mouseMoved() {
        mWavetable.set_phase_offset((int) map(mouseX, 0, width, 0, 1));
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = mWavetable.output() + mWavetableRef.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestWavetablePhaseOffset.class.getName());
    }
}
