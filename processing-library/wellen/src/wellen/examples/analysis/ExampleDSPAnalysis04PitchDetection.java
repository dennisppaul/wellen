package wellen.examples.analysis;

import processing.core.PApplet;
import wellen.Wellen;
import wellen.analysis.PitchDetection;
import wellen.dsp.DSP;
import wellen.dsp.Wavetable;

public class ExampleDSPAnalysis04PitchDetection extends PApplet {

    //@add import wellen.analysis.*;

    /*
     * this example demonstrates how to detect a pitch from an input signal and play the detected pitch back through
     * an oscillator.
     */

    private float fInputAmplification = 1.0f;
    private final PitchDetection fPitchDetection = new PitchDetection();
    private final Wavetable fWavetable = new Wavetable();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        fWavetable.set_waveform(10, Wellen.WAVEFORM_SQUARE);
        DSP.start(this, 1, 1);
    }

    public void mouseMoved() {
        fInputAmplification = Wellen.clamp(map(mouseY, 0, height * 0.75f, 2.0f, 0.0f), 0, 2);
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f,
                height * 0.5f,
                fPitchDetection.is_pitched() ? 100 : 5,
                fPitchDetection.is_pitched() ? 100 : 5);
        DSP.draw_buffers(g, width, height);
    }

    public void audioblock(float[] output_signal, float[] pInputSignal) {
        /* amplify audio signal */
        for (int i = 0; i < pInputSignal.length; i++) {
            pInputSignal[i] *= fInputAmplification;
        }

        /* detect pitch and set oscillator */
        fPitchDetection.process(pInputSignal);
        if (fPitchDetection.is_pitched()) {
            fWavetable.set_frequency(fPitchDetection.get_pitch());
            fWavetable.set_amplitude(0.25f, 32);
        } else {
            fWavetable.set_amplitude(0.0f, 32);
        }

        /* process output signal */
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = fWavetable.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSPAnalysis04PitchDetection.class.getName());
    }
}
