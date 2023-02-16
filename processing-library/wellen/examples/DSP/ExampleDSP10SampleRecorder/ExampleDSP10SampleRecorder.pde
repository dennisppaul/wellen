import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to record the input of `DSP` into a float array which is then played back via
 * `Sampler`. this example also demonstrates how to play a sample backwards.
 */

boolean fIsRecording;

float[] fRecording;

Sampler fSampler;

void settings() {
    size(640, 480);
}

void setup() {
    fSampler = new Sampler();
    fSampler.load(SampleDataSNARE.data);
    fSampler.set_loop_all();
    fIsRecording = false;
    DSP.start(this, 1, 1);
}

void draw() {
    background(255);
    DSP.draw_buffers(g, width, height);
    fill(0);
    float mSize = fRecording != null ? fRecording.length : fSampler.get_data().length;
    mSize /= Wellen.DEFAULT_SAMPLING_RATE;
    mSize *= 100.0f;
    ellipse(width * 0.5f, height * 0.5f, mSize + 5, mSize + 5);
}

void mouseMoved() {
    fSampler.set_speed(map(mouseX, 0, width, -5, 5));
    fSampler.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
}

void keyPressed() {
    if (key == ' ') {
        fIsRecording = true;
    }
}

void keyReleased() {
    fIsRecording = false;
}

void audioblock(float[] output_signal, float[] pInputSignal) {
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
