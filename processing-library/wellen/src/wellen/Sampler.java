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

package wellen;

import processing.core.PApplet;

/**
 * plays back an array of samples at different speeds.
 */
public class Sampler implements DSPNodeOutput {

    public static final int NO_LOOP_POINT = -1;
    private boolean mIsPlaying;
    private final float mSamplingRate;
    private float[] mData;
    private float mFrequency;
    private float mStepSize;
    private float mDataIndex;
    private float mAmplitude;
    private boolean mLoop;
    private boolean mDirectionForward;
    private float mSpeed;
    private boolean mInterpolateSamples;
    private int mIn;
    private int mOut;
    private int mLoopIn;
    private int mLoopOut;
    private int mEdgeFadePadding;

    public Sampler() {
        this(0);
    }

    public Sampler(int pWavetableSize) {
        this(new float[pWavetableSize], Wellen.DEFAULT_SAMPLING_RATE);
    }

    public Sampler(float[] pWavetable) {
        this(pWavetable, Wellen.DEFAULT_SAMPLING_RATE);
    }

    public Sampler(float[] pWavetable, float pSamplingRate) {
        mData = pWavetable;
        mSamplingRate = pSamplingRate;
        mDataIndex = 0;
        mLoop = false;
        mInterpolateSamples = false;
        mEdgeFadePadding = 0;
        mIn = 0;
        mOut = 0;
        mLoopIn = NO_LOOP_POINT;
        mLoopOut = NO_LOOP_POINT;
        mIsPlaying = false;
        set_speed(1.0f);
        set_amplitude(1.0f);
        set_in(0);
        set_out(mData.length - 1);
    }


    /**
     * load the sample buffer from *raw* byte data. the method assumes a raw format with 32bit float in a value range
     * from -1.0 to 1.0. from -1.0 to 1.0.
     *
     * @param pData raw byte data ( assuming 4 bytes per sample, 32-bit float aka WAVE_FORMAT_IEEE_FLOAT_32BIT )
     * @return instance with data loaded
     */
    public Sampler load(byte[] pData) {
        load(pData, true);
        return this;
    }

    /**
     * load the sample buffer from *raw* byte data. the method assumes a raw format with 32bit float in a value range
     * from -1.0 to 1.0.
     *
     * @param pData         raw byte data ( assuming 4 bytes per sample, 32-bit float aka WAVE_FORMAT_IEEE_FLOAT_32BIT
     *                      )
     * @param pLittleEndian true if byte data is arranged in little endian order
     * @return instance with data loaded
     */
    public Sampler load(byte[] pData, boolean pLittleEndian) {
        if (mData == null || mData.length != pData.length / 4) {
            mData = new float[pData.length / 4];
        }
        Wellen.bytes_to_floatIEEEs(pData, data(), pLittleEndian);
        rewind();
        set_speed(mSpeed);
        set_in(0);
        set_out(mData.length - 1);
        return this;
    }

    public int get_in() {
        return mIn;
    }

    public void set_in(int pIn) {
        if (pIn > mOut) {
            pIn = mOut;
        }
        mIn = pIn;
    }

    public int get_out() {
        return mOut;
    }

    public void set_out(int pOut) {
        mOut = pOut > last_index() ? last_index() : (pOut < mIn ? mIn : pOut);
    }

    public float get_speed() {
        return mSpeed;
    }

    public void set_speed(float pSpeed) {
        mSpeed = pSpeed;
        mDirectionForward = pSpeed > 0;
        set_frequency(PApplet.abs(pSpeed) * mSamplingRate / data().length); /* aka `mStepSize = pSpeed;` */
    }

    public void set_frequency(float pFrequency) {
        if (mFrequency != pFrequency) {
            mFrequency = pFrequency;
            mStepSize = mFrequency * ((float) mData.length / mSamplingRate);
        }
    }

    public void set_amplitude(float pAmplitude) {
        mAmplitude = pAmplitude;
    }

    public float[] data() {
        return mData;
    }

    public void set_data(float[] pData) {
        mData = pData;
        rewind();
        set_speed(mSpeed);
        set_in(0);
        set_out(mData.length - 1);
    }

    public void interpolate_samples(boolean pInterpolateSamples) {
        mInterpolateSamples = pInterpolateSamples;
    }

    public int get_position() {
        return (int) mDataIndex;
    }

    public float output() {
        float mSample;
        mDataIndex += mDirectionForward ? mStepSize : -mStepSize;
        final int mPreviousIndex = (int) mDataIndex;
        if (mData.length == 0 || mDirectionForward ? (mPreviousIndex > mOut && !mLoop) :
            (mPreviousIndex < mIn && !mLoop)) {
            return 0.0f;
        }
        final float mFrac = mDataIndex - mPreviousIndex;
        final int mCurrentIndex = wrapIndex(mPreviousIndex);
        mDataIndex = mCurrentIndex + mFrac;

        /* interpolate */
        mSample = mData[mCurrentIndex];
        if (mInterpolateSamples) {
            final int mNextIndex = wrapIndex(mCurrentIndex + 1);
            final float mNextSample = mData[mNextIndex];
            mSample = mSample * (1.0f - mFrac) + mNextSample * mFrac;
        }
        mSample *= mAmplitude;

        /* fade edges */
        if (mEdgeFadePadding > 0) {
            final int mRelativeIndex = mData.length - mCurrentIndex;
            if (mCurrentIndex < mEdgeFadePadding) {
                final float mFadeInAmount = (float) mCurrentIndex / mEdgeFadePadding;
                mSample *= mFadeInAmount;
            } else if (mRelativeIndex < mEdgeFadePadding) {
                final float mFadeOutAmount = (float) mRelativeIndex / mEdgeFadePadding;
                mSample *= mFadeOutAmount;
            }
        }
        return mSample;
    }

    public int get_edge_fading() {
        return mEdgeFadePadding;
    }

    public void set_edge_fading(int pEdgeFadePadding) {
        mEdgeFadePadding = pEdgeFadePadding;
    }

    public void rewind() {
        mDataIndex = mDirectionForward ? mIn : mOut;
    }


    public void forward() {
        mDataIndex = mDirectionForward ? mOut : mIn;
    }

    public void loop(boolean pLoop) {
        enable_loop(pLoop);
    }

    public boolean is_looping() {
        return mLoop;
    }

    public void enable_loop(boolean pLoop) {
        mLoop = pLoop;
    }


    private int last_index() {
        return mData.length - 1;
    }

    private int wrapIndex(int i) {
        if (mIsPlaying && mLoopIn != NO_LOOP_POINT && mLoopOut != NO_LOOP_POINT) {
            if (mDirectionForward) {
                if (i > mLoopOut) {
                    i = mLoopIn;
                }
            } else {
                if (i < mLoopIn) {
                    i = mLoopOut;
                }
            }
        } else {
            if (i > mOut) {
                i = mIn;
            } else if (i < mIn) {
                i = mOut;
            }
        }
        return i;
    }

    public void start() {
        mIsPlaying = true;
    }

    public void stop() {
        mIsPlaying = false;
    }

    public int get_loop_in() {
        return mLoopIn;
    }

    public void set_loop_in(int pLoopIn) {
        mLoopIn = pLoopIn;
    }

    public int get_loop_out() {
        return mLoopOut;
    }

    public void set_loop_out(int pLoopOut) {
        mLoopOut = pLoopOut;
    }
}
