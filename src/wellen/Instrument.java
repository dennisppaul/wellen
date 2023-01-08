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

/**
 * base class for all instruments
 */
public abstract class Instrument {

    protected float fAttack = Wellen.DEFAULT_ATTACK;
    protected float fDecay = Wellen.DEFAULT_DECAY;
    protected boolean fEnableADSR = false;
    protected boolean fEnableAdditionalOscillator = false;
    protected boolean fEnableAmplitudeLFO = false;
    protected boolean fEnableFrequencyLFO = false;
    protected boolean fEnableLPF = false;
    protected boolean fEnableDetune = false;
    protected boolean fIsPlaying = false;
    protected float fPan = 0.0f;
    protected float fRelease = Wellen.DEFAULT_RELEASE;
    protected float fSustain = Wellen.DEFAULT_SUSTAIN;
    private final int fID;

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

    public void enable_ADSR(boolean enable_ADSR) {
        fEnableADSR = enable_ADSR;
    }

    public void enable_amplitude_LFO(boolean enable_amplitude_LFO) {
        fEnableAmplitudeLFO = enable_amplitude_LFO;
    }

    public void enable_frequency_LFO(boolean enable_frequency_LFO) {
        fEnableFrequencyLFO = enable_frequency_LFO;
    }

    public void enable_LPF(boolean enable_LPF) {
        fEnableLPF = enable_LPF;
    }

    public void enable_detune(boolean enable_detune) {
        fEnableDetune = enable_detune;
    }

    public abstract void set_detune(float detune);

    public abstract float get_detune();

    public abstract void set_detune_amplitude(float detune);

    public abstract float get_detune_amplitude();

    public abstract void set_detune_oscillator_type(int oscillator);

    public abstract void note_off();

    public abstract void note_on(int note, int velocity);

    public boolean is_playing() {
        return fIsPlaying;
    }

    public void enable_additional_oscillator(boolean enable_additional_oscillator) {
        fEnableAdditionalOscillator = enable_additional_oscillator;
    }

    protected float note_to_frequency(int note) {
        return Note.note_to_frequency(Wellen.clamp127(note));
    }

    protected float velocity_to_amplitude(int velocity) {
        return Wellen.clamp127(velocity) / 127.0f;
    }
}
