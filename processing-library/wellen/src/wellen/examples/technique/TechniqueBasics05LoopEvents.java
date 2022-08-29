package wellen.examples.technique;

import processing.core.PApplet;
import wellen.Beat;
import wellen.Loop;
import wellen.Tone;
import wellen.Wellen;

public class TechniqueBasics05LoopEvents extends PApplet {

    /*
     * this example demonstrates how to loop events to create a composition.
     */

    private final Loop mLoopA = new Loop();
    private final Loop mLoopB = new Loop();
    private final Loop mLoopC = new Loop();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Beat.start(this, 300);

        mLoopA.set_length(3);
        mLoopB.set_length(4);
        mLoopC.set_length(5);

        Tone.instrument(0).set_pan(-0.5f);
        Tone.instrument(1).set_pan(0.0f);
        Tone.instrument(2).set_pan(0.5f);
    }

    public void draw() {
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

    public void beat(int pBeat) {
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

    public static void main(String[] args) {
        PApplet.main(TechniqueBasics05LoopEvents.class.getName());
    }
}
