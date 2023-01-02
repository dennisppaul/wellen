package wellen.examples.technique;

import processing.core.PApplet;
import wellen.Beat;
import wellen.Tone;
import wellen.Track;
import wellen.Wellen;

public class TechniqueBasics04Tracks extends PApplet {

    /*
     * this example demonstrates how to build a composition with tracks.
     */

    private final Track fTrack = new Track();
    private final ModuleToneEngine fModuleBleepBleep = new ModuleToneEngine();
    private static final int PPQN = 24;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        fTrack.tracks().add(fModuleBleepBleep);
        Beat.start(this, 120 * PPQN);
    }

    public void draw() {
        background(255);
        translate(16, 16);
        fill(0);
        stroke(255);
        rect(0, 0, 128, 96);
        Wellen.draw_tone_stereo(g, 128, 96);
    }

    public void beat(int beat) {
        fTrack.update(beat);
    }

    private static class ModuleToneEngine extends Track {

        public ModuleToneEngine() {
            set_in_out_point(0, 3);
            set_loop(Wellen.LOOP_INFINITE);
        }

        public void beat(int beat_absolute, int beat_relative) {
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

    public static void main(String[] args) {
        PApplet.main(TechniqueBasics04Tracks.class.getName());
    }
}
