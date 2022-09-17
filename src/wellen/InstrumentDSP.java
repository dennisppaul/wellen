/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2022 Dennis P Paul.
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package wellen;

/**
 * implementation of {@link wellen.Instrument} for the internal tone engine.
 */
public class InstrumentDSP extends Instrument implements DSPNodeOutputSignal {

    public static final float DEFAULT_FREQUENCY = 220.0f;
    public static final int DEFAULT_WAVETABLE_SIZE = 512;
    protected final ADSR mADSR;
    protected final Wavetable mAmplitudeLFO;
    protected final Wavetable mFrequencyLFO;
    protected final LowPassFilter mLPF;
    protected final Wavetable mVCO;
    private float mAmp;
    private float mFreq;
    private float mFreqOffset;
    private int mNumChannels;
    private int mOscType;
    private final int mSamplingRate;

    public InstrumentDSP(int pID, int pSamplingRate, int pWavetableSize) {
        super(pID);
        mNumChannels = 1;
        mSamplingRate = pSamplingRate;
        mADSR = new ADSR(pSamplingRate);
        mADSR.set_attack(Wellen.DEFAULT_ATTACK);
        mADSR.set_decay(Wellen.DEFAULT_DECAY);
        mADSR.set_sustain(Wellen.DEFAULT_SUSTAIN);
        mADSR.set_release(Wellen.DEFAULT_RELEASE);
        enable_ADSR(true);

        mVCO = new Wavetable(pWavetableSize, pSamplingRate);
        mVCO.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
        set_oscillator_type(Wellen.WAVEFORM_SINE);
        set_amplitude(0.0f);
        set_frequency(DEFAULT_FREQUENCY);

        /* setup LFO for frequency */
        mFrequencyLFO = new Wavetable(pWavetableSize, pSamplingRate);
        Wavetable.sine(mFrequencyLFO.get_wavetable());
        mFrequencyLFO.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
        mFrequencyLFO.set_frequency(0);
        mFrequencyLFO.set_amplitude(0);
        enable_frequency_LFO(false);

        /* setup LFO for amplitude */
        mAmplitudeLFO = new Wavetable(pWavetableSize, pSamplingRate);
        Wavetable.sine(mAmplitudeLFO.get_wavetable());
        mAmplitudeLFO.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
        mAmplitudeLFO.set_frequency(0);
        mAmplitudeLFO.set_amplitude(0);
        enable_amplitude_LFO(false);

        /* setup LPF */
        mLPF = new LowPassFilter(pSamplingRate);
        enable_LPF(false);
    }

    public InstrumentDSP(int pID, int pSamplingRate) {
        this(pID, pSamplingRate, DEFAULT_WAVETABLE_SIZE);
    }

    public InstrumentDSP(int pID) {
        this(pID, Wellen.DEFAULT_SAMPLING_RATE, DEFAULT_WAVETABLE_SIZE);
    }

//    @Override
//    public float output() {
//        final Signal mSignal = new Signal(1);
//        output(mSignal);
//        return mSignal.signal[0];
//    }

    @Override
    public Signal output_signal() {
        final float mLFOFreq = mEnableFrequencyLFO ? mFrequencyLFO.output() : 0.0f;
        final float mLFOAmp = mEnableAmplitudeLFO ? mAmplitudeLFO.output() : 0.0f;

        mVCO.set_frequency(mFreq + mLFOFreq + mFreqOffset);
        mVCO.set_amplitude((mAmp + mLFOAmp) * (mEnableAmplitudeLFO ? 0.5f : 1.0f));

        final float mADSRAmp = mEnableADSR ? mADSR.output() : 1.0f;
        float mSample = mVCO.output();
        if (mEnableLPF) {
            mSample = mLPF.process(mSample);
        }
        mSample = Wellen.clamp(mSample, -1.0f, 1.0f);

        Signal pSignal = new Signal(get_channels());
        final float s = mADSRAmp * mSample;
        for (int i = 0; i < get_channels(); i++) {
            pSignal.signal[i] = s;
        }
        return pSignal;
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
        Wavetable.fill(mVCO.get_wavetable(), pOscillator);
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
        set_frequency(note_to_frequency(pNote));
        set_amplitude(velocity_to_amplitude(pVelocity));
        mADSR.start();
    }

    public Wavetable get_VCO() {
        return mVCO;
    }

    public int get_channels() {
        return mNumChannels;
    }

    public void set_channels(int pNumChannels) {
        mNumChannels = pNumChannels;
    }
}