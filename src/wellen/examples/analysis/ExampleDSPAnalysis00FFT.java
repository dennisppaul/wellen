package wellen.examples.analysis;

import processing.core.PApplet;
import wellen.FFT;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Sampler;

public class ExampleDSPAnalysis00FFT extends PApplet {

    /*
     * this example demonstrate how to perform Fast Fourier Transformation (FFT).
     */

    private Sampler fSampler;
    private int x = 0;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        byte[] mData = loadBytes("../../../resources/teilchen.raw");
        fSampler = new Sampler();
        fSampler.load(mData);
        fSampler.set_loop_all();
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

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = fSampler.output();
        }
        FFT.perform_forward_transform(output_signal);
    }

    public static void main(String[] args) {
        Wellen.run_sketch_with_resources(ExampleDSPAnalysis00FFT.class);
    }
}