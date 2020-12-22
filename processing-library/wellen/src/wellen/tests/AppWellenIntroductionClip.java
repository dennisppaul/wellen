package wellen.tests;

import processing.core.PApplet;
import processing.core.PFont;
import wellen.BeatEvent;
import wellen.BeatListener;
import wellen.Note;
import wellen.Tone;
import wellen.Wellen;

/**
 * this sketch is used to produce the opening sequence for the video tutorials.
 */
public class AppWellenIntroductionClip extends PApplet {

    private static final float SCALE = 720.0f / 480.0f;
    private static final float FONT_SCALE = 36 * 3;
    private static final int BASE_NOTE = Note.NOTE_C3;
    private static boolean mIntroDone;
    private static float mSceneDuration = 0.0f;
    private static BeatEvent mBeatA;
    private static BeatEvent mBeatB;
    private static BeatEvent mBeatC;
    private static Loop mLoopA;
    private static Loop mLoopB;
    private static Loop mLoopC;

    public void settings() {
        size(1280, 720);
    }

    public void setup() {
        noStroke();

        printArray(PFont.list());
//        textFont(createFont("RobotoMono-Bold", FONT_SCALE * SCALE));
        textFont(createFont("Times New Roman", FONT_SCALE * SCALE));
        textAlign(CENTER);

        Tone.start();

        mBeatB = BeatEvent.create(140);
        mBeatA = BeatEvent.create(120);
        mBeatC = BeatEvent.create(160);

        mLoopA = new Loop(0, BASE_NOTE);
        mBeatA.add(mLoopA);
//        mLoopB = new Loop(1, BASE_NOTE + 4);
        mLoopB = new Loop(1, BASE_NOTE + 4);
        mBeatB.add(mLoopB);
//        mLoopC = new Loop(2, BASE_NOTE + 7);
        mLoopC = new Loop(2, BASE_NOTE + 7);
        mBeatC.add(mLoopC);
    }

    public void draw() {
        background(0);
        fill(255);

        if (!mIntroDone) {
            if (mLoopA.playing) {
                ellipse(width * 0.25f, height * 0.5f, width * 0.15f, width * 0.15f);
            }
            if (mLoopB.playing) {
                ellipse(width * 0.5f, height * 0.5f, width * 0.15f, width * 0.15f);
            }
            if (mLoopC.playing) {
                ellipse(width * 0.75f, height * 0.5f, width * 0.15f, width * 0.15f);
            }
        } else {
            if (mSceneDuration < 3.0f) {
                text("WELLEN", width * 0.5f, height * 0.5f + FONT_SCALE * 0.25f * SCALE);
//                text("DIGITAL MUSIC WORKSHOP", width * 0.5f, height * 0.5f + FONT_SCALE * 0.25f * SCALE);
                mSceneDuration += 1.0f / frameRate;
            }
            if (mSceneDuration > 2.0f) {
                mLoopA.stop();
                mLoopB.stop();
                mLoopC.stop();
            }
        }
    }

    private static class Loop implements BeatListener {

        final int note;
        final int instrument;
        boolean playing;

        Loop(int pInstrument, int pNote) {
            note = pNote;
            instrument = pInstrument;
            playing = false;
            Tone.instrument(instrument).set_oscillator_type(Wellen.WAVESHAPE_TRIANGLE);
        }

        public void beat(int pBeatCount) {

            if (instrument == 0 && pBeatCount == 12) {
                mIntroDone = true;
                mBeatA.stop();
                mBeatB.stop();
                mBeatC.stop();
            }

            Tone.instrument(instrument);
            if (pBeatCount % 2 == 0) {
                Tone.note_on(note, 35);
                playing = true;
            } else if (pBeatCount % 2 == 1) {
                Tone.note_off(note);
                playing = false;
            }
        }

        public void stop() {
            Tone.instrument(instrument);
            Tone.note_off(note);
            playing = false;
        }
    }

    public static void main(String[] args) {
        PApplet.main(AppWellenIntroductionClip.class.getName());
    }
}
