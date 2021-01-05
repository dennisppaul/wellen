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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * similar to {@link wellen.Beat} with the exception that events are triggered from {@link wellen.DSP}.
 */
public class BeatDSP implements DSPNodeInput {

    private static final String METHOD_NAME = "beat";
    private final Object mListener;
    private final int mSamplingRate;
    private Method mMethod = null;
    private int mBeat;
    private int mCounter;
    private float mInterval;

    public BeatDSP(Object pListener) {
        this(pListener, Wellen.DEFAULT_SAMPLING_RATE);
    }

    public BeatDSP(Object pListener, int pSamplingRate) {
        mListener = pListener;
        mSamplingRate = pSamplingRate;
        mBeat = -1;
        set_bpm(120);
        try {
            mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME, Integer.TYPE);
        } catch (NoSuchMethodException | SecurityException ex) {
            System.err.println("+++ @" + getClass().getSimpleName() + " / could not find `" + METHOD_NAME + "(int)`");
        }
    }

    public static BeatDSP start(Object pListener, int pSamplingRate) {
        return new BeatDSP(pListener);
    }

    public static BeatDSP start(Object pListener) {
        return new BeatDSP(pListener, Wellen.DEFAULT_SAMPLING_RATE);
    }

    public void set_bpm(float pBPM) {
        final float mPeriod = 60.0f / pBPM;
        mInterval = mSamplingRate * mPeriod;
    }

    public void tick() {
        input(0.0f);
    }

    public void input(float pSignal) {
        mCounter++;
        if (mCounter >= mInterval) {
            fireEvent();
            mCounter -= mInterval;
        }
    }

    private void fireEvent() {
        try {
            mBeat++;
            mMethod.invoke(mListener, mBeat);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }
}