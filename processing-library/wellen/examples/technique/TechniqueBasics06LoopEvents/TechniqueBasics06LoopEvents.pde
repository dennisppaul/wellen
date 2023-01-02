import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to use loop events to create a composition.
 */

final Loop fLoopA = new Loop();

final Loop fLoopB = new Loop();

final Loop fLoopC = new Loop();

void settings() {
    size(640, 480);
}

void setup() {
    fLoopA.set_length(3);
    fLoopB.set_length(4);
    fLoopC.set_length(5);
    Tone.instrument(0).set_pan(-0.5f);
    Tone.instrument(1).set_pan(0.0f);
    Tone.instrument(2).set_pan(0.5f);
    Beat.start(this, 300);
}

void draw() {
    background(255);
    stroke(0);
    noFill();
    Wellen.draw_tone_stereo(g, width, height);
    noStroke();
    fill(0);
    circle(width * 0.5f - 100, height * 0.5f, Tone.instrument(0).is_playing() ? 100 : 10);
    circle(width * 0.5f, height * 0.5f, Tone.instrument(1).is_playing() ? 100 : 10);
    circle(width * 0.5f + 100, height * 0.5f, Tone.instrument(2).is_playing() ? 100 : 10);
}

void beat(int beat) {
    Tone.instrument(0);
    if (fLoopA.event(beat, 0)) {
        Tone.note_on(36, 80, 0.1f);
    }
    Tone.instrument(1);
    if (fLoopB.event(beat, 1)) {
        Tone.note_on(48, 70, 0.1f);
    }
    Tone.instrument(2);
    if (fLoopC.event(beat, 2)) {
        Tone.note_on(60, 40, 0.1f);
    }
}
