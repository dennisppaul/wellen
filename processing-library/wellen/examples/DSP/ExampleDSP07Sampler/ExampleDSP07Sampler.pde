import wellen.*; 

/*
 * this example demonstrates how to use a sampler ( a pre-recorded chunk of memory ) and play it at different speeds
 * and amplitudes. the sample data can also be loaded from external sources. the `load` method assumes a raw audio
 * format with 32-bit floats and a value range from [-1.0, 1.0].
 *
 * note that samples can either be played once or looped. if a sample is played once it must be rewound before it
 * can be played again.
 */

Sampler mSampler;

void settings() {
    size(640, 480);
}

void setup() {
    byte[] mData = SampleDataSNARE.data;
    // alternatively load data with `loadBytes("audio.raw")` ( raw format, 32bit float )
    mSampler = new Sampler();
    mSampler.load(mData);
    mSampler.loop(false);
    DSP.start(this);
}

void draw() {
    background(255);
    DSP.draw_buffer(g, width, height);
}

void mousePressed() {
    mSampler.rewind();
}

void mouseMoved() {
    mSampler.set_speed(map(mouseX, 0, width, 0, 32));
    mSampler.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
}

void keyPressed() {
    switch (key) {
        case 'l':
        case 'L':
            mSampler.loop(true);
            break;
        default:
            mSampler.loop(false);
    }
}

void audioblock(float[] pOutputSamples) {
    for (int i = 0; i < pOutputSamples.length; i++) {
        pOutputSamples[i] = mSampler.output();
    }
}
