package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.DSP;
import processing.core.PApplet;

public class SketchExampleDSP05Wavetable extends PApplet {

    private final Wavetable mWavetable = new Wavetable(16);

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        DSP.dumpAudioDevices();
        DSP.start(this);
        triangle(mWavetable.wavetable());
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mouseMoved() {
        mWavetable.set_frequency(map(mouseX, 0, width, 55, 220));
        mWavetable.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                sine(mWavetable.wavetable());
                break;
            case '2':
                sawtooth(mWavetable.wavetable());
                break;
            case '3':
                triangle(mWavetable.wavetable());
                break;
            case '4':
                square(mWavetable.wavetable());
                break;
            case '5':
                randomize(mWavetable.wavetable());
                break;
        }
    }

    public void audioblock(float[] pOutputSamples) {
        for (int i = 0; i < pOutputSamples.length; i++) {
            pOutputSamples[i] = mWavetable.process();
        }
    }

    private void randomize(float[] pWavetable) {
        for (int i = 0; i < pWavetable.length; i++) {
            pWavetable[i] = random(-1, 1);
        }
    }

    public static void sine(float[] pWavetable) {
        for (int i = 0; i < pWavetable.length; i++) {
            pWavetable[i] = PApplet.sin(2.0f * PI * ((float) i / (float) (pWavetable.length)));
        }
    }

    public static void sawtooth(float[] pWavetable) {
        for (int i = 0; i < pWavetable.length; i++) {
            pWavetable[i] = 2.0f * ((float) i / (float) (pWavetable.length - 1)) - 1.0f;
        }
    }

    public static void triangle(float[] pWavetable) {
        final int q = pWavetable.length / 4;
        final float qf = pWavetable.length * 0.25f;
        for (int i = 0; i < q; i++) {
            pWavetable[i] = i / qf;
            pWavetable[i + (q * 1)] = (qf - i) / qf;
            pWavetable[i + (q * 2)] = -i / qf;
            pWavetable[i + (q * 3)] = -(qf - i) / qf;
        }
    }

    public static void square(float[] pWavetable) {
        for (int i = 0; i < pWavetable.length / 2; i++) {
            pWavetable[i] = 1.0f;
            pWavetable[i + pWavetable.length / 2] = -1.0f;
        }
    }

    private static class Wavetable {

        private final float[] mWavetable;
        private float mFrequency;
        private float mStepSize;
        private float mArrayPtr;
        private float mAmplitude;

        public Wavetable(int pWavetableSize) {
            mWavetable = new float[pWavetableSize];
            mArrayPtr = 0;
            mAmplitude = 0.75f;
            set_frequency(220);
        }

        public void set_frequency(float pFrequency) {
            if (mFrequency != pFrequency) {
                mFrequency = pFrequency;
                mStepSize = mFrequency * ((float) mWavetable.length / (float) DSP.DEFAULT_SAMPLING_RATE);
            }
        }

        public void set_amplitude(float pAmplitude) {
            mAmplitude = pAmplitude;
        }

        public float[] wavetable() {
            return mWavetable;
        }

        public float process() {
            mArrayPtr += mStepSize;
            final int i = (int) mArrayPtr;
            final float mFrac = mArrayPtr - i;
            final int j = i % mWavetable.length;
            mArrayPtr = j + mFrac;
            return mWavetable[j] * mAmplitude;
        }
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleDSP05Wavetable.class.getName());
    }
}