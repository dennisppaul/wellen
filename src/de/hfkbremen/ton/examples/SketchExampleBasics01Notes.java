package de.hfkbremen.ton.examples;

import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

/**
 * this examples shows how to use the default tone engine to play notes.
 */
public class SketchExampleBasics01Notes extends PApplet {

    public void settings() {
        size(640, 480);
    }

    public void setup() {
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, Ton.isPlaying() ? 100 : 5, Ton.isPlaying() ? 100 : 5);
    }

    public void mousePressed() {
        int mNote = 45 + (int) random(0, 12);
        Ton.note_on(mNote, 100);
    }

    public void mouseReleased() {
        Ton.note_off();
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleBasics01Notes.class.getName());
    }
}