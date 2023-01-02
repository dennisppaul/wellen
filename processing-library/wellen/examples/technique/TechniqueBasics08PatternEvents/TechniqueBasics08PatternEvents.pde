import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to use patterns to create events.
 */

int fBeat = 0;

int fEventPositionPatternC;

final Pattern fPatternA = new Pattern();

final Pattern fPatternB = new Pattern();

final Pattern fPatternC = new Pattern();

final Sequencer<Integer> fSequenceA = new Sequencer<>(0, 1, 3, 5);

final Sequencer<Integer> fSequenceB = new Sequencer<>(0, 1, 3, 5);

final Sequencer<Integer> fSequenceC = new Sequencer<>(0, 1, 3, 5, 5, 3, 1, 0);

void settings() {
    size(640, 480);
}

void setup() {
    ToneEngineDSP mToneEngine = Tone.get_DSP_engine();
    Reverb mReverb = new Reverb();
    mToneEngine.add_effect(mReverb);
    fPatternA.set_in_point(0);
    fPatternA.set_length(8);
    fPatternA.set_loop(LOOP_INFINITE);
    fPatternB.set_in_point(2);
    fPatternB.set_length(4);
    fPatternB.set_loop(LOOP_INFINITE);
    fPatternC.set_in_point(0);
    fPatternC.set_length(1);
    fPatternC.set_loop(16);
    fEventPositionPatternC = 0;
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
    int mQuantizedInpoint = (fBeat / 8) * 8 + 8;
    fPatternC.set_in_point(mQuantizedInpoint);
    switch (key) {
        case '1':
            fPatternC.set_length(1);
            fPatternC.set_loop(16);
            fEventPositionPatternC = 0;
            break;
        case '2':
            fPatternC.set_length(4);
            fPatternC.set_loop(8);
            fEventPositionPatternC = 2;
            break;
    }
    fSequenceC.reset();
}

void beat(int beat) {
    fBeat = beat;
    Tone.instrument(0);
    if (fPatternA.event(beat, 0)) {
        int mNoteOffset = fSequenceA.step();
        Tone.note_on(Scale.get_note(Scale.MINOR_PENTATONIC, Note.NOTE_C2, mNoteOffset), 100, 0.1f);
    }
    Tone.instrument(1);
    if (fPatternB.event(beat, 2)) {
        int mNoteOffset = fSequenceB.step();
        Tone.note_on(Scale.get_note(Scale.MINOR_PENTATONIC, Note.NOTE_C3, mNoteOffset), 70, 0.1f);
    }
    Tone.instrument(2);
    if (fPatternC.event(beat, fEventPositionPatternC)) {
        int mNoteOffset = fSequenceC.step();
        if (mNoteOffset != -1) {
            Tone.note_on(Scale.get_note(Scale.MINOR_PENTATONIC, Note.NOTE_C4, mNoteOffset), 40, 0.05f);
        }
    }
}
