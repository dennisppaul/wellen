package de.hfkbremen.ton;

import static de.hfkbremen.ton.Note.note_to_frequency;
import static de.hfkbremen.ton.Ton.DEFAULT_ATTACK;
import static de.hfkbremen.ton.Ton.DEFAULT_DECAY;
import static de.hfkbremen.ton.Ton.DEFAULT_RELEASE;
import static de.hfkbremen.ton.Ton.DEFAULT_SUSTAIN;
import static de.hfkbremen.ton.Ton.clamp127;

public abstract class Instrument {

    private final int mID;
    protected float mAttack = DEFAULT_ATTACK;
    protected float mDecay = DEFAULT_DECAY;
    protected float mSustain = DEFAULT_SUSTAIN;
    protected float mRelease = DEFAULT_RELEASE;
    protected float mPan = 0.0f;
    protected boolean mEnableLFOFrequency;
    protected boolean mEnableLFOAmplitude;
    protected boolean mEnableADSR;

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

    public abstract int get_osc_type();

    public abstract void set_osc_type(int pOsc);

    public abstract float get_freq_LFO_amp();

    public abstract void set_freq_LFO_amp(float pLFOAmp);

    public abstract float get_freq_LFO_freq();

    public abstract void set_freq_LFO_freq(float pLFOFreq);

    public abstract float get_amp_LFO_amp();

    public abstract void set_amp_LFO_amp(float pLFOAmp);

    public abstract float get_amp_LFO_freq();

    public abstract void set_amp_LFO_freq(float pLFOFreq);

    public abstract float get_filter_q();

    public abstract void set_filter_q(float pResonance);

    public abstract float get_filter_freq();

    public abstract void set_filter_freq(float pFreq);

    public abstract void pitch_bend(float pFreqOffset);

    public abstract float get_amplitude();

    public abstract void set_amplitude(float pAmp);

    public abstract float get_frequency();

    public abstract void set_frequency(float pFreq);

    public float get_pan() {return mPan;}

    /**
     * @param pPan panning of instrument with -1.0 is the left side and 1.0 is the right side
     */
    public void set_pan(float pPan) {
        mPan = pPan;
    }

    public void enable_ADSR(boolean pEnableADSR) {
        mEnableADSR = pEnableADSR;
    }

    public void enable_LFO_amplitude(boolean pEnableLFOAmplitude) {
        mEnableLFOAmplitude = pEnableLFOAmplitude;
    }

    public void enable_LFO_frequency(boolean pEnableLFOFrequency) {
        mEnableLFOFrequency = pEnableLFOFrequency;
    }

    public abstract void note_off();

    public abstract void note_on(int pNote, int pVelocity);

    public abstract boolean is_playing();

    protected float _note_to_frequency(int pNote) {
        return note_to_frequency(clamp127(pNote));
    }

    protected float _velocity_to_amplitude(int pVelocity) {
        return clamp127(pVelocity) / 127.0f;
    }
}
