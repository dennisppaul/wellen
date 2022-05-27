package wellen.tests;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Sampler;
import wellen.Wellen;

public class TestFindZeroCrossingInSampleData extends PApplet {

    private Sampler mSampler;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mSampler = new Sampler(Wellen.DEFAULT_SAMPLING_RATE);
        mSampler.loop(true);

        for (int i = 0; i < mSampler.data().length; i++) {
            final float mFrequency = 110.0f;
            final float mAmplitude = 0.33f;
            float r = PApplet.TWO_PI * (float) i / (float) Wellen.DEFAULT_SAMPLING_RATE;
            float s = sin(r * mFrequency) * mAmplitude * 0.2f +
                      sin(r * 0.55f * mFrequency) * mAmplitude * 0.2f +
                      sin(r * 0.44f * mFrequency) * mAmplitude * 0.2f +
                      sin(r * 0.33f * mFrequency) * mAmplitude * 0.2f +
                      sin(r * 0.22f * mFrequency) * mAmplitude * 0.2f +
                      sin(r * 0.11f * mFrequency) * mAmplitude * 0.2f +
                      sin(r * 10.0f) * mAmplitude * 0.2f +
                      sin(r * 43.0f) * mAmplitude * 0.4f
            ;
            mSampler.data()[i] = s;
        }

        DSP.start(this);
    }

    public void draw() {
        background(255);

        noFill();
        stroke(0);
        line(32, height * 0.5f, width - 32, height * 0.5f);
        beginShape();
        for (int i = mSampler.get_in(); i < mSampler.get_out(); i++) {
            float x = map(i, mSampler.get_in(), mSampler.get_out(), 32, width - 32);
            float y = map(mSampler.data()[i], -1.0f, 1.0f, 32, height - 32);
            vertex(x, y);
        }
        endShape();

        fill(0);
        noStroke();
        circle(map(mSampler.get_in(), 0, mSampler.data().length, 32, width - 32), height / 2.0f, 20);
        circle(map(mSampler.get_out(), 0, mSampler.data().length, 32, width - 32), height / 2.0f, 20);
    }

    public static void make_loopable(Sampler mSampler) {
        int[] mAdaptedInOutPoints = find_zero_crossings(mSampler.data(), mSampler.get_in(), mSampler.get_out());
        mSampler.set_in(mAdaptedInOutPoints[0]);
        mSampler.set_out(mAdaptedInOutPoints[1]);
    }

    private static int[] find_zero_crossings(float[] pData, int pInPoint, int pOutPoint) {
        int mAdaptedInPoint = pInPoint;
        int mAdaptedOutPoint = pOutPoint;
        int mInPointEdgeKind = 0;
        {
            float mInValue = pData[pInPoint];
            if (mInValue != 0.0f) {
                for (int i = pInPoint + 1; i < pData.length; i++) {
                    float v = pData[i];
                    boolean mRisingEdge = (mInValue < 0 && v >= 0);
                    boolean mFallingEdge = (mInValue > 0 && v <= 0);
                    if (mRisingEdge || mFallingEdge) {
                        mAdaptedInPoint = i;
                        mInPointEdgeKind = mRisingEdge ? 1 : -1;
                        break;
                    }
                }
            }
        }
        {
            float mOutValue = pData[pOutPoint];
            if (mOutValue != 0.0f && pOutPoint > 0) {
                for (int i = pOutPoint - 1; i > 0; i--) {
                    float v = pData[i];
                    boolean mRisingEdge = (mOutValue < 0 && v >= 0);
                    boolean mFallingEdge = (mOutValue > 0 && v <= 0);
                    if (mInPointEdgeKind == 0 && (mRisingEdge || mFallingEdge)) {
                        mAdaptedOutPoint = i;
                        break;
                    } else if ((mRisingEdge && mInPointEdgeKind == -1) || (mFallingEdge && mInPointEdgeKind == 1)) {
                        mAdaptedOutPoint = i;
                        break;
                    }
                }
            }
        }
        return new int[]{mAdaptedInPoint, mAdaptedOutPoint};
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                mSampler.set_in((int) map(mouseX, 0, width, 0, mSampler.data().length));
                break;
            case '2':
                mSampler.set_out((int) map(mouseX, 0, width, 0, mSampler.data().length));
                break;
            case '3':
                make_loopable(mSampler);
                break;
        }
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mSampler.output();
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestFindZeroCrossingInSampleData.class.getName());
    }
}
