import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to use patterns to create events.
 */

final Pattern mPatternA = new Pattern();

final Pattern mPatternB = new Pattern();

final Pattern mPatternC = new Pattern();

final Sequencer<Integer> mSequenceA = new Sequencer<>(0, 1, 3, 5);

final Sequencer<Integer> mSequenceB = new Sequencer<>(0, 1, 3, 5);

final Sequencer<Integer> mSequenceC = new Sequencer<>(0, 1, 3, 5, 5, 3, 1, 0);

int mEventPositionPatternC;

int mBeat = 0;

void settings() {
    size(640, 480);
}

void setup() {
    ToneEngineDSP mToneEngine = Tone.get_DSP_engine();
    Reverb mReverb = new Reverb();
    mToneEngine.add_effect(mReverb);
    mPatternA.set_in_point(0);
    mPatternA.set_length(8);
    mPatternA.set_loop(LOOP_INFINITE);
    mPatternB.set_in_point(2);
    mPatternB.set_length(4);
    mPatternB.set_loop(LOOP_INFINITE);
    mPatternC.set_in_point(0);
    mPatternC.set_length(1);
    mPatternC.set_loop(16);
    mEventPositionPatternC = 0;
    Tone.instrument(0).set_pan(-0.5f);
    Tone.instrument(2).set_pan(0.0f);
    Tone.instrument(1).set_pan(0.5f);
    Beat.start(this, 120 * 4);
}

void draw() {
    background(255);
    stroke(0);
    noFill();
    Wellen.draw_tone_stereo(g, width, height);
    noStroke();
    fill(0);
    circle(width * 0.5f - 100, height * 0.5f, Tone.instrument(0).is_playing() ? 100 : 10);
    circle(width * 0.5f, height * 0.5f, Tone.instrument(2).is_playing() ? 100 : 10);
    circle(width * 0.5f + 100, height * 0.5f, Tone.instrument(1).is_playing() ? 100 : 10);
}

void keyPressed() {
    int mQuantizedInpoint = (mBeat / 8) * 8 + 8;
    mPatternC.set_in_point(mQuantizedInpoint);
    switch (key) {
        case '1':
            mPatternC.set_length(1);
            mPatternC.set_loop(16);
            mEventPositionPatternC = 0;
            break;
        case '2':
            mPatternC.set_length(4);
            mPatternC.set_loop(8);
            mEventPositionPatternC = 2;
            break;
    }
    mSequenceC.reset();
}

void beat(int pBeat) {
    mBeat = pBeat;
    Tone.instrument(0);
    if (mPatternA.event(pBeat, 0)) {
        int mNoteOffset = mSequenceA.step();
        Tone.note_on(Scale.get_note(Scale.MINOR_PENTATONIC, Note.NOTE_C2, mNoteOffset), 100, 0.1f);
    }
    Tone.instrument(1);
    if (mPatternB.event(pBeat, 2)) {
        int mNoteOffset = mSequenceB.step();
        Tone.note_on(Scale.get_note(Scale.MINOR_PENTATONIC, Note.NOTE_C3, mNoteOffset), 70, 0.1f);
    }
    Tone.instrument(2);
    if (mPatternC.event(pBeat, mEventPositionPatternC)) {
        int mNoteOffset = mSequenceC.step();
        if (mNoteOffset != -1) {
            Tone.note_on(Scale.get_note(Scale.MINOR_PENTATONIC, Note.NOTE_C4, mNoteOffset), 40, 0.05f);
        }
    }
}
