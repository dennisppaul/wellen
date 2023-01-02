package wellen.examples.DSP.analysis;

import processing.core.PApplet;
import wellen.Beat;
import wellen.Tone;
import wellen.ToneEngineDSP;
import wellen.Wellen;
import wellen.analysis.BeatDetection;
import wellen.dsp.DSP;

public class ExampleDSPAnalysis03BeatDetection extends PApplet {

    //@add import wellen.analysis.*;

    /*
     * this example demonstrates how to detect a beat from an input signal.
     * @TODO(the algorithm is not working particularuly fine …)
     */

    private final BeatDetection fBeatDetection = new BeatDetection();
    private ToneEngineDSP fToneEngine;
    private final int[] fBassPattern = {1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1,};
    private final int[] fSnarePattern = {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0,};
    private final boolean[] fBeatDetectedPattern = new boolean[fBassPattern.length];
    private int fCurrentBeat = fBassPattern.length - 1;
    private boolean fBeatDetected = false;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        fToneEngine = Tone.start(Wellen.TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT);
        Tone.instrument(0);
        Tone.instrument().set_adsr(0.005f, 0.01f, 0.25f, 0.01f);
        Tone.instrument(1);
        Tone.instrument().set_oscillator_type(Wellen.WAVEFORM_NOISE);
        Tone.instrument().set_adsr(0.005f, 0.5f, 0.01f, 0.01f);

        fBeatDetection.set_sensitivity(60);
        fBeatDetection.set_threshold(3);

        DSP.start(this, 1);
        Beat.start(this, 120 * 4);
    }

    public void draw() {
        background(255);
        fill(0);
        circle(width * 0.5f, height * 0.5f, height * 0.98f);

        final boolean mBeatEvent = (fBassPattern[fCurrentBeat] == 1);
        fill(255);
        circle(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 50);

        stroke(255);
        DSP.draw_buffers(g, width, height);

        for (int i = 0; i < fBeatDetectedPattern.length; i++) {
            float x = map(i % 4, 0, 3, width * 0.30f, width * 0.7f);
            float y = map(i / 4, 0, 3, height * 0.25f, height * 0.75f);
            noStroke();
            fill(255);
            circle(x, y, fBeatDetectedPattern[i] ? 20 : 5);
            if (i == fCurrentBeat) {
                noFill();
                stroke(255);
                circle(x, y, 25);
            }
        }
    }

    public void mousePressed() {
        Tone.instrument(1);
        Tone.note_on(36, 80, 0.1f);
    }

    public void mouseMoved() {
        fBeatDetection.set_threshold(map(mouseX, 0, width, 0, 20));
        fBeatDetection.set_sensitivity(map(mouseY, 0, height, 0, 100));
        System.out.print(fBeatDetection.get_threshold());
        System.out.print(", ");
        System.out.println(fBeatDetection.get_sensitivity());
    }

    public void beat(int beatCount) {
        fBeatDetectedPattern[fCurrentBeat] = fBeatDetected;
        if (fBeatDetected) {
            fBeatDetected = false;
        }

        fCurrentBeat++;
        fCurrentBeat %= fBassPattern.length;
        if (fBassPattern[fCurrentBeat] == 1) {
            Tone.instrument(0);
            Tone.note_on(36, 100, 0.1f);
        }
        if (fSnarePattern[fCurrentBeat] == 1) {
            Tone.instrument(1);
            Tone.note_on(36, 80, 0.05f);
        }
    }

    public void audioblock(float[] output_signal) {
        fToneEngine.audioblock(output_signal);

        /* detect pitch and set oscillator */
        fBeatDetection.process(output_signal);
        if (fBeatDetection.get_time_stamp() > 0) {
            fBeatDetected = true;
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSPAnalysis03BeatDetection.class.getName());
    }
}
