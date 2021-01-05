package wellen;

/**
 * envelope with (A)ttack (D)ecay (S)ustain (R)elease stages: {@link wellen.ADSR}
 * <p>
 * it is usually used to control the amplitude of an *oscillator*.
 *
 * <pre><code>
 *                |----->|-->|   |-->|
 *                |---A--|-D-|-S-|-R-|
 *                |      /\
 *                |     /  \
 *                |    /    \_____
 *                |   /        ^  \
 *                |  /         |   \
 *                | /          |    \
 *                |/___________|_____\
 *                |
 *                |Press          |Release
 *  </code></pre>
 */
public class ADSR implements DSPNodeOutput {

    /*
     *       [ NODE_ADSR           ]
     *       +---------------------+
     *       |                     |
     * IN00--| SIGNAL       SIGNAL |--OUT00
     *       |                     |
     *       +---------------------+
     *
     *       @description(
     *
     *
     *          |----->|-->|   |-->|
     *          |---A--|-D-|-S-|-R-|
     *          |      /\
     *          |     /  \
     *          |    /    \_____
     *          |   /        ^  \
     *          |  /         |   \
     *          | /          |    \
     *          |/___________|_____\
     *          |
     *          |Press          |Release
     *       )
     *
     *
     */

    public static final String ADSR_DIAGRAM = "    ^    /\\\n" + "    |   /  \\\n" + "    |  /    \\______\n" + "    "
            + "| /            \\\n" + "    |/              \\\n" + "    +---------------------->\n" + "    [A   " +
            "][D][S " + "  ][R]\n";
    private static final boolean DEBUG_ADSR = false;
    private final int mSamplingRate;
    private final float FADE_TO_ZERO_RATE_SEC;
    private final boolean USE_FADE_TO_ZERO_STATE = false;
    private float mAttack = Wellen.DEFAULT_ATTACK;
    private float mDecay = Wellen.DEFAULT_DECAY;
    private float mSustain = Wellen.DEFAULT_SUSTAIN;
    private float mRelease = Wellen.DEFAULT_RELEASE;
    private ENVELOPE_STATE mState;
    private float mAmp = 0.0f;
    private float mDelta = 0.0f;

    private enum ENVELOPE_STATE {
        IDLE,
        ATTACK,
        DECAY,
        SUSTAIN,
        RELEASE,
        PRE_ATTACK_FADE_TO_ZERO
    }

    public ADSR(int pSamplingRate) {
        mSamplingRate = pSamplingRate;
        FADE_TO_ZERO_RATE_SEC = 0.01f;
        setState(ENVELOPE_STATE.IDLE);
    }

    public ADSR() {
        this(Wellen.DEFAULT_SAMPLING_RATE);
    }

    @Override
    public float output() {
        step();
        return mAmp;
    }

    public void start() {
        check_scheduled_attack_state();
    }

    public void stop() {
        check_scheduled_release_state();
    }

    public float get_attack() {
        return mAttack;
    }

    public void set_attack(float pAttack) {
        mAttack = pAttack;
    }

    public void set_adsr(float pAttack, float pDecay, float pSustain, float pRelease) {
        set_attack(pAttack);
        set_decay(pDecay);
        set_sustain(pSustain);
        set_release(pRelease);
    }

    public float get_decay() {
        return mDecay;
    }

    public void set_decay(float pDecay) {
        mDecay = pDecay;
    }

    public float get_sustain() {
        return mSustain;
    }

    public void set_sustain(float pSustain) {
        mSustain = pSustain;
    }

    public float get_release() {
        return mRelease;
    }

    public void set_release(float pRelease) {
        mRelease = pRelease;
    }

    float compute_delta_fraction(float pDelta, float pDuration) {
        return pDuration > 0 ? (pDelta / mSamplingRate) / pDuration : pDelta;
    }

    private void check_scheduled_attack_state() {
        if (mAmp > 0.0f) {
            if (USE_FADE_TO_ZERO_STATE) {
                if (mState != ENVELOPE_STATE.PRE_ATTACK_FADE_TO_ZERO) {
                    mDelta = compute_delta_fraction(-mAmp, FADE_TO_ZERO_RATE_SEC);
                    setState(ENVELOPE_STATE.PRE_ATTACK_FADE_TO_ZERO);
                }
            } else {
                mDelta = compute_delta_fraction(1.0f, mAttack);
                setState(ENVELOPE_STATE.ATTACK);
            }
        } else {
            mDelta = compute_delta_fraction(1.0f, mAttack);
            setState(ENVELOPE_STATE.ATTACK);
        }
    }

    private void check_scheduled_release_state() {
        if (mState != ENVELOPE_STATE.RELEASE) {
            mDelta = compute_delta_fraction(-mAmp, mRelease);
            setState(ENVELOPE_STATE.RELEASE);
        }
    }

    private void setState(ENVELOPE_STATE pState) {
        if (DEBUG_ADSR) {
            System.out.print(pState.name());
            System.out.print(" : ");
            System.out.println(mAmp);
        }
        mState = pState;
    }

    private void step() {
        switch (mState) {
            case IDLE:
            case SUSTAIN:
                break;
            case ATTACK:
                // increase amp to sustain_level in ATTACK sec
                mAmp += mDelta;
                if (mAmp >= 1.0f) {
                    mAmp = 1.0f;
                    mDelta = compute_delta_fraction(-(1.0f - mSustain), mDecay);
                    setState(ENVELOPE_STATE.DECAY);
                }
                break;
            case DECAY:
                // decrease amp to sustain_level in DECAY sec
                mAmp += mDelta;
                if (mAmp <= mSustain) {
                    mAmp = mSustain;
                    setState(ENVELOPE_STATE.SUSTAIN);
                }
                break;
            case RELEASE:
                // decrease amp to 0.0 in RELEASE sec
                mAmp += mDelta;
                if (mAmp <= 0.0f) {
                    mAmp = 0.0f;
                    setState(ENVELOPE_STATE.IDLE);
                }
                break;
            case PRE_ATTACK_FADE_TO_ZERO:
                mAmp += mDelta;
                if (mAmp <= 0.0f) {
                    mAmp = 0.0f;
                    mDelta = compute_delta_fraction(1.0f, mAttack);
                    setState(ENVELOPE_STATE.ATTACK);
                }
                break;
        }
    }
}