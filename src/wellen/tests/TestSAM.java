package wellen.tests;

import processing.core.PApplet;
import wellen.DSP;
import wellen.SAM;

public class TestSAM extends PApplet {

    private SAM mSAM;
    private float[] mBuffer;
    private float mCounter;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mSAM = new SAM();
        mSAM.speak("wellen, wellen all is full of wellen!", false);
        mBuffer = mSAM.get_samples();
        mCounter = 0;
        DSP.start(this);
    }

    public void draw() {
        background(255);
        stroke(0);
        DSP.draw_buffer(g, width, height);
    }

    public void audioblock(float[] pSamples) {
        for (int i = 0; i < pSamples.length; i++) {
            pSamples[i] = mBuffer[(int)mCounter] * 0.33f;
            mCounter+=0.5f;
            mCounter %= mBuffer.length;
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestSAM.class.getName());
    }
}