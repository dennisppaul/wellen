package wellen.examples.basics;

import processing.core.PApplet;
import wellen.Note;
import wellen.Tone;
import wellen.Wellen;

public class ExampleBasics01Notes extends PApplet {

    /*
     * this example demonstrates how to play *musical notes*. notes are played when mouse
     * is pressed. keys 1–4 change the presets.
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
        Tone.note_on(Note.NOTE_C3, 100);
    }

    public void mouseReleased() {
        Tone.note_off();
    }

    public void keyPressed() {
        if (key == '1') {
            Tone.preset(Wellen.INSTRUMENT_PRESET_SIMPLE);
        }
        if (key == '2') {
            Tone.preset(Wellen.INSTRUMENT_PRESET_SUB_SINE);
        }
        if (key == '3') {
            Tone.preset(Wellen.INSTRUMENT_PRESET_FAT);
        }
        if (key == '4') {
            Tone.preset(Wellen.INSTRUMENT_PRESET_NOISE);
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleBasics01Notes.class.getName());
    }
}