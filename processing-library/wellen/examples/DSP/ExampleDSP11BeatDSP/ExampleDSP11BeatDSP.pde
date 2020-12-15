import wellen.*; 

/*
 * this example demonstrates how to use `BeatDSP` to trigger beats, similar to the *normal* `Beat` mechanism, with
 * the only difference that events are timed by the speed at which `DSP` requests audio blocks.
 *
 * this has three consequences: beats are synced with the `DSP` audio engine, depending on the time stability of the
 * underlying audio engine beats might run more precise over time, and a timing error depending on the audio block
 * size is introduced ( e.g ( AUDIOBLOCK_SIZE=512 / SAMPLING_RATE=44100Hz ) = 0.01161SEC maximum timing error ).
 */

final int[] mNotes = {Note.NOTE_C3, Note.NOTE_C4, Note.NOTE_A2, Note.NOTE_A3};

int mBeatCount;

BeatDSP mBeat;

float mSignal;

void settings() {
    size(640, 480);
}

void setup() {
    Tone.start();
    mBeat = BeatDSP.start(this); /* create beat before `DSP.start` */
    DSP.start(this); /* DSP is only used to create beat events */
}

void draw() {
    background(255);
    fill(0);
    ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
}

void mouseMoved() {
    mBeat.set_bpm(map(mouseX, 0, width, 1, 480));
}

void audioblock(float[] pOutputSamples) {
    for (int i = 0; i < pOutputSamples.length; i++) {
        mBeat.tick();
    }
}

void beat(int pBeatCount) {
    int mNote = 45 + (int) random(0, 12);
    Tone.note_on(mNote, 100, 0.1f);
}
