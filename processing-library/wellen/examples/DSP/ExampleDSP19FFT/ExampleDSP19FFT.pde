import wellen.*; 

/*
 * this example demonstrate how to perform Fast Fourier Transformation (FFT).
 */

int x = 0;

Sampler mSampler;

void settings() {
    size(640, 480);
}

void setup() {
    byte[] mData = loadBytes("../../../resources/teilchen.raw");
    mSampler = new Sampler();
    mSampler.load(mData);
    mSampler.loop(true);
    DSP.start(this);
    background(255);
}

void draw() {
    for (int i = 0; i < FFT.get_spectrum().length; i++) {
        float y = map(i, 0, FFT.get_spectrum().length, height, 0);
        float b = map(FFT.get_spectrum()[i], 0.0f, 10.0f, 255, 0);
        stroke(b);
        line(x, 0, x, y);
    }
    x++;
    x %= width;
}

void audioblock(float[] pOutputSignal) {
    for (int i = 0; i < pOutputSignal.length; i++) {
        pOutputSignal[i] = mSampler.output();
    }
    FFT.perform_forward_transform(pOutputSignal);
}
