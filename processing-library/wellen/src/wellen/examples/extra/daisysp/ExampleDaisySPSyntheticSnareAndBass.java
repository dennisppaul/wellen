package wellen.examples.extra.daisysp;

import processing.core.PApplet;
import wellen.Beat;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.extra.daisysp.SyntheticBassDrum;
import wellen.extra.daisysp.SyntheticSnareDrum;

public class ExampleDaisySPSyntheticSnareAndBass extends PApplet {
    //@add import wellen.extra.daisysp.*;

    private int mBeatCount;
    private SyntheticBassDrum mBassDrum;
    private SyntheticSnareDrum mSnareDrum;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mBassDrum = new SyntheticBassDrum();
        mBassDrum.Init(Wellen.DEFAULT_SAMPLING_RATE);
        mSnareDrum = new SyntheticSnareDrum();
        mSnareDrum.Init(Wellen.DEFAULT_SAMPLING_RATE);
        DSP.start(this);
        Beat.start(this, 120);
    }

    public void draw() {
        background(255);
        noStroke();
        fill(0);
        float mScale = 0.98f * height - (mBeatCount % 2) * 50;
        circle(width * 0.5f, height * 0.5f, mScale);
        stroke(255);
        DSP.draw_buffers(g, width, height);
    }

    public void mouseMoved() {
        mBassDrum.SetDirtiness(map(mouseX, 0, width, 0, 1));
        mBassDrum.SetAccent(map(mouseY, 0, height, 0, 1));

        if (keyCode == SHIFT) {
            mSnareDrum.SetFreq(map(mouseX, 0, width, 0, 400));
            mSnareDrum.SetAccent(map(mouseY, 0, height, 0, 1));
        }
        if (keyCode == ALT) {
            mBassDrum.SetFreq(map(mouseX, 0, width, 0, 400));
            mBassDrum.SetAccent(map(mouseY, 0, height, 0, 1));
        }
    }

    public void beat(int beatCount) {
        mBeatCount = beatCount;
        if (mBeatCount % 2 == 1) {
            mSnareDrum.Trig();
        } else {
            mBassDrum.Trig();
        }
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = mSnareDrum.Process() + mBassDrum.Process();
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDaisySPSyntheticSnareAndBass.class.getName());
    }
}
