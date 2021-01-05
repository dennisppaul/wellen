/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2020 Dennis P Paul.
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package wellen;

import processing.core.PApplet;

/**
 * a collection of DSP instruments.
 * <p>
 * these instruments can be used to replace the default instrument ( i.e {@link wellen.InstrumentInternal} ) e.g by
 * using the method <code>Tone.replace_instrument(InstrumentInternal)</code>.
 * <p>
 * all instruments are extended from {@link wellen.InstrumentInternal}, however some the original functionality is
 * extended, changed or even removed.
 * <p>
 * note that these instruments only work with the internal tone engine. however, due to the modular nature of the
 * library these instruments can also be integrated into {@link wellen.DSP} applications.
 */
public class InstrumentInternalLibrary {
    public static class BELL extends InstrumentInternal {

        private static final int NUM_OSC = 7;
        private final Wavetable[] mVCOs;
        private final ADSR[] mADSRs;
        private final float[] mOscillatorDetune;
        private final float[] mOscillatorAmplitudes;
        private float mDetune;
        private float mBaseRelease;
        private float mReleaseFalloff;
        private float mAmplitudeFalloff;
        private float mAmplify = 2.5f;
        private final boolean mUseTriangleForLowFrequencies = true;

        public BELL(int pID) {
            super(pID);
            mVCOs = new Wavetable[NUM_OSC];
            mADSRs = new ADSR[NUM_OSC];
            mOscillatorDetune = new float[NUM_OSC];
            mOscillatorAmplitudes = new float[NUM_OSC];
            for (int i = 0; i < NUM_OSC; i++) {
                mVCOs[i] = new Wavetable();
                mVCOs[i].interpolate_samples(true);
                Wavetable.fill(mVCOs[i].get_wavetable(), Wellen.WAVESHAPE_SINE);
                mADSRs[i] = new ADSR();
            }
            set_detune(0.23f);
            set_amplitude_falloff(0.5f);
            set_sustain(2.1f);
            set_sustain_falloff(-0.4f);
            set_attack(0.02f);
            updateADSRs();
        }

        public void set_amplitude_falloff(float pAmplitudeFalloff) {
            mAmplitudeFalloff = pAmplitudeFalloff;
            for (int i = 0; i < NUM_OSC; i++) {
                mOscillatorAmplitudes[i] = PApplet.map(i, 0, mVCOs.length - 1, 1.0f, 1.0f - mAmplitudeFalloff);
            }
        }

        public void set_amplification(float pAmplify) {
            mAmplify = pAmplify;
        }

        public void set_sustain_falloff(float pReleaseFalloff) {
            mReleaseFalloff = pReleaseFalloff;
            updateADSRs();
        }

        public void set_sustain(float pBaseRelease) {
            mBaseRelease = pBaseRelease;
            updateADSRs();
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
                mVCOs[i].set_frequency(get_frequency() * mOscillatorDetune[i]);
                mVCOs[i].set_amplitude(get_amplitude() * mOscillatorAmplitudes[i]);
                mSample += mVCOs[i].output() * mADSRs[i].output();
            }
            mSample /= mVCOs.length;
            mSample *= mAmplify;
            return mSample;
        }

        public void set_detune(float pDetune) {
            mDetune = pDetune;
            for (int i = 0; i < NUM_OSC; i++) {
                mOscillatorDetune[i] = i + 2 + i * PApplet.pow(mDetune, 2);
            }
        }

        public float get_attack() {
            return mAttack;
        }

        public void set_attack(float pAttack) {
            mAttack = pAttack;
            updateADSRs();
        }

        private void updateADSRs() {
            for (int i = 0; i < mVCOs.length; i++) {
                mADSRs[i].set_attack(mAttack);
                mADSRs[i].set_decay(mBaseRelease + PApplet.map(i, 0, mVCOs.length - 1, 0.0f, mReleaseFalloff));
                mADSRs[i].set_sustain(0.0f);
                mADSRs[i].set_release(0.0f);
            }
        }
    }

    public static class FAT_LEAD extends InstrumentInternal {

        private final Wavetable mLowerVCO;
        private final Wavetable mVeryLowVCO;

        public FAT_LEAD(int pID) {
            super(pID);

            mLowerVCO = new Wavetable(DEFAULT_WAVETABLE_SIZE);
            mLowerVCO.interpolate_samples(true);
            mVeryLowVCO = new Wavetable(DEFAULT_WAVETABLE_SIZE);
            mVeryLowVCO.interpolate_samples(true);
            Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVESHAPE_TRIANGLE);
            Wavetable.fill(mLowerVCO.get_wavetable(), Wellen.WAVESHAPE_SINE);
            Wavetable.fill(mVeryLowVCO.get_wavetable(), Wellen.WAVESHAPE_SQUARE);
        }

        public float output() {
            mVCO.set_frequency(get_frequency());
            mVCO.set_amplitude(get_amplitude() * 0.2f);
            mLowerVCO.set_frequency(get_frequency() * 0.5f);
            mLowerVCO.set_amplitude(get_amplitude());
            mVeryLowVCO.set_frequency(get_frequency() * 0.25f);
            mVeryLowVCO.set_amplitude(get_amplitude() * 0.075f);

            final float mADSRAmp = mADSR.output();
            float mSample = mVCO.output();
            mSample += mLowerVCO.output();
            mSample += mVeryLowVCO.output();
            return mADSRAmp * mSample;
        }
    }

    public static class HI_HAT extends InstrumentInternal {

        public HI_HAT(int pID) {
            super(pID);
            mADSR.set_attack(0.005f);
            mADSR.set_decay(0.05f);
            mADSR.set_sustain(0.0f);
            mADSR.set_release(0.0f);
        }

        public float output() {
            return Wellen.random(-get_amplitude(), get_amplitude()) * mADSR.output();
        }
    }

    public static class KICK_DRUM extends InstrumentInternal {

        private final ADSR mFrequencyEnvelope;
        private final float mFrequencyRange = 80;
        private final float mDecaySpeed = 0.25f;

        public KICK_DRUM(int pID) {
            super(pID);

            set_oscillator_type(Wellen.WAVESHAPE_SINE);
            set_amplitude(0.5f);
            set_frequency(90);

            mFrequencyEnvelope = new ADSR();

            mADSR.set_attack(0.001f);
            mADSR.set_decay(mDecaySpeed);
            mADSR.set_sustain(0.0f);
            mADSR.set_release(0.0f);

            mFrequencyEnvelope.set_attack(0.001f);
            mFrequencyEnvelope.set_decay(mDecaySpeed);
            mFrequencyEnvelope.set_sustain(0.0f);
            mFrequencyEnvelope.set_release(0.0f);
        }

        public float output() {
            final float mFrequencyOffset = mFrequencyEnvelope.output() * mFrequencyRange;
            mVCO.set_frequency(get_frequency() + mFrequencyOffset);
            mVCO.set_amplitude(get_amplitude());

            float mSample = mVCO.output();
            final float mADSRAmp = mADSR.output();
            return mSample * mADSRAmp;
        }

        public void note_off() {
            mIsPlaying = false;
        }

        public void note_on(int pNote, int pVelocity) {
            mIsPlaying = true;
            mADSR.start();
            mFrequencyEnvelope.start();
        }

    }

    public static class SAMPLER extends InstrumentInternal {

        private final Sampler mSampler;

        public SAMPLER(int pID, float[] pSampleData) {
            super(pID);

            mSampler = new Sampler();
            mSampler.set_data(pSampleData);
            mSampler.loop(false);
        }

        public float output() {
            return mSampler.output() * get_amplitude();
        }


        public void note_off() {
            mIsPlaying = false;
        }

        public void note_on(int pNote, int pVelocity) {
            mIsPlaying = true;
            set_amplitude(velocity_to_amplitude(pVelocity));
            mSampler.rewind();
        }
    }
}
