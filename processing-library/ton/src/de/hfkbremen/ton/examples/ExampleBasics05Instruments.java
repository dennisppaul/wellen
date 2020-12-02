package de.hfkbremen.ton.examples;

import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

/**
 * this example demonstrates how to use `instruments` to play multiple notes at the same time.
 */
public class ExampleBasics05Instruments extends PApplet {

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
        int mNote = 45 + (int) random(0, 12);
        Ton.instrument(0);
        Ton.instrument().set_pan(0.0f);
        Ton.note_on(mNote, 80);
        Ton.instrument(1);
        Ton.instrument().set_pan(0.2f);
        Ton.note_on(mNote + 7, 80);
        Ton.instrument(2);
        Ton.instrument().set_pan(-0.2f);
        Ton.note_on(mNote - 5, 80);
    }

    public void mouseReleased() {
        Ton.instrument(0);
        Ton.note_off();
        Ton.instrument(1);
        Ton.note_off();
        Ton.instrument(2);
        Ton.note_off();
    }

    public static void main(String[] args) {
        PApplet.main(ExampleBasics05Instruments.class.getName());
    }
}
