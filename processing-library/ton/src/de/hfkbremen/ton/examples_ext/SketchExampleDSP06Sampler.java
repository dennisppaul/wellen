package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.DSP;
import de.hfkbremen.ton.SampleDataSNARE;
import de.hfkbremen.ton.Sampler;
import processing.core.PApplet;

public class SketchExampleDSP06Sampler extends PApplet {

    private Sampler mSampler;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        byte[] mData = SampleDataSNARE.data;
        // alternatively load data with `loadBytes("audio.raw")` ( raw format, 32bit float )
        mSampler = new Sampler();
        mSampler.load(mData);
        mSampler.loop(false);

        DSP.dumpAudioDevices();
        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mousePressed() {
        mSampler.rewind();
    }

    public void mouseMoved() {
        mSampler.set_speed(map(mouseX, 0, width, 0, 32));
        mSampler.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
    }

    public void keyPressed() {
        switch (key) {
            case 'l':
            case 'L':
                mSampler.loop(true);
                break;
            default:
                mSampler.loop(false);
        }
    }

    public void audioblock(float[] pOutputSamples) {
        for (int i = 0; i < pOutputSamples.length; i++) {
            pOutputSamples[i] = mSampler.process();
        }
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleDSP06Sampler.class.getName());
    }
}
