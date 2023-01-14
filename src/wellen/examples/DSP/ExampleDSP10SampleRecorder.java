package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.SampleDataSNARE;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Sampler;

public class ExampleDSP10SampleRecorder extends PApplet {

    /*
     * this example demonstrates how to record the input of `DSP` into a float array which is then played back via
     * `Sampler`. this example also demonstrates how to play a sample backwards.
     */

    private boolean mIsRecording;
    private float[] mRecording;
    private Sampler mSampler;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mSampler = new Sampler();
        mSampler.load(SampleDataSNARE.data);
        mSampler.enable_loop(true);

        mIsRecording = false;

        DSP.start(this, 1, 1);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
        fill(0);
        float mSize = mRecording != null ? mRecording.length : mSampler.data().length;
        mSize /= Wellen.DEFAULT_SAMPLING_RATE;
        mSize *= 100.0f;
        ellipse(width * 0.5f, height * 0.5f, mSize + 5, mSize + 5);
    }

    public void mouseMoved() {
        mSampler.set_speed(map(mouseX, 0, width, -5, 5));
        mSampler.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
    }

    public void keyPressed() {
        if (key == ' ') {
            mIsRecording = true;
        }
    }

    public void keyReleased() {
        mIsRecording = false;
    }

    public void audioblock(float[] output_signal, float[] pInputSignal) {
        if (mIsRecording) {
            if (mRecording == null) {
                mRecording = new float[0];
            }
            mRecording = concat(mRecording, pInputSignal);
        } else {
            if (mRecording != null) {
                System.out.println("+++ recorded " + mRecording.length + " samples.");
                mSampler = new Sampler(mRecording);
                mSampler.enable_loop(true);
                mRecording = null;
            }
        }
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = mSampler.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP10SampleRecorder.class.getName());
    }
}
