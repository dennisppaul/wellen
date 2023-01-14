import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to use a sampler ( a pre-recorded chunk of memory ) and play it at different speeds
 * and amplitudes. the sample data can also be loaded from external sources. the `load` method assumes a raw audio
 * format with 32-bit floats and a value range from [-1.0, 1.0].
 *
 * use mouse to change playback speed and amplitude. toggle looping behavior by pressing 'L'. press mouse to
 * rewind sample ( if not set to looping ).
 *
 * note that samples can either be played once or looped. if a sample is played once it must be rewound before it
 * can be played again. also note that a sample buffer can be cropped with `set_in()` + `set_out()`.
 */

Sampler mSampler;

void settings() {
    size(640, 480);
}

void setup() {
    byte[] mData = SampleDataSNARE.data;
    // alternatively load data with `loadBytes("audio.raw")` ( raw format, 32bit IEEE float )
    mSampler = new Sampler();
    mSampler.load(mData);
    mSampler.enable_loop(true);
    mSampler.set_loop_all();
    mSampler.start();
    DSP.start(this);
}

void draw() {
    background(255);
    stroke(0);
    DSP.draw_buffers(g, width, height);
    line(width * 0.5f, height * 0.5f + 5, width * 0.5f, height * 0.5f - 5);
    fill(0);
    noStroke();
    circle(60, 60, mSampler.is_looping() ? 50 : 10);
    circle(120, 60, mSampler.interpolate_samples() ? 50 : 10);
}

void mousePressed() {
    mSampler.rewind();
}

void mouseMoved() {
    mSampler.set_speed(map(mouseX, 0, width, -4, 4));
    mSampler.set_amplitude(map(mouseY, 0, height, 0.9f, 0.0f));
}

void keyPressed() {
    switch (key) {
        case 'l':
            mSampler.enable_loop(false);
            break;
        case 'L':
            mSampler.enable_loop(true);
            break;
        case 'i':
            mSampler.interpolate_samples(false);
            break;
        case 'I':
            mSampler.interpolate_samples(true);
            break;
    }
}

void audioblock(float[] output_signal) {
    for (int i = 0; i < output_signal.length; i++) {
        output_signal[i] = mSampler.output();
    }
}
