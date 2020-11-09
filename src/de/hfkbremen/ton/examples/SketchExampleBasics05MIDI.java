package de.hfkbremen.ton.examples;

import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

/**
 * this examples shows how to use the MIDI tone engine to control a MIDI instrument ( i.e sending MIDI events to a MIDI
 * device ). make sure to set up the MIDI configuration properly in system control.
 */
public class SketchExampleBasics05MIDI extends PApplet {

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Ton.dumpMidiOutputDevices();
        /* ton enginges can be selected with `start`. in this case MIDI engine is selected with the first argument.
        the second argument selects the MIDI bus. note `start` must be the first call to `Ton` otherwise a default
         enginge is automatically selected. */
        Ton.start("midi", "Bus 1");
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, Ton.isPlaying() ? 100 : 5, Ton.isPlaying() ? 100 : 5);
    }

    public void mousePressed() {
        /* `instrument` in this context is equivalent to a *MIDI channel* ID. this also means that sound characteristics
        ( e.g `osc_type` ) are not available. */
        Ton.instrument(mouseX < width / 2.0 ? 0 : 1);
        int mNote = 45 + (int) random(0, 12);
        Ton.note_on(mNote, 127);
    }

    public void mouseReleased() {
        Ton.note_off();
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleBasics05MIDI.class.getName());
    }
}
