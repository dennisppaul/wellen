package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.Tone;
import wellen.ToneEngineDSP;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Distortion;

public class ExampleDSP16Distortion extends PApplet {

    /*
     * this example demonstrate how to use distortion. there is a series of different distortion types available. all
     * types are capable of pre-amplifying the signal ( see `set_amplification(float)` ) most types then clip or distort
     * the signal at a specified threshold ( `set_clip(float)` ).
     *
     * for distortion type `DISTORTION_BIT_CRUSHING` a number of bits needs to be specified through `set_bits(int)` to
     * achieve distortion by artificially reducing the bit range.
     */

    private ToneEngineDSP mToneEngine;
    private Distortion mDistortion;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mDistortion = new Distortion();
        mToneEngine = Tone.start(Wellen.TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT);
        Tone.instrument().set_oscillator_type(Wellen.OSC_TRIANGLE);
        DSP.start(this);
    }

    public void draw() {
        background(255);
        fill(0);
        stroke(0);
        ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
        line(50, height * 0.1f, width - 50, height * 0.1f);
        DSP.draw_buffers(g, width, height);
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                mDistortion.set_type(Wellen.DISTORTION_HARD_CLIPPING);
                break;
            case '2':
                mDistortion.set_type(Wellen.DISTORTION_FOLDBACK);
                break;
            case '3':
                mDistortion.set_type(Wellen.DISTORTION_FOLDBACK_SINGLE);
                break;
            case '4':
                mDistortion.set_type(Wellen.DISTORTION_FULL_WAVE_RECTIFICATION);
                break;
            case '5':
                mDistortion.set_type(Wellen.DISTORTION_HALF_WAVE_RECTIFICATION);
                break;
            case '6':
                mDistortion.set_type(Wellen.DISTORTION_INFINITE_CLIPPING);
                break;
            case '7':
                mDistortion.set_type(Wellen.DISTORTION_SOFT_CLIPPING_CUBIC);
                break;
            case '8':
                mDistortion.set_type(Wellen.DISTORTION_SOFT_CLIPPING_ARC_TANGENT);
                break;
            case '9':
                mDistortion.set_type(Wellen.DISTORTION_BIT_CRUSHING);
                break;
        }
    }

    public void mouseDragged() {
        mDistortion.set_clip(map(mouseX, 0, width, 0.0f, 1.0f));
        mDistortion.set_bits((int) map(mouseX, 0, width, 1, 17));
        mDistortion.set_amplification(map(mouseY, 0, height, 0.0f, 10.0f));
    }

    public void mousePressed() {
        int mNote = 36 + (int) random(12);
        Tone.instrument(0).note_on(mNote, 80);
    }

    public void mouseReleased() {
        Tone.instrument(0).note_off();
    }

    public void audioblock(float[] output_signal) {
        mToneEngine.audioblock(output_signal);
        for (int i = 0; i < output_signal.length; i++) {
            /* apply distortion to process sample. */
            output_signal[i] = Wellen.clamp(mDistortion.process(output_signal[i]));
            /* note that it might not be a good idea to apply the distortion *after* the ADSR envelope as this can
            cause quite step attacks. */
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP16Distortion.class.getName());
    }
}