package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Wellen;

public class ExampleDSP02PassThrough extends PApplet {

    /*
     * this example demonstrates how to receive audio data from the input device and pass it through to the output
     * device. this is somewhat the *hello world* of DSP.
     *
     * note that a microphone or some other line in must be available to run this example.
     */

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wellen.dumpAudioInputAndOutputDevices();
        DSP.start(this, 1, 1);
    }

    public void draw() {
        background(255);
        stroke(0);
        final int mBufferSize = DSP.get_buffer_size();
        DSP.draw_buffer(g, width, height);
    }

    public void audioblock(float[] pOutputSignal, float[] pInputSignal) {
        for (int i = 0; i < pInputSignal.length; i++) {
            pOutputSignal[i] = pInputSignal[i] * 0.25f;
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP02PassThrough.class.getName());
    }
}
