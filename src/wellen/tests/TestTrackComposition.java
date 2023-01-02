package wellen.tests;

import processing.core.PApplet;
import wellen.Beat;
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
        mComposition.tracks().add(new Module_0());
        mComposition.tracks().add(new Module_1());
        mComposition.tracks().add(new Module_2());
        Beat.start(this, 60);
        DSP.start(this, 2);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
    }

    public void mouseMoved() {
        mComposition.track(0).set_volume(map(mouseY, 0, height, 0, 0.5f));
    }

    public void beat(int beat) {
        System.out.println("------- " + nf(beat, 2));
        mComposition.update(beat);
    }

    public void audioblock(float[] output_signalLeft, float[] output_signalRight) {
        for (int i = 0; i < output_signalLeft.length; i++) {
            Signal s = mComposition.output_signal();
            output_signalLeft[i] = s.left();
            output_signalRight[i] = s.right();
        }
    }

    private static class Module_0 extends Track {
        public Module_0() {
            set_in_out_point(1, 7);
        }

        public Signal output_signal() {
            return Signal.create(-0.1f);
        }

        public void beat(int beat_absolute, int beat_relative) {
            System.out.println("000: " + nf(get_relative_position(beat_relative), 2));
        }
    }

    private static class Module_1 extends Track {
        public Module_1() {
            set_in_out_point(2, 4);
            set_loop(LOOP_INFINITE);
        }

        public Signal output_signal() {
            return Signal.create(0.1f);
        }

        public void beat(int beat_absolute, int beat_relative) {
            System.out.println("001: " + nf(get_relative_position(beat_relative), 2));
        }
    }

    private static class Module_2 extends Track {
        public Module_2() {
            set_out_point(6);
        }

        public Signal output_signal() {
            return Signal.create(0.1f);
        }

        public void beat(int beat_absolute, int beat_relative) {
            System.out.println("002: " + nf(get_relative_position(beat_relative), 2));
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestTrackComposition.class.getName());
    }
}

