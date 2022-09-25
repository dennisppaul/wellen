package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.DSP;
import wellen.FFT;
import wellen.Sampler;
import wellen.Wellen;

public class ExampleDSP19FFT extends PApplet {

    /*
     * this example demonstrate how to perform Fast Fourier Transformation (FFT).
     */

    private int x = 0;
    private Sampler mSampler;

    public void settings() {
        size(640, 480);
    }

    public void setup() {

        byte[] mData = loadBytes("../../../resources/teilchen.raw");
        mSampler = new Sampler();
        mSampler.load(mData);
        mSampler.loop(true);
        DSP.start(this);
        background(255);
    }

    public void draw() {
        for (int i = 0; i < FFT.get_spectrum().length; i++) {
            float y = pow(map(i, 0, FFT.get_spectrum().length, 1, 0), 4) * height;
            float b = map(FFT.get_spectrum()[i], 0.0f, 10.0f, 255, 0);
            stroke(b);
            line(x, 0, x, y);
        }
        x++;
        x %= width;
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mSampler.output();
        }
        FFT.perform_forward_transform(pOutputSignal);
    }

    public static void main(String[] args) {
        Wellen.run_sketch_with_resources(ExampleDSP19FFT.class);
    }
}