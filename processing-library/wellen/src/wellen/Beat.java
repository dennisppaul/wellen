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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * continuously triggers a <code>beat(int)</code> event.
 */
public class Beat {

    private static final String METHOD_NAME = "beat";
    private static Beat fInstance = null;
    private int fBeat;
    private float fBPM;
    private final Object fListener;
    private final Method fMethod;
    private TimerTask fTask;
    private final Timer fTimer;
    private final ArrayList<Listener> fListeners;

    public Beat(Object beat_listener, int BPM) {
        this(beat_listener);
        set_bpm(BPM);
    }

    public Beat(Object beat_listener) {
        fListener = beat_listener;
        Method mMethod = null;
        if (fListener != null) {
            try {
                mMethod = beat_listener.getClass().getDeclaredMethod(METHOD_NAME, Integer.TYPE);
            } catch (NoSuchMethodException | SecurityException ex) {
                System.err.println("+++ @" + getClass().getSimpleName() + " / could not find `" + METHOD_NAME +
                                           "(int)`");
            }
        }
        fMethod = mMethod;
        fBeat = -1;
        fListeners = new ArrayList<>();
        fTimer = new Timer();
        fBPM = 0;
    }

    public ArrayList<Listener> listeners() {
        return fListeners;
    }

    public void add(Listener beat_trigger_listener) {
        listeners().add(beat_trigger_listener);
    }

    public boolean remove(Listener beat_trigger_listener) {
        return listeners().remove(beat_trigger_listener);
    }

    public void set_bpm(float BPM) {
        fBPM = BPM;
        final int mPeriod = (int) (60.0f / BPM * 1000.0f);
        if (fTask != null) {
            fTask.cancel();
        }
        fTask = new BeatTimerTaskP5();
        fTimer.scheduleAtFixedRate(fTask, 1000, mPeriod);
    }

    public float get_bpm() {
        return fBPM;
    }

    public int get_beat_count() {
        return fBeat;
    }

    public void clean_up() {
        fTimer.cancel();
        fTimer.purge();
        fTask.cancel();
    }

    public static Beat instance() {
        if (fInstance == null) {
            System.err.println("+++ no `Beat` instantiated or started.");
        }
        return fInstance;
    }

    public static Beat start(Object beat_listener) {
        fInstance = new Beat(beat_listener);
        return fInstance;
    }

    public static Beat start(Object beat_listener, int BPM) {
        fInstance = new Beat(beat_listener, BPM);
        return fInstance;
    }

    public static void stop() {
        if (fInstance != null) {
            fInstance.clean_up();
        }
    }

    private class BeatTimerTaskP5 extends TimerTask {

        @Override
        public void run() {
            fBeat++;
            if (fMethod != null) {
                try {
                    fMethod.invoke(fListener, fBeat);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
            for (Listener l : fListeners) {
                l.trigger(fBeat);
            }
        }
    }

    public interface Listener {
        void trigger(int beat_count);
    }
}
