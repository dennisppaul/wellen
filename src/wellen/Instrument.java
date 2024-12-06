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

import static wellen.Wellen.*;

/**
 * base class for all instruments
 */
public abstract class Instrument {

    protected     float   fAttack                     = Wellen.DEFAULT_ATTACK;
    protected     float   fDecay                      = Wellen.DEFAULT_DECAY;
    protected     boolean fEnableVCO                  = true;
    protected     boolean fEnableADSR                 = true;
    protected     boolean fEnableAmplitudeLFO         = false;
    protected     boolean fEnableFrequencyLFO         = false;
    protected     boolean fEnableLPF                  = false;
    protected     boolean fEnableLPFEnvelopeCutoff    = false;
    protected     boolean fEnableLPFEnvelopeResonance = false;
    protected     boolean fEnableSubVCO               = false;
    protected     boolean fEnableNoise                = false;
    protected     boolean fIsPlaying                  = false;
    protected     float   fPan                        = 0.0f;
    protected     float   fRelease                    = Wellen.DEFAULT_RELEASE;
    protected     float   fSustain                    = Wellen.DEFAULT_SUSTAIN;
    protected     float   fNoiseAmplitude             = 0.25f;
    private final int     fID;

    public Instrument(int ID) {
        fID = ID;
    }

    public int ID() {
        return fID;
    }

    public float get_attack() {
        return fAttack;
    }

    /**
     * @param attack time parameter defining the time it takes for the set_amp to reach maximum level.
     */
    public void set_attack(float attack) {
        fAttack = attack;
    }

    public float get_decay() {
        return fDecay;
    }

    /**
     * @param decay time parameter defining the time it takes to go from maximum to get_sustain level.
     */
    public void set_decay(float decay) {
        fDecay = decay;
    }

    public float get_sustain() {
        return fSustain;
    }

    /**
     * @param sustain level parameter defining the level hold while note is still played.
     */
    public void set_sustain(float sustain) {
        fSustain = sustain;
    }

    public float get_release() {
        return fRelease;
    }

    /**
     * @param release time parameter defining the time it takes for the set_amp to reach zero after note is off.
     */
    public void set_release(float release) {
        fRelease = release;
    }

    public void set_adsr(float attack, float decay, float sustain, float release) {
        set_attack(attack);
        set_decay(decay);
        set_sustain(sustain);
        set_release(release);
    }

    public abstract int get_oscillator_type();

    public abstract void set_oscillator_type(int oscillator);

    public abstract float get_frequency_LFO_amplitude();

    public abstract void set_frequency_LFO_amplitude(float amplitude);

    public abstract float get_frequency_LFO_frequency();

    public abstract void set_frequency_LFO_frequency(float frequency);

    public abstract float get_amplitude_LFO_amplitude();

    public abstract void set_amplitude_LFO_amplitude(float amplitude);

    public abstract float get_amplitude_LFO_frequency();

    public abstract void set_amplitude_LFO_frequency(float frequency);

    public abstract float get_filter_resonance();

    public abstract void set_filter_resonance(float resonance);

    public abstract float get_filter_frequency();

    public abstract void set_filter_frequency(float frequency);

    public abstract void pitch_bend(float frequency_offset);

    public abstract float get_amplitude();

    public abstract void set_amplitude(float amplitude);

    public void set_amplitude(float amplitude, int interpolation_duration_in_samples) {
        set_amplitude(amplitude);
    }

    public abstract float get_frequency();

    public abstract void set_frequency(float frequency);

    public void set_frequency(float frequency, int interpolation_duration_in_samples) {
        set_frequency(frequency);
    }

    public float get_pan() {
        return fPan;
    }

    /**
     * @param pan panning of instrument with -1.0 is the left side and 1.0 is the right side
     */
    public void set_pan(float pan) {
        fPan = pan;
    }

    public void enable_oscillator(boolean enable) {
        fEnableVCO = enable;
    }

    public void enable_ADSR(boolean enable) {
        fEnableADSR = enable;
    }

    public void enable_amplitude_LFO(boolean enable) {
        fEnableAmplitudeLFO = enable;
    }

    public void enable_frequency_LFO(boolean enable) {
        fEnableFrequencyLFO = enable;
    }

    public void enable_LPF(boolean enable_LPF) {
        fEnableLPF = enable_LPF;
    }

    public void enable_LPF_envelope_cutoff(boolean enable) {
        fEnableLPFEnvelopeCutoff = enable;
    }

    public void enable_LPF_envelope_resonance(boolean enable) {
        fEnableLPFEnvelopeResonance = enable;
    }

    public void enable_sub_oscillator(boolean enable) {
        fEnableSubVCO = enable;
    }

    public void enable_noise(boolean enable) {
        fEnableNoise = enable;
    }

    public void set_noise_amplitude(float amplitude) {
        fNoiseAmplitude = amplitude;
    }

    public float get_noise_amplitude(float amplitude) {
        return fNoiseAmplitude;
    }

    public abstract float get_LPF_envelope_cutoff_min();

    public abstract void set_LPF_envelope_cutoff_min(float value);

    public abstract float get_LPF_envelope_cutoff_max();

    public abstract void set_LPF_envelope_cutoff_max(float value);

    public abstract float get_LPF_envelope_resonance_min();

    public abstract void set_LPF_envelope_resonance_min(float value);

    public abstract float get_LPF_envelope_resonance_max();

    public abstract void set_LPF_envelope_resonance_max(float value);

    public abstract ADSR get_LPF_envelope_cutoff();

    public abstract ADSR get_LPF_envelope_resonance();

    public abstract void set_sub_ratio(float frequency_ratio);

    public abstract float get_sub_ratio();

    public abstract void set_sub_amplitude(float amplitude);

    public abstract float get_sub_amplitude();

    public abstract void set_sub_oscillator_type(int oscillator);

    public abstract void note_off();

    public abstract void note_on(int note, int velocity);

    public void set_volume(float volume) {
    }

    public float get_volume() {
        return 0.0f;
    }

    public boolean is_playing() {
        return fIsPlaying;
    }

    protected float note_to_frequency(int note) {
        return Note.note_to_frequency(Wellen.clamp127(note));
    }

    protected float velocity_to_amplitude(int velocity) {
        return Wellen.clamp127(velocity) / 127.0f;
    }

    public void preset(int preset_type) {
        switch (preset_type) {
            case INSTRUMENT_PRESET_FAT:
                preset_fat();
                break;
            case INSTRUMENT_PRESET_SUB_SINE:
                preset_sub_sine();
                break;
            case INSTRUMENT_PRESET_NOISE:
                preset_noise();
                break;
            case INSTRUMENT_PRESET_SIMPLE:
                preset_simple();
                break;
        }
    }

    private void preset_simple() {
        enable_ADSR(true);
        enable_oscillator(true);
        set_oscillator_type(WAVEFORM_TRIANGLE);
        set_amplitude(0.7f);
        enable_sub_oscillator(false);
        enable_LPF(false);
        enable_frequency_LFO(false);
        enable_amplitude_LFO(false);
        enable_noise(false);
    }

    private void preset_noise() {
        enable_ADSR(true);
        enable_oscillator(false);
        enable_sub_oscillator(false);
        enable_LPF(false);
        enable_frequency_LFO(false);
        enable_amplitude_LFO(false);
        enable_noise(true);
        set_noise_amplitude(0.6f);
    }

    private void preset_sub_sine() {
        enable_ADSR(true);
        enable_oscillator(true);
        set_oscillator_type(Wellen.WAVEFORM_SINE);
        set_amplitude(0.8f);
        enable_sub_oscillator(true);
        set_sub_ratio(0.5f);
        set_sub_amplitude(0.4f);
        set_sub_oscillator_type(Wellen.WAVEFORM_SINE);
        enable_LPF(false);
        enable_frequency_LFO(false);
        enable_amplitude_LFO(false);
        enable_noise(false);
    }

    private void preset_fat() {
        enable_ADSR(true);
        enable_oscillator(true);
        set_oscillator_type(Wellen.WAVEFORM_SAWTOOTH);
        set_amplitude(0.8f);
        enable_sub_oscillator(true);
        set_sub_ratio(0.505f);
        set_sub_amplitude(0.6f);
        set_sub_oscillator_type(Wellen.WAVEFORM_SQUARE);
        enable_LPF(true);
        enable_LPF_envelope_cutoff(true);
        enable_LPF_envelope_resonance(true);
        enable_frequency_LFO(true);
        set_frequency_LFO_amplitude(1.0f);
        set_frequency_LFO_frequency(11.0f);
        enable_amplitude_LFO(false);
        enable_noise(true);
        set_noise_amplitude(0.25f);
    }
}
