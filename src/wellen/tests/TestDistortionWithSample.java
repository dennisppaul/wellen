package wellen.tests;

import processing.core.PApplet;
import wellen.Tone;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Distortion;
import wellen.dsp.Sampler;

public class TestDistortionWithSample extends PApplet {

    private Sampler mSampler;
    private Distortion mDistortion;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        byte[] mData = loadBytes("../../../resources/a_portrait_in_reverse.raw");
        mSampler = new Sampler();
        mSampler.load(mData);
        mSampler.loop(true);

        mDistortion = new Distortion();
        DSP.start(this);
    }

    public void draw() {
        background(255);
        fill(0);
        stroke(0);
        ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
        line(50, height * 0.1f, width - 50, height * 0.1f);
        DSP.draw_buffers(g, width, height);
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                mDistortion.set_type(Wellen.DISTORTION_HARD_CLIPPING);
                break;
            case '2':
                mDistortion.set_type(Wellen.DISTORTION_FOLDBACK);
                break;
            case '3':
                mDistortion.set_type(Wellen.DISTORTION_FOLDBACK_SINGLE);
                break;
            case '4':
                mDistortion.set_type(Wellen.DISTORTION_FULL_WAVE_RECTIFICATION);
                break;
            case '5':
                mDistortion.set_type(Wellen.DISTORTION_HALF_WAVE_RECTIFICATION);
                break;
            case '6':
                mDistortion.set_type(Wellen.DISTORTION_INFINITE_CLIPPING);
                break;
            case '7':
                mDistortion.set_type(Wellen.DISTORTION_SOFT_CLIPPING_CUBIC);
                break;
            case '8':
                mDistortion.set_type(Wellen.DISTORTION_SOFT_CLIPPING_ARC_TANGENT);
                break;
            case '9':
                mDistortion.set_type(Wellen.DISTORTION_BIT_CRUSHING);
                break;
        }
    }

    public void mouseMoved() {
        mDistortion.set_clip(map(mouseX, 0, width, 0.0f, 1.0f));
        mDistortion.set_bits((int) map(mouseX, 0, width, 1, 17));
        mDistortion.set_amplification(map(mouseY, 0, height, 0.0f, 10.0f));
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = mSampler.output();
            output_signal[i] = Wellen.clamp(mDistortion.process(output_signal[i]));
        }
    }

    public static void main(String[] args) {
        Wellen.run_sketch_with_resources(TestDistortionWithSample.class);
    }
}