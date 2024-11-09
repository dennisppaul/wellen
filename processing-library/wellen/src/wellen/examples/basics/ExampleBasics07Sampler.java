package wellen.examples.basics;

import processing.core.PApplet;
import wellen.SampleDataSNARE;
import wellen.Tone;
import wellen.dsp.Sampler;

public class ExampleBasics07Sampler extends PApplet {

    /*
     * this example demonstrates how to load and play a sample with Tone.
     */

    private Sampler mSampler;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mSampler = Tone.load_sample(SampleDataSNARE.data);
        // alternatively load data with `loadBytes("audio.raw")` ( raw format, 32bit IEEE float )
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, mSampler.is_playing() ? 100 : 5, mSampler.is_playing() ? 100 : 5);
    }

    public void keyPressed() {
        if (key == 'l') {
            mSampler.enable_loop(false);
        }
        if (key == 'L') {
            mSampler.enable_loop(true);
            mSampler.set_loop_all();
        }
    }

    public void mousePressed() {
        mSampler.trigger();
    }

    public void mouseReleased() {
        mSampler.stop();
    }

    public static void main(String[] args) {
        PApplet.main(ExampleBasics07Sampler.class.getName());
    }
}
