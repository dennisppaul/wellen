package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.Note;
import de.hfkbremen.ton.Scale;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

/**
 * this examples shows how use pitch bending i.e offsetting the note by a fraction of its frequency.
 */
public class ExampleInstruments08PitchBend extends PApplet {

    public void settings() {
        size(640, 480);
    }

    public void setup() {
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, Ton.is_playing() ? 100 : 5, Ton.is_playing() ? 100 : 5);
    }

    public void mousePressed() {
        int mNote = Scale.note(Scale.MAJOR_CHORD_7, Note.NOTE_A3, (int) random(0, 10));
        Ton.note_on(mNote, 127);
    }

    public void mouseReleased() {
        Ton.note_off();
        Ton.pitch_bend(8192);
    }

    public void mouseDragged() {
        Ton.pitch_bend((int) map(mouseY, 0, height, 16383, 0));
    }

    public static void main(String[] args) {
        PApplet.main(ExampleInstruments08PitchBend.class.getName());
    }
}
