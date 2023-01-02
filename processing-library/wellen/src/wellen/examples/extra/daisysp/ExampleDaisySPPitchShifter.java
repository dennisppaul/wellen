package wellen.examples.extra.daisysp;

import processing.core.PApplet;
import wellen.SampleDataSNARE;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Sampler;
import wellen.extra.daisysp.PitchShifter;

public class ExampleDaisySPPitchShifter extends PApplet {
    //@add import wellen.extra.daisysp.*;

    private PitchShifter mPitchShifter;
    private Sampler mSampler;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        byte[] mData = SampleDataSNARE.data;
        mSampler = new Sampler();
        mSampler.load(mData);
        mSampler.loop(true);
        mSampler.start();

        mPitchShifter = new PitchShifter();
        mPitchShifter.Init(Wellen.DEFAULT_SAMPLING_RATE);

        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
        line(width * 0.5f, height * 0.5f + 5, width * 0.5f, height * 0.5f - 5);
    }

    public void mousePressed() {
        mSampler.rewind();
    }

    public void mouseMoved() {
        mPitchShifter.SetDelSize((int) map(mouseX, 0, width, 1, 16384));
        mPitchShifter.SetTransposition(map(mouseY, 0, height, 1.0f, 24.0f));
    }

    public void keyPressed() {
        switch (key) {
            case 'l':
            case 'L':
                mSampler.loop(!mSampler.is_looping());
                break;
        }
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = mPitchShifter.Process(mSampler.output());
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDaisySPPitchShifter.class.getName());
    }
}
