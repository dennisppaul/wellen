import wellen.*; 

/**
 * this example demonstrates how to export sample data to a WAV file and then import the WAV file to play it back
 * with a sampler.
 */

static final String WAV_FILE_NAME = "sine.wav";

Sampler mSampler;

void settings() {
    size(640, 480);
}

void setup() {
    final int WAV_FILE_LENGTH = Wellen.DEFAULT_SAMPLING_RATE / 100; /* file length is 1/100 second */
    final int NUM_OF_CHANNELS = 1; /* export single channel aka mono WAV file */
    final float[][] mExportSamples = new float[NUM_OF_CHANNELS][WAV_FILE_LENGTH];
    /* write sample data ( i.e a sine wave phase ) to sample buffer */
    final float mFrequency = 100.0f;
    final float mAmplitude = 0.5f;
    for (int i = 0; i < WAV_FILE_LENGTH; i++) {
        final float r = mFrequency * PApplet.TWO_PI * i / Wellen.DEFAULT_SAMPLING_RATE;
        mExportSamples[0][i] = PApplet.sin(r) * mAmplitude;
    }
    /* export samples to WAV file */
    Wellen.exportWAV(this,
                     WAV_FILE_NAME,
                     mExportSamples,
                     32,
                     Wellen.DEFAULT_SAMPLING_RATE,
                     Wellen.WAV_FORMAT_IEEE_FLOAT_32BIT);
    /* import samples from WAV file */
    float[][] mImportSamples = Wellen.importWAV(this, WAV_FILE_NAME);
    mSampler = new Sampler();
    mSampler.set_data(mImportSamples[0]);
    mSampler.loop(true);
    DSP.start(this);
}

void draw() {
    background(255);
    stroke(0);
    Wellen.draw_buffer(g, width, height, mSampler.data());
    DSP.draw_buffer(g, width, height);
}

void audioblock(float[] pOutputSignal) {
    for (int i = 0; i < pOutputSignal.length; i++) {
        pOutputSignal[i] = mSampler.output();
    }
}
