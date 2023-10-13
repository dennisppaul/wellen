package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Sampler;
import wellen.dsp.Wavetable;

public class ExampleDSP27SamplerEnvelope extends PApplet {

    /*
     * this example demonstrates how to use a `Sampler` as an envelope on a signal.
     *
     * press `+` or `-` to change the envelope form. press `space` to play the envelope.
     */

    private Sampler fEnvelope;
    private int fEnvelopeForm = Wellen.ENVELOPE_FORM_BLACKMAN;
    private Wavetable fWavetable;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        fEnvelope = new Sampler(1024);
        Wellen.fill_envelope(fEnvelope.get_buffer(), fEnvelopeForm);

        fWavetable = new Wavetable();
        fWavetable.set_frequency(2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE);
        fWavetable.set_amplitude(0.25f);
        fWavetable.set_waveform(Wellen.WAVEFORM_TRIANGLE);

        DSP.start(this);
    }

    public void draw() {
        background(255);
        stroke(0);
        DSP.draw_buffers(g, width, height);

        translate(10, 10);
        final float mScale = 0.25f;
        final float mPadding = 8;
        final float mWidth = width * mScale;
        final float mHeight = height * mScale;
        noStroke();
        fill(0);
        rect(mPadding / -2, mPadding / -2, mWidth + mPadding, mHeight + mPadding);
        stroke(255);
        Wellen.draw_buffer(g, mWidth, mHeight, fEnvelope.get_buffer());
    }

    public void mouseMoved() {
        final float mDuration = map(mouseX, 0, width, 0, 5);
        fEnvelope.set_duration(mDuration);
    }

    public void keyPressed() {
        switch (key) {
            case '+':
                fEnvelopeForm++;
                if (fEnvelopeForm >= Wellen.NUM_ENVELOPE_FORM) {
                    fEnvelopeForm = Wellen.NUM_ENVELOPE_FORM - 1;
                }
                Wellen.fill_envelope(fEnvelope.get_buffer(), fEnvelopeForm);
                break;
            case '-':
                fEnvelopeForm--;
                if (fEnvelopeForm < 0) {
                    fEnvelopeForm = 0;
                }
                Wellen.fill_envelope(fEnvelope.get_buffer(), fEnvelopeForm);
                break;
            case ' ':
                fEnvelope.rewind();
                fEnvelope.play();
                break;
        }
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = fWavetable.output() * fEnvelope.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP27SamplerEnvelope.class.getName());
    }
}
