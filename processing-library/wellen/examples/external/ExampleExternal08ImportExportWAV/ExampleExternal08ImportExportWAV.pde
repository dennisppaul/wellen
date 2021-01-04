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
    drawImportedSamples();
    DSP.draw_buffer(g, width, height);
}

void audioblock(float[] pOutputSamples) {
    for (int i = 0; i < pOutputSamples.length; i++) {
        pOutputSamples[i] = mSampler.output();
    }
}

void drawImportedSamples() {
    float[] mSamples = mSampler.data();
    for (int i = 1; i < mSamples.length; i++) {
        PVector p0 = mapSampleToScreen(mSamples, i - 1);
        PVector p1 = mapSampleToScreen(mSamples, i);
        line(p0.x, p0.y, p1.x, p1.y);
    }
}

PVector mapSampleToScreen(float[] mSamples, int i) {
    PVector p = new PVector();
    p.x = map(i, 0, mSamples.length, 0, width);
    p.y = map(mSamples[i], -1, 1, 0, height);
    return p;
}
