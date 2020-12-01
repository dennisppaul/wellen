package de.hfkbremen.ton;

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
        set_osc_type(OSC_SINE);
        set_amplitude(0.0f);
        set_frequency(DEFAULT_FREQUENCY);
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

        mEnableLFOFrequency = false;
        mEnableLFOAmplitude = false;

//        mFrequencyLFO.set_frequency(5);
//        mFrequencyLFO.set_amplitude(40);
        mAmplitudeLFO.set_amplitude(0.5f);
        mAmplitudeLFO.set_frequency(1.0f);
    }

    public InstrumentSoftware(int pID, int pSamplingRate) {
        this(pID, pSamplingRate, DEFAULT_WAVETABLE_SIZE);
    }

    @Override
    public float output() {
        final float mLFOFreq = mEnableLFOFrequency ? mFrequencyLFO.output() : 0.0f;
        final float mLFOAmp = mEnableLFOAmplitude ? mAmplitudeLFO.output() : 0.0f;

        mVCO.set_frequency(mFreq + mLFOFreq + mFreqOffset);
        mVCO.set_amplitude((mAmp + mLFOAmp) * (mEnableLFOAmplitude ? 0.5f : 1.0f));

        final float mADSRAmp = mEnableADSR ? mADSR.output() : 1.0f;
        float mSample = mVCO.output();
        mSample = Ton.clamp(mSample, -1.0f, 1.0f);
        return mADSRAmp * mSample;
    }



    @Override
    public void set_attack(float pAttack) {
        mAttack = pAttack;
        mADSR.set_attack(mAttack);
    }

    @Override
    public void set_decay(float pDecay) {
        mDecay = pDecay;
        mADSR.set_decay(mDecay);
    }

    @Override
    public void set_sustain(float pSustain) {
        mSustain = pSustain;
        mADSR.set_sustain(mSustain);
    }

    @Override
    public void set_release(float pRelease) {
        mRelease = pRelease;
        mADSR.set_release(mRelease);
    }

    @Override
    public void set_osc_type(int pOsc) {
        mOscType = pOsc;
        Wavetable.fill(mVCO.wavetable(), pOsc);
    }

    @Override
    public int get_osc_type() {
        return mOscType;
    }

    @Override
    public void set_freq_LFO_amp(float pLFOAmp) {
    }

    @Override
    public float get_freq_LFO_amp() {
        return 0;
    }

    @Override
    public void set_freq_LFO_freq(float pLFOFreq) {

    }

    @Override
    public float get_freq_LFO_freq() {
        return 0;
    }

    @Override
    public void set_amp_LFO_amp(float pLFOAmp) {

    }

    @Override
    public float get_amp_LFO_amp() {
        return 0;
    }

    @Override
    public void set_amp_LFO_freq(float pLFOFreq) {

    }

    @Override
    public float get_amp_LFO_freq() {
        return 0;
    }

    @Override
    public void set_filter_q(float pResonance) {

    }

    @Override
    public float get_filter_q() {
        return 0;
    }

    @Override
    public void set_filter_freq(float pFreq) {

    }

    @Override
    public float get_filter_freq() {
        return 0;
    }

    @Override
    public void pitch_bend(float pFreqOffset) {
        mFreqOffset = pFreqOffset;
    }

    @Override
    public void set_amplitude(float pAmp) {
        mAmp = pAmp;
    }

    @Override
    public float get_amplitude() {
        return mAmp;
    }

    @Override
    public void set_frequency(float pFreq) {
        mFreq = pFreq;
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
    public void note_on(int pNote, int pVelocity) {
        mIsPlaying = true;
        set_frequency(_note_to_frequency(pNote));
        set_amplitude(_velocity_to_amplitude(pVelocity));
        mADSR.start();
    }

    @Override
    public boolean is_playing() {
        return mIsPlaying;
    }
}