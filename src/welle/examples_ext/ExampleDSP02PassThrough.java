package welle.examples_ext;

import processing.core.PApplet;
import welle.DSP;
import welle.Welle;

/**
 * this example demonstrates how to receive audio data from the input device and pass it through to the output device.
 * this is somewhat the *hello world* of DSP.
 */
public class ExampleDSP02PassThrough extends PApplet {

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Welle.dumpAudioInputAndOutputDevices();
        DSP.start(this, 1, 1);
    }

    public void draw() {
        background(255);
        stroke(0);
        final int mBufferSize = DSP.get_buffer_size();
        if (DSP.get_buffer() != null) {
            for (int i = 0; i < mBufferSize; i++) {
                final float x = map(i, 0, mBufferSize, 0, width);
                point(x, map(DSP.get_buffer()[i], -1, 1, 0, height));
            }
        }
    }

    public void audioblock(float[] pOutputSamples, float[] pInputSamples) {
        for (int i = 0; i < pInputSamples.length; i++) {
            pOutputSamples[i] = pInputSamples[i] * 0.25f;
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP02PassThrough.class.getName());
    }
}
