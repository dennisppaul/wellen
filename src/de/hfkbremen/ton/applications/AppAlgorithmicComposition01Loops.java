package de.hfkbremen.ton.applications;

import de.hfkbremen.ton.BeatEvent;
import de.hfkbremen.ton.BeatListener;
import de.hfkbremen.ton.Note;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

public class AppAlgorithmicComposition01Loops extends PApplet {

    private BeatEvent mBeatA;
    private BeatEvent mBeatB;
    private BeatEvent mBeatC;
    private Loop mLoopA;
    private Loop mLoopB;
    private Loop mLoopC;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        noStroke();
        Ton.start();

        mBeatA = BeatEvent.create(120);
        mBeatB = BeatEvent.create(140);
        mBeatC = BeatEvent.create(160);

        mLoopA = new Loop(0, Note.NOTE_C4);
        mBeatA.add(mLoopA);
        mLoopB = new Loop(1, Note.NOTE_C4 + 4);
        mBeatB.add(mLoopB);
        mLoopC = new Loop(2, Note.NOTE_C4 + 7);
        mBeatC.add(mLoopC);
    }

    public void draw() {
        background(255);
        fill(0);
        if (mLoopA.playing) {
            ellipse(width * 0.25f, height * 0.5f, width * 0.15f, width * 0.15f);
        }
        if (mLoopB.playing) {
            ellipse(width * 0.5f, height * 0.5f, width * 0.15f, width * 0.15f);
        }
        if (mLoopC.playing) {
            ellipse(width * 0.75f, height * 0.5f, width * 0.15f, width * 0.15f);
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
        }

        public void beat(int pBeatCount) {
            Ton.instrument(instrument);
            if (pBeatCount % 2 == 0) {
                Ton.note_on(note, 50);
                playing = true;
            } else if (pBeatCount % 2 == 1) {
                Ton.note_off(note);
                playing = false;
            }
        }
    }

    public static void main(String[] args) {
        PApplet.main(AppAlgorithmicComposition01Loops.class.getName());
    }
}