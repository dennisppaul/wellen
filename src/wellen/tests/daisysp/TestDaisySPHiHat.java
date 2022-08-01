package wellen.tests.daisysp;

import processing.core.PApplet;
import wellen.Beat;
import wellen.DSP;
import wellen.Wellen;
import wellen.extern.daisysp.HiHat;

public class TestDaisySPHiHat extends PApplet {

    private int mBeatCount;
    private HiHat mBassDrum;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
//        mBassDrum = new HiHat(new HiHat.SquareNoise(), new HiHat.LinearVCA(), true);
        mBassDrum = new HiHat(new HiHat.RingModNoise(), new HiHat.SwingVCA(), true);
        mBassDrum.Init(Wellen.DEFAULT_SAMPLING_RATE);
        mBassDrum.SetDecay(0.8f);
        mBassDrum.SetNoisiness(0.6f);

        DSP.start(this);
        Beat.start(this, 120*4);
    }

    public void draw() {
        background(255);
        noStroke();
        fill(0);
        float mScale = 0.98f * height - (mBeatCount % 4) * 50;
        circle(width * 0.5f, height * 0.5f, mScale);
        stroke(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mouseMoved() {
        if (keyCode == SHIFT) {
            mBassDrum.SetFreq(map(mouseX, 0, width, 0, 1000));
            mBassDrum.SetTone(map(mouseY, 0, height, 0, 1));
        }
        if (keyCode == ALT) {
            mBassDrum.SetDecay(map(mouseX, 0, width, 0, 1));
            mBassDrum.SetNoisiness(map(mouseY, 0, height, 0, 1));
        }
    }

    public void beat(int pBeatCount) {
        mBeatCount = pBeatCount;
        if (mBeatCount % 13 != 0) {
            if (mBeatCount % 4 == 0) {
                mBassDrum.SetAccent(0.9f);
                mBassDrum.Trig();
            } else {
                mBassDrum.SetAccent(0.3f);
                mBassDrum.Trig();
            }
        }
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mBassDrum.Process();
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestDaisySPHiHat.class.getName());
    }
}
