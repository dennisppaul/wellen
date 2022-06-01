package wellen.tests;

import processing.core.PApplet;
import wellen.Beat;
import wellen.DSP;
import wellen.Note;
import wellen.SAM;
import wellen.Sampler;
import wellen.Tone;

public class TestSAMSingsLongerNotes extends PApplet {

    private SAM mSAM;
    private SingFragment[] mWords;
    private Sampler mSampler;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mSampler = new Sampler();
        mSampler.set_speed(0.5f);
        mSAM = new SAM();
        mSAM.set_sing_mode(true);

        mWords = new SingFragment[]{new SingFragment("EH", Note.NOTE_C3, 2),
                                    new SingFragment("VERIY", Note.NOTE_D3, 2),
                                    new SingFragment("TAYM", Note.NOTE_D3 + 1, 1),
                                    new SingFragment("AY", Note.NOTE_F3, 2),
                                    new SingFragment("SIYIY", Note.NOTE_G3, 2),
                                    new SingFragment("YUW", Note.NOTE_F3, 2),
                                    new SingFragment("FAO", Note.NOTE_D3 + 1, 2),
                                    new SingFragment("LIHNX", Note.NOTE_F3, 1),
                                    new SingFragment("", Note.NOTE_F3, 1),

                                    new SingFragment("AY", Note.NOTE_C3, 1),
                                    new SingFragment("GEHT", Note.NOTE_A3 + 1, 2),
                                    new SingFragment("DAWN", Note.NOTE_G3 + 1, 2),
                                    new SingFragment("AAN", Note.NOTE_G3, 1),
                                    new SingFragment("MAY", Note.NOTE_F3, 2),
                                    new SingFragment("NIYZ", Note.NOTE_D3 + 1, 2),
                                    new SingFragment("AEND", Note.NOTE_F3, 2),
                                    new SingFragment("PREY", Note.NOTE_C3, 2),
                                    new SingFragment("", Note.NOTE_C3, 3),
                                    };

        // | 1     |       | 2     |       | 3     |       | 4     |       |
        // | EH----------- | VERIY-------- | TAYM- | AY ---------- | SIY----

        // | 5     |       | 6     |       | 7     |       | 8     |       |
        // ------- |YUW----------- | FAO---------- | LIHNX-------- | AY--- |

        // | 9     |       | 10    |       | 11    |       | 12    |       |
        // | GEHT--------- | DAWN--------- | AAN-- | MAY---------- | NIYZ---

        // | 13    |       | 14    |       | 15    |       | 16    |       |
        // ------- | AEND--------- | PREY--------- |                       |

        DSP.start(this);
        Beat.start(this, 240);
    }

    static class SingFragment {
        final String text;
        final int pitch;
        final int duration;

        SingFragment(String pText, int pPitch, int pDuration) {
            text = pText;
            pitch = pPitch;
            duration = pDuration;
        }
    }

    public void draw() {
        background(255);

        /* selection */
        noStroke();
        fill(255, 127, 0, 63);
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
        stroke(255, 127, 0, 127);
        line(x0, 0, x0, height);
        stroke(255, 127, 0, 191);
        line(x1, 0, x1, height);

        /* samples */
        noFill();
        stroke(255, 127, 0);
        beginShape();
        for (int i = 0; i < mSampler.data().length; i++) {
            float x = map(i, 0, mSampler.data().length, 0, width);
            float y = map(mSampler.data()[i], -1.0f, 1.0f, 0, height);
            vertex(x, y);
        }
        endShape();

        /* draw audio buffer */
        stroke(0);
        DSP.draw_buffer(g, width, height);
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                mLoopIn = map(mouseX, 0, width, 0, 1);
                mSampler.set_loop_in(mLoopIn);
                break;
            case '2':
                mLoopOut = map(mouseX, 0, width, 0, 1);
                mSampler.set_loop_out(mLoopOut);
                break;
        }
    }

    private int mWordIndex = -1;
    private int mWordCounter = 0;
    private float mLoopIn = 0.5f;
    private float mLoopOut = 0.9f;

    public void beat(int pBeatCount) {
        Tone.note_on(pBeatCount % 2 == 0 ? Note.NOTE_C2 : Note.NOTE_C3, 50, 0.1f);

        if (mWordCounter == 0) {
            mWordIndex++;
            mWordIndex %= mWords.length;
            mWordCounter = mWords[mWordIndex].duration;
            if (mWords[mWordIndex].text.isEmpty()) {
                mSampler.stop();
            } else {
                mSAM.set_pitch(SAM.get_pitch_from_MIDI_note(mWords[mWordIndex].pitch));
                mSampler.set_data(mSAM.say(mWords[mWordIndex].text, true));
                mSampler.set_loop_in((int) (mLoopIn * mSampler.data().length - 1));
                mSampler.set_loop_out((int) (mLoopOut * mSampler.data().length - 1));
                mSampler.start();
            }
        }
        mWordCounter--;
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mSampler.output() * 0.1f;
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestSAMSingsLongerNotes.class.getName());
    }
}