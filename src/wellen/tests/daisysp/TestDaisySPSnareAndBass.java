package wellen.tests.daisysp;

import processing.core.PApplet;
import wellen.Beat;
import wellen.DSP;
import wellen.Wellen;
import wellen.extern.daisysp.AnalogBassDrum;
import wellen.extern.daisysp.AnalogSnareDrum;

public class TestDaisySPSnareAndBass extends PApplet {

    private int mBeatCount;
    private AnalogBassDrum mAnalogBassDrum;
    private AnalogSnareDrum mAnalogSnareDrum;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mAnalogBassDrum = new AnalogBassDrum();
        mAnalogBassDrum.Init(Wellen.DEFAULT_SAMPLING_RATE);
        mAnalogSnareDrum = new AnalogSnareDrum();
        mAnalogSnareDrum.Init(Wellen.DEFAULT_SAMPLING_RATE);
        mAnalogSnareDrum.SetSustain(false);
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
        mAnalogSnareDrum.SetFreq(map(mouseX, 0, width, 0, 400));
        mAnalogSnareDrum.SetAccent(map(mouseY, 0, height, 0, 1));
    }

    public void beat(int pBeatCount) {
        mBeatCount = pBeatCount;
        mAnalogBassDrum.Trig();
        if (mBeatCount % 2 == 1) {
            mAnalogSnareDrum.Trig();
        }
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mAnalogBassDrum.Process() * 1.7f + mAnalogSnareDrum.Process() * 0.3f;
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestDaisySPSnareAndBass.class.getName());
    }
}
