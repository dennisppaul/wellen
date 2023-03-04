package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.Note;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Sampler;

public class ExampleDSP24SamplerAsInstrument extends PApplet {

    /**
     * this example demonstrates how to use a sampler with as an instrument. the general idea is that a sample is loaded
     * into the sampler and then the sampler is *tuned* with <code>tune_frequency_to()</code>. in this example a simple
     * sinewave sample, rendered at 261.63Hz ( C4 ), is loaded into the sampler. the sampler is then tuned with
     * <code>tune_frequency_to(261.63f)</code>. after this the sampler plays the sample at the frequency specdified
     * with <code>set_frequency(float)</code>.
     * <p>
     * this example also demonstrates how to set loop in- and outpoints around a region of the sample buffer that can be
     * played in a loop. after this the methods <code>note_on()</code> and <code>note_off(int, int)</code> can be used
     * to play the sample like an *instrument*.
     * <p>
     * press mouse to play
     */

    private Sampler mSampler;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        byte[] mData = loadBytes("../../../resources/sine-fade-in-out.raw"); // sinewave wuth 261.63 Hz
        mSampler = new Sampler();
        mSampler.load(mData);
        mSampler.set_loop_in(16007);
        mSampler.set_loop_out(31969);
        mSampler.enable_loop(true);
        mSampler.set_amplitude(0.5f);
        mSampler.tune_frequency_to(261.63f);
        mSampler.set_frequency(261.63f);
        DSP.start(this);
    }

    public void draw() {
        background(255);
        stroke(0);
        noFill();

        /* draw audio buffer */
        DSP.draw_buffers(g, width, height);
        line(0, height * 0.5f, width, height * 0.5f);

        /* draw samples */
        beginShape();
        for (int i = 0; i < mSampler.get_buffer().length; i += 128) {
            float x = map(i, 0, mSampler.get_buffer().length, 0, width);
            float y = map(mSampler.get_buffer()[i], -1.0f, 1.0f, 0, height);
            vertex(x, y);
        }
        endShape();

        /* draw play head */
        line(mSampler.get_position_normalized() * width, 0, mSampler.get_position_normalized() * width, height);
    }

    public void mousePressed() {
        mSampler.note_on();
    }

    public void mouseReleased() {
        mSampler.note_off();
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                mSampler.set_frequency(261.63f); // C4
                break;
            case '2':
                mSampler.set_frequency(329.63f); // E4
                break;
            case '3':
                mSampler.set_frequency(392.0f); // G4
                break;
            case '4':
                mSampler.set_frequency(466.16f); // Bb4
                break;
            case '5':
                mSampler.set_frequency(523.25f); // C5
                break;
            case '6':
                mSampler.note_on(Note.NOTE_C5, 63); // same note as above but set mith MIDI note value C5 = 72
                break;
        }
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = mSampler.output();
        }
    }

    public static void main(String[] args) {
        Wellen.run_sketch_with_resources(ExampleDSP24SamplerAsInstrument.class);
    }
}
