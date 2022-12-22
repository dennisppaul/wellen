package wellen.tests;

import processing.core.PApplet;
import wellen.Beat;
import wellen.Module;
import wellen.Track;
import wellen.dsp.DSP;
import wellen.dsp.Signal;

import static wellen.Wellen.LOOP_INFINITE;

public class TestTrackComposition extends PApplet {

    private final Track mComposition = new Track();

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
        DSP.draw_buffers(g, width, height);
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

    private static class Module_0 extends Module {
        public Module_0() {
            set_in_out_point(1, 7);
        }

        public Signal output_signal() {
            return Signal.create(-0.1f);
        }

        public void beat(int beat) {
            System.out.println("000: " + nf(get_relative_position(beat), 2));
        }
    }

    private static class Module_1 extends Module {
        public Module_1() {
            set_in_out_point(2, 4);
            set_loop(LOOP_INFINITE);
        }

        public Signal output_signal() {
            return Signal.create(0.1f);
        }

        public void beat(int beat) {
            System.out.println("001: " + nf(get_relative_position(beat), 2));
        }
    }

    private static class Module_2 extends Module {
        public Module_2() {
            set_out_point(6);
        }

        public Signal output_signal() {
            return Signal.create(0.1f);
        }

        public void beat(int beat) {
            System.out.println("002: " + nf(get_relative_position(beat), 2));
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestTrackComposition.class.getName());
    }
}

