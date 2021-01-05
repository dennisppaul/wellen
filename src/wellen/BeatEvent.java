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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * similar to {@link wellen.Beat} except that it handles multiple listeners.
 */
public class BeatEvent {

    private final Timer mTimer;
    private final ArrayList<BeatListener> pListeners;
    private BeatPool instance;
    private int mBeat = -1;
    private boolean mFlagged = false;
    private TimerTask mTask;

    private BeatEvent(int pBPM) {
        this();
        set_bpm(pBPM);
    }

    private BeatEvent() {
        pListeners = new ArrayList<>();
        mTimer = new Timer();
    }

    public static BeatEvent create(int pBPM) {
        BeatEvent mBeatEvent = new BeatEvent(pBPM);
        mBeatEvent.instance().pool.add(mBeatEvent);
        return mBeatEvent;
    }

    public void stop() {
        instance().end();
    }

    public void add(BeatListener pListener) {
        pListeners.add(pListener);
    }

    public void set_bpm(float pBPM) {
        final int mPeriod = (int) (60.0f / pBPM * 1000.0f);
        if (mTask != null) {
            mTask.cancel();
        }
        mTask = new BeatTimerTask();
        mTimer.scheduleAtFixedRate(mTask, 1000, mPeriod);
    }

    private BeatPool instance() {
        if (instance == null) {
            instance = new BeatPool();
        }
        return instance;
    }

    private static class BeatPool extends Thread {

        final List<BeatEvent> pool = Collections.synchronizedList(new ArrayList<BeatEvent>());
        private boolean mActive = true;

        public BeatPool() {
            start();
        }

        public void end() {
            mActive = false;
        }

        public void run() {
            while (mActive) {
                try {
                    synchronized (pool) {
                        final Iterator<BeatEvent> i = pool.iterator();
                        while (i.hasNext()) {
                            final BeatEvent mBeatEvent = i.next();
                            if (mBeatEvent.mFlagged) {
                                mBeatEvent.mFlagged = false;
                                for (BeatListener mListener : mBeatEvent.pListeners) {
                                    mListener.beat(mBeatEvent.mBeat);
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
            mBeat++;
            mFlagged = true;
        }
    }
}
