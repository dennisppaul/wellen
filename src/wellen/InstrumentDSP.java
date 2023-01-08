/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2023 Dennis P Paul.
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

import wellen.dsp.ADSR;
import wellen.dsp.DSPNodeOutputSignal;
import wellen.dsp.LowPassFilter;
import wellen.dsp.Signal;
import wellen.dsp.Wavetable;

/**
 * implementation of {@link wellen.Instrument} for the internal tone engine.
 */
public class InstrumentDSP extends Instrument implements DSPNodeOutputSignal {

    public static final float DEFAULT_FREQUENCY = 220.0f;
    public static final int DEFAULT_WAVETABLE_SIZE = 512;
    public boolean always_interpolate_frequency_amplitude_changes = true;

    protected final ADSR fADSR;
    protected final Wavetable fAmplitudeLFO;
    protected final Wavetable fFrequencyLFO;
    protected final LowPassFilter fLPF;
    protected final Wavetable fVCO;
    protected final Wavetable fDetuneVCO;

    private float fAmp;
    private float fFreq;
    private float fFreqOffset;
    private int fNumChannels;
    private int fVCOType;
    private final int fSamplingRate;
    private int fDetuneVCOType;
    private float fDetune;
    private float fDetuneAmp;

    public InstrumentDSP(int ID, int sampling_rate, int wavetable_size) {
        super(ID);
        fNumChannels = 1;
        fSamplingRate = sampling_rate;
        fADSR = new ADSR(sampling_rate);
        fADSR.set_attack(Wellen.DEFAULT_ATTACK);
        fADSR.set_decay(Wellen.DEFAULT_DECAY);
        fADSR.set_sustain(Wellen.DEFAULT_SUSTAIN);
        fADSR.set_release(Wellen.DEFAULT_RELEASE);
        enable_ADSR(true);

        /* setup detune VCO */
        fDetune = 1.01f;
        fDetuneAmp = 1.0f;
        fDetuneVCO = new Wavetable(wavetable_size, sampling_rate);
        fDetuneVCO.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
        fDetuneVCO.set_amplitude(1.0f);
        set_detune_oscillator_type(Wellen.WAVEFORM_SINE);

        /* setup main VCO */
        fVCO = new Wavetable(wavetable_size, sampling_rate);
        fVCO.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
        set_oscillator_type(Wellen.WAVEFORM_SINE);
        set_amplitude(0.0f);
        set_frequency(DEFAULT_FREQUENCY);

        /* setup LFO for frequency */
        fFrequencyLFO = new Wavetable(wavetable_size, sampling_rate);
        Wavetable.sine(fFrequencyLFO.get_wavetable());
        fFrequencyLFO.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
        fFrequencyLFO.set_frequency(0);
        fFrequencyLFO.set_amplitude(0);
        enable_frequency_LFO(false);

        /* setup LFO for amplitude */
        fAmplitudeLFO = new Wavetable(wavetable_size, sampling_rate);
        Wavetable.sine(fAmplitudeLFO.get_wavetable());
        fAmplitudeLFO.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
        fAmplitudeLFO.set_frequency(0);
        fAmplitudeLFO.set_amplitude(0);
        enable_amplitude_LFO(false);

        /* setup LPF */
        fLPF = new LowPassFilter(sampling_rate);
        enable_LPF(false);
    }

    public InstrumentDSP(int ID, int sampling_rate) {
        this(ID, sampling_rate, DEFAULT_WAVETABLE_SIZE);
    }

    public InstrumentDSP(int ID) {
        this(ID, Wellen.DEFAULT_SAMPLING_RATE, DEFAULT_WAVETABLE_SIZE);
    }

//    @Override
//    public float output() {
//        final Signal mSignal = new Signal(1);
//        output(mSignal);
//        return mSignal.signal[0];
//    }

    @Override
    public Signal output_signal() {
        if (fEnableFrequencyLFO) {
            final float mLFOFreq = fEnableFrequencyLFO ? fFrequencyLFO.output() : 0.0f;
            fVCO.set_frequency(fFreq + mLFOFreq + fFreqOffset);
            if (fEnableDetune) {
                fDetuneVCO.set_frequency(fVCO.get_frequency() * fDetune);
            }
        }

        if (fEnableAmplitudeLFO) {
            final float mLFOAmp = fEnableAmplitudeLFO ? fAmplitudeLFO.output() : 0.0f;
            fVCO.set_amplitude((fAmp + mLFOAmp) * (fEnableAmplitudeLFO ? 0.5f : 1.0f));
        }

        final float mADSRAmp = fEnableADSR ? fADSR.output() : 1.0f;
        float mSample = fVCO.output();
        if (fEnableDetune) {
            mSample += fDetuneVCO.output() * fVCO.get_amplitude() * fDetuneAmp;
            mSample *= 0.5f; // TODO not sure if this is good
        }
        if (fEnableLPF) {
            mSample = fLPF.process(mSample);
        }
        mSample = Wellen.clamp(mSample, -1.0f, 1.0f);

        final Signal mSignal = new Signal(get_channels());
        final float s = mADSRAmp * mSample;
        for (int i = 0; i < get_channels(); i++) {
            mSignal.signal[i] = s;
        }
        return mSignal;
    }

    @Override
    public void set_attack(float attack) {
        fAttack = attack;
        fADSR.set_attack(fAttack);
    }

    @Override
    public void set_decay(float decay) {
        fDecay = decay;
        fADSR.set_decay(fDecay);
    }

    @Override
    public void set_sustain(float sustain) {
        fSustain = sustain;
        fADSR.set_sustain(fSustain);
    }

    @Override
    public void set_release(float release) {
        fRelease = release;
        fADSR.set_release(fRelease);
    }

    @Override
    public int get_oscillator_type() {
        return fVCOType;
    }

    @Override
    public void set_oscillator_type(int oscillator) {
        fVCOType = oscillator;
        Wavetable.fill(fVCO.get_wavetable(), oscillator);
    }

    @Override
    public float get_frequency_LFO_amplitude() {
        return fFrequencyLFO.get_amplitude();
    }

    @Override
    public void set_frequency_LFO_amplitude(float amplitude) {
        fFrequencyLFO.set_amplitude(amplitude);
    }

    @Override
    public float get_frequency_LFO_frequency() {
        return fFrequencyLFO.get_frequency();
    }

    @Override
    public void set_frequency_LFO_frequency(float frequency) {
        fFrequencyLFO.set_frequency(frequency);
    }

    @Override
    public float get_amplitude_LFO_amplitude() {
        return fAmplitudeLFO.get_amplitude();
    }

    @Override
    public void set_amplitude_LFO_amplitude(float amplitude) {
        fAmplitudeLFO.set_amplitude(amplitude);
    }

    @Override
    public float get_amplitude_LFO_frequency() {
        return fAmplitudeLFO.get_frequency();
    }

    @Override
    public void set_amplitude_LFO_frequency(float frequency) {
        fAmplitudeLFO.set_frequency(frequency);
    }

    @Override
    public float get_filter_resonance() {
        return fLPF.get_resonance();
    }

    @Override
    public void set_filter_resonance(float resonance) {
        fLPF.set_resonance(resonance);
    }

    @Override
    public float get_filter_frequency() {
        return fLPF.get_frequency();
    }

    @Override
    public void set_filter_frequency(float frequency) {
        fLPF.set_frequency(frequency);
    }

    @Override
    public float get_amplitude() {
        return fAmp;
    }

    @Override
    public void set_amplitude(float amplitude) {
        fAmp = amplitude;
        if (always_interpolate_frequency_amplitude_changes) {
            fVCO.set_amplitude(fAmp, Wellen.DEFAULT_INTERPOLATE_AMP_FREQ_DURATION);
        } else {
            fVCO.set_amplitude(fAmp);
        }
    }

    @Override
    public void set_amplitude(float amplitude, int interpolation_duration_in_samples) {
        fAmp = amplitude;
        fVCO.set_amplitude(fAmp, interpolation_duration_in_samples);
    }

    @Override
    public float get_frequency() {
        return fFreq;
    }

    @Override
    public void set_frequency(float frequency) {
        fFreq = frequency;
        updateVCOFrequency();
    }

    @Override
    public void set_frequency(float frequency, int interpolation_duration_in_samples) {
        fFreq = frequency;
        fVCO.set_frequency(fFreq + fFreqOffset, interpolation_duration_in_samples);
        fDetuneVCO.set_frequency(getDetuneFreq(), interpolation_duration_in_samples);
    }

    private float getDetuneFreq() {
        return (fFreq + fFreqOffset) * fDetune;
    }

    private void updateVCOFrequency() {
        if (always_interpolate_frequency_amplitude_changes) {
            fVCO.set_frequency(fFreq + fFreqOffset, Wellen.DEFAULT_INTERPOLATE_AMP_FREQ_DURATION);
            fDetuneVCO.set_frequency(getDetuneFreq(), Wellen.DEFAULT_INTERPOLATE_AMP_FREQ_DURATION);
        } else {
            fVCO.set_frequency(fFreq + fFreqOffset);
            fDetuneVCO.set_frequency(getDetuneFreq());
        }
    }

    @Override
    public void pitch_bend(float frequency_offset) {
        fFreqOffset = frequency_offset;
        updateVCOFrequency();
    }

    /**
     * detune of second oscillator in relation to main oscillator
     *
     * @param detune in percent. a value of 1.0 will tune the second oscillator to the exact frequency as the main
     *               oscillator. a value of 0.5 will tune the second oscillator to half the frequency of the main
     *               oscillator, etcetera.
     */
    @Override
    public void set_detune(float detune) {
        fDetune = detune;
        fDetuneVCO.set_frequency(getDetuneFreq());
    }

    @Override
    public float get_detune() {
        return fDetune;
    }

    @Override
    public void set_detune_amplitude(float amplitude) {
        fDetuneAmp = amplitude;
    }

    @Override
    public float get_detune_amplitude() {
        return fDetuneAmp;
    }

    @Override
    public void set_detune_oscillator_type(int oscillator) {
        fDetuneVCOType = oscillator;
        Wavetable.fill(fDetuneVCO.get_wavetable(), oscillator);
    }

    @Override
    public void note_off() {
        fIsPlaying = false;
        fADSR.stop();
    }

    @Override
    public void note_on(int note, int velocity) {
        fIsPlaying = true;
        set_frequency(note_to_frequency(note));
        set_amplitude(velocity_to_amplitude(velocity));
        fADSR.start();
    }

    public Wavetable get_VCO() {
        return fVCO;
    }

    public Wavetable get_detune_VCO() {
        return fDetuneVCO;
    }

    public int get_channels() {
        return fNumChannels;
    }

    public void set_channels(int num_channels) {
        fNumChannels = num_channels;
    }
}