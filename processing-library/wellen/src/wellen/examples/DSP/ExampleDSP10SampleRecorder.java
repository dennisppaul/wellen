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

    private boolean fIsRecording;
    private float[] fRecording;
    private Sampler fSampler;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        fSampler = new Sampler();
        fSampler.load(SampleDataSNARE.data);
        fSampler.set_loop_all();

        fIsRecording = false;

        DSP.start(this, 1, 1);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
        fill(0);
        float mSize = fRecording != null ? fRecording.length : fSampler.get_data().length;
        mSize /= Wellen.DEFAULT_SAMPLING_RATE;
        mSize *= 100.0f;
        ellipse(width * 0.5f, height * 0.5f, mSize + 5, mSize + 5);
    }

    public void mouseMoved() {
        fSampler.set_speed(map(mouseX, 0, width, -5, 5));
        fSampler.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
    }

    public void keyPressed() {
        if (key == ' ') {
            fIsRecording = true;
        }
    }

    public void keyReleased() {
        fIsRecording = false;
    }

    public void audioblock(float[] output_signal, float[] pInputSignal) {
        if (fIsRecording) {
            if (fRecording == null) {
                fRecording = new float[0];
            }
            fRecording = concat(fRecording, pInputSignal);
        } else {
            if (fRecording != null) {
                System.out.println("+++ recorded " + fRecording.length + " samples.");
                fSampler.set_data(fRecording);
                fSampler.enable_loop(true);
                fSampler.set_loop_all();
                fSampler.start();
                fRecording = null;
            }
        }
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = fSampler.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP10SampleRecorder.class.getName());
    }
}
