package wellen.examples.extra.daisysp;

import processing.core.PApplet;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.extra.daisysp.AdEnv;
import wellen.extra.daisysp.Adsr;
import wellen.extra.daisysp.Oscillator;

public class ExampleDaisySPAdEnv extends PApplet {
    //@add import wellen.extra.daisysp.*;

    private Adsr mAdsr;
    private AdEnv mAdEnv;
    private Oscillator mOscillator;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mAdEnv = new AdEnv();
        mAdEnv.Init(Wellen.DEFAULT_SAMPLING_RATE);
        mAdEnv.SetTime(AdEnv.ADENV_SEG_ATTACK, 0.5f);
        mAdEnv.SetTime(AdEnv.ADENV_SEG_DECAY, 0.5f);
        mAdEnv.SetMin(110);
        mAdEnv.SetMax(880);

        mAdsr = new Adsr();
        mAdsr.Init(Wellen.DEFAULT_SAMPLING_RATE);

        mOscillator = new Oscillator();
        mOscillator.Init(Wellen.DEFAULT_SAMPLING_RATE);
        mOscillator.SetFreq(220);
        mOscillator.SetAmp(0.75f);
        DSP.start(this);
    }

    public void draw() {
        background(255);
        noStroke();
        fill(0);
        float mScale = 0.98f * height;
        circle(width * 0.5f, height * 0.5f, mScale);
        stroke(255);
        DSP.draw_buffers(g, width, height);
    }

    public void mousePressed() {
        mAdEnv.Trigger();
    }

    public void mouseMoved() {
        mAdEnv.SetMin(map(mouseX, 0, width, 55, 880));
        mAdEnv.SetMax(map(mouseY, 0, height, 55, 880));
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            mOscillator.SetFreq(mAdEnv.Process());
            pOutputSignal[i] = mOscillator.Process() * mAdsr.Process(mousePressed);
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDaisySPAdEnv.class.getName());
    }
}
