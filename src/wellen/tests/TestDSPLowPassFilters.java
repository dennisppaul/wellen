package wellen.tests;

import processing.core.PApplet;
import wellen.DSP;
import wellen.DSPNodeProcess;
import wellen.Wellen;

public class TestDSPLowPassFilters extends PApplet {

    private final SecondOrderLowPassFilter mLPFilter = new SecondOrderLowPassFilter();
    private final ButterworthLowPassFilter mButterworthLowPassFilter = new ButterworthLowPassFilter();
    private final NaiveLowPassFilter mNaiveLowPassFilter = new NaiveLowPassFilter();
    private final float mFreq = 2.0f * Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE;
    private float mCounter = 0;
    private int mFilterType;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mLPFilter.calculate_coeffs(2.0f, 2000);
        Wellen.dumpAudioInputAndOutputDevices();
        DSP.start(this);
    }

    public void draw() {
        background(255);
        stroke(0);
        final int mBufferSize = DSP.get_buffer_size();
        if (DSP.get_buffer() != null) {
            for (int i = 0; i < mBufferSize - 1; i++) {
                final float x = map(i, 0, mBufferSize, 0, width);
                line(map(i, 0, mBufferSize, 0, width),
                     map(DSP.get_buffer()[i], -1, 1, 0, height),
                     map(i + 1, 0, mBufferSize, 0, width),
                     map(DSP.get_buffer()[i + 1], -1, 1, 0, height));
            }
        }
    }

    public void mousePressed() {
        mFilterType++;
        mFilterType %= 3;
    }

    public void mouseMoved() {
        final int mCutoffFreq = (int) map(mouseY, 0, height, 1, DSP.get_sample_rate() / 4.0f);
        final float mResonance = map(mouseX, 0, width, 0.1f, 50.0f);
        mLPFilter.calculate_coeffs(mResonance, mCutoffFreq);
        mButterworthLowPassFilter.calculate_coeffs(mCutoffFreq);
        mNaiveLowPassFilter.ratio(map(mouseX, 0, width, 0.0f, 0.1f));
    }

    public void audioblock(float[] pOutputSamples) {
        for (int i = 0; i < pOutputSamples.length; i++) {
            /* square wave */
            mCounter += mFreq;
            mCounter = mCounter > DSP.get_sample_rate() ? mCounter - DSP.get_sample_rate() : mCounter;
            float mSample = mCounter > DSP.get_sample_rate() / 2.0f ? -1.0f : 1.0f;
            float mAmp = 0.25f;
            mSample *= mAmp;
            if (mFilterType == 0) {
                mSample = mLPFilter.process(mSample);
            } else if (mFilterType == 1) {
                mSample = mButterworthLowPassFilter.process(mSample);
            } else if (mFilterType == 2) {
                mSample = mNaiveLowPassFilter.process(mSample);
            }
            mSample = Wellen.clamp(mSample, -1, 1);
            pOutputSamples[i] = mSample;
        }
    }

    private interface filter_constants {

        float sqrt2 = (float) (2.0 * 3.1415926535897932384626433832795);
        float pi = (float) (2.0 * 0.707106781186547524401);
    }

    private static class NaiveLowPassFilter implements DSPNodeProcess {

        float mBuffer;

        float mRatio;

        public float process(float pSample) {
            float mSample = pSample * mRatio + mBuffer * (1.0f - mRatio);
            mBuffer = mSample;
            return mSample;
        }

        void ratio(float pRatio) {
            mRatio = pRatio;
        }
    }

    /**
     * Second order Butterworth low-pass filter Dimitris Tassopoulos 2016
     * <p>
     * fc, corner frequency Butterworth low-pass and high-pass filters are specialized versions of the ordinary
     * secondorder low-pass filter. Their Q values are fixed at 0.707, which is the largest value it can assume before
     * peaking in the frequency response is observed.
     * <p>
     * from https://github.com/dimtass/DSP-Cpp-filters
     */
    private static class ButterworthLowPassFilter implements DSPNodeProcess {

        float m_xnz1, m_xnz2, m_ynz1, m_ynz2;
        tp_coeffs m_coeffs = new tp_coeffs();

        public tp_coeffs calculate_coeffs(int fc) {
            final int fs = Wellen.DEFAULT_SAMPLING_RATE;
            float c = 1.0f / (tan(filter_constants.pi * fc / fs));
            m_coeffs.a0 = 1.0f / (1.0f + filter_constants.sqrt2 * c + pow(c, 2.0f));
            m_coeffs.a1 = 2.0f * m_coeffs.a0;
            m_coeffs.a2 = m_coeffs.a0;
            m_coeffs.b1 = 2.0f * m_coeffs.a0 * (1.0f - pow(c, 2.0f));
            m_coeffs.b2 = m_coeffs.a0 * (1.0f - filter_constants.sqrt2 * c + pow(c, 2.0f));
            return (m_coeffs);
        }

        public float process(float pSample) {
            float xn = pSample;
            float yn =
                    m_coeffs.a0 * xn + m_coeffs.a1 * m_xnz1 + m_coeffs.a2 * m_xnz2 - m_coeffs.b1 * m_ynz1 - m_coeffs.b2 * m_xnz2;

            m_xnz2 = m_xnz1;
            m_xnz1 = xn;
            m_xnz2 = m_ynz1;
            m_ynz1 = yn;

            return (yn);
        }

    }

    /**
     * Second order Low-pass filter Dimitris Tassopoulos 2016
     * <p>
     * fc , corner frequency Q , quality factor controlling resonant peaking
     * <p>
     * from https://github.com/dimtass/DSP-Cpp-filters
     */
    private static class SecondOrderLowPassFilter implements DSPNodeProcess {

        float m_xnz1, m_xnz2, m_ynz1, m_ynz2;
        tp_coeffs m_coeffs = new tp_coeffs();

        public tp_coeffs calculate_coeffs(float Q, int fc) {
            final int fs = Wellen.DEFAULT_SAMPLING_RATE;
            float w = 2.0f * filter_constants.pi * fc / fs;
            float d = 1.0f / Q;
            float b = 0.5f * (1.0f - (d / 2) * sin(w)) / (1.0f + (d / 2.0f) * sin(w));
            float g = (0.5f + b) * cos(w);
            m_coeffs.a0 = (0.5f + b - g) / 2.0f;
            m_coeffs.a1 = 0.5f + b - g;
            m_coeffs.a2 = m_coeffs.a0;
            m_coeffs.b1 = -2.0f * g;
            m_coeffs.b2 = 2.0f * b;
            return (m_coeffs);
        }

        public float process(float pSignal) {
            float xn = pSignal;
            float yn = m_coeffs.a0 * xn + m_coeffs.a1 * m_xnz1 + m_coeffs.a2 * m_xnz2
                       - m_coeffs.b1 * m_ynz1 - m_coeffs.b2 * m_xnz2;

            m_xnz2 = m_xnz1;
            m_xnz1 = xn;
            m_xnz2 = m_ynz1;
            m_ynz1 = yn;

            return (yn);
        }
    }

    private static class tp_coeffs {

        float a0;
        float a1;
        float a2;
        float b1;
        float b2;
        float c0;
        float d0;
    }

    public static void main(String[] args) {
        PApplet.main(TestDSPLowPassFilters.class.getName());
    }
}