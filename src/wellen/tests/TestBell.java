package wellen.tests;

import processing.core.PApplet;
import wellen.ADSR;
import wellen.Beat;
import wellen.InstrumentInternal;
import wellen.Note;
import wellen.Scale;
import wellen.Tone;
import wellen.Wavetable;
import wellen.Wellen;

public class TestBell extends PApplet {

    private InstrumentBell[] mInstrumentBells;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mInstrumentBells = new InstrumentBell[Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS];
        for (int i = 0; i < Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS; i++) {
            mInstrumentBells[i] = new InstrumentBell(i);
            Tone.replace_instrument(mInstrumentBells[i]);
        }
        Beat.start(this, 240);
        Tone.get_internal_engine().enable_reverb(true);
        Tone.get_internal_engine().get_reverb().set_roomsize(0.9f);
    }

    public void draw() {
        background(255);
        stroke(0);
        Wellen.draw_buffer(g, width, height, Tone.get_buffer());
    }

    public void beat(int pBeatCount) {
        Tone.instrument(pBeatCount % Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS);
        final int[] mScale = Scale.MAJOR;
        final int mOffset = mScale.length - ((pBeatCount + mScale.length) % (mScale.length + 1));
        int mNote = Scale.get_note(mScale, Note.NOTE_C3, mOffset);
        Tone.note_on(mNote, mOffset == 0 ? 80 : 60);
    }

    private static class InstrumentBell extends InstrumentInternal {

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

        public InstrumentBell(int pID) {
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
                mOscillatorAmplitudes[i] = map(i, 0, mVCOs.length - 1, 1.0f, 1.0f - mAmplitudeFalloff);
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
                mOscillatorDetune[i] = i + 2 + i * pow(mDetune, 2);
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