package wellen.tests;

import processing.core.PApplet;
import wellen.Beat;
import wellen.DSP;
import wellen.DSPModule;
import wellen.DSPTrack;
import wellen.Signal;

import static wellen.Wellen.LOOP_INFINITE;

public class TestTrackComposition extends PApplet {

    private final DSPTrack mComposition = new DSPTrack();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mComposition.modules().add(new Module_0());
        mComposition.modules().add(new Module_1());
        mComposition.modules().add(new Module_2());
        Beat.start(this, 60);
        DSP.start(this, 2);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mouseMoved() {
        mComposition.module(0).set_volume(map(mouseY, 0, height, 0, 0.5f));
    }

    public void beat(int pBeat) {
        System.out.println("------- " + nf(pBeat, 2));
        mComposition.beat(pBeat);
    }

    public void audioblock(float[] pOutputSignalLeft, float[] pOutputSignalRight) {
        for (int i = 0; i < pOutputSignalLeft.length; i++) {
            Signal s = mComposition.output_signal();
            pOutputSignalLeft[i] = s.left();
            pOutputSignalRight[i] = s.right();
        }
    }

    private static class Module_0 extends DSPModule {
        public Module_0() {
            set_in_outpoint(1, 7);
        }

        public Signal output_signal() {
            return Signal.create(-0.1f);
        }

        public void beat(int pBeat) {
            System.out.println("000: " + nf(get_relative_position(pBeat), 2));
        }
    }

    private static class Module_1 extends DSPModule {
        public Module_1() {
            set_in_outpoint(2, 4);
            set_loop(LOOP_INFINITE);
        }

        public Signal output_signal() {
            return Signal.create(0.1f);
        }

        public void beat(int pBeat) {
            System.out.println("001: " + nf(get_relative_position(pBeat), 2));
        }
    }

    private static class Module_2 extends DSPModule {
        public Module_2() {
            set_outpoint(6);
        }

        public Signal output_signal() {
            return Signal.create(0.1f);
        }

        public void beat(int pBeat) {
            System.out.println("002: " + nf(get_relative_position(pBeat), 2));
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestTrackComposition.class.getName());
    }
}

