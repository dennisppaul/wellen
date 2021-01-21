package wellen.tests;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Sampler;
import wellen.Wellen;

public class TestSampleEdgeFading extends PApplet {

    private Sampler mSampler;
    private float[] mFadedData;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mSampler = new Sampler(Wellen.DEFAULT_SAMPLING_RATE / 2);
        for (int i = 0; i < mSampler.data().length; i++) {
            float r = (float) i / mSampler.data().length;
            r *= TWO_PI;
            r *= 110.0f;
            mSampler.data()[i] = sin(r) * 0.5f;
        }
        mSampler.loop(true);

        mSampler.set_edge_fading(mSampler.data().length / 4);

        mFadedData = new float[mSampler.data().length];
        for (int i = 0; i < mFadedData.length; i++) {
            mFadedData[i] = mSampler.output();
        }

        DSP.start(this);
    }

    public void draw() {
        if (mousePressed) {
            mSampler.set_edge_fading(0);
        } else {
            mSampler.set_edge_fading(mSampler.data().length / 4);
        }

        background(255);

        stroke(0, 31);
        Wellen.draw_buffer(g, width, height, mSampler.data(), 10);
        stroke(0, 127);
        Wellen.draw_buffer(g, width, height, mFadedData, 10);
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mSampler.output();
        }
    }

    public static void main(String[] args) {
        Wellen.run_sketch_with_resources(TestSampleEdgeFading.class);
    }
}
