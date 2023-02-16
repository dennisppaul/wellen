import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrate how to perform Fast Fourier Transformation (FFT).
 */

Sampler fSampler;

int x = 0;

void settings() {
    size(640, 480);
}

void setup() {
    byte[] mData = loadBytes("../../../resources/teilchen.raw");
    fSampler = new Sampler();
    fSampler.load(mData);
    fSampler.set_loop_all();
    DSP.start(this);
    background(255);
}

void draw() {
    for (int i = 0; i < FFT.get_spectrum().length; i++) {
        float y = pow(map(i, 0, FFT.get_spectrum().length, 1, 0), 4) * height;
        float b = map(FFT.get_spectrum()[i], 0.0f, 10.0f, 255, 0);
        stroke(b);
        line(x, 0, x, y);
    }
    x++;
    x %= width;
}

void audioblock(float[] output_signal) {
    for (int i = 0; i < output_signal.length; i++) {
        output_signal[i] = fSampler.output();
    }
    FFT.perform_forward_transform(output_signal);
}
