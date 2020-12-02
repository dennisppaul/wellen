package ton.examples_ext;

import ton.DSP;
import ton.SampleDataSNARE;
import ton.Sampler;
import ton.Ton;
import processing.core.PApplet;

/**
 * this examples demonstrates how to use a sampler ( a pre-recorded chunk of memory ) and play it at different speeds
 * and amplitudes. the sample data can also be loaded from external sources. the `load` method assumes a raw audio
 * format with 32-bit floats and a value range from [-1.0, 1.0].
 * <p>
 * note that samples can either be played once or looped. if a sample is played once it must be rewound before it can
 * be played again.
 */
public class ExampleDSP07Sampler extends PApplet {

    private Sampler mSampler;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        byte[] mData = SampleDataSNARE.data;
        // alternatively load data with `loadBytes("audio.raw")` ( raw format, 32bit float )
        mSampler = new Sampler();
        mSampler.load(mData);
        mSampler.loop(false);

        Ton.dumpAudioInputAndOutputDevices();
        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mousePressed() {
        mSampler.rewind();
    }

    public void mouseMoved() {
        mSampler.set_speed(map(mouseX, 0, width, 0, 32));
        mSampler.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
    }

    public void keyPressed() {
        switch (key) {
            case 'l':
            case 'L':
                mSampler.loop(true);
                break;
            default:
                mSampler.loop(false);
        }
    }

    public void audioblock(float[] pOutputSamples) {
        for (int i = 0; i < pOutputSamples.length; i++) {
            pOutputSamples[i] = mSampler.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP07Sampler.class.getName());
    }
}
