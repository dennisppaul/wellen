package wellen.examples.instruments;

import processing.core.PApplet;
import wellen.Beat;
import wellen.Note;
import wellen.Scale;
import wellen.Tone;
import wellen.Wellen;

public class ExampleInstruments06SixteenVoices extends PApplet {

    /*
     * this examples demonstrates how to play all 16 instruments at the same time.
     */

    private int mBeatCount;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wellen.dumpMidiInputDevices();
        Wellen.dumpMidiOutputDevices();
        Wellen.dumpAudioInputAndOutputDevices();
        for (int i = 0; i < Tone.instruments().size(); i++) {
            final float mPan = 2.0f * i / Tone.instruments().size() - 1.0f;
            Tone.instruments().get(i).set_pan(mPan);
        }
        Beat.start(this, 120 * 3);
    }

    public void draw() {
        background(255);
        noStroke();
        fill(0);
        float mScale = (mBeatCount % 32) * 0.025f + 0.25f;
        ellipse(width * 0.5f, height * 0.5f, width * mScale, width * mScale);
    }

    public void beat(int pBeatCount) {
        mBeatCount = pBeatCount;
        int mInstrument = 15 - pBeatCount % 16;
        Tone.instrument(mInstrument);
        if (Tone.is_playing()) {
            Tone.note_off();
        } else {
            final int mVelocity = (int) map(mInstrument, 0, 15, 16, 2);
            Tone.note_on(Scale.get_note(Scale.CHORD_MAJOR_7TH, Note.NOTE_C3, mInstrument), mVelocity);
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleInstruments06SixteenVoices.class.getName());
    }
}
