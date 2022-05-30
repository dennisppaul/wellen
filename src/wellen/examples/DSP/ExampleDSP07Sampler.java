package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.DSP;
import wellen.SampleDataSNARE;
import wellen.Sampler;

public class ExampleDSP07Sampler extends PApplet {

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

    private Sampler mSampler;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        byte[] mData = SampleDataSNARE.data;
        // alternatively load data with `loadBytes("audio.raw")` ( raw format, 32bit IEEE float )
        mSampler = new Sampler();
        mSampler.load(mData);
        mSampler.loop(true);
        mSampler.start();

        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
        line(width * 0.5f, height * 0.5f + 5, width * 0.5f, height * 0.5f - 5);
    }

    public void mousePressed() {
        mSampler.rewind();
    }

    public void mouseMoved() {
        mSampler.set_speed(map(mouseX, 0, width, -8, 8));
        mSampler.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
    }

    public void keyPressed() {
        switch (key) {
            case 'l':
            case 'L':
                mSampler.loop(!mSampler.is_looping());
                break;
        }
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mSampler.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP07Sampler.class.getName());
    }
}
