package wellen.tests;

import processing.core.PApplet;
import wellen.Beat;
import wellen.dsp.DSP;
import wellen.dsp.SAM;

public class TestSAMTuningMIDINotes extends PApplet {

    private int mPitch;
    private SAM mSAM;
    private String[] mWords;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        textFont(createFont("Helvetica", 72));
        mSAM = new SAM();
        mSAM.set_sing_mode(true);

        /* from https://github.com/s-macke/SAM/wiki/Phonetic-Alphabet */
        mWords = new String[]{"AOAOAOAOAOAO",
//        "AOAO4AO-4",
                              "IYIYIYIY", "UXUX",};
        mPitch = 57;

        DSP.start(this);
        Beat.start(this, 120);
    }

//    public void mouseMoved() {
//        mPitch = (int) map(mouseY, 0, height, 180, 0);
//    }

    public void keyPressed() {
        if (keyCode == UP) {
            mPitch++;
        }
        if (keyCode == DOWN) {
            mPitch--;
        }
        if (key == ' ') {
            mPitch = 57;
        }
    }

    public void draw() {
        background(255);

        fill(0);
        noStroke();
        text(nf(mPitch, 3), 20, height / 2.0f);

        stroke(0);
        noFill();
        DSP.draw_buffers(g, width, height);
    }

    public void beat(int beatCount) {
        int mWordIndex = beatCount % mWords.length;
        mSAM.set_pitch(mPitch);
        mSAM.say(mWords[mWordIndex], true);
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = mSAM.output() * 0.5f;
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestSAMTuningMIDINotes.class.getName());
    }
}