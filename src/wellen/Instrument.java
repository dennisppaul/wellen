/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2020 Dennis P Paul.
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

    protected float mAttack = Wellen.DEFAULT_ATTACK;
    protected float mDecay = Wellen.DEFAULT_DECAY;
    protected float mSustain = Wellen.DEFAULT_SUSTAIN;
    protected float mRelease = Wellen.DEFAULT_RELEASE;
    protected float mPan = 0.0f;
    protected boolean mEnableFrequencyLFO = false;
    protected boolean mEnableAmplitudeLFO = false;
    protected boolean mEnableADSR = false;
    protected boolean mEnableLPF = false;
    protected boolean mIsPlaying = false;
    private final int mID;

    public Instrument(int pID) {
        mID = pID;
    }

    public int ID() {
        return mID;
    }

    public float get_attack() {
        return mAttack;
    }

    /**
     * @param pAttack time parameter defining the time it takes for the set_amp to reach maximum level.
     */
    public void set_attack(float pAttack) {
        mAttack = pAttack;
    }

    public float get_decay() {
        return mDecay;
    }

    /**
     * @param pDecay time parameter defining the time it takes to go from maximum to get_sustain level.
     */
    public void set_decay(float pDecay) {
        mDecay = pDecay;
    }

    public float get_sustain() {
        return mSustain;
    }

    /**
     * @param pSustain level parameter defining the level hold while note is still played.
     */
    public void set_sustain(float pSustain) {
        mSustain = pSustain;
    }

    public float get_release() {
        return mRelease;
    }

    /**
     * @param pRelease time parameter defining the time it takes for the set_amp to reach zero after note is off.
     */
    public void set_release(float pRelease) {
        mRelease = pRelease;
    }

    public abstract int get_oscillator_type();

    public abstract void set_oscillator_type(int pOscillator);

    public abstract float get_frequency_LFO_amplitude();

    public abstract void set_frequency_LFO_amplitude(float pAmplitude);

    public abstract float get_frequency_LFO_frequency();

    public abstract void set_frequency_LFO_frequency(float pFrequency);

    public abstract float get_amplitude_LFO_amplitude();

    public abstract void set_amplitude_LFO_amplitude(float pAmplitude);

    public abstract float get_amplitude_LFO_frequency();

    public abstract void set_amplitude_LFO_frequency(float pFrequency);

    public abstract float get_filter_resonance();

    public abstract void set_filter_resonance(float pResonance);

    public abstract float get_filter_frequency();

    public abstract void set_filter_frequency(float pFrequency);

    public abstract void pitch_bend(float pFreqOffset);

    public abstract float get_amplitude();

    public abstract void set_amplitude(float pAmplitude);

    public abstract float get_frequency();

    public abstract void set_frequency(float pFrequency);

    public float get_pan() {
        return mPan;
    }

    /**
     * @param pPan panning of instrument with -1.0 is the left side and 1.0 is the right side
     */
    public void set_pan(float pPan) {
        mPan = pPan;
    }

    public void enable_ADSR(boolean pEnableADSR) {
        mEnableADSR = pEnableADSR;
    }

    public void enable_amplitude_LFO(boolean pEnableAmplitudeLFO) {
        mEnableAmplitudeLFO = pEnableAmplitudeLFO;
    }

    public void enable_frequency_LFO(boolean pEnableFrequencyLFO) {
        mEnableFrequencyLFO = pEnableFrequencyLFO;
    }

    public void enable_LPF(boolean pEnableLPF) {
        mEnableLPF = pEnableLPF;
    }

    public abstract void note_off();

    public abstract void note_on(int pNote, int pVelocity);


    public boolean is_playing() {
        return mIsPlaying;
    }

    protected float note_to_frequency(int pNote) {
        return Note.note_to_frequency(Wellen.clamp127(pNote));
    }

    protected float velocity_to_amplitude(int pVelocity) {
        return Wellen.clamp127(pVelocity) / 127.0f;
    }
}
