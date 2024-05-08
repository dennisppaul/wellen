/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2024 Dennis P Paul.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * generates an event from an oscillating input signal.
 */
public class Trigger implements DSPNodeInput {

    public static final int EVENT_FALLING_EDGE = -1;
    public static final int EVENT_RISING_EDGE = 1;
    private static final String METHOD_NAME = "trigger";
    private boolean mEnableFallingEdge = true;
    private boolean mEnableRisingEdge = true;
    private final Object mListener;
    private final ArrayList<Listener> mListeners;
    private Method mMethod = null;
    private float mPreviousSignal = 0.0f;

    public Trigger() {
        this(null);
    }

    public Trigger(Object pListener) {
        mListener = pListener;
        if (mListener != null) {
            try {
                mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME);
            } catch (NoSuchMethodException | SecurityException ex) {
                System.err.println("+++ @" + getClass().getSimpleName() + " / could not find `" + METHOD_NAME + "`");
            }
        }
        mListeners = new ArrayList<>();
    }

    public static Trigger start(Object pListener) {
        return new Trigger(pListener);
    }

    public ArrayList<Listener> listeners() {
        return mListeners;
    }

    public void add(Listener pTriggerListener) {
        listeners().add(pTriggerListener);
    }

    public boolean remove(Listener pTriggerListener) {
        return listeners().remove(pTriggerListener);
    }

    public void trigger_rising_edge(boolean pEnableRisingEdge) {
        mEnableRisingEdge = pEnableRisingEdge;
    }

    public void trigger_falling_edge(boolean pEnableFallingEdge) {
        mEnableFallingEdge = pEnableFallingEdge;
    }

    public void input(float signal) {
        if (mEnableRisingEdge && (mPreviousSignal <= 0 && signal > 0)) {
            fireEvent(EVENT_RISING_EDGE);
        }
        if (mEnableFallingEdge && (mPreviousSignal >= 0 && signal < 0)) {
            fireEvent(EVENT_FALLING_EDGE);
        }
        mPreviousSignal = signal;
    }

    private void fireEvent(int pEventType) {
        if (mListener != null) {
            try {
                mMethod.invoke(mListener);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
        for (Listener l : mListeners) {
            l.trigger(pEventType);
        }
    }
    public interface Listener {
        void trigger(int pEventType);
    }
}
