package de.hfkbremen.ton;

public abstract class Instrument {

    public static final int SINE = 0;
    public static final int TRIANGLE = 1;
    public static final int SAWTOOTH = 2;
    public static final int SQUARE = 3;
    public static final int NOISE = 4;
    public static final int NUMBER_OF_OSCILLATORS = 5;
    public static final String ADSR_DIAGRAM = "    ^    /\\\n"
                                              + "    |   /  \\\n"
                                              + "    |  /    \\______\n"
                                              + "    | /            \\\n"
                                              + "    |/              \\\n"
                                              + "    +---------------------->\n"
                                              + "    [A   ][D][S   ][R]\n";
    protected final float DEFAULT_ATTACK = 0.001f;
    protected final float DEFAULT_DECAY = 0.0f;
    protected final float DEFAULT_SUSTAIN = 1.0f;
    protected final float DEFAULT_RELEASE = 0.1f;
    private final int mID;
    /**
     * time parameter defining the time it takes for the set_amp to reach maximum
     * level.
     */
    protected float mAttack = DEFAULT_ATTACK;
    /**
     * time parameter defining the time it takes to go from maximum to
     * get_sustain
     * level.
     */
    protected float mDecay = DEFAULT_DECAY;
    /**
     * level parameter defining the level hold while note is still played.
     */
    protected float mSustain = DEFAULT_SUSTAIN;
    /**
     * time parameter defining the time it takes for the set_amp to reach zero after
     * note is off.
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

    public abstract void noteOff();

    public abstract void noteOn(float pFreq, float pAmp);
}
