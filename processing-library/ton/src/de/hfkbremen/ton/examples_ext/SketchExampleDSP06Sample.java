package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.DSP;
import de.hfkbremen.ton.SampleDataSNARE;
import de.hfkbremen.ton.Wavetable;
import processing.core.PApplet;

public class SketchExampleDSP06Sample extends PApplet {

    private Wavetable mWavetable;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mWavetable = new Wavetable(SampleDataSNARE.data.length / 4);
        Wavetable.from_bytes(SampleDataSNARE.data, mWavetable.wavetable());

        DSP.dumpAudioDevices();
        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mouseDragged() {
        mWavetable.set_frequency((float) DSP.DEFAULT_SAMPLING_RATE / mWavetable.wavetable().length);
        mWavetable.set_amplitude(0.85f);
    }

    public void mouseMoved() {
        mWavetable.set_frequency(map(mouseX, 0, width, 0.1f, 50));
        mWavetable.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
    }

    public void keyPressed() {
    }

    public void audioblock(float[] pOutputSamples) {
        for (int i = 0; i < pOutputSamples.length; i++) {
            pOutputSamples[i] = mWavetable.process();
        }
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleDSP06Sample.class.getName());
    }
}
