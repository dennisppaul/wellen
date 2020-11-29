package de.hfkbremen.ton;

import static de.hfkbremen.ton.Ton.DEFAULT_ATTACK;
import static de.hfkbremen.ton.Ton.DEFAULT_DECAY;
import static de.hfkbremen.ton.Ton.DEFAULT_RELEASE;
import static de.hfkbremen.ton.Ton.DEFAULT_SAMPLING_RATE;
import static de.hfkbremen.ton.Ton.DEFAULT_SUSTAIN;

//@TODO(when re-triggering attack stage while sustain/release is still running causes *click*)
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
     *          the ADSR node provides a envelope with four different stages:
     *          (A)ttack (D)ecay (S)ustain (R)elease. it is usually used to
     *          control the amplitude of an *oscillator*.
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

    private final int mSamplingRate;
    private float mTrigger = 0.0f;
    private float mAttack = DEFAULT_ATTACK;
    private float mDecay = DEFAULT_DECAY;
    private float mSustain = DEFAULT_SUSTAIN;
    private float mRelease = DEFAULT_RELEASE;
    private ENVELOPE_STATE mState = ENVELOPE_STATE.IDLE;
    private float mAmp = 0.0f;
    private float mDelta = 0.0f;

    public ADSR(int pSamplingRate) {
        mSamplingRate = pSamplingRate;
    }

    public ADSR() {
        this(DEFAULT_SAMPLING_RATE);
    }

    @Override
    public float output() {
        step_float();
        return mAmp;
    }

    public void trigger(float pTrigger) {
        mTrigger = pTrigger;
    }

    public void start() {
        trigger(1);
        trigger_attack();
    }

    public void stop() {
        trigger(0);
        trigger_release();
    }

    public float get_attack() { return mAttack; }

    public void set_attack(float pAttack) { mAttack = pAttack; }

    public float get_decay() { return mDecay; }

    public void set_decay(float pDecay) { mDecay = pDecay; }

    public float get_sustain() { return mSustain; }

    public void set_sustain(float pSustain) { mSustain = pSustain; }

    public float get_release() { return mRelease; }

    public void set_release(float pRelease) { mRelease = pRelease; }

    float compute_delta_fraction(float pDelta, float pDuration) {
        return pDuration > 0 ? (pDelta / mSamplingRate) / pDuration : pDelta;
    }

    void trigger_attack() {
        if (mTrigger > 0.0f) {
            if (mAmp > 0.0f) {
                mDelta = 0.0f;
                mState = ENVELOPE_STATE.FADE_TO_ZERO;
            } else {
                mDelta = compute_delta_fraction(1.0f, mAttack);
                mState = ENVELOPE_STATE.ATTACK;
            }
        }
    }

    void trigger_release() {
        if (mTrigger <= 0.0f) {
            mDelta = compute_delta_fraction(-mAmp, mRelease);
            mState = ENVELOPE_STATE.RELEASE;
        }
    }

    private void step_float() {
        switch (mState) {
            case IDLE:
                trigger_attack();
                break;
            case ATTACK:
                // increase amp to sustain_level in ATTACK sec
                mAmp += mDelta;
                if (mAmp >= 1.0f) {
                    mAmp = 1.0f;
                    mDelta = compute_delta_fraction(-(1.0f - mSustain), mDecay);
                    mState = ENVELOPE_STATE.DECAY;
                }
                trigger_release();
                break;
            case DECAY:
                // decrease amp to sustain_level in DECAY sec
                mAmp += mDelta;
                if (mAmp <= mSustain) {
                    mAmp = mSustain;
                    mState = ENVELOPE_STATE.SUSTAIN;
                }
                trigger_release();
                break;
            case SUSTAIN:
                trigger_release();
                break;
            case RELEASE:
                // decrease amp to 0.0 in RELEASE sec
                mAmp += mDelta;
                if (mAmp <= 0.0f) {
                    mAmp = 0.0f;
                    mState = ENVELOPE_STATE.IDLE;
                }
                trigger_attack();
                break;
            case FADE_TO_ZERO:
                final float FADE_TO_ZERO_RATE_SEC = 0.01f;
                mAmp -= 1.0f / (mSamplingRate * FADE_TO_ZERO_RATE_SEC);
                if (mAmp <= 0.0f) {
                    mAmp = 0.0f;
                    mDelta = compute_delta_fraction(1.0f, mAttack);
                    mState = ENVELOPE_STATE.ATTACK;
                }
                break;
        }
    }

    private enum ENVELOPE_STATE {
        IDLE, ATTACK, DECAY, SUSTAIN, RELEASE, FADE_TO_ZERO
    }
}