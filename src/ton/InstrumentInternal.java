package ton;

public class InstrumentInternal extends Instrument implements DSPNodeOutput {

    public static final int DEFAULT_WAVETABLE_SIZE = 512;
    public static final float DEFAULT_FREQUENCY = 220.0f;
    private final int mSamplingRate;
    private final ADSR mADSR;
    private final Wavetable mVCO;
    private final Wavetable mFrequencyLFO;
    private final Wavetable mAmplitudeLFO;
    private final LowPassFilter mLPF;
    private float mAmp;
    private float mFreq;
    private float mFreqOffset;
    private int mOscType;

    public InstrumentInternal(int pID, int pSamplingRate, int pWavetableSize) {
        super(pID);
        mSamplingRate = pSamplingRate;
        mADSR = new ADSR(pSamplingRate);
        mADSR.set_attack(Ton.DEFAULT_ATTACK);
        mADSR.set_decay(Ton.DEFAULT_DECAY);
        mADSR.set_sustain(Ton.DEFAULT_SUSTAIN);
        mADSR.set_release(Ton.DEFAULT_RELEASE);
        enable_ADSR(true);

        mVCO = new Wavetable(pWavetableSize, pSamplingRate);
        mVCO.interpolate_samples(true);
        set_oscillator_type(Ton.OSC_SINE);
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
        enable_frequency_LFO(false);

        /* setup LFO for amplitude */
        mAmplitudeLFO = new Wavetable(pWavetableSize, pSamplingRate);
        Wavetable.sine(mAmplitudeLFO.wavetable());
        mAmplitudeLFO.interpolate_samples(true);
        mAmplitudeLFO.set_frequency(0);
        mAmplitudeLFO.set_amplitude(0);
        enable_amplitude_LFO(false);

        /* setup LPF */
        mLPF = new LowPassFilter(pSamplingRate);
        enable_LPF(false);
    }

    public InstrumentInternal(int pID, int pSamplingRate) {
        this(pID, pSamplingRate, DEFAULT_WAVETABLE_SIZE);
    }

    @Override
    public float output() {
        final float mLFOFreq = mEnableFrequencyLFO ? mFrequencyLFO.output() : 0.0f;
        final float mLFOAmp = mEnableAmplitudeLFO ? mAmplitudeLFO.output() : 0.0f;

        mVCO.set_frequency(mFreq + mLFOFreq + mFreqOffset);
        mVCO.set_amplitude((mAmp + mLFOAmp) * (mEnableAmplitudeLFO ? 0.5f : 1.0f));

        final float mADSRAmp = mEnableADSR ? mADSR.output() : 1.0f;
        float mSample = mVCO.output();
        if (mEnableLPF) { mSample = mLPF.process(mSample); }
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
    public int get_oscillator_type() {
        return mOscType;
    }

    @Override
    public void set_oscillator_type(int pOscillator) {
        mOscType = pOscillator;
        Wavetable.fill(mVCO.wavetable(), pOscillator);
    }

    @Override
    public float get_frequency_LFO_amplitude() {
        return mFrequencyLFO.get_amplitude();
    }

    @Override
    public void set_frequency_LFO_amplitude(float pAmplitude) {
        mFrequencyLFO.set_amplitude(pAmplitude);
    }

    @Override
    public float get_frequency_LFO_frequency() {
        return mFrequencyLFO.get_frequency();
    }

    @Override
    public void set_frequency_LFO_frequency(float pFrequency) {
        mFrequencyLFO.set_frequency(pFrequency);
    }

    @Override
    public float get_amplitude_LFO_amplitude() {
        return mAmplitudeLFO.get_amplitude();
    }

    @Override
    public void set_amplitude_LFO_amplitude(float pAmplitude) {
        mAmplitudeLFO.set_amplitude(pAmplitude);
    }

    @Override
    public float get_amplitude_LFO_frequency() {
        return mAmplitudeLFO.get_frequency();
    }

    @Override
    public void set_amplitude_LFO_frequency(float pFrequency) {
        mAmplitudeLFO.set_frequency(pFrequency);
    }

    @Override
    public float get_filter_resonance() {
        return mLPF.get_resonance();
    }

    @Override
    public void set_filter_resonance(float pResonance) {
        mLPF.set_resonance(pResonance);
    }

    @Override
    public float get_filter_frequency() {
        return mLPF.get_frequency();
    }

    @Override
    public void set_filter_frequency(float pFrequency) {
        mLPF.set_frequency(pFrequency);
    }

    @Override
    public void pitch_bend(float pFreqOffset) {
        mFreqOffset = pFreqOffset;
    }

    @Override
    public float get_amplitude() {
        return mAmp;
    }

    @Override
    public void set_amplitude(float pAmplitude) {
        mAmp = pAmplitude;
    }

    @Override
    public float get_frequency() {
        return mFreq;
    }

    @Override
    public void set_frequency(float pFrequency) {
        mFreq = pFrequency;
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
}