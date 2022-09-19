package wellen.examples.DSP.analysis;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Wavetable;
import wellen.Wellen;
import wellen.analysis.EnvelopeFollower;

public class ExampleDSPAnalysis02EnvelopeFollower extends PApplet {

    /*
     * this example demonstrates how to detect an envelope from an input signal.
     */

    private final EnvelopeFollower fEnvelopeFollower = new EnvelopeFollower();
    private final Wavetable fWavetable = new Wavetable();
    private float[] mEnvelopeFollowerBuffer;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        fWavetable.set_waveform(10, Wellen.WAVEFORM_SQUARE);
        fWavetable.set_frequency(110);

        fEnvelopeFollower.set_attack(0.0002f);
        fEnvelopeFollower.set_release(0.0004f);

        DSP.start(this, 1, 1);
    }

    public void mouseMoved() {
        fEnvelopeFollower.set_attack(map(mouseX, 0, width, 0, Wellen.seconds_to_samples(0.1f)));
        fEnvelopeFollower.set_release(map(mouseY, 0, height, 0, Wellen.seconds_to_samples(0.1f)));
    }

    public void draw() {
        background(255);

        noStroke();
        fill(0);
        circle(width * 0.5f, height * 0.5f, height * 0.98f);

        fill(255);
        float mEnvelopeAverage = getEnvelopeAverage();
        circle(width * 0.5f, height * 0.5f, mEnvelopeAverage * 100);

        stroke(255);
        DSP.draw_buffers(g, width, height);
        Wellen.draw_buffer(g, width, height, mEnvelopeFollowerBuffer);
    }

    private float getEnvelopeAverage() {
        float mEnvelopeAverage = 0;
        for (float v : mEnvelopeFollowerBuffer) {
            mEnvelopeAverage += v;
        }
        mEnvelopeAverage /= mEnvelopeFollowerBuffer.length;
        return mEnvelopeAverage;
    }

    public void audioblock(float[] pOutputSignal, float[] pInputSignal) {
        mEnvelopeFollowerBuffer = fEnvelopeFollower.process(pInputSignal);

        for (int i = 0; i < pOutputSignal.length; i++) {
            fWavetable.set_amplitude(mEnvelopeFollowerBuffer[i]);
            pOutputSignal[i] = fWavetable.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSPAnalysis02EnvelopeFollower.class.getName());
    }
}
