import wellen.*; 

/*
 * this example demonstrates how to use the OSC tone engine to send OSC commands. the defined OSC address patterns
 * can be found in `ToneEngineOSC`
 *
 * @note in order to run this sketch the oscP5 library must be installed:
 *
 *     Sketch > Import Library… > Add Library…
 *
 * and imported:
 *
 *     import netP5.*;
 *     import oscP5.*;
 *
 */

int mNote;

void settings() {
    size(640, 480);
}

void setup() {
    Tone.start("osc", "127.0.0.1", "7001");
}

void draw() {
    background(255);
    fill(0);
    ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
}

void mousePressed() {
    Tone.instrument(mouseX < width / 2.0 ? 0 : 1);
    mNote = 45 + (int) random(0, 12);
    Tone.note_on(mNote, 100);
}

void mouseReleased() {
    Tone.note_off(mNote);
}
