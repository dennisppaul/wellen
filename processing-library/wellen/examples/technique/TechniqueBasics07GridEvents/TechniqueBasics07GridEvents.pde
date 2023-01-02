import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to use grid events to create a composition.
 */

final int fBaseNoteDefault = 36;

int fBaseNote = fBaseNoteDefault;

final int fBaseNoteIncrease = 7;

final int fBaseNoteIncreaseMax = 2;

final Grid fGrid = new Grid();

void settings() {
    size(640, 480);
}

void setup() {
    Tone.enable_reverb(0.5f, 0.75f, 0.33f);
    fGrid.set_PPQN(24);
    Beat.start(this, 100 * fGrid.get_PPQN());
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

void beat(int beat) {
    /* change base note every 4 bars ( or 16 quarter notes ) */
    if (fGrid.event_phase(beat, fGrid.get_PPQN() * 16, 0)) {
        fBaseNote += fBaseNoteIncrease;
        if (fBaseNote > fBaseNoteDefault + fBaseNoteIncrease * fBaseNoteIncreaseMax) {
            fBaseNote = fBaseNoteDefault;
        }
        Tone.instrument(3);
        Tone.note_on(fBaseNote - 12, 18);
        Tone.instrument(4);
        Tone.note_on(fBaseNote - 5, 14);
    }
    /* play 1/8 notes */
    Tone.instrument(1);
    final int mEightNoteCount = fGrid.event_count(beat, Wellen.NOTE_EIGHTH);
    if (mEightNoteCount != Wellen.NO_EVENT && (mEightNoteCount % 4) != 3) {
        Tone.note_on(fBaseNote + 7, 70, 0.1f);
    }
    /* play 1/4 notes */
    Tone.instrument(0);
    if (fGrid.event(beat, Wellen.NOTE_QUARTER)) {
        Tone.note_on(fBaseNote, 80, 0.1f);
    }
    /* play 1/2 notes */
    Tone.instrument(2);
    final int mHalfNoteCount = fGrid.event_count(beat, Wellen.NOTE_HALF);
    if (mHalfNoteCount != Wellen.NO_EVENT) {
        if (mHalfNoteCount % 3 == 0) {
            Tone.note_on(fBaseNote + 12, 40, 0.1f);
        } else {
            Tone.note_on(fBaseNote + 12 + 2, 40, 0.1f);
        }
    }
    /* play 1/1 notes */
    Tone.instrument(5);
    if (fGrid.event_count(beat, Wellen.NOTE_WHOLE) % 3 == 2) {
        Tone.note_on(fBaseNote + 35, 15, 0.025f);
    }
}
