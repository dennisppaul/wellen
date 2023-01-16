package wellen.tests;

import processing.core.PApplet;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Sampler;

public class TestSampleEdgeFading extends PApplet {

    private float[] mFadedData;
    private Sampler mSampler;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mSampler = new Sampler(Wellen.DEFAULT_SAMPLING_RATE / 2);
        for (int i = 0; i < mSampler.get_data().length; i++) {
            float r = (float) i / mSampler.get_data().length;
            r *= TWO_PI;
            r *= 110.0f;
            mSampler.get_data()[i] = sin(r) * 0.5f;
        }
        mSampler.enable_loop(true);

        mSampler.set_edge_fading(mSampler.get_data().length / 4);

        mFadedData = new float[mSampler.get_data().length];
        for (int i = 0; i < mFadedData.length; i++) {
            mFadedData[i] = mSampler.output();
        }

        DSP.start(this);
    }

    public void draw() {
        if (mousePressed) {
            mSampler.set_edge_fading(0);
        } else {
            mSampler.set_edge_fading(mSampler.get_data().length / 4);
        }

        background(255);

        stroke(0, 31);
        Wellen.draw_buffer(g, width, height, mSampler.get_data(), 10);
        stroke(0, 127);
        Wellen.draw_buffer(g, width, height, mFadedData, 10);
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = mSampler.output();
        }
    }

    public static void main(String[] args) {
        Wellen.run_sketch_with_resources(TestSampleEdgeFading.class);
    }
}
