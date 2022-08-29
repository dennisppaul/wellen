import wellen.*; 

/*
 * this example demonstrates how to loop events to create a composition.
 */

final Loop mLoopA = new Loop();

final Loop mLoopB = new Loop();

final Loop mLoopC = new Loop();

void settings() {
    size(640, 480);
}

void setup() {
    Beat.start(this, 300);
    mLoopA.set_length(3);
    mLoopB.set_length(4);
    mLoopC.set_length(5);
    Tone.instrument(0).set_pan(-0.5f);
    Tone.instrument(1).set_pan(0.0f);
    Tone.instrument(2).set_pan(0.5f);
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

void beat(int pBeat) {
    Tone.instrument(0);
    if (mLoopA.event(pBeat, 0)) {
        Tone.note_on(36, 80, 0.1f);
    }
    Tone.instrument(1);
    if (mLoopB.event(pBeat, 1)) {
        Tone.note_on(48, 70, 0.1f);
    }
    Tone.instrument(2);
    if (mLoopC.event(pBeat, 2)) {
        Tone.note_on(60, 40, 0.1f);
    }
}
