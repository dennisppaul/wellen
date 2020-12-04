import ton.*; 
import netP5.*; 
import oscP5.*; 

CustomInstrumentMultipleOscillators mInstrument;

void settings() {
    size(640, 480);
}

void setup() {
    Ton.replace_instrument(new CustomInstrumentSampler(1));
    mInstrument = new CustomInstrumentMultipleOscillators(0);
    Ton.replace_instrument(mInstrument);
    Ton.instrument(0);
}

void draw() {
    background(255);
    fill(0);
    ellipse(width * 0.5f, height * 0.5f, Ton.is_playing() ? 100 : 5, Ton.is_playing() ? 100 : 5);
}

void keyPressed() {
    int mNote = 45 + (int) random(0, 12);
    switch (key) {
        case '0':
            Ton.instrument(0);
            break;
        case '1':
            Ton.instrument(1);
            break;
        case '2':
            Ton.instrument(2);
            break;
    }
    Ton.note_on(mNote, 100);
}

void keyReleased() {
    Ton.note_off();
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
        set_amplitude(_velocity_to_amplitude(pVelocity));
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
        Wavetable.fill(mVCO.wavetable(), Ton.OSC_TRIANGLE);
        Wavetable.fill(mLowerVCO.wavetable(), Ton.OSC_SINE);
        Wavetable.fill(mVeryLowVCO.wavetable(), Ton.OSC_SQUARE);
    }
    
float output() {
        /* this custom instrumetn ignores LFOs and LPF for now */
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
