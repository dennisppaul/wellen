/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2022 Dennis P Paul.
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

package wellen.dsp;

import wellen.Wellen;

/**
 * envelope with (A)ttack (D)ecay (S)ustain (R)elease stages: {@link ADSR}
 * <p>
 * it is usually used to control the amplitude of an *oscillator*.
 * <pre><code>
 *                |-----›|--›|   |--›|
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

    /**
     * ASCII diagram of an ADSR envelope.
     */
    public static final String ADSR_DIAGRAM = "    ^    /\\\n" + "    |   /  \\\n" + "    |  /    \\______\n" + "    "
            + "| /            \\\n" + "    |/              \\\n" + "    +---------------------->\n" + "    [A   " +
            "][D][S " + "  ][R]\n";

    private enum ENVELOPE_STATE {
        IDLE,
        ATTACK,
        DECAY,
        SUSTAIN,
        RELEASE,
        PRE_ATTACK_FADE_TO_ZERO
    }

    private static final boolean DEBUG_ADSR = false;
    private final float FADE_TO_ZERO_RATE_SEC;
    private final boolean USE_FADE_TO_ZERO_STATE = false;
    private float mAmp = 0.0f;
    private float mAttack = Wellen.DEFAULT_ATTACK;
    private float mDecay = Wellen.DEFAULT_DECAY;
    private float mDelta = 0.0f;
    private float mRelease = Wellen.DEFAULT_RELEASE;
    private final int mSamplingRate;
    private ENVELOPE_STATE mState;
    private float mSustain = Wellen.DEFAULT_SUSTAIN;

    /**
     * @param pSamplingRate sampling rate in Hz.
     */
    public ADSR(int pSamplingRate) {
        mSamplingRate = pSamplingRate;
        FADE_TO_ZERO_RATE_SEC = 0.01f;
        setState(ENVELOPE_STATE.IDLE);
    }

    /**
     *
     */
    public ADSR() {
        this(Wellen.DEFAULT_SAMPLING_RATE);
    }

    /**
     * @return current envelope value
     */
    @Override
    public float output() {
        step();
        return mAmp;
    }

    /**
     *
     */
    public void start() {
        check_scheduled_attack_state();
    }

    /**
     *
     */
    public void stop() {
        check_scheduled_release_state();
    }

    /**
     * @return attack value in seconds
     */
    public float get_attack() {
        return mAttack;
    }

    /**
     * @param pAttack attack value in seconds
     */
    public void set_attack(float pAttack) {
        mAttack = pAttack;
    }

    /**
     * @param pAttack  attack value in seconds
     * @param pDecay   decay value in seconds
     * @param pSustain sustain value in seconds
     * @param pRelease release value in seconds
     */
    public void set_adsr(float pAttack, float pDecay, float pSustain, float pRelease) {
        set_attack(pAttack);
        set_decay(pDecay);
        set_sustain(pSustain);
        set_release(pRelease);
    }

    /**
     * @return decay value in seconds
     */
    public float get_decay() {
        return mDecay;
    }

    /**
     * @param pDecay decay value in seconds
     */
    public void set_decay(float pDecay) {
        mDecay = pDecay;
    }

    /**
     * @return sustain value in seconds
     */
    public float get_sustain() {
        return mSustain;
    }

    /**
     * @param pSustain sustain value in seconds
     */
    public void set_sustain(float pSustain) {
        mSustain = pSustain;
    }

    /**
     * @return release value in seconds
     */
    public float get_release() {
        return mRelease;
    }

    /**
     * @param pRelease release value in seconds
     */
    public void set_release(float pRelease) {
        mRelease = pRelease;
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

    private float compute_delta_fraction(float pDelta, float pDuration) {
        return pDuration > 0 ? (pDelta / mSamplingRate) / pDuration : pDelta;
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