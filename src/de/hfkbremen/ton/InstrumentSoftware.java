package de.hfkbremen.ton;

import processing.core.PApplet;

import static de.hfkbremen.ton.Ton.DEFAULT_ATTACK;
import static de.hfkbremen.ton.Ton.DEFAULT_DECAY;
import static de.hfkbremen.ton.Ton.DEFAULT_RELEASE;
import static de.hfkbremen.ton.Ton.DEFAULT_SUSTAIN;
import static de.hfkbremen.ton.Ton.OSC_SINE;

public class InstrumentSoftware extends Instrument implements DSPNodeOutput {

    public static final int DEFAULT_WAVETABLE_SIZE = 512;
    public static final float DEFAULT_FREQUENCY = 220.0f;
    private final int mSamplingRate;
    private final ADSR mADSR;
    private final Wavetable mVCO;
    private final Wavetable mFrequencyLFO;
    private final Wavetable mAmplitudeLFO;
    protected float mAmp;
    protected float mFreq;
    protected boolean mIsPlaying = false;
    protected float mFreqOffset;
    private boolean mEnableADSR;
    private int mOscType;

    public InstrumentSoftware(int pID, int pSamplingRate, int pWavetableSize) {
        super(pID);
        mSamplingRate = pSamplingRate;
        mADSR = new ADSR(pSamplingRate);
        mADSR.set_attack(DEFAULT_ATTACK);
        mADSR.set_decay(DEFAULT_DECAY);
        mADSR.set_sustain(DEFAULT_SUSTAIN);
        mADSR.set_release(DEFAULT_RELEASE);
        enable_ADSR(true);

        mVCO = new Wavetable(pWavetableSize, pSamplingRate);
        mVCO.interpolate_samples(true);
        osc_type(OSC_SINE);
        amplitude(0.0f);
        frequency(DEFAULT_FREQUENCY);
        mVCO.set_amplitude(mAmp);
        mVCO.set_frequency(mFreq);

        /* setup LFO for frequency */
        mFrequencyLFO = new Wavetable(pWavetableSize, pSamplingRate);
        Wavetable.sine(mFrequencyLFO.wavetable());
        mFrequencyLFO.interpolate_samples(true);
        mFrequencyLFO.set_frequency(0);
        mFrequencyLFO.set_amplitude(0);

        /* setup LFO for amplitude */
        mAmplitudeLFO = new Wavetable(pWavetableSize, pSamplingRate);
        Wavetable.sine(mAmplitudeLFO.wavetable());
        mAmplitudeLFO.interpolate_samples(true);
        mAmplitudeLFO.set_frequency(0);
        mAmplitudeLFO.set_amplitude(0);

//        mFrequencyLFO.set_frequency(5);
//        mFrequencyLFO.set_amplitude(40);
//        mAmplitudeLFO.set_amplitude(0.3f);
//        mAmplitudeLFO.set_frequency(5);
    }

    public InstrumentSoftware(int pID, int pSamplingRate) {
        this(pID, pSamplingRate, DEFAULT_WAVETABLE_SIZE);
    }

    @Override
    public float output() {
        final float mLFOAmp = PApplet.map(mAmplitudeLFO.output(), -1.0f, 1.0f, 0, 1);
        final float mLFOFreq = mFrequencyLFO.output();

        mVCO.set_amplitude(mAmp);
        mVCO.set_frequency(mFreq);

        final float mADSRAmp = mEnableADSR ? mADSR.output() : 1.0f;
        float mSample = mVCO.output();
        mSample = Ton.clamp(mSample, -1.0f, 1.0f);
        return mADSRAmp * mSample;
    }

    public void enable_ADSR(boolean pEnableADSR) {
        mEnableADSR = pEnableADSR;
    }

    @Override
    public void attack(float pAttack) {
        mAttack = pAttack;
        mADSR.set_attack(mAttack);
    }

    @Override
    public void decay(float pDecay) {
        mDecay = pDecay;
        mADSR.set_decay(mDecay);
    }

    @Override
    public void sustain(float pSustain) {
        mSustain = pSustain;
        mADSR.set_sustain(mSustain);
    }

    @Override
    public void release(float pRelease) {
        mRelease = pRelease;
        mADSR.set_release(mRelease);
    }

    @Override
    public void osc_type(int pOsc) {
        mOscType = pOsc;
        Wavetable.fill(mVCO.wavetable(), pOsc);
    }

    @Override
    public int get_osc_type() {
        return mOscType;
    }

    @Override
    public void lfo_amp(float pLFOAmp) {
    }

    @Override
    public float get_lfo_amp() {
        return 0;
    }

    @Override
    public void lfo_freq(float pLFOFreq) {

    }

    @Override
    public float get_lfo_freq() {
        return 0;
    }

    @Override
    public void filter_q(float f) {

    }

    @Override
    public float get_filter_q() {
        return 0;
    }

    @Override
    public void filter_freq(float f) {

    }

    @Override
    public float get_filter_freq() {
        return 0;
    }

    @Override
    public void pitch_bend(float freq_offset) {
        mFreqOffset = freq_offset;
        frequency(mFreq);
    }

    @Override
    public void amplitude(float pAmp) {
        mAmp = pAmp;
    }

    @Override
    public float get_amplitude() {
        return mAmp;
    }

    @Override
    public void frequency(float freq) {
        mFreq = freq;
    }

    @Override
    public float get_frequency() {
        return mFreq;
    }

    @Override
    public void note_off() {
        mIsPlaying = false;
        mADSR.stop();
    }

    @Override
    public void note_on(int note, int velocity) {
        mIsPlaying = true;
        frequency(_note_to_frequency(note));
        amplitude(_velocity_to_amplitude(velocity));
        mADSR.start();
    }

    @Override
    public boolean isPlaying() {
        return mIsPlaying;
    }
}