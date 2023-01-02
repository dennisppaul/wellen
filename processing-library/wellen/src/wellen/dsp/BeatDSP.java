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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * similar to {@link wellen.Beat} with the exception that events are triggered from {@link DSP}.
 */
public class BeatDSP implements DSPNodeInput {

    private static final String METHOD_NAME = "beat";
    private int fBeat;
    private final Object fListener;
    private final ArrayList<Trigger.Listener> fListeners;
    private final Method fMethod;
    private final int fSamplingRate;
    private int fTickCounter;
    private float fTickInterval;

    public BeatDSP(Object pListener, int pSamplingRate) {
        Method mMethod;
        if (pListener != null) {
            try {
                mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME, Integer.TYPE);
            } catch (NoSuchMethodException | SecurityException ex) {
                mMethod = null;
                System.err.println("+++ @" + getClass().getSimpleName() + " / could not find `" + METHOD_NAME +
                                           "(int)`");
            }
        } else {
            mMethod = null;
        }
        fMethod = mMethod;
        fListener = pListener;
        fSamplingRate = pSamplingRate;
        fBeat = -1;
        fListeners = new ArrayList<>();
        set_bpm(120);
    }

    public BeatDSP(Object pListener) {
        this(pListener, Wellen.DEFAULT_SAMPLING_RATE);
    }

    public BeatDSP() {
        this(null, Wellen.DEFAULT_SAMPLING_RATE);
    }

    public static BeatDSP start(Object pListener, int pSamplingRate) {
        return new BeatDSP(pListener);
    }

    public static BeatDSP start(Object pListener) {
        return new BeatDSP(pListener, Wellen.DEFAULT_SAMPLING_RATE);
    }

    public ArrayList<Trigger.Listener> listeners() {
        return fListeners;
    }

    public void add(Trigger.Listener pTriggerListener) {
        listeners().add(pTriggerListener);
    }

    public boolean remove(Trigger.Listener pTriggerListener) {
        return listeners().remove(pTriggerListener);
    }

    public void set_bpm(float pBPM) {
        final float mPeriod = 60.0f / pBPM;
        fTickInterval = fSamplingRate * mPeriod;
    }

    /**
     * sets the interval between beat events in samples
     *
     * @param interval in samples
     */
    public void set_interval(float interval) {
        fTickInterval = interval;
    }

    /**
     * sets the interval between beat events in seconds
     *
     * @param interval in seconds
     */
    public void set_interval_sec(float interval) {
        fTickInterval = fSamplingRate * interval;
    }

    public int get_beat_count() {
        return fBeat;
    }

    public void tick() {
        input(0.0f);
    }

    public void input(float signal) {
        fTickCounter++;
        if (fTickCounter >= fTickInterval) {
            fireEvent();
            fTickCounter -= fTickInterval;
        }
    }

    private void fireEvent() {
        fBeat++;
        if (fMethod != null) {
            try {
                fMethod.invoke(fListener, fBeat);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
        for (Trigger.Listener l : fListeners) {
            l.trigger(fBeat);
        }
    }
    public interface Listener {
        public abstract void trigger(int beat_count);
    }
}