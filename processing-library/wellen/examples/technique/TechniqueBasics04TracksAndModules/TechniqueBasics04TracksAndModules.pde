import wellen.*; 

/*
 * this example demonstrates how to build a composition with tracks and modules.
 */

final DSPTrack mMaster = new DSPTrack();

final ModuleToneEngine mModuleBleepBleep = new ModuleToneEngine();

static final int PPQN = 24;

void settings() {
    size(640, 480);
}

void setup() {
    mMaster.modules().add(mModuleBleepBleep);
    mMaster.modules().add(new ModuleOhhhhUhh());
    Beat.start(this, 120 * PPQN);
    DSP.start(this, 2);
}

void draw() {
    background(255);
    DSP.draw_buffer_stereo(g, width, height);
}

void beat(int pBeat) {
    mMaster.beat(pBeat);
}

void audioblock(float[] pOutputSignalLeft, float[] pOutputSignalRight) {
    for (int i = 0; i < pOutputSignalLeft.length; i++) {
        Signal s = mMaster.output_signal();
        pOutputSignalLeft[i] = s.left();
        pOutputSignalRight[i] = s.right();
    }
}

static class ModuleToneEngine extends DSPModule {
    
final ToneEngineDSP mToneEngine;
    
ModuleToneEngine() {
        mToneEngine = ToneEngineDSP.create_without_audio_output(4);
        mToneEngine.enable_reverb(true);
        set_in_out_point(0, 3);
        set_loop(Wellen.LOOP_INFINITE);
    }
    @Override
    
Signal output_signal() {
        return mToneEngine.output_signal();
    }
    
void beat(int pBeat) {
        if (pBeat % (PPQN / 4) == 0) {
            int mBeat = pBeat / PPQN;
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

class ModuleOhhhhUhh extends DSPModule {
    
final Oscillator mOSC = new OscillatorFunction();
    
final VowelFormantFilter mFormantFilter = new VowelFormantFilter();
    
final float mMaxAmplitude = 0.2f;
    
final float mNoiseScale = 0.02f;
    
final float mBaseFreq = Note.note_to_frequency(12);
    
ModuleOhhhhUhh() {
        mOSC.set_frequency(mBaseFreq);
        mOSC.set_waveform(Wellen.WAVESHAPE_SQUARE);
        mOSC.set_amplitude(0.0f);
        mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_O);
    }
    
Signal output_signal() {
        return Signal.create(mFormantFilter.process(mOSC.output()));
    }
    
void beat(int pBeat) {
        mOSC.set_frequency(mBaseFreq + noise(pBeat * mNoiseScale) * 6 - 3);
        final int mPhase = PPQN * 16;
        if (Loop.before(pBeat / mPhase, 3, 4)) {
            float mAmp = (pBeat % mPhase) / (mPhase * 0.5f);
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
        if (mLoop.event(pBeat, 0)) {
            mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_O);
        } else if (mLoop.event(pBeat, PPQN * 2)) {
            mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_A);
        } else if (mLoop.event(pBeat, PPQN * 3)) {
            mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_U);
        }
    }
}
