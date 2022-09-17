package wellen.examples.extra.daisysp;

import processing.core.PApplet;
import wellen.Beat;
import wellen.DSP;
import wellen.Wellen;
import wellen.extra.daisysp.DaisySP;
import wellen.extra.daisysp.Flanger;
import wellen.extra.daisysp.Pluck;

public class ExampleDaisySPFlanger extends PApplet {
    //@add import wellen.extra.daisysp.*;

    private Pluck mPluck;
    private Flanger mFlanger;

    private int mMIDINoteCounter = 0;
    private final int[] mMIDINotes = {36, 48, 39, 51};

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mFlanger = new Flanger();
        mFlanger.Init(Wellen.DEFAULT_SAMPLING_RATE);

        mPluck = new Pluck();
        mPluck.Init();
        mPluck.SetDecay(0.5f);
        mPluck.SetDamp(0.85f);

        DSP.start(this);
        Beat.start(this, 240);
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

    public void mouseMoved() {
        if (keyCode == SHIFT) {
            mFlanger.SetFeedback(map(mouseX, 0, width, 0, 1));
            mFlanger.SetLfoDepth(map(mouseY, 0, height, 0, 1));
        }
        if (keyCode == ALT) {
            mFlanger.SetLfoFreq(map(mouseX, 0, width, 0, 20));
            mFlanger.SetDelay(map(mouseY, 0, height, 0, 1));
        }
    }

    public void beat(int pBeatCount) {
        mPluck.Trig();
        mPluck.SetFreq(DaisySP.mtof(mMIDINotes[mMIDINoteCounter]));
        mMIDINoteCounter++;
        mMIDINoteCounter %= mMIDINotes.length;
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mFlanger.Process(mPluck.Process());
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDaisySPFlanger.class.getName());
    }
}
