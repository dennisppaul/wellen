package de.hfkbremen.ton.applications;

import de.hfkbremen.ton.BeatEvent;
import de.hfkbremen.ton.BeatListener;
import de.hfkbremen.ton.Instrument;
import de.hfkbremen.ton.Note;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

public class AppTonIntroductionClip extends PApplet {

    private static final float SCALE = 720.0f / 480.0f;
    private static final float FONT_SCALE = 36;
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

        textFont(createFont("RobotoMono-Bold", FONT_SCALE * SCALE));
        textAlign(CENTER);

        Ton.start();

        mBeatB = BeatEvent.create(140);
        mBeatA = BeatEvent.create(120);
        mBeatC = BeatEvent.create(160);

        mLoopA = new Loop(0, Note.NOTE_C6);
        mBeatA.add(mLoopA);
        mLoopB = new Loop(1, Note.NOTE_C6 + 4);
        mBeatB.add(mLoopB);
        mLoopC = new Loop(2, Note.NOTE_C6 + 7);
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
                text("DIGITAL MUSIC WORKSHOP", width * 0.5f, height * 0.5f + FONT_SCALE * 0.25f * SCALE);
                mSceneDuration += 1.0f / frameRate;
            } else {
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
            Ton.instrument(instrument).osc_type(Instrument.TRIANGLE);
        }

        public void beat(int pBeatCount) {

            if (instrument == 0 && pBeatCount == 12) {
                mIntroDone = true;
                mBeatA.stop();
                mBeatB.stop();
                mBeatC.stop();
            }

            Ton.instrument(instrument);
            if (pBeatCount % 2 == 0) {
                Ton.note_on(note, 35, 0.25f);
                playing = true;
            } else if (pBeatCount % 2 == 1) {
                Ton.note_off(note);
                playing = false;
            }
        }

        public void stop() {
            Ton.instrument(instrument);
            Ton.note_off(note);
            playing = false;
        }
    }

    public static void main(String[] args) {
        PApplet.main(AppTonIntroductionClip.class.getName());
    }
}
