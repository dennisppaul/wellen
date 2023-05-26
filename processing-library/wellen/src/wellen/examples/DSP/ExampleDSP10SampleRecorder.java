package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Sampler;

public class ExampleDSP10SampleRecorder extends PApplet {

    /**
     * this example demonstrates how to record the input of `DSP` with a `Sampler`. this example also demonstrates how
     * to play a sample backwards.
     */

    private Sampler fSampler;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        fSampler = new Sampler();
        DSP.start(this, 1, 1);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
        fill(0);
        float mSize = fSampler.get_buffer().length;
        mSize /= Wellen.DEFAULT_SAMPLING_RATE;
        mSize *= 100.0f;
        ellipse(width * 0.5f, height * 0.5f, mSize + 5, mSize + 5);

        float mOriginalSpeedX = map(1, -5, 5, 0, width);
        line(mOriginalSpeedX, height / 2 - 10, mOriginalSpeedX, height / 2 + 10);
    }

    public void mouseMoved() {
        fSampler.set_speed(map(mouseX, 0, width, -5, 5));
        fSampler.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
    }

    public void keyPressed() {
        fSampler.start_recording();
    }

    public void keyReleased() {
        int mLengthRecording = fSampler.end_recording();
        float mLengthRecordingInSeconds = (float) mLengthRecording / Wellen.DEFAULT_SAMPLING_RATE;
        print("+++ recorded " + mLengthRecording + " samples");
        println(" or " + nf(mLengthRecordingInSeconds, 0, 2) + " sec.");
        fSampler.set_loop_all();
        fSampler.play();
    }

    public void audioblock(float[] output_signal, float[] pInputSignal) {
        if (fSampler.is_recording()) {
            fSampler.record(pInputSignal);
        }
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = fSampler.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP10SampleRecorder.class.getName());
    }
}
