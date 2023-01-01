import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to use note events to create a composition.
 */

final NoteEvents fEvents = new NoteEvents();

void settings() {
    size(640, 480);
}

void setup() {
    Tone.enable_reverb(0.5f, 0.75f, 0.33f);
    fEvents.set_PPQ(24);
    Beat.start(this, 100 * fEvents.get_PPQ());
    Tone.instrument(0).set_pan(-0.5f);
    Tone.instrument(1).set_pan(0.0f);
    Tone.instrument(2).set_pan(0.5f);
}

void draw() {
    background(255);
    stroke(0);
    noFill();
    Wellen.draw_tone_stereo(g, width, height);
    noStroke();
    fill(0);
    circle(width * 0.5f - 100, height * 0.5f, Tone.instrument(0).is_playing() ? 100 : 10);
    circle(width * 0.5f, height * 0.5f, Tone.instrument(1).is_playing() ? 100 : 10);
    circle(width * 0.5f + 100, height * 0.5f, Tone.instrument(2).is_playing() ? 100 : 10);
}

final int mBaseNoteDefault = 36;

int mBaseNote = mBaseNoteDefault;

final int mBaseNoteIncrease = 7;

final int mBaseNoteIncreaseMax = 2;

void beat(int beat) {
    if (fEvents.is_phase(beat, fEvents.get_PPQ() * 16, 0)) {
        mBaseNote += mBaseNoteIncrease;
        if (mBaseNote > mBaseNoteDefault + mBaseNoteIncrease * mBaseNoteIncreaseMax) {
            mBaseNote = mBaseNoteDefault;
        }
        Tone.instrument(3);
        Tone.note_on(mBaseNote - 12, 18);
        Tone.instrument(4);
        Tone.note_on(mBaseNote - 5, 14);
    }
    Tone.instrument(0);
    if (fEvents.is_note(beat, NoteEvents.QUARTER_NOTE)) {
        Tone.note_on(mBaseNote, 80, 0.1f);
    }
    Tone.instrument(1);
    final int mEightNoteCount = fEvents.get_note(beat, NoteEvents.EIGHTH_NOTE);
    if (mEightNoteCount != NoteEvents.NO_EVENT && (mEightNoteCount % 4) != 3) {
        Tone.note_on(mBaseNote + 7, 70, 0.1f);
    }
    Tone.instrument(2);
    final int mHalfNoteCount = fEvents.get_note(beat, NoteEvents.HALF_NOTE);
    if (mHalfNoteCount != NoteEvents.NO_EVENT) {
        if (mHalfNoteCount % 3 == 0) {
            Tone.note_on(mBaseNote + 12, 40, 0.1f);
        } else {
            Tone.note_on(mBaseNote + 12 + 2, 40, 0.1f);
        }
    }
    Tone.instrument(5);
    if (fEvents.get_note(beat, NoteEvents.FULL_NOTE) % 3 == 2) {
        Tone.note_on(mBaseNote + 35, 15, 0.025f);
    }
}
