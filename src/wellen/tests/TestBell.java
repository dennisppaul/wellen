package wellen.tests;

import processing.core.PApplet;
import wellen.ADSR;
import wellen.Beat;
import wellen.InstrumentInternal;
import wellen.Scale;
import wellen.Tone;
import wellen.Wavetable;
import wellen.Wellen;

public class TestBell extends PApplet {

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        for (int i = 0; i < Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS; i++) {
            final InstrumentBell mBell = new InstrumentBell(i);
            Tone.replace_instrument(mBell);
        }
        Beat.start(this, 120 * 2);
        Tone.get_internal_engine().enable_reverb(true);
    }

    public void draw() {
        background(255);
        stroke(0);
        Wellen.draw_buffer(g, width, height, Tone.get_buffer());
    }

    public void beat(int pBeatCount) {
        Tone.instrument(pBeatCount % Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS);
        final int[] mScale = Scale.MAJOR;
        final int mLength = mScale.length + 1;
        final int mOffset = mLength - ((pBeatCount+7) % mLength);
        int mNote = Scale.get_note(mScale, 36, mOffset);
        Tone.note_on(mNote, mOffset == 0 ? 100 : 25);
    }

    private static class InstrumentBell extends InstrumentInternal {

        private static final int NUM_OSC = 7;
        private final Wavetable[] mVCOs;
        private final ADSR[] mADSRs;
        private float mDetune = 0.27f;
        private float mBaseRelease = 1.8f;
        private float mReleaseFalloff = 0.4f;
        private float mAmplitudeFalloff = 0.1f;
        private float mAmplify = 6.0f;
        private final boolean mUseTriangleForLowFrequencies = true;

        public InstrumentBell(int pID) {
            super(pID);
            mVCOs = new Wavetable[NUM_OSC];
            mADSRs = new ADSR[NUM_OSC];
            for (int i = 0; i < NUM_OSC; i++) {
                mVCOs[i] = new Wavetable();
                mVCOs[i].interpolate_samples(true);
                Wavetable.fill(mVCOs[i].get_wavetable(), Wellen.WAVESHAPE_SINE);
                mADSRs[i] = new ADSR();
            }
            if (mUseTriangleForLowFrequencies && mVCOs.length > 1) {
                Wavetable.fill(mVCOs[0].get_wavetable(), Wellen.WAVESHAPE_TRIANGLE);
                Wavetable.fill(mVCOs[1].get_wavetable(), Wellen.WAVESHAPE_TRIANGLE);
            }
            updateADSR();
        }

        public void set_amplification(float pAmplify) {
            mAmplify = pAmplify;
        }

        public void set_detune(float pDetune) {
            mDetune = pDetune;
        }

        public void set_sustain_falloff(float pAmplitudeFalloff) {
            mAmplitudeFalloff = pAmplitudeFalloff;
        }

        public void set_release_falloff(float pReleaseFalloff) {
            mReleaseFalloff = pReleaseFalloff;
            updateADSR();
        }

        public void set_sustain(float pBaseRelease) {
            mBaseRelease = pBaseRelease;
            updateADSR();
        }

        public void note_on(int pNote, int pVelocity) {
            super.note_on(pNote, pVelocity);
            for (ADSR mADSR : mADSRs) {
                mADSR.start();
            }
        }

        public void note_off() {
            super.note_off();
            /* never stop oscillators as they are stopped by their ADSR */
        }

        public float output() {
            float mSample = 0.0f;
            for (int i = 0; i < mVCOs.length; i++) {
                float mLocalDetune = mDetune * i;
                mVCOs[i].set_frequency(get_frequency() * map(i, 0, mVCOs.length - 1, 1, mVCOs.length) * mLocalDetune);
                mVCOs[i].set_amplitude(get_amplitude() * map(i, 0, mVCOs.length - 1, 1.0f, mAmplitudeFalloff));
                mSample += mVCOs[i].output() * mADSRs[i].output();
            }
            mSample /= mVCOs.length;
            mSample *= mAmplify;
            return mSample;
        }

        private void updateADSR() {
            for (int i = 0; i < mVCOs.length; i++) {
                mADSRs[i].set_attack(0.005f);
                mADSRs[i].set_decay(mBaseRelease + map(i, 0, mVCOs.length - 1, 0.0f, mReleaseFalloff));
                mADSRs[i].set_sustain(0.0f);
                mADSRs[i].set_release(0.0f);
            }
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestBell.class.getName());
    }
}