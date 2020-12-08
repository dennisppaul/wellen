import wellen.*; 
import netP5.*; 
import oscP5.*; 

static final int INSTRUMENT_DEFAULT = 0;

static final int INSTRUMENT_SAMPLER = 1;

static final int INSTRUMENT_KICK_DRUM = 2;

static final int INSTRUMENT_MULTIPLE_OSCILLATOR = 3;

void settings() {
    size(640, 480);
}

void setup() {
    Tone.replace_instrument(new CustomInstrumentKickDrum(INSTRUMENT_KICK_DRUM));
    Tone.replace_instrument(new CustomInstrumentSampler(INSTRUMENT_SAMPLER));
    Tone.replace_instrument(new CustomInstrumentMultipleOscillators(INSTRUMENT_MULTIPLE_OSCILLATOR));
}

void draw() {
    background(255);
    fill(0);
    ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
}

void keyPressed() {
    int mNote = 45 + (int) random(0, 12);
    switch (key) {
        case '0':
            Tone.instrument(INSTRUMENT_DEFAULT);
            break;
        case '1':
            Tone.instrument(INSTRUMENT_SAMPLER);
            break;
        case '2':
            Tone.instrument(INSTRUMENT_KICK_DRUM);
            break;
        case '3':
            Tone.instrument(INSTRUMENT_MULTIPLE_OSCILLATOR);
            break;
    }
    Tone.note_on(mNote, 100);
}

void keyReleased() {
    Tone.note_off();
}

static class CustomInstrumentSampler extends InstrumentInternal {
    
final Sampler mSampler;
    
CustomInstrumentSampler(int pID) {
        super(pID);
        mSampler = new Sampler();
        mSampler.load(SampleDataSNARE.data);
        mSampler.loop(false);
    }
    
float output() {
        return mSampler.output() * get_amplitude();
    }
    
void note_off() {
        mIsPlaying = false;
    }
    
void note_on(int pNote, int pVelocity) {
        mIsPlaying = true;
        set_amplitude(velocity_to_amplitude(pVelocity));
        mSampler.rewind();
    }
}

static class CustomInstrumentMultipleOscillators extends InstrumentInternal {
    
final Wavetable mLowerVCO;
    
final Wavetable mVeryLowVCO;
    
CustomInstrumentMultipleOscillators(int pID) {
        super(pID);
        mLowerVCO = new Wavetable(DEFAULT_WAVETABLE_SIZE);
        mLowerVCO.interpolate_samples(true);
        mVeryLowVCO = new Wavetable(DEFAULT_WAVETABLE_SIZE);
        mVeryLowVCO.interpolate_samples(true);
        Wavetable.fill(mVCO.get_wavetable(), Wellen.OSC_TRIANGLE);
        Wavetable.fill(mLowerVCO.get_wavetable(), Wellen.OSC_SINE);
        Wavetable.fill(mVeryLowVCO.get_wavetable(), Wellen.OSC_SQUARE);
    }
    
float output() {
        /* this custom instrument ignores LFOs and LPF */
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

static class CustomInstrumentKickDrum extends InstrumentInternal {
    
final ADSR mFrequencyEnvelope;
    
final float mFrequencyRange = 80;
    
final float mDecaySpeed = 0.25f;
    
CustomInstrumentKickDrum(int pID) {
        super(pID);
        set_oscillator_type(Wellen.OSC_SINE);
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
    
float output() {
        final float mFrequencyOffset = mFrequencyEnvelope.output() * mFrequencyRange;
        mVCO.set_frequency(get_frequency() + mFrequencyOffset);
        mVCO.set_amplitude(get_amplitude());
        float mSample = mVCO.output();
        final float mADSRAmp = mADSR.output();
        return mSample * mADSRAmp;
    }
    
void note_off() {
        mIsPlaying = false;
    }
    
void note_on(int pNote, int pVelocity) {
        mIsPlaying = true;
        mADSR.start();
        mFrequencyEnvelope.start();
    }
}
