package wellen.examples.technique;

import processing.core.PApplet;
import wellen.Beat;
import wellen.Loop;
import wellen.Module;
import wellen.Note;
import wellen.ToneEngineDSP;
import wellen.Track;
import wellen.Wellen;
import wellen.dsp.DSP;
import wellen.dsp.Oscillator;
import wellen.dsp.OscillatorFunction;
import wellen.dsp.Signal;
import wellen.dsp.VowelFormantFilter;

public class TechniqueBasics04TracksAndModules extends PApplet {

    /*
     * this example demonstrates how to build a composition with tracks and modules.
     */

    private final Track mMaster = new Track();
    private final ModuleToneEngine mModuleBleepBleep = new ModuleToneEngine();
    private static final int PPQN = 24;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mMaster.modules().add(mModuleBleepBleep);
        mMaster.modules().add(new ModuleOhhhhUhh());
        Beat.start(this, 120 * PPQN);
        DSP.start(this, 2);
    }

    public void draw() {
        background(255);
        DSP.draw_buffers(g, width, height);
    }

    public void beat(int pBeat) {
        mMaster.beat(pBeat);
    }

    public void audioblock(float[] pOutputSignalLeft, float[] pOutputSignalRight) {
        for (int i = 0; i < pOutputSignalLeft.length; i++) {
            Signal s = mMaster.output_signal();
            pOutputSignalLeft[i] = s.left();
            pOutputSignalRight[i] = s.right();
        }
    }

    private static class ModuleToneEngine extends Module {
        private final ToneEngineDSP mToneEngine;

        public ModuleToneEngine() {
            mToneEngine = ToneEngineDSP.create_without_audio_output(4);
            mToneEngine.enable_reverb(true);
            set_in_out_point(0, 3);
            set_loop(Wellen.LOOP_INFINITE);
        }

        @Override
        public Signal output_signal() {
            return mToneEngine.output_signal();
        }

        public void beat(int beat) {
            if (beat % (PPQN / 4) == 0) {
                int mBeat = beat / PPQN;
                if ((get_loop_count(mBeat) % 8) < 4) {
                    mToneEngine.instrument(0);
                    mToneEngine.note_on(48 + (mBeat % 4) * 12, 70, 0.1f);
                    if (mBeat % 4 == 0) {
                        mToneEngine.instrument(1);
                        mToneEngine.note_on(24, 85, 0.3f);
                    }
                    if (mBeat % 4 == 1) {
                        mToneEngine.instrument(2);
                        mToneEngine.note_on(36, 80, 0.2f);
                    }
                    if (mBeat % 4 == 3) {
                        mToneEngine.instrument(3);
                        mToneEngine.note_on(36 + 7, 75, 0.25f);
                    }
                }
            }
        }
    }

    private class ModuleOhhhhUhh extends Module {

        private final Oscillator mOSC = new OscillatorFunction();
        private final VowelFormantFilter mFormantFilter = new VowelFormantFilter();
        private final float mMaxAmplitude = 0.2f;
        private final float mNoiseScale = 0.02f;
        private final float mBaseFreq = Note.note_to_frequency(12);

        public ModuleOhhhhUhh() {
            mOSC.set_frequency(mBaseFreq);
            mOSC.set_waveform(Wellen.WAVEFORM_SQUARE);
            mOSC.set_amplitude(0.0f);
            mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_O);
        }

        public Signal output_signal() {
            return Signal.create(mFormantFilter.process(mOSC.output()));
        }

        public void beat(int beat) {
            mOSC.set_frequency(mBaseFreq + noise(beat * mNoiseScale) * 6 - 3);

            final int mPhase = PPQN * 16;
            if (Loop.before(beat / mPhase, 3, 4)) {
                float mAmp = (beat % mPhase) / (mPhase * 0.5f);
                mAmp -= 1.0f;
                mAmp = abs(mAmp);
                mAmp = 1.0f - mAmp;
                mAmp *= mMaxAmplitude;
                mOSC.set_amplitude(mAmp);
            } else {
                mOSC.set_amplitude(0.0f);
            }

            Loop mLoop = new Loop();
            mLoop.set_length(PPQN * 4);
            if (mLoop.event(beat, 0)) {
                mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_O);
            } else if (mLoop.event(beat, PPQN * 2)) {
                mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_A);
            } else if (mLoop.event(beat, PPQN * 3)) {
                mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_U);
            }
        }
    }

    public static void main(String[] args) {
        PApplet.main(TechniqueBasics04TracksAndModules.class.getName());
    }
}
