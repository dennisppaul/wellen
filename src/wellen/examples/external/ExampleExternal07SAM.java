package wellen.examples.external;

import processing.core.PApplet;
import wellen.Beat;
import wellen.DSP;
import wellen.SAM;

public class ExampleExternal07SAM extends PApplet {

    /*
     * this example demonstrates how to use the SAM ( Software Automatic Mouth ) a speech software first published in
     * 1982 for Commodore C64 ( MacOS only ).
     *
     * move and drag mouse to change parameters.
     */

    private SAM mSAM;
    private String[] mWords;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mSAM = new SAM();

        String mText = "I know not by what power I am made bold, Nor how it may concern my modesty, In such a " +
                "presence here to plead my thoughts; But I beseech your grace that I may know The worst that may " +
                "befall me in this case, If I refuse to wed Demetrius.";
        mWords = split(mText, ' ');

        DSP.start(this);
        Beat.start(this, 140);
    }

    public void mouseMoved() {
        mSAM.set_pitch((int) map(mouseX, 0, width, 0, 255));
        mSAM.set_throat((int) map(mouseY, 0, height, 0, 255));
    }

    public void mouseDragged() {
        mSAM.set_speed((int) map(mouseX, 0, width, 0, 255));
        mSAM.set_mouth((int) map(mouseY, 0, height, 0, 255));
    }

    public void draw() {
        background(255);
        stroke(0);
        DSP.draw_buffer(g, width, height);
    }

    public void beat(int pBeatCount) {
        int mWordIndex = pBeatCount % mWords.length;
        mSAM.say(mWords[mWordIndex]);
    }

    public void audioblock(float[] pSamples) {
        for (int i = 0; i < pSamples.length; i++) {
            pSamples[i] = mSAM.output() * 0.5f;
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleExternal07SAM.class.getName());
    }
}