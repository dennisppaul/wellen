package de.hfkbremen.ton;

import static de.hfkbremen.ton.Note.note_to_frequency;
import static de.hfkbremen.ton.Ton.DEFAULT_ATTACK;
import static de.hfkbremen.ton.Ton.DEFAULT_DECAY;
import static de.hfkbremen.ton.Ton.DEFAULT_RELEASE;
import static de.hfkbremen.ton.Ton.DEFAULT_SUSTAIN;
import static de.hfkbremen.ton.Ton.clamp127;

public abstract class Instrument {

    public static final String ADSR_DIAGRAM = "    ^    /\\\n"
                                              + "    |   /  \\\n"
                                              + "    |  /    \\______\n"
                                              + "    | /            \\\n"
                                              + "    |/              \\\n"
                                              + "    +---------------------->\n"
                                              + "    [A   ][D][S   ][R]\n";
    private final int mID;
    /**
     * time parameter defining the time it takes for the set_amp to reach maximum level.
     */
    protected float mAttack = DEFAULT_ATTACK;
    /**
     * time parameter defining the time it takes to go from maximum to get_sustain level.
     */
    protected float mDecay = DEFAULT_DECAY;
    /**
     * level parameter defining the level hold while note is still played.
     */
    protected float mSustain = DEFAULT_SUSTAIN;
    /**
     * time parameter defining the time it takes for the set_amp to reach zero after note is off.
     */
    protected float mRelease = DEFAULT_RELEASE;

    public Instrument(int pID) {
        mID = pID;
    }

    public int ID() {
        return mID;
    }

    public void attack(float pAttack) {
        mAttack = pAttack;
    }

    public void decay(float pDecay) {
        mDecay = pDecay;
    }

    public void sustain(float pSustain) {
        mSustain = pSustain;
    }

    public void release(float pRelease) {
        mRelease = pRelease;
    }

    public float get_attack() {
        return mAttack;
    }

    public float get_decay() {
        return mDecay;
    }

    public float get_sustain() {
        return mSustain;
    }

    public float get_release() {
        return mRelease;
    }

    public abstract void osc_type(int pOsc);

    public abstract int get_osc_type();

    public abstract void lfo_amp(float pLFOAmp);

    public abstract float get_lfo_amp();

    public abstract void lfo_freq(float pLFOFreq);

    public abstract float get_lfo_freq();

    public abstract void filter_q(float f);

    public abstract float get_filter_q();

    public abstract void filter_freq(float f);

    public abstract float get_filter_freq();

    public abstract void pitch_bend(float freq_offset);

    public abstract void amplitude(float pAmp);

    public abstract float get_amplitude();

    public abstract void frequency(float freq);

    public abstract float get_frequency();

    public abstract void note_off();

    public abstract void note_on(int note, int velocity);

    // @TODO (move scheduled note_on/off to instrument)
    // public abstract void note_on(int note, int velocity, float duration);

    public abstract boolean isPlaying();

    protected float _note_to_frequency(int note) {
        return note_to_frequency(clamp127(note));
    }

    protected float _velocity_to_amplitude(int velocity) {
        return clamp127(velocity) / 127.0f;
    }
}
