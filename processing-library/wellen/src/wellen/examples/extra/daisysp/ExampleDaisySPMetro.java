package wellen.examples.extra.daisysp;

import processing.core.PApplet;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.extra.daisysp.Metro;
import wellen.extra.daisysp.Pluck;

public class ExampleDaisySPMetro extends PApplet {
    //@add import wellen.extra.daisysp.*;

    private Metro mMetro;
    private Pluck mPluck;
    private boolean mBeat;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mPluck = new Pluck();
        mPluck.Init();

        mMetro = new Metro();
        mMetro.Init(2, Wellen.DEFAULT_SAMPLING_RATE);

        DSP.start(this);
    }

    public void draw() {
        background(255);
        noStroke();
        fill(0);
        float mScale = 0.98f * height - (mBeat ? 0 : 50);
        mBeat = false;
        circle(width * 0.5f, height * 0.5f, mScale);
        stroke(255);
        DSP.draw_buffers(g, width, height);
    }

    public void mouseMoved() {
        mMetro.SetFreq(map(mouseX, 0, width, 0, 16));
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            boolean mTrigger = mMetro.Process();
            if (mTrigger) {
                mBeat = true;
            }
            pOutputSignal[i] = mPluck.Process(mTrigger);
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDaisySPMetro.class.getName());
    }
}
