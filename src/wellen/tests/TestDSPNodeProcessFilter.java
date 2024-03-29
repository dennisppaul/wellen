package wellen.tests;

import processing.core.PApplet;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.FilterMoogLadderLowPass;
import wellen.dsp.Wavetable;

public class TestDSPNodeProcessFilter extends PApplet {

    private final FilterMoogLadderLowPass mFilter = new FilterMoogLadderLowPass(Wellen.DEFAULT_SAMPLING_RATE);
    private final Wavetable mWavetable = new Wavetable(512);

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable.sawtooth(mWavetable.get_wavetable());
        mWavetable.set_frequency(172.265625f);
        mWavetable.set_amplitude(0.55f);
        Wellen.dumpAudioInputAndOutputDevices();
        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
    }

    public void mouseMoved() {
        mFilter.set_frequency(map(mouseX, 0, width, 1.0f, 5000.0f));
        mFilter.set_resonance(map(mouseY, 0, height, 0.0f, 0.97f));
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
        }
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = mFilter.process(mWavetable.output());
        }
    }

    private void randomize(float[] pWavetable) {
        for (int i = 0; i < pWavetable.length; i++) {
            pWavetable[i] = random(-1, 1);
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestDSPNodeProcessFilter.class.getName());
    }
}
