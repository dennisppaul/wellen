import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to record the input of `DSP` into a float array which is then played back via
 * `Sampler`. this example also demonstrates how to play a sample backwards.
 */

boolean mIsRecording;

float[] mRecording;

Sampler mSampler;

void settings() {
    size(640, 480);
}

void setup() {
    mSampler = new Sampler();
    mSampler.load(SampleDataSNARE.data);
    mSampler.loop(true);
    mIsRecording = false;
    DSP.start(this, 1, 1);
}

void draw() {
    background(255);
    DSP.draw_buffers(g, width, height);
    fill(0);
    float mSize = mRecording != null ? mRecording.length : mSampler.data().length;
    mSize /= Wellen.DEFAULT_SAMPLING_RATE;
    mSize *= 100.0f;
    ellipse(width * 0.5f, height * 0.5f, mSize + 5, mSize + 5);
}

void mouseMoved() {
    mSampler.set_speed(map(mouseX, 0, width, -5, 5));
    mSampler.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
}

void keyPressed() {
    if (key == ' ') {
        mIsRecording = true;
    }
}

void keyReleased() {
    mIsRecording = false;
}

void audioblock(float[] output_signal, float[] pInputSignal) {
    if (mIsRecording) {
        if (mRecording == null) {
            mRecording = new float[0];
        }
        mRecording = concat(mRecording, pInputSignal);
    } else {
        if (mRecording != null) {
            System.out.println("+++ recorded " + mRecording.length + " samples.");
            mSampler = new Sampler(mRecording);
            mSampler.loop(true);
            mRecording = null;
        }
    }
    for (int i = 0; i < output_signal.length; i++) {
        output_signal[i] = mSampler.output();
    }
}
