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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * similar to {@link wellen.Beat} except that it handles multiple listeners.
 */
public class BeatEvent {

    private int fBeat = -1;
    private boolean fFlagged = false;
    private BeatPool fInstance;
    private final ArrayList<BeatListener> fListeners;
    private TimerTask fTask;
    private final Timer fTimer;

    private BeatEvent(int BPM) {
        this();
        set_bpm(BPM);
    }

    private BeatEvent() {
        fListeners = new ArrayList<>();
        fTimer = new Timer();
    }

    public static BeatEvent create(int BPM) {
        BeatEvent mBeatEvent = new BeatEvent(BPM);
        mBeatEvent.instance().fPool.add(mBeatEvent);
        return mBeatEvent;
    }

    public void stop() {
        instance().end();
    }

    /**
     * add {@link wellen.BeatListener}
     *
     * @param listener object that listens to beat events
     */
    public void add(BeatListener listener) {
        fListeners.add(listener);
    }

    /**
     * removes all {@link wellen.BeatListener}
     */
    public void clear() {
        fListeners.clear();
    }

    public int current_beat_count() {
        return fBeat;
    }

    public void set_beat_count(int beat_counter) {
        fBeat = beat_counter;
    }

    /**
     * reset beat counter
     */
    public void reset() {
        fBeat = 0;
    }

    public void set_bpm(float BPM) {
        final int mPeriod = (int) (60.0f / BPM * 1000.0f);
        if (fTask != null) {
            fTask.cancel();
        }
        fTask = new BeatTimerTask();
        fTimer.scheduleAtFixedRate(fTask, 1000, mPeriod);
    }

    private BeatPool instance() {
        if (fInstance == null) {
            fInstance = new BeatPool();
        }
        return fInstance;
    }

    private static class BeatPool extends Thread {

        final List<BeatEvent> fPool = Collections.synchronizedList(new ArrayList<>());
        private boolean fActive = true;

        public BeatPool() {
            start();
        }

        public void end() {
            fActive = false;
        }

        public void run() {
            while (fActive) {
                try {
                    synchronized (fPool) {
                        for (BeatEvent mBeatEvent : fPool) {
                            if (mBeatEvent.fFlagged) {
                                mBeatEvent.fFlagged = false;
                                for (BeatListener mListener : mBeatEvent.fListeners) {
                                    mListener.beat(mBeatEvent.fBeat);
                                }
                            }
                        }
                    }
                    Thread.sleep(12);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class BeatTimerTask extends TimerTask {

        @Override
        public void run() {
            fBeat++;
            fFlagged = true;
        }
    }
}
