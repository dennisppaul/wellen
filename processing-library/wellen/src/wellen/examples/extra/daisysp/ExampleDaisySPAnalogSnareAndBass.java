package wellen.examples.extra.daisysp;

import processing.core.PApplet;
import wellen.Beat;
import wellen.DSP;
import wellen.Wellen;
import wellen.extra.daisysp.AnalogBassDrum;
import wellen.extra.daisysp.AnalogSnareDrum;

public class ExampleDaisySPAnalogSnareAndBass extends PApplet {
    //@add import wellen.extra.daisysp.*;

    private int mBeatCount;
    private AnalogBassDrum mBassDrum;
    private AnalogSnareDrum mSnareDrum;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mBassDrum = new AnalogBassDrum();
        mBassDrum.Init(Wellen.DEFAULT_SAMPLING_RATE);
        mSnareDrum = new AnalogSnareDrum();
        mSnareDrum.Init(Wellen.DEFAULT_SAMPLING_RATE);
        mSnareDrum.SetSustain(false);
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
        DSP.draw_buffer(g, width, height);
    }

    public void mouseMoved() {
        if (keyCode == SHIFT) {
            mSnareDrum.SetFreq(map(mouseX, 0, width, 0, 400));
            mSnareDrum.SetAccent(map(mouseY, 0, height, 0, 1));
        }
        if (keyCode == ALT) {
            mBassDrum.SetFreq(map(mouseX, 0, width, 0, 400));
            mBassDrum.SetAccent(map(mouseY, 0, height, 0, 1));
        }
    }

    public void beat(int pBeatCount) {
        mBeatCount = pBeatCount;
        if (mBeatCount % 2 == 1) {
            mSnareDrum.Trig();
        } else {
            mBassDrum.Trig();
        }
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mBassDrum.Process() * 1.7f + mSnareDrum.Process() * 0.3f;
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDaisySPAnalogSnareAndBass.class.getName());
    }
}
