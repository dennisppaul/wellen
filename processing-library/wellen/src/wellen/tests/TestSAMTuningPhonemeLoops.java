package wellen.tests;

import processing.core.PApplet;
import wellen.Note;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.SAM;
import wellen.dsp.Sampler;

public class TestSAMTuningPhonemeLoops extends PApplet {

    private SAM mSAM;
    private SingFragment[] mWords;
    private Sampler mSampler;
    private int mWordIndex = -1;
    private float mLoopIn = 0.5f;
    private float mLoopOut = 0.9f;
    private static final float BORDER = 32;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mSampler = new Sampler();
        mSampler.set_speed(0.5f);
        mSAM = new SAM();
        mSAM.set_sing_mode(true);

        mWords = new SingFragment[]{new SingFragment("EH", Note.NOTE_C3, 4, 0.50241286f, 0.8981233f), new SingFragment(
                "VERIY",
                Note.NOTE_D3,
                4,
                0.85431075f,
                0.9947749f), new SingFragment("TAYM", Note.NOTE_D3 + 1, 2, 0.2726049f, 0.43558058f), new SingFragment(
                "AY",
                Note.NOTE_F3,
                4,
                0.07763485f,
                0.3692169f), new SingFragment("SIYIY", Note.NOTE_G3, 4, 0.49991727f, 0.89988416f), new SingFragment(
                "YUW",
                Note.NOTE_F3,
                4,
                0.5f,
                0.9f), new SingFragment("FAO", Note.NOTE_D3 + 1, 4, 0.5917517f, 0.98083735f), new SingFragment("LIHNX",
                                                                                                               Note.NOTE_F3,
                                                                                                               2,
                                                                                                               0.4180811f,
                                                                                                               0.61693764f),
                                    new SingFragment("", Note.NOTE_F3, 2, 0.5f, 0.9f),

                                    new SingFragment("AY", Note.NOTE_C3, 2, 0.07763485f, 0.3692169f), new SingFragment(
                "GEHT",
                Note.NOTE_A3 + 1,
                4,
                0.5871952f,
                0.64489114f), new SingFragment("DAWN", Note.NOTE_G3 + 1, 4, 0.6535342f, 0.76152956f), new SingFragment(
                "AAN",
                Note.NOTE_G3,
                2,
                0.028384725f,
                0.5691239f), new SingFragment("MAY", Note.NOTE_F3, 4, 0.36823878f, 0.59779024f),
                                    new SingFragment("NIYZ", Note.NOTE_D3 + 1, 4, 0.42345494f, 0.60453105f),
                                    new SingFragment("AEND", Note.NOTE_F3, 4, 0.10612828f, 0.34875825f),
                                    new SingFragment("PREY", Note.NOTE_C3, 4, 0.56590533f, 0.7307875f),
                                    new SingFragment("", Note.NOTE_C3, 6, 0.5f, 0.9f),};
        step();
        DSP.start(this);
    }

    static class SingFragment {
        final String text;
        final int pitch;
        final int duration;
        final float loop_in;
        final float loop_out;

        SingFragment(String pText, int pPitch, int pDuration, float pLoopIn, float pLoopOut) {
            text = pText;
            pitch = pPitch;
            duration = pDuration;
            loop_in = pLoopIn;
            loop_out = pLoopOut;
        }
    }

    public void draw() {
        background(255);
        translate(BORDER, BORDER);
        scale((width - BORDER * 2.0f) / width, (height - BORDER * 2.0f) / height);

        /* backdrop */
        noStroke();
        fill(0, 31);
        rect(0, 0, width, height);

        /* selection */
        noStroke();
        fill(191);
        float x0 = map(mSampler.get_loop_in(), 0, mSampler.data().length - 1, 0, width);
        float x1 = map(mSampler.get_loop_out(), 0, mSampler.data().length - 1, 0, width);
        if (mSampler.get_loop_in() >= 0 && mSampler.get_loop_out() >= 0) {
            if (mSampler.get_loop_in() < mSampler.get_loop_out()) {
                noStroke();
                beginShape();
                vertex(x0, 0);
                vertex(x1, 0);
                vertex(x1, height);
                vertex(x0, height);
                endShape();
            }
        }
        strokeWeight(4);
        stroke(0);
        line(x0, 0, x0, height);
        strokeWeight(1);
        stroke(0);
        line(x1, 0, x1, height);

        /* samples */
        noFill();
        stroke(0);
        beginShape();
        for (int i = 0; i < mSampler.data().length; i++) {
            float x = map(i, 0, mSampler.data().length, 0, width);
            float y = map(mSampler.data()[i], -1.0f, 1.0f, 0, height);
            vertex(x, y);
        }
        endShape();

        /* draw audio buffer */
        stroke(0, 63);
        DSP.draw_buffers(g, width, height);
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                mLoopIn = get_value_from_mouse_X();
                mSampler.set_loop_in_normalized(mLoopIn);
                break;
            case '2':
                mLoopOut = get_value_from_mouse_X();
                mSampler.set_loop_out_normalized(mLoopOut);
                break;
            case ' ':
                println(mWords[mWordIndex].text + ": " + mSampler.get_loop_in_normalized() + "f" + ", " + mSampler.get_loop_out_normalized() + "f");
                step();
                break;
            case 'z':
                int[] mLoopPoints = Wellen.find_zero_crossings(mSampler.data(),
                                                               mSampler.get_loop_in(),
                                                               mSampler.get_loop_out());
                if (mLoopPoints[0] > 0 && mLoopPoints[1] > 0) {
                    mSampler.set_loop_in(mLoopPoints[0]);
                    mSampler.set_loop_out(mLoopPoints[1]);
                }
                break;
        }
    }

    private float get_value_from_mouse_X() {
        return map(mouseX, BORDER, width - BORDER, 0, 1);
    }

    public void mousePressed() {
        mSampler.rewind();
        mSampler.start();
    }

    public void mouseReleased() {
        mSampler.stop();
    }

    private void step() {
        mWordIndex++;
        mWordIndex %= mWords.length;
        mSAM.set_pitch(SAM.get_pitch_from_MIDI_note(mWords[mWordIndex].pitch));
        mSampler.set_data(mSAM.say(mWords[mWordIndex].text, true));
        mLoopIn = mWords[mWordIndex].loop_in;
        mLoopOut = mWords[mWordIndex].loop_out;
        mSampler.set_loop_in_normalized(mLoopIn);
        mSampler.set_loop_out_normalized(mLoopOut);
        mSampler.forward();
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mSampler.output() * 0.1f;
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestSAMTuningPhonemeLoops.class.getName());
    }
}