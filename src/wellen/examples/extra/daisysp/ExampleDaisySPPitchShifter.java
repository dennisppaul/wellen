package wellen.examples.extra.daisysp;

import processing.core.PApplet;
import wellen.SampleDataSNARE;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Sampler;
import wellen.extra.daisysp.PitchShifter;

public class ExampleDaisySPPitchShifter extends PApplet {
    //@add import wellen.extra.daisysp.*;

    private PitchShifter fPitchShifter;
    private Sampler fSampler;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        byte[] mData = SampleDataSNARE.data;
        fSampler = new Sampler();
        fSampler.load(mData);
        fSampler.set_loop_all();
        fSampler.start();

        fPitchShifter = new PitchShifter();
        fPitchShifter.Init(Wellen.DEFAULT_SAMPLING_RATE);

        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
        line(width * 0.5f, height * 0.5f + 5, width * 0.5f, height * 0.5f - 5);
    }

    public void mousePressed() {
        fSampler.rewind();
    }

    public void mouseMoved() {
        fPitchShifter.SetDelSize((int) map(mouseX, 0, width, 1, 16384));
        fPitchShifter.SetTransposition(map(mouseY, 0, height, 1.0f, 24.0f));
    }

    public void keyPressed() {
        switch (key) {
            case 'l':
            case 'L':
                fSampler.enable_loop(!fSampler.is_looping());
                break;
        }
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = fPitchShifter.Process(fSampler.output());
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDaisySPPitchShifter.class.getName());
    }
}
