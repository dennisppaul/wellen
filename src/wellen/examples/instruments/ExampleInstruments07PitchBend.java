package wellen.examples.instruments;

import processing.core.PApplet;
import wellen.Note;
import wellen.Tone;

public class ExampleInstruments07PitchBend extends PApplet {

    /*
     * this example shows how use pitch bending i.e offsetting the note’s frequency by a fraction of its frequency.
     */

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
        Tone.note_on(Note.NOTE_A4, 127);
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
