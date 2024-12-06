package wellen.tests;

import processing.core.PApplet;
import wellen.Beat;
import wellen.InstrumentDSP;
import wellen.Note;
import wellen.Scale;
import wellen.Tone;
import wellen.Wellen;
import wellen.dsp.ADSR;
import wellen.dsp.Signal;
import wellen.dsp.Wavetable;

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
        Tone.get_DSP_engine().enable_reverb(true);
        Tone.get_DSP_engine().get_reverb().set_roomsize(0.9f);
    }

    public void draw() {
        background(255);
        stroke(0);
        Wellen.draw_buffer(g, width, height, Tone.get_buffer());
    }

    public void beat(int beatCount) {
        Tone.instrument(beatCount % Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS);
        final int[] mScale = Scale.MAJOR;
        final int mOffset = mScale.length - ((beatCount + mScale.length) % (mScale.length + 1));
        int mNote = Scale.get_note(mScale, Note.NOTE_C3, mOffset);
        Tone.note_on(mNote, mOffset == 0 ? 80 : 60);
    }

    private static class InstrumentBell extends InstrumentDSP {

        private static final int NUM_OSC = 7;
        private final ADSR[] mADSRs;
        private float mAmplify = 2.5f;
        private float mAmplitudeFalloff;
        private float mBaseRelease;
        private float mDetune;
        private final float[] mOscillatorAmplitudes;
        private final float[] mOscillatorDetune;
        private float mReleaseFalloff;
        private final boolean mUseTriangleForLowFrequencies = true;
        private final Wavetable[] mVCOs;

        public InstrumentBell(int pID) {
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
            set_sub_ratio(0.23f);
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

        public void set_sustain(float sustain) {
            mBaseRelease = sustain;
            updateADSRs();
        }

        public void note_on(int note, int velocity) {
            super.note_on(note, velocity);
            for (ADSR mADSR : mADSRs) {
                mADSR.start();
            }
        }

        public void note_off() {
            super.note_off();
            /* never stop oscillators as they are stopped by their ADSR */
        }

        public Signal output_signal() {
            float mSample = 0.0f;
            for (int i = 0; i < mVCOs.length; i++) {
                mVCOs[i].set_frequency(get_frequency() * mOscillatorDetune[i]);
                mVCOs[i].set_amplitude(get_amplitude() * mOscillatorAmplitudes[i]);
                mSample += mVCOs[i].output() * mADSRs[i].output();
            }
            mSample /= mVCOs.length;
            mSample *= mAmplify;
            return new Signal(mSample, mSample);
        }

        public void set_sub_ratio(float frequency_ratio) {
            mDetune = frequency_ratio;
            for (int i = 0; i < NUM_OSC; i++) {
                mOscillatorDetune[i] = i + 2 + i * pow(mDetune, 2);
            }
        }

        public float get_attack() {
            return fAttack;
        }

        public void set_attack(float attack) {
            fAttack = attack;
            updateADSRs();
        }

        private void updateADSRs() {
            for (int i = 0; i < mVCOs.length; i++) {
                mADSRs[i].set_attack(fAttack);
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