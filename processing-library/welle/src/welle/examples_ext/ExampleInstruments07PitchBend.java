package welle.examples_ext;

import processing.core.PApplet;
import welle.Note;
import welle.Scale;
import welle.Tone;

/**
 * this example shows how use pitch bending i.e offsetting the note’s frequency by a fraction of its frequency.
 */
public class ExampleInstruments07PitchBend extends PApplet {

    public void settings() {
        size(640, 480);
    }

    public void setup() {
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
    }

    public void mousePressed() {
        int mNote = Scale.get_note(Scale.MAJOR_CHORD_7, Note.NOTE_A3, (int) random(0, 10));
        Tone.note_on(mNote, 127);
    }

    public void mouseReleased() {
        Tone.note_off();
        Tone.pitch_bend(8192);
    }

    public void mouseDragged() {
        Tone.pitch_bend((int) map(mouseY, 0, height, 16383, 0));
    }

    public static void main(String[] args) {
        PApplet.main(ExampleInstruments07PitchBend.class.getName());
    }
}
