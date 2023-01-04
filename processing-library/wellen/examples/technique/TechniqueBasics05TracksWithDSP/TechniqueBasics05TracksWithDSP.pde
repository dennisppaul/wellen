import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to build a composition with tracks where each track produces its own an audio
 * signal. in contrast to the previous example, the tracks in this example also overrides <code>output_signal()
 * </code> to produce its own audio signal. note that a master track accumulates the signals from all child
 * tracks when <code>output_signal()</code> is called.
 */

static final int PPQN = 24;

final Track fMaster = new Track();

final ModuleToneEngine fModuleBleepBleep = new ModuleToneEngine();

void settings() {
    size(640, 480);
}

void setup() {
    fMaster.tracks().add(fModuleBleepBleep);
    fMaster.tracks().add(new ModuleOhhhhUhh());
    Beat.start(this, 120 * PPQN);
    DSP.start(this, 2);
}

void draw() {
    background(255);
    DSP.draw_buffers(g, width, height);
}

void beat(int beat) {
    fMaster.update(beat);
}

void audioblock(float[] output_signalLeft, float[] output_signalRight) {
    for (int i = 0; i < output_signalLeft.length; i++) {
        Signal s = fMaster.output_signal();
        output_signalLeft[i] = s.left();
        output_signalRight[i] = s.right();
    }
}

static class ModuleToneEngine extends Track {
    
final ToneEngineDSP mToneEngine;
    
ModuleToneEngine() {
        mToneEngine = ToneEngineDSP.create_without_audio_output(4);
        mToneEngine.enable_reverb(true);
        set_in_out_point(0, 3);
        set_loop(Wellen.LOOP_INFINITE);
    }
    
    
Signal output_signal() {
        return mToneEngine.output_signal();
    }
    
void beat(int beat_absolute, int beat_relative) {
        if (beat_relative % (PPQN / 4) == 0) {
            int mBeat = beat_relative / PPQN;
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

class ModuleOhhhhUhh extends Track {
    
final float mBaseFreq = Note.note_to_frequency(12);
    
final VowelFormantFilter mFormantFilter = new VowelFormantFilter();
    
final float mMaxAmplitude = 0.2f;
    
final float mNoiseScale = 0.02f;
    
final Oscillator mOSC = new OscillatorFunction();
    
ModuleOhhhhUhh() {
        mOSC.set_frequency(mBaseFreq);
        mOSC.set_waveform(Wellen.WAVEFORM_SQUARE);
        mOSC.set_amplitude(0.0f);
        mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_O);
    }
    
Signal output_signal() {
        return Signal.create(mFormantFilter.process(mOSC.output()));
    }
    
void beat(int beat_absolute, int beat_relative) {
        mOSC.set_frequency(mBaseFreq + noise(beat_relative * mNoiseScale) * 6 - 3);
        final int mPhase = PPQN * 16;
        if (Loop.before(beat_relative / mPhase, 3, 4)) {
            float mAmp = (beat_relative % mPhase) / (mPhase * 0.5f);
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
        if (mLoop.event(beat_relative, 0)) {
            mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_O);
        } else if (mLoop.event(beat_relative, PPQN * 2)) {
            mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_A);
        } else if (mLoop.event(beat_relative, PPQN * 3)) {
            mFormantFilter.set_vowel(VowelFormantFilter.VOWEL_U);
        }
    }
}
