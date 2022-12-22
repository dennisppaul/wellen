package wellen.examples.extra.daisysp;

import processing.core.PApplet;
import wellen.Beat;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.extra.daisysp.DaisySP;
import wellen.extra.daisysp.Overdrive;
import wellen.extra.daisysp.Pluck;
import wellen.extra.daisysp.ReverbSc;

public class ExampleDaisySPOverdrive extends PApplet {
    //@add import wellen.extra.daisysp.*;

    private Pluck mPluck;
    private ReverbSc mReverb;
    private Overdrive mOverdrive;

    private int mMIDINoteCounter = 0;
    private final int[] mMIDINotes = {36, 48, 39, 51};

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mReverb = new ReverbSc();
        mReverb.Init(Wellen.DEFAULT_SAMPLING_RATE);
        mReverb.SetFeedback(0.75f);
        mReverb.SetLpFreq(8000);

        mOverdrive = new Overdrive();
        mOverdrive.Init();

        mPluck = new Pluck();
        mPluck.Init();
        mPluck.SetDecay(0.5f);
        mPluck.SetDamp(0.95f);

        DSP.start(this, 2);
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
        mOverdrive.SetDrive(map(mouseX, 0, width, 0, 1));
    }

    public void beat(int pBeatCount) {
        mPluck.Trig();
        mPluck.SetFreq(DaisySP.mtof(mMIDINotes[mMIDINoteCounter]));
        mMIDINoteCounter++;
        mMIDINoteCounter %= mMIDINotes.length;
    }

    public void audioblock(float[] pOutputSignalLeft, float[] pOutputSignalRight) {
        for (int i = 0; i < pOutputSignalLeft.length; i++) {
            mReverb.Process(mOverdrive.Process(mPluck.Process()));
            pOutputSignalLeft[i] = mReverb.GetLeft();
            pOutputSignalRight[i] = mReverb.GetRight();
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDaisySPOverdrive.class.getName());
    }
}
