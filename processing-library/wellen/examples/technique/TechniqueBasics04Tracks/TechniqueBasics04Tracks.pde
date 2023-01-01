import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to build a composition with tracks.
 */

final Track mTrack = new Track();

final ModuleToneEngine mModuleBleepBleep = new ModuleToneEngine();

static final int PPQN = 24;

void settings() {
    size(640, 480);
}

void setup() {
    mTrack.tracks().add(mModuleBleepBleep);
    Beat.start(this, 120 * PPQN);
}

void draw() {
    background(255);
    translate(16, 16);
    fill(0);
    stroke(255);
    rect(0, 0, 128, 96);
    Wellen.draw_tone_stereo(g, 128, 96);
}

void beat(int pBeat) {
    mTrack.update(pBeat);
}

static class ModuleToneEngine extends Track {
    
ModuleToneEngine() {
        set_in_out_point(0, 3);
        set_loop(Wellen.LOOP_INFINITE);
    }
    
void beat(int beat_absolute, int beat_relative) {
        boolean mIs16thBeat = beat_relative % (PPQN / 4) == 0;
        if (mIs16thBeat) {
            int mQuarterNoteCount = beat_relative / PPQN;
            Tone.instrument(0);
            Tone.note_on(48 + (mQuarterNoteCount % 4) * 12, 70, 0.1f);
            if (mQuarterNoteCount % 4 == 0) {
                Tone.instrument(1);
                Tone.note_on(24, 85, 0.3f);
            }
            if (mQuarterNoteCount % 4 == 1) {
                Tone.instrument(2);
                Tone.note_on(36, 80, 0.2f);
            }
            if (mQuarterNoteCount % 4 == 3) {
                Tone.instrument(3);
                Tone.note_on(36 + 7, 75, 0.25f);
            }
        }
    }
}
