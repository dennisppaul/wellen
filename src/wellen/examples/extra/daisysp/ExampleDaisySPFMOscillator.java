package wellen.examples.extra.daisysp;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Wellen;
import wellen.extra.daisysp.Fm2;

public class ExampleDaisySPFMOscillator extends PApplet {

    private Fm2 mOscillator;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mOscillator = new Fm2();
        mOscillator.Init(Wellen.DEFAULT_SAMPLING_RATE);
        mOscillator.SetIndex(2);
        DSP.start(this);
    }

    public void draw() {
        background(255);
        noStroke();
        fill(0);
        float mScale = 0.98f * height;
        circle(width * 0.5f, height * 0.5f, mScale);
        stroke(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mouseMoved() {
        if (keyCode == ALT) {
            mOscillator.SetFrequency(map(mouseX, 0, width, 0, 10));
            mOscillator.SetRatio(map(mouseY, 0, height, 0, 10));
        }
        if (keyCode == SHIFT) {
            mOscillator.SetIndex(map(mouseY, 0, height, 0, 5));
        }
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mOscillator.Process() * 0.5f;
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDaisySPFMOscillator.class.getName());
    }
}
