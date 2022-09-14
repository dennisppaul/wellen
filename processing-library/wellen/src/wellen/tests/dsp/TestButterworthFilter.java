package wellen.tests.dsp;

import processing.core.PApplet;
import wellen.DSP;
import wellen.OscillatorFunction;
import wellen.Wellen;

public class TestButterworthFilter extends PApplet {

    private OscillatorFunction mOSCLeft;
    private OscillatorFunction mOSCRight;
    private ButterworthFilters mButterworthFilters;
    private Filters mFilters;
    private int mButterworthType = ButterworthFilters.LOW_PASS;
    private int mFilterType = Filters.LOW_PASS;
    private float mFilterFrequency = 1000.0f;
    private float mFilterBandwidth = 10.0f;

    public void settings() {
        size(1024, 768);
    }

    public void setup() {
        mOSCLeft = new OscillatorFunction();
        mOSCLeft.set_frequency(2f * 48000f / 1024f);
        mOSCLeft.set_amplitude(0.5f);
        mOSCLeft.set_waveform(Wellen.WAVESHAPE_SQUARE);

        mOSCRight = new OscillatorFunction();
        mOSCRight.set_frequency(2f * 48000f / 1024f);
        mOSCRight.set_amplitude(0.5f);
        mOSCRight.set_waveform(Wellen.WAVESHAPE_SQUARE);

        mButterworthFilters = new ButterworthFilters(Wellen.DEFAULT_SAMPLING_RATE);
        mFilters = new Filters(Wellen.DEFAULT_SAMPLING_RATE);

        Wellen.dumpAudioInputAndOutputDevices();
        DSP.start(this, 2);
    }

    public void draw() {
        background(255);
        stroke(0);
        DSP.draw_buffer_stereo(g, width, height);
    }

    public void mouseMoved() {
        mFilterFrequency = map(mouseX, 0, width, 1, 10000);
        mFilterBandwidth = map(mouseY, 0, height, 0.001f, 100);
        System.out.print(" frequency : " + mFilterFrequency);
        System.out.print(" bandwidth : " + mFilterBandwidth);
        System.out.print(" filtertype: " + mButterworthType);
        System.out.print(" filtertype: " + mFilterType);
        System.out.println();
    }

    public void keyPressed() {
        switch (key) {
            case '0':
                mButterworthType = -1;
                mFilterType = -1;
                break;
            case '1':
                mButterworthType = ButterworthFilters.LOW_PASS;
                mFilterType = Filters.LOW_PASS;
                break;
            case '2':
                mButterworthType = ButterworthFilters.HIGH_PASS;
                mFilterType =  Filters.HIGH_PASS;
                break;
            case '3':
                mButterworthType = ButterworthFilters.BAND_PASS;
                mFilterType = Filters.BAND_PASS;
                break;
            case '4':
                mButterworthType = ButterworthFilters.BAND_REJECT;
                mFilterType =  Filters.RESONATOR;
                break;
        }
    }

    public void audioblock(float[] pOutputSignalLeft, float[] pOutputSignalRight) {
        for (int i = 0; i < pOutputSignalLeft.length; i++) {
            pOutputSignalLeft[i] = mOSCLeft.output();
            pOutputSignalRight[i] = mOSCRight.output();
        }
        mButterworthFilters.process(pOutputSignalLeft,
                mFilterFrequency,
                mFilterBandwidth,
                mButterworthType);
        mFilters.process(pOutputSignalRight,
                mFilterFrequency,
                mFilterBandwidth,
                mFilterType);
    }

    public static void main(String[] args) {
        PApplet.main(TestButterworthFilter.class.getName());
    }
}
