package wellen.examples.extra.daisysp;

import processing.core.PApplet;
import wellen.Beat;
import wellen.DSP;
import wellen.Wellen;
import wellen.extra.daisysp.DaisySP;
import wellen.extra.daisysp.StringVoice;

public class ExampleDaisySPStringVoice extends PApplet {
    //@add import wellen.extra.daisysp.*;

    private StringVoice mStringVoice;

    private int mMIDINoteCounter = 0;
    private final int[] mMIDINotes = {36, 48, 39, 51};

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mStringVoice = new StringVoice();
        mStringVoice.Init(Wellen.DEFAULT_SAMPLING_RATE);

        DSP.start(this);
        Beat.start(this, 240);
    }

    public void draw() {
        background(0);
        noStroke();
        fill(255);
        float mScale = 0.98f * height;
        circle(width * 0.5f, height * 0.5f, mScale);
        stroke(0);
        DSP.draw_buffers(g, width, height);
    }

    public void mouseMoved() {
        if (keyCode == SHIFT) {
            mStringVoice.SetAccent(map(mouseX, 0, width, 0, 1));
            mStringVoice.SetStructure(map(mouseY, 0, height, 0, 1));
        }
        if (keyCode == ALT) {
            mStringVoice.SetBrightness(map(mouseX, 0, width, 0, 1));
            mStringVoice.SetDamping(map(mouseY, 0, height, 0, 1));
        }
    }

    public void keyPressed() {
        switch (key) {
            case 's':
                mStringVoice.SetSustain(true);
                break;
            case 'S':
                mStringVoice.SetSustain(false);
                break;
        }
    }

    public void beat(int pBeatCount) {
        mStringVoice.Trig();
        mStringVoice.SetFreq(DaisySP.mtof(mMIDINotes[mMIDINoteCounter]));
        mMIDINoteCounter++;
        mMIDINoteCounter %= mMIDINotes.length;
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mStringVoice.Process();
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDaisySPStringVoice.class.getName());
    }
}
