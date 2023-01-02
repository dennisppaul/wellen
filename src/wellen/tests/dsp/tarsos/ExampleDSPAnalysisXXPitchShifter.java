package wellen.tests.dsp.tarsos;

import processing.core.PApplet;
import wellen.Wellen;
import wellen.dsp.DSP;

public class ExampleDSPAnalysisXXPitchShifter extends PApplet {

    /*
     * TODO BROKEN, FIX IT
     * this example demonstrates how to use üitch shifter to transform an input signal.
     */

    private final PitchShifter fPitchShifter = new PitchShifter();
    private float fPitchShiftFactor = 1.8f;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        DSP.start(this, 1, 1);
    }

    public void mouseMoved() {
        fPitchShiftFactor = map(mouseY, 0, height, 0.5f, 2.0f);
    }

    public void draw() {
        background(255);

        noStroke();
        fill(0);
        circle(width * 0.5f, height * 0.5f, height * 0.98f);

        stroke(255);
        DSP.draw_buffers(g, width, height);
    }

    public void audioblock(float[] output_signal, float[] pInputSignal) {
        float[] mBuffer = new float[pInputSignal.length];
        Wellen.copy(pInputSignal, mBuffer);

//        fPitchShifter.process(mBuffer);
        fPitchShifter.smbPitchShift(fPitchShiftFactor,
                                    output_signal.length,
                                    1024,
                                    32,
                                    Wellen.DEFAULT_SAMPLING_RATE,
                                    mBuffer,
                                    output_signal);

        Wellen.copy(mBuffer, output_signal);
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSPAnalysisXXPitchShifter.class.getName());
    }
}

