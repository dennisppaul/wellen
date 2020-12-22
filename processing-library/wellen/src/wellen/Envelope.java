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

import java.util.ArrayList;

/**
 * envelope with multiple stages. the envelope is configured with value and duration defined in <code>Stage</code>. once
 * started the envelope interpolates linearly from current stage value to next stage value in current stage’s duration.
 * by definition the duration of the last stage is ignored.
 * <p>
 * a typical application of an envelope could look as follows:
 * <pre>
 * <code>
 *     Envelope mEnvelope = new Envelope();
 *     mEnvelope.add_stage(0.0f, 2.0f);
 *     mEnvelope.add_stage(1.0f, 5.0f);
 *     mEnvelope.add_stage(0.0f);
 * </code>
 * </pre>
 * the code above would read as follows:
 * <ul>
 * <li>interpolate from <code>0.0</code> ( value of 1st stage ) to <code>1.0</code> ( value of 2nd stage ) in
 * <code>2.0</code> seconds ( duration of 1st stage )
 * <li>interpolate from <code>1.0</code> to <code>0.0</code> in <code>5.0</code> seconds
 * <li>envelope is done
 * </ul>
 * <p>
 * the following example creates a simple ramp:
 * <pre>
 * <code>
 *     Envelope mEnvelope = new Envelope();
 *     mEnvelope.add_stage(0.0f, 2.0f);
 *     mEnvelope.add_stage(1.0f);
 * </code>
 * </pre>
 * the code above would read as follows:
 * <ul>
 * <li>interpolate from <code>0.0</code> to <code>1.0</code> in <code>2.0</code> seconds
 * <li>envelope is done
 * </ul>
 */
public class Envelope implements DSPNodeOutput {

    /*
     *
     *          Stages(S)
     *
     *          |S0-----|S1--|S2-|S3--|S4---
     *          |      /\
     *          |     /  \      /\
     *          |    /    \    /  \
     *          |   /      \  /    \
     *          |  /        \/      \
     *          | /                  \______
     *          |/__________________________
     *          |0>>>>>>>1>>>>0.3>0.7>>0.1>>
     *       )
     *
     */

    private int mEnvStage = 0;
    private float mValue = 0.0f;
    private float mDelta = 0.0f;
    private float mTimeScale = 1.0f;
    private float mStageDuration = 0.0f;
    private boolean mEnvelopeDone = true;
    private boolean mLoop = false;
    private final ArrayList<Stage> mEnvelopeStages;
    private final ArrayList<EnvelopeListener> mEnvelopeListeners;
    private final float mSamplingRate;

    public Envelope(int pSamplingRate) {
        mSamplingRate = pSamplingRate;
        mEnvelopeStages = new ArrayList<>();
        mEnvelopeListeners = new ArrayList<>();
    }

    public Envelope() {
        this(Wellen.DEFAULT_SAMPLING_RATE);
    }

    /**
     * enable looping of envelope. once the last stage has completed the envelope is reset to the first stage and
     * repeats.
     *
     * @param pLoop flag to enable looping.
     */
    public void enable_loop(boolean pLoop) {
        mLoop = pLoop;
    }

    /**
     * advance envelope on step
     *
     * @return current value
     */
    @Override
    public float output() {
        if (!mEnvelopeDone) {
            if (mEnvStage < mEnvelopeStages.size()) {
                mValue += mTimeScale * mDelta;
                mStageDuration += mTimeScale * 1.0f / mSamplingRate;
                if (mStageDuration > mEnvelopeStages.get(mEnvStage).duration) {
                    final float mRemainder = mStageDuration - mEnvelopeStages.get(mEnvStage).duration;
                    finished_stage(mEnvStage);
                    mEnvStage++;
                    if (mEnvStage < mEnvelopeStages.size() - 1) {
                        prepareNextStage(mEnvStage, mRemainder);
                    } else {
                        // @TODO(is it of interest to be able to loop envelopes?)
                        stop();
                        finished_envelope();
                    }
                }
            }
        }
        return mValue;
    }

    /**
     * clears all current stages from the envelope and creates a ramp from start to end value in specified duration.
     *
     * @param pStartValue start value of ramp
     * @param pEndValue   end value of ramp
     * @param pDuration   duration of ramp
     */
    public void ramp(float pStartValue, float pEndValue, float pDuration) {
        mEnvelopeStages.clear();
        add_stage(pStartValue, pDuration);
        add_stage(pEndValue);
    }

    /**
     * clears all current stages from envelope and creates a ramp from the current value to specified value in specified
     * duration.
     *
     * @param pValue    end value of ramp
     * @param pDuration duration of ramp
     */
    public void ramp_to(float pValue, float pDuration) {
        mEnvelopeStages.clear();
        add_stage(mValue, pDuration);
        add_stage(pValue);
    }

    public ArrayList<Stage> stages() {
        return mEnvelopeStages;
    }

    public void add_stage(float pValue, float pDuration) {
        mEnvelopeStages.add(new Stage(pValue, pDuration));
    }

    public void add_stage(float pValue) {
        mEnvelopeStages.add(new Stage(pValue, 0.0f));
    }

    public void clear_stages() {
        mEnvelopeStages.clear();
    }

    public void start() {
        mEnvelopeDone = false;
        if (!mEnvelopeStages.isEmpty()) {
            prepareNextStage(0, 0.0f);
        }
        mStageDuration = 0.0f;
    }

    public void stop() {
        mEnvelopeDone = true;
    }

    public float get_time_scale() {
        return mTimeScale;
    }

    public void set_time_scale(float pTimeScale) {
        mTimeScale = pTimeScale;
    }

    public float get_current_value() {
        return mValue;
    }

    public void set_current_value(float pValue) {
        mValue = pValue;
    }

    public void add_listener(EnvelopeListener pEnvelopeListener) {
        mEnvelopeListeners.add(pEnvelopeListener);
    }

    public boolean remove_listener(EnvelopeListener pEnvelopeListener) {
        return mEnvelopeListeners.remove(pEnvelopeListener);
    }

    public void clear_listeners() {
        mEnvelopeListeners.clear();
    }

    public ArrayList<EnvelopeListener> get_listeners() {
        return mEnvelopeListeners;
    }

    private float compute_delta_fraction(float pDelta, float pDuration) {
        return pDuration > 0 ? (pDelta / mSamplingRate) / pDuration : pDelta;
    }

    private void finished_envelope() {
        for (EnvelopeListener el : mEnvelopeListeners) {
            el.finished_envelope(this);
        }
        if (mLoop) {
            start();
        }
    }

    private void finished_stage(int mEnvStage) {
        for (EnvelopeListener el : mEnvelopeListeners) {
            el.finished_stage(this, mEnvStage);
        }
    }

    private void prepareNextStage(int pEnvStage, float pFraction) {
        mEnvStage = pEnvStage;
        mStageDuration = 0.0f;
        // @TODO maybe keep fractional part.
        // @TODO but take care to also factor in the fraction when computing the delta in `setDelta`
        mValue = mEnvelopeStages.get(mEnvStage).value;
        if (mEnvelopeStages.size() > 1) {
            setDelta(mEnvStage);
        }
    }

    private void setDelta(int pEnvStage) {
        final float mDeltaTMP = mEnvelopeStages.get(pEnvStage + 1).value - mEnvelopeStages.get(pEnvStage).value;
        mDelta = compute_delta_fraction(mDeltaTMP, mEnvelopeStages.get(mEnvStage).duration);
    }

    public static class Stage {

        /**
         * stage duration in seconds
         */
        public float duration;
        /**
         * stage starting value
         */
        public float value;

        Stage(float pValue, float pDuration) {
            duration = pDuration;
            value = pValue;
        }

        Stage(float pValue) {
            this(pValue, 0.0f);
        }

        Stage() {
            this(0.0f, 0.0f);
        }
    }
}
