package wellen.examples.DSP;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Wavetable;
import wellen.Wellen;

public class ExampleDSP06LFO extends PApplet {

    /*
     * this example demonstrates how to implement a low-frequency oscillator (LFO) by using wavetables to emulate
     * oscillators that affect the amplitude and frequency parameter of an oscillator (VCO).
     *
     * move mouse to change amplitude and frequency of LFO connected to frequency. keep mouse pressed to change
     * amplitude and frequency of LFO connected to amplitude.
     *
     * use keys to change waveform of LFOs and VCO.
     */

    private final Wavetable mVCO = new Wavetable(512);
    private final Wavetable mFrequencyLFO = new Wavetable(512);
    private final Wavetable mAmplitudeLFO = new Wavetable(512);
    private final float mBaseFrequency = 2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Wavetable.sine(mVCO.get_wavetable());
        mVCO.set_frequency(mBaseFrequency);
        mVCO.set_amplitude(0.25f);

        /* setup LFO for frequency */
        Wavetable.sine(mFrequencyLFO.get_wavetable());
        mFrequencyLFO.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_CUBIC);
        mFrequencyLFO.set_frequency(0);

        /* setup LFO for amplitude */
        Wavetable.sine(mAmplitudeLFO.get_wavetable());
        mAmplitudeLFO.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_CUBIC);
        mAmplitudeLFO.set_frequency(0);

        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
    }

    public void mouseDragged() {
        mAmplitudeLFO.set_frequency(map(mouseX, 0, width, 0.1f, 100.0f));
        mAmplitudeLFO.set_amplitude(map(mouseY, 0, height, 0.0f, 1.0f));
    }

    public void mouseMoved() {
        mFrequencyLFO.set_frequency(map(mouseX, 0, width, 0.1f, 100.0f));
        mFrequencyLFO.set_amplitude(map(mouseY, 0, height, 0.0f, 1.0f));
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                Wavetable.fill(mFrequencyLFO.get_wavetable(), Wellen.WAVEFORM_SINE);
                break;
            case '2':
                Wavetable.fill(mFrequencyLFO.get_wavetable(), Wellen.WAVEFORM_TRIANGLE);
                break;
            case '3':
                Wavetable.fill(mFrequencyLFO.get_wavetable(), Wellen.WAVEFORM_SAWTOOTH);
                break;
            case '4':
                Wavetable.fill(mFrequencyLFO.get_wavetable(), Wellen.WAVEFORM_SQUARE);
                break;
            case 'q':
                Wavetable.fill(mAmplitudeLFO.get_wavetable(), Wellen.WAVEFORM_SINE);
                break;
            case 'w':
                Wavetable.fill(mAmplitudeLFO.get_wavetable(), Wellen.WAVEFORM_TRIANGLE);
                break;
            case 'e':
                Wavetable.fill(mAmplitudeLFO.get_wavetable(), Wellen.WAVEFORM_SAWTOOTH);
                break;
            case 'r':
                Wavetable.fill(mAmplitudeLFO.get_wavetable(), Wellen.WAVEFORM_SQUARE);
                break;
            case 'a':
                Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVEFORM_SINE);
                break;
            case 's':
                Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVEFORM_TRIANGLE);
                break;
            case 'd':
                Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVEFORM_SAWTOOTH);
                break;
            case 'f':
                Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVEFORM_SQUARE);
                break;
        }
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            /* get frequency from LFO, map value range from [-1.0, 1.0] to [-40.0, 40.0] */
            float mFreq = map(mFrequencyLFO.output(), -1.0f, 1.0f, -40, 40);
            /* get ampliude from LFO, map value range from [-1.0, 1.0] to [0.0, 1.0] */
            float mAmp = map(mAmplitudeLFO.output(), -1.0f, 1.0f, 0, 1);
            /* set VCO */
            mVCO.set_frequency(mFreq + mBaseFrequency);
            mVCO.set_amplitude(mAmp);
            pOutputSignal[i] = mVCO.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleDSP06LFO.class.getName());
    }
}