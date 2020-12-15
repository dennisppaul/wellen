package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.BeatDSP;
import wellen.DSP;
import wellen.Note;
import wellen.Tone;

public class ExampleDSP11BeatDSP extends PApplet {

    /*
     * this example demonstrates how to use `BeatDSP` to trigger beats, similar to the *normal* `Beat` mechanism, with
     * the only difference that events are timed by the speed at which `DSP` requests audio blocks.
     *
     * this has three consequences: beats are synced with the `DSP` audio engine, depending on the time stability of the
     * underlying audio engine beats might run more precise over time, and a timing error depending on the audio block
     * size is introduced ( e.g ( AUDIOBLOCK_SIZE=512 / SAMPLING_RATE=44100Hz ) = 0.01161SEC maximum timing error ).
     */

    private final int[] mNotes = {Note.NOTE_C3, Note.NOTE_C4, Note.NOTE_A2, Note.NOTE_A3};
    private int mBeatCount;

    private BeatDSP mBeat;
    private float mSignal;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Tone.start();
        mBeat = BeatDSP.start(this); /* create beat before `DSP.start` */
        DSP.start(this); /* DSP is only used to create beat events */
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
    }

    public void mouseMoved() {
        mBeat.set_bpm(map(mouseX, 0, width, 1, 480));
    }

    public void audioblock(float[] pOutputSamples) {
        for (int i = 0; i < pOutputSamples.length; i++) {
            mBeat.tick();
        }
    }

    public void beat(int pBeatCount) {
        int mNote = 45 + (int) random(0, 12);
        Tone.note_on(mNote, 100, 0.1f);
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP11BeatDSP.class.getName());
    }
}
