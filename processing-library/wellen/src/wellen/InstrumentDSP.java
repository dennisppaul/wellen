/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2024 Dennis P Paul.
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
import wellen.dsp.FilterMoogLadderLowPass;
import wellen.dsp.Signal;
import wellen.dsp.Wavetable;

/**
 * implementation of {@link wellen.Instrument} for the internal tone engine.
 */
public class InstrumentDSP extends Instrument implements DSPNodeOutputSignal {

    public static final float DEFAULT_FREQUENCY      = 220.0f;
    public static final int   DEFAULT_WAVETABLE_SIZE = 512;

    protected final ADSR                    fADSR;
    protected final Wavetable               fAmplitudeLFO;
    protected final Wavetable               fFrequencyLFO;
    protected final FilterMoogLadderLowPass fLPF;
    protected final Wavetable               fVCO;
    protected final Wavetable               fSubVCO;
    public          boolean                 always_interpolate_frequency_amplitude_changes = true;
    private         float                   fInstrumentFreq;
    private         float                   fInstrumentVolume;
    private         float                   fFreqOffset                                    = 0;
    private final   int                     fSamplingRate;
    private         int                     fNumChannels;
    private         int                     fVCOType;
    private         int                     fSubVCOType;
    private         float                   fSubVCOFreqRatio;
    private final   ADSR                    fLPFCutoffEnvelope;
    private final   ADSR                    fLPFResonanceEnvelope;
    private         float                   fLPFEnvelopCutoffMin;
    private         float                   fLPFEnvelopCutoffMax;
    private         float                   fLPFEnvelopResonanceMin;
    private         float                   fLPFEnvelopResonanceMax;

    public InstrumentDSP(int ID, int sampling_rate, int wavetable_size) {
        super(ID);
        fNumChannels      = 1;
        fSamplingRate     = sampling_rate;
        fInstrumentVolume = 1.0f;

        fADSR = new ADSR(fSamplingRate);
        fADSR.set_attack(Wellen.DEFAULT_ATTACK);
        fADSR.set_decay(Wellen.DEFAULT_DECAY);
        fADSR.set_sustain(Wellen.DEFAULT_SUSTAIN);
        fADSR.set_release(Wellen.DEFAULT_RELEASE);
        enable_ADSR(true);

        /* setup sub VCO */
        fSubVCO = new Wavetable(wavetable_size, fSamplingRate);
        fSubVCO.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
        fSubVCO.set_amplitude(1.0f);
        set_sub_oscillator_type(Wellen.WAVEFORM_SINE);

        /* setup main VCO */
        fVCO = new Wavetable(wavetable_size, fSamplingRate);
        fVCO.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
        set_oscillator_type(Wellen.WAVEFORM_SINE);

        fSubVCOFreqRatio = 1.01f;
        set_amplitude(0.7f);
        set_frequency(DEFAULT_FREQUENCY);

        /* setup LFO for frequency */
        fFrequencyLFO = new Wavetable(wavetable_size, fSamplingRate);
        Wavetable.sine(fFrequencyLFO.get_wavetable());
        fFrequencyLFO.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
        fFrequencyLFO.set_frequency(0);
        fFrequencyLFO.set_amplitude(0);
        enable_frequency_LFO(false);

        /* setup LFO for amplitude */
        fAmplitudeLFO = new Wavetable(wavetable_size, fSamplingRate);
        Wavetable.sine(fAmplitudeLFO.get_wavetable());
        fAmplitudeLFO.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
        fAmplitudeLFO.set_frequency(0);
        fAmplitudeLFO.set_amplitude(0);
        enable_amplitude_LFO(false);

        /* setup LPF */
        fLPF = new FilterMoogLadderLowPass(fSamplingRate);
        enable_LPF(false);

        /* setup LPF envelopes */
        fLPFCutoffEnvelope = new ADSR();
        fLPFCutoffEnvelope.set_attack(0.25f);
        fLPFCutoffEnvelope.set_decay(0.01f);
        fLPFCutoffEnvelope.set_sustain(1.0f);
        fLPFCutoffEnvelope.set_release(0.1f);
        fLPFResonanceEnvelope = new ADSR();
        fLPFResonanceEnvelope.set_attack(0.25f);
        fLPFResonanceEnvelope.set_decay(0.01f);
        fLPFResonanceEnvelope.set_sustain(1.0f);
        fLPFResonanceEnvelope.set_release(0.1f);
        fLPFEnvelopCutoffMin    = 400.0f;
        fLPFEnvelopCutoffMax    = 2000.0f;
        fLPFEnvelopResonanceMin = 0.2f;
        fLPFEnvelopResonanceMax = 0.8f;
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
            final float mLFOFreq = fFrequencyLFO.output();
            fVCO.set_frequency(getVCOFreq() + mLFOFreq);
            if (fEnableSubVCO) {
                fSubVCO.set_frequency(fVCO.get_frequency() * fSubVCOFreqRatio);
            }
        }

        if (fEnableAmplitudeLFO) {
            final float mLFOAmp = fAmplitudeLFO.output();
            fVCO.set_amplitude((get_amplitude() + mLFOAmp) * (fEnableAmplitudeLFO ? 0.5f : 1.0f));
        }

        float mSample = 0.0f;
        if (fEnableVCO) {
            mSample += fVCO.output();
        }

        if (fEnableSubVCO) {
            mSample += fSubVCO.output();
        }

        if (fEnableNoise) {
            mSample += ((float) Math.random() * 2.0f - 1.0f) * fNoiseAmplitude;
        }

        if (fEnableLPF) {
            if (fEnableLPFEnvelopeCutoff) {
                final float mRange = fLPFEnvelopCutoffMax - fLPFEnvelopCutoffMin;
                fLPF.set_frequency(fLPFEnvelopCutoffMin + fLPFCutoffEnvelope.output() * mRange);
            }
            if (fEnableLPFEnvelopeResonance) {
                final float mRange = fLPFEnvelopResonanceMax - fLPFEnvelopResonanceMin;
                fLPF.set_resonance(fLPFEnvelopResonanceMin + fLPFResonanceEnvelope.output() * mRange);
            }
            mSample = fLPF.process(mSample);
        }

        mSample = Wellen.clamp(mSample, -1.0f, 1.0f);

        final float mADSRAmp = fEnableADSR ? fADSR.output() : 1.0f;
        mSample *= mADSRAmp;
        mSample *= fInstrumentVolume;
        mSample = Wellen.clamp(mSample, -1.0f, 1.0f);

        final Signal mSignal = new Signal(get_channels());
        for (int i = 0; i < get_channels(); i++) {
            mSignal.signal[i] = mSample;
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
        return fVCO.get_amplitude();
    }

    @Override
    public void set_amplitude(float amplitude) {
        if (always_interpolate_frequency_amplitude_changes) {
            fVCO.set_amplitude(amplitude, Wellen.DEFAULT_INTERPOLATE_AMP_FREQ_DURATION);
        } else {
            fVCO.set_amplitude(amplitude);
        }
    }

    @Override
    public void set_amplitude(float amplitude, int interpolation_duration_in_samples) {
        fVCO.set_amplitude(amplitude, interpolation_duration_in_samples);
    }

    @Override
    public float get_frequency() {
        return fInstrumentFreq;
    }

    @Override
    public void set_frequency(float frequency) {
        fInstrumentFreq = frequency;
        updateVCOFreq();
    }

    @Override
    public void set_frequency(float frequency, int interpolation_duration_in_samples) {
        fInstrumentFreq = frequency;
        fVCO.set_frequency(getVCOFreq(), interpolation_duration_in_samples);
        fSubVCO.set_frequency(getSubVCOFreq(), interpolation_duration_in_samples);
    }

    @Override
    public float get_LPF_envelope_cutoff_min() {
        return fLPFEnvelopCutoffMin;
    }

    @Override
    public void set_volume(float volume) {
        fInstrumentVolume = volume;
    }

    @Override
    public float get_volume() {
        return fInstrumentVolume;
    }

    @Override
    public void set_LPF_envelope_cutoff_min(float value) {
        fLPFEnvelopCutoffMin = value;
    }

    @Override
    public float get_LPF_envelope_cutoff_max() {
        return fLPFEnvelopCutoffMax;
    }

    @Override
    public void set_LPF_envelope_cutoff_max(float value) {
        fLPFEnvelopCutoffMax = value;
    }

    @Override
    public float get_LPF_envelope_resonance_min() {
        return fLPFEnvelopResonanceMin;
    }

    @Override
    public void set_LPF_envelope_resonance_min(float value) {
        fLPFEnvelopResonanceMin = value;
    }

    @Override
    public float get_LPF_envelope_resonance_max() {
        return fLPFEnvelopResonanceMax;
    }

    @Override
    public void set_LPF_envelope_resonance_max(float value) {
        fLPFEnvelopResonanceMax = value;
    }

    @Override
    public ADSR get_LPF_envelope_cutoff() {
        return fLPFCutoffEnvelope;
    }

    @Override
    public ADSR get_LPF_envelope_resonance() {
        return fLPFResonanceEnvelope;
    }

    private float getSubVCOFreq() {
        return getVCOFreq() * fSubVCOFreqRatio;
    }

    private float getVCOFreq() {
        return fInstrumentFreq + fFreqOffset;
    }

    private void updateVCOFreq() {
        if (always_interpolate_frequency_amplitude_changes) {
            fVCO.set_frequency(getVCOFreq(), Wellen.DEFAULT_INTERPOLATE_AMP_FREQ_DURATION);
            fSubVCO.set_frequency(getSubVCOFreq(), Wellen.DEFAULT_INTERPOLATE_AMP_FREQ_DURATION);
        } else {
            fVCO.set_frequency(getVCOFreq());
            fSubVCO.set_frequency(getSubVCOFreq());
        }
    }

    @Override
    public void pitch_bend(float frequency_offset) {
        fFreqOffset = frequency_offset;
        updateVCOFreq();
    }

    /**
     * set sub oscillator in relation to main oscillator
     *
     * @param frequency_ratio in percent. a value of 1.0 will tune the second oscillator to the exact frequency as the main
     *                        oscillator. a value of 0.5 will tune the second oscillator to half the frequency of the main
     *                        oscillator, etcetera.
     */
    @Override
    public void set_sub_ratio(float frequency_ratio) {
        fSubVCOFreqRatio = frequency_ratio;
        fSubVCO.set_frequency(getSubVCOFreq());
    }

    @Override
    public float get_sub_ratio() {
        return fSubVCOFreqRatio;
    }

    @Override
    public void set_sub_amplitude(float amplitude) {
        fSubVCO.set_amplitude(amplitude);
    }

    @Override
    public float get_sub_amplitude() {
        return fSubVCO.get_amplitude();
    }

    @Override
    public void set_sub_oscillator_type(int oscillator) {
        fSubVCOType = oscillator;
        Wavetable.fill(fSubVCO.get_wavetable(), fSubVCOType);
    }

    public int get_sub_oscillator_type() {
        return fSubVCOType;
    }

    @Override
    public void note_off() {
        fIsPlaying = false;
        fADSR.stop();
        if (fEnableLPFEnvelopeCutoff) {
            fLPFCutoffEnvelope.stop();
        }
        if (fEnableLPFEnvelopeResonance) {
            fLPFResonanceEnvelope.stop();
        }
    }

    @Override
    public void note_on(int note, int velocity) {
        fIsPlaying = true;
        set_frequency(note_to_frequency(note));
        set_volume(velocity_to_amplitude(velocity));
        fADSR.start();
        if (fEnableLPFEnvelopeCutoff) {
            fLPFCutoffEnvelope.start();
        }
        if (fEnableLPFEnvelopeResonance) {
            fLPFResonanceEnvelope.start();
        }
    }

    public Wavetable get_VCO() {
        return fVCO;
    }

    public Wavetable get_sub_VCO() {
        return fSubVCO;
    }

    public int get_channels() {
        return fNumChannels;
    }

    public void set_channels(int num_channels) {
        fNumChannels = num_channels;
    }
}