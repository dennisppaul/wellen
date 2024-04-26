import wellen.*; 
import wellen.dsp.*; 

static final int INSTRUMENT_DETUNE_STEREO = 0;
void settings() {
    size(640, 480);
}
void setup() {
    Tone.replace_instrument(new CustomInstrumentDetunedOscillatorsStereo(INSTRUMENT_DETUNE_STEREO));
}
void draw() {
    background(255);
    fill(0);
    final float mTranslate = width / 2.0f;
    Tone.instrument(INSTRUMENT_DETUNE_STEREO);
    translate(mTranslate, 0);
    ellipse(0, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
}
void mousePressed() {
    int mNote = 45 + (int) random(0, 12);
    Tone.instrument(INSTRUMENT_DETUNE_STEREO);
    Tone.note_on(mNote, 100);
}
void mouseReleased() {
    Tone.instrument(INSTRUMENT_DETUNE_STEREO).note_off();
}
static class CustomInstrumentDetunedOscillatorsStereo extends InstrumentDSP {
    /**
     * detunes the oscillators in percentage of the main frequency. one oscillator is detuned below the main
     * frequency and the other above.
     * <p>
     * e.g `detune = 0.01, main_frequency = 220.0`
     * <p>
     * > `osc_a_frequency = main_frequency * ( 1.0 - detune) = 217.8`
     * <p>
     * > `osc_a_frequency = main_frequency * ( 1.0 + detune) = 222.2`
     */
    float mDetune;
    /**
     * spreads the oscillators over left and right channel: `0.0` no spread, `1.0` fully spread over both channels
     */
    float mSpread;
    final Wavetable mVCOSecond;
    CustomInstrumentDetunedOscillatorsStereo(int pID) {
        super(pID);
        set_channels(2);
        set_detune(0.01f);
        set_spread(0.5f);
        Wavetable.fill(fVCO.get_wavetable(), Wellen.WAVEFORM_SINE);
        mVCOSecond = new Wavetable(DEFAULT_WAVETABLE_SIZE);
        mVCOSecond.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
        Wavetable.fill(mVCOSecond.get_wavetable(), Wellen.WAVEFORM_SINE);
    }
    float get_detune() {
        return mDetune;
    }
    void set_detune(float pDetune) {
        mDetune = pDetune;
    }
    float get_spread() {
        return mSpread;
    }
    void set_spread(float pSpread) {
        mSpread = pSpread;
    }
    Signal output_signal() {
        /* this custom instrument ignores LFOs and LPF */
        fVCO.set_frequency(get_frequency() * (1.0f - mDetune));
        fVCO.set_amplitude(get_amplitude());
        mVCOSecond.set_frequency(get_frequency() * (1.0f + mDetune));
        mVCOSecond.set_amplitude(get_amplitude());
        /* use inherited ADSR envelope to control the amplitude */
        final float mADSRAmp = fADSR.output();
        final float mSignalA = fVCO.output();
        final float mSignalB = mVCOSecond.output();
        final float mMix = mSpread * 0.5f + 0.5f;
        final float mInvMix = 1.0f - mMix;
        final Signal pSignal = new Signal();
        pSignal.left(mSignalA * mMix + mSignalB * mInvMix);
        pSignal.right(mSignalA * mInvMix + mSignalB * mMix);
        pSignal.left_mult(mADSRAmp);
        pSignal.right_mult(mADSRAmp);
        return pSignal;
    }
}
