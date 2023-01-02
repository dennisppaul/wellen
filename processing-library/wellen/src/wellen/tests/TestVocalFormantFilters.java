package wellen.tests;

import processing.core.PApplet;
import wellen.Wellen;
import wellen.dsp.ADSR;
import wellen.dsp.DSP;
import wellen.dsp.DSPNodeProcess;
import wellen.dsp.FilterBiquad;
import wellen.dsp.Oscillator;
import wellen.dsp.OscillatorFunction;
import wellen.dsp.VowelFormantFilter;

import java.util.ArrayList;

public class TestVocalFormantFilters extends PApplet {

    private static final boolean USE_FIXED_FILTER = true;
    private static final int mFirstNum = '1';
    private final ADSR mADSR = new ADSR();
    private final VowelFormantFilter mFormantFilter = new VowelFormantFilter();
    private final VowelFilterBankHiLow mFormantFilterDIY = new VowelFilterBankHiLow();
    // https://github.com/joric/bmxplay
    private boolean mIsKeyPressed = false;
    //    private final VowelFilterBank mFormantFilterDIY = new VowelFilterBank();
    private final Oscillator mOsc = new OscillatorFunction();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mOsc.set_frequency(55);
        mOsc.set_amplitude(0.33f);
        mOsc.set_waveform(Wellen.WAVEFORM_SQUARE);

        DSP.start(this);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
    }

    public void mouseMoved() {
//        if (key >= mFirstNum && key < mFirstNum + mFormantValues.length) {
//            int mID = key - mFirstNum;
//            mFilter.get(mID).set_resonance(map(mouseY, 0, height, 0.0f, 3.99f));
//        }
        if (keyPressed && keyCode == SHIFT) {
            mOsc.set_frequency(map(mouseX, 0, width, 1, 220));
        }
        final float mResonance = map(mouseY, 0, height, 0.000001f, 0.96f);
        mFormantFilterDIY.set_resonance(mResonance);
//        if (keyPressed && keyCode == ALT) {
//            for (FilterBiquad filterBiquad : mFilter) {
//                filterBiquad.set_resonance(map(mouseY, 0, height, 0.0f, 3.99f));
//            }
//        }
    }

    public void keyPressed() {
        if (!mIsKeyPressed) {
            mIsKeyPressed = true;

            switch (key) {
                case 'a':
                    mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_A);
                    mFormantFilterDIY.set_vowel(VowelFilterBank.VOWEL_A);
                    break;
                case 'e':
                    mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_E);
                    mFormantFilterDIY.set_vowel(VowelFilterBank.VOWEL_E);
                    break;
                case 'i':
                    mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_I);
                    mFormantFilterDIY.set_vowel(VowelFilterBank.VOWEL_I);
                    break;
                case 'o':
                    mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_O);
                    mFormantFilterDIY.set_vowel(VowelFilterBank.VOWEL_O);
                    break;
                case 'u':
                    mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_U);
                    mFormantFilterDIY.set_vowel(VowelFilterBank.VOWEL_U);
                    break;
                case '1':
                    mOsc.set_waveform(Wellen.WAVEFORM_SQUARE);
                    break;
                case '2':
                    mOsc.set_waveform(Wellen.WAVEFORM_SAWTOOTH);
                    break;
                case '3':
                    mOsc.set_waveform(Wellen.WAVEFORM_NOISE);
                    break;
            }

            mADSR.start();
        }
    }

    public void keyReleased() {
        if (mIsKeyPressed) {
            mIsKeyPressed = false;
            mADSR.stop();
        }
    }

    public void audioblock(float[] output_signal) {
        for (int i = 0; i < output_signal.length; i++) {
            output_signal[i] = mOsc.output();
            if (USE_FIXED_FILTER) {
                output_signal[i] = mFormantFilter.process(output_signal[i]);
                output_signal[i] *= 0.5f;
            } else {
                output_signal[i] = mFormantFilterDIY.process(output_signal[i]);
                output_signal[i] *= 0.75f;
            }
            output_signal[i] *= mADSR.output();
        }
    }

    private static class VowelFilterBank implements DSPNodeProcess {

        public static final int VOWEL_A = 0;
        public static final int VOWEL_E = 1;
        public static final int VOWEL_I = 2;
        public static final int VOWEL_O = 3;
        public static final int VOWEL_U = 4;
        private final float mAttenuation = 0.15f;
        private final ArrayList<FilterBiquad> mFilter = new ArrayList<>();
        private int mFormantFilterSetID = VOWEL_A;
        // see https://github.com/surge-synthesizer/surge/issues/1584
//        private final float[][] mFormantValues = {{740, 1180, 2640}, // tenor 'a'
//                                                  {600, 2060, 2840}, // tenor 'e'
//                                                  {280, 2620, 3380}, // tenor 'i'
//                                                  {380, 940, 2300}, // tenor 'o'
//                                                  {320, 920, 2200}  // tenor 'u'
        private final float[][] mFormantValues = {{800, 1150, 2900, 3900, 4950}, // tenor 'a'
                                                  {350, 2000, 2800, 3600, 4950}, // tenor 'e'
                                                  {270, 2140, 2950, 3900, 4950}, // tenor 'i'
                                                  {450, 800, 2830, 3800, 4950}, // tenor 'o'
                                                  {325, 700, 2700, 3800, 4950}  // tenor 'u'
//    private final float[][] mFormantValues = {{650, 1080, 2650, 2900, 3250}, //  'a'
//                                              {400, 1700, 2600, 3200, 3580}, //  'e'
//                                              {290, 1870, 2800, 3250, 3540}, //  'i'
//                                              {400, 800, 2600, 2800, 3000}, //  'o'
//                                              {350, 600, 2700, 2900, 3300}  //  'u'
        };
        private final int mPasses = 2;
        private float mResonance = 0.25f;
        public VowelFilterBank() {
            for (int i = 0; i < mFormantValues[VOWEL_E].length; i++) {
                FilterBiquad f = new FilterBiquad();
                f.set_mode(Wellen.FILTER_MODE_BANDPASS);
                mFilter.add(f);
            }
            set_vowel(VOWEL_E);
        }

        public void set_vowel(int pVowel) {
            mFormantFilterSetID = pVowel;
            updateFilterBank();
        }

        public void set_resonance(float pResonance) {
            mResonance = pResonance;
            updateFilterBank();
        }

        public float process(float pSignal) {
            float mSignal = 0;
            for (FilterBiquad f : mFilter) {
                float s = f.process(pSignal);
                for (int j = 0; j < mPasses; j++) {
                    s += f.process(s);
                }
                mSignal += s * mAttenuation;
            }
            return mSignal;
        }

        private void updateFilterBank() {
            for (int i = 0; i < mFilter.size(); i++) {
                mFilter.get(i).set_frequency(mFormantValues[mFormantFilterSetID][i]);
                mFilter.get(i).set_resonance(mResonance);
            }
        }
    }

    private static class VowelFilterBankHiLow implements DSPNodeProcess {

        public static final int VOWEL_A = 0;
        public static final int VOWEL_E = 1;
        public static final int VOWEL_I = 2;
        public static final int VOWEL_O = 3;
        public static final int VOWEL_U = 4;
        private final float mAttenuation = 0.4f;
        private final ArrayList<FilterBiquad> mFilterHi = new ArrayList<>();
        private final ArrayList<FilterBiquad> mFilterLow = new ArrayList<>();
        private int mFormantFilterSetID = VOWEL_A;
        // see https://github.com/surge-synthesizer/surge/issues/1584
        private final float[][] mFormantValues = {{740, 1180, 2640}, // tenor 'a'
                                                  {600, 2060, 2840}, // tenor 'e'
                                                  {280, 2620, 3380}, // tenor 'i'
                                                  {380, 940, 2300}, // tenor 'o'
                                                  {320, 920, 2200}  // tenor 'u'
//        private final float[][] mFormantValues = {{800, 1150, 2900, 3900, 4950}, // tenor 'a'
//                                                  {350, 2000, 2800, 3600, 4950}, // tenor 'e'
//                                                  {270, 2140, 2950, 3900, 4950}, // tenor 'i'
//                                                  {450, 800, 2830, 3800, 4950}, // tenor 'o'
//                                                  {325, 700, 2700, 3800, 4950}  // tenor 'u'
//    private final float[][] mFormantValues = {{650, 1080, 2650, 2900, 3250}, //  'a'
//                                              {400, 1700, 2600, 3200, 3580}, //  'e'
//                                              {290, 1870, 2800, 3250, 3540}, //  'i'
//                                              {400, 800, 2600, 2800, 3000}, //  'o'
//                                              {350, 600, 2700, 2900, 3300}  //  'u'
        };
        private final float mInitialResonance = 0.5f;
        private final int mPasses = 2;
        private float mResonance = 0.92f;
        public VowelFilterBankHiLow() {
            for (int i = 0; i < mFormantValues[VOWEL_E].length; i++) {
                FilterBiquad mHi = new FilterBiquad();
                mHi.set_mode(Wellen.FILTER_MODE_HIGHPASS);
                mHi.set_resonance(mInitialResonance);
                mFilterHi.add(mHi);

                FilterBiquad mLow = new FilterBiquad();
                mLow.set_mode(Wellen.FILTER_MODE_LOWPASS);
                mLow.set_resonance(mInitialResonance);
                mFilterLow.add(mLow);
            }
            set_vowel(VOWEL_E);
        }

        public void set_vowel(int pVowel) {
            mFormantFilterSetID = pVowel;
            updateFilterBank();
        }

        public void set_resonance(float pResonance) {
            mResonance = pResonance;
            updateFilterBank();
        }

        public float process(float pSignal) {
            float mSignal = 0;
            for (int i = 0; i < mFilterHi.size(); i++) {
                FilterBiquad mHi = mFilterHi.get(i);
                FilterBiquad mLow = mFilterLow.get(i);
                float s = mHi.process(mLow.process(pSignal));
                for (int j = 0; j < mPasses; j++) {
                    s += mHi.process(mLow.process(s));
                }
                mSignal += s * mAttenuation;
            }
            return mSignal;
        }

        private void updateFilterBank() {
            for (int i = 0; i < mFilterHi.size(); i++) {
                final float mSpread = mFormantValues[mFormantFilterSetID][i] * mResonance;
                mFilterHi.get(i).set_frequency(mFormantValues[mFormantFilterSetID][i] - mSpread);
                mFilterLow.get(i).set_frequency(mFormantValues[mFormantFilterSetID][i] + mSpread);
            }
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestVocalFormantFilters.class.getName());
    }
}
