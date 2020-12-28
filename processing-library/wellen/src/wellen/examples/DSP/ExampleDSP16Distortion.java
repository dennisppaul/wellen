package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Distortion;
import wellen.Tone;
import wellen.ToneEngineInternal;
import wellen.Wellen;

public class ExampleDSP16Distortion extends PApplet {

    /*
     * this example demonstrate how to use distortion.
     */

    private ToneEngineInternal mToneEngine;
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
        ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
        DSP.draw_buffer(g, width, height);
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                mDistortion.set_type(Distortion.TYPE.CLIP);
                break;
            case '2':
                mDistortion.set_type(Distortion.TYPE.FOLDBACK);
                break;
            case '3':
                mDistortion.set_type(Distortion.TYPE.FOLDBACK_SINGLE);
                break;
            case '4':
                mDistortion.set_type(Distortion.TYPE.ARC_TANGENT);
                break;
            case '5':
                mDistortion.set_type(Distortion.TYPE.ARC_HYPERBOLIC);
                break;
        }
    }

    public void mouseDragged() {
        mDistortion.set_clip(map(mouseX, 0, width, 0.0f, 1.0f));
        mDistortion.set_amplification(map(mouseY, 0, height, 0.0f, 10.0f));
    }


    public void mousePressed() {
        int mNote = 36 + (int) random(12);
        Tone.instrument(0).note_on(mNote, 80);
    }

    public void mouseReleased() {
        Tone.instrument(0).note_off();
    }

    public void audioblock(float[] pSamples) {
        mToneEngine.audioblock(pSamples);
        for (int i = 0; i < pSamples.length; i++) {
            /* apply distortion to process sample. */
            pSamples[i] = Wellen.clamp(mDistortion.process(pSamples[i]));
            /* note that it might not be a good idea to apply the distortion *after* the ADSR envelope as this can
            cause quite step attacks. */
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP16Distortion.class.getName());
    }
}