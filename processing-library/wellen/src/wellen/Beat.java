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
import java.util.Timer;
import java.util.TimerTask;

/**
 * continuously triggers a <code>beat(int)</code> event.
 */
public class Beat {

    private static final String METHOD_NAME = "beat";
    private final Object mListener;
    private final Timer mTimer;
    private int mBeat;
    private Method mMethod = null;
    private TimerTask mTask;

    public Beat(Object pListener, int pBPM) {
        this(pListener);
        set_bpm(pBPM);
    }

    public Beat(Object pListener) {
        mListener = pListener;
        mBeat = -1;
        try {
            mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME, Integer.TYPE);
        } catch (NoSuchMethodException | SecurityException ex) {
            System.err.println("+++ @" + getClass().getSimpleName() + " / could not find `" + METHOD_NAME + "(int)`");
        }
        mTimer = new Timer();
    }

    public static Beat start(Object pListener, int pBPM) {
        return new Beat(pListener, pBPM);
    }

    public static Beat start(Object pListener) {
        return new Beat(pListener);
    }

    public void set_bpm(float pBPM) {
        final int mPeriod = (int) (60.0f / pBPM * 1000.0f);
        if (mTask != null) {
            mTask.cancel();
        }
        mTask = new BeatTimerTaskP5();
        mTimer.scheduleAtFixedRate(mTask, 1000, mPeriod);
    }

    public int get_beat_count() {
        return mBeat;
    }

    private class BeatTimerTaskP5 extends TimerTask {

        @Override
        public void run() {
            try {
                mBeat++;
                mMethod.invoke(mListener, mBeat);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
    }
}
