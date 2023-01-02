/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2022 Dennis P Paul.
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
import wellen.dsp.ADSR;
import wellen.dsp.Sampler;
import wellen.dsp.Signal;
import wellen.dsp.Wavetable;

/**
 * a collection of DSP instruments.
 * <p>
 * these instruments can be used to replace the default instrument ( i.e {@link InstrumentDSP} ) e.g by using the
 * method
 * <code>Tone.replace_instrument(InstrumentDSP)</code>.
 * <p>
 * all instruments are extended from {@link InstrumentDSP}, however some the original functionality is extended, changed
 * or even removed.
 * <p>
 * note that these instruments only work with the internal tone engine. however, due to the modular nature of the
 * library these instruments can also be integrated into {@link wellen.dsp.DSP} applications.
 */
public class InstrumentDSPLibrary {

    public static class BELL extends InstrumentDSP {

        private static final int NUM_OSC = 7;
        private final ADSR[] mADSRs;
        private float mAmplify = 1.0f;
        private float mAmplitudeFalloff;
        private float mBaseRelease;
        private float mDetune;
        private final float[] mOscillatorAmplitudes;
        private final float[] mOscillatorDetune;
        private float mReleaseFalloff;
        private final boolean mUseTriangleForLowFrequencies = true;
        private final Wavetable[] mVCOs;

        public BELL(int pID) {
            super(pID);
            mVCOs = new Wavetable[NUM_OSC];
            mADSRs = new ADSR[NUM_OSC];
            mOscillatorDetune = new float[NUM_OSC];
            mOscillatorAmplitudes = new float[NUM_OSC];
            for (int i = 0; i < NUM_OSC; i++) {
                mVCOs[i] = new Wavetable();
                mVCOs[i].set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
                Wavetable.fill(mVCOs[i].get_wavetable(), Wellen.WAVEFORM_SINE);
                mADSRs[i] = new ADSR();
            }
            set_detune(0.23f);
            set_amplitude_falloff(0.5f);
            set_sustain(2.1f);
            set_sustain_falloff(-0.4f);
            set_attack(0.005f);
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

        @Override
        public Signal output_signal() {
            float mSample = 0.0f;
            for (int i = 0; i < mVCOs.length; i++) {
                mVCOs[i].set_frequency(get_frequency() * mOscillatorDetune[i]);
                mVCOs[i].set_amplitude(get_amplitude() * mOscillatorAmplitudes[i]);
                mSample += mVCOs[i].output() * mADSRs[i].output();
            }
            mSample /= mVCOs.length;
            mSample *= mAmplify;
            return Signal.create(mSample);
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

    public static class FAT_LEAD extends InstrumentDSP {

        private final Wavetable mLowerVCO;
        private final Wavetable mVeryLowVCO;

        public FAT_LEAD(int pID) {
            super(pID);

            mLowerVCO = new Wavetable(DEFAULT_WAVETABLE_SIZE);
            mLowerVCO.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
            mVeryLowVCO = new Wavetable(DEFAULT_WAVETABLE_SIZE);
            mVeryLowVCO.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
            Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVEFORM_TRIANGLE);
            Wavetable.fill(mLowerVCO.get_wavetable(), Wellen.WAVEFORM_SINE);
            Wavetable.fill(mVeryLowVCO.get_wavetable(), Wellen.WAVEFORM_SQUARE);
        }

        @Override
        public Signal output_signal() {
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

            return Signal.create(mADSRAmp * mSample);
        }
    }

    public static class HI_HAT extends InstrumentDSP {

        public HI_HAT(int pID) {
            super(pID);
            mADSR.set_attack(0.005f);
            mADSR.set_decay(0.05f);
            mADSR.set_sustain(0.0f);
            mADSR.set_release(0.0f);
        }

        @Override
        public Signal output_signal() {
            return Signal.create(Wellen.random(-get_amplitude(), get_amplitude()) * mADSR.output());
        }
    }

    public static class KICK_DRUM extends InstrumentDSP {

        private final float mDecaySpeed = 0.25f;
        private final ADSR mFrequencyEnvelope;
        private final float mFrequencyRange = 80;

        public KICK_DRUM(int pID) {
            super(pID);

            set_oscillator_type(Wellen.WAVEFORM_SINE);
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

        @Override
        public Signal output_signal() {
            final float mFrequencyOffset = mFrequencyEnvelope.output() * mFrequencyRange;
            mVCO.set_frequency(get_frequency() + mFrequencyOffset);
            mVCO.set_amplitude(get_amplitude());

            float mSample = mVCO.output();
            final float mADSRAmp = mADSR.output();
            return Signal.create(mSample * mADSRAmp);
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

    public static class SAMPLER extends InstrumentDSP {

        private final Sampler mSampler;

        public SAMPLER(int pID, float[] pSampleData) {
            super(pID);

            mSampler = new Sampler();
            mSampler.set_data(pSampleData);
            mSampler.loop(false);
        }

        @Override
        public Signal output_signal() {
            return Signal.create(mSampler.output() * get_amplitude());
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
