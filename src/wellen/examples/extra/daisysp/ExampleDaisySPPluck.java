package wellen.examples.extra.daisysp;

import processing.core.PApplet;
import wellen.Beat;
import wellen.DSP;
import wellen.extra.daisysp.DaisySP;
import wellen.extra.daisysp.Pluck;

public class ExampleDaisySPPluck extends PApplet {
    //@add import wellen.extra.daisysp.*;

    private Pluck mPluck;
    private boolean mTrigger = false;

    private int mMIDINoteCounter = 0;
    private final int[] mMIDINotes = {36, 48, 39, 51};

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mPluck = new Pluck();
        mPluck.Init();
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
        mPluck.SetDecay(map(mouseX, 0, width, 0, 1));
        mPluck.SetDamp(map(mouseY, 0, height, 0, 1));
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                mPluck.SetMode(Pluck.PLUCK_MODE_RECURSIVE);
                break;
            case '2':
                mPluck.SetMode(Pluck.PLUCK_MODE_WEIGHTED_AVERAGE);
                break;
        }
    }

    public void beat(int pBeatCount) {
        mTrigger = true;
        mPluck.SetFreq(DaisySP.mtof(mMIDINotes[mMIDINoteCounter]));
        mMIDINoteCounter++;
        mMIDINoteCounter %= mMIDINotes.length;
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mPluck.Process(mTrigger);
            mTrigger = false;
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDaisySPPluck.class.getName());
    }
}
