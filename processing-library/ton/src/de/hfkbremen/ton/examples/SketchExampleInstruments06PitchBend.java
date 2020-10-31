package de.hfkbremen.ton.examples;

import de.hfkbremen.ton.Note;
import de.hfkbremen.ton.Scale;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

/**
 * this examples shows how use pitch bending e.g offsetting the actual frequency.
 */
public class SketchExampleInstruments06PitchBend extends PApplet {

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        background(255);
    }

    public void draw() {
        background(Ton.isPlaying() ? 255 : 0);
    }

    public void mousePressed() {
        int mNote = Scale.note(Scale.MAJOR_CHORD_7, Note.NOTE_A3, (int) random(0, 10));
        Ton.noteOn(mNote, 127);
    }

    public void mouseReleased() {
        Ton.noteOff();
        Ton.pitch_bend(8192);
    }

    public void mouseDragged() {
        Ton.pitch_bend((int) map(mouseY, 0, height, 16383, 0));
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleInstruments06PitchBend.class.getName());
    }
}
