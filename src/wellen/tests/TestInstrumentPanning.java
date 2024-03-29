package wellen.tests;

import processing.core.PApplet;
import wellen.Tone;

public class TestInstrumentPanning extends PApplet {

    /*
     * this example demonstrates how to play *musical notes*.
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

        Tone.instrument().set_pan(map(mouseX, 0, width, -1, 1));
    }

    public void mousePressed() {
        int mNote = 45 + (int) random(0, 12);
        Tone.note_on(mNote, 100);
    }

    public void mouseReleased() {
        Tone.note_off();
    }

    public static void main(String[] args) {
        PApplet.main(TestInstrumentPanning.class.getName());
    }
}