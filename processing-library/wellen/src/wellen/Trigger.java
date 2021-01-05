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
 * generates an event from an oscillating input signal.
 */
public class Trigger implements DSPNodeInput {

    private static final String METHOD_NAME = "trigger";
    private final Object mListener;
    private float mPreviousSignal = 0.0f;
    private Method mMethod = null;
    private boolean mEnableRisingEdge = true;
    private boolean mEnableFallingEdge = true;

    public Trigger(Object pListener) {
        mListener = pListener;
        try {
            mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME);
        } catch (NoSuchMethodException | SecurityException ex) {
            System.err.println("+++ @" + getClass().getSimpleName() + " / could not find `" + METHOD_NAME + "`");
        }
    }

    public static Trigger start(Object pListener) {
        return new Trigger(pListener);
    }

    public void trigger_rising_edge(boolean pEnableRisingEdge) {
        mEnableRisingEdge = pEnableRisingEdge;
    }

    public void trigger_falling_edge(boolean pEnableFallingEdge) {
        mEnableFallingEdge = pEnableFallingEdge;
    }

    public void input(float pSignal) {
        if (mEnableRisingEdge && mPreviousSignal <= 0 && pSignal > 0) {
            fireEvent();
        }
        if (mEnableFallingEdge && mPreviousSignal >= 0 && pSignal < 0) {
            fireEvent();
        }
        mPreviousSignal = pSignal;
    }

    private void fireEvent() {
        try {
            mMethod.invoke(mListener);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }
}
