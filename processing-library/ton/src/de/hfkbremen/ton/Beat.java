package de.hfkbremen.ton;

import processing.core.PApplet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

public class Beat {

    private int mBeat = -1;

    private Method mMethod = null;

    private final PApplet mPApplet;

    private final Timer mTimer;

    private TimerTask mTask;

    public Beat(PApplet pPApplet, int pBPM) {
        this(pPApplet);
        bpm(pBPM);
    }

    public Beat(PApplet pPApplet) {
        mPApplet = pPApplet;
        try {
            mMethod = pPApplet.getClass().getDeclaredMethod("beat", Integer.TYPE);
        } catch (NoSuchMethodException | SecurityException ex) {
            ex.printStackTrace();
        }
        mTimer = new Timer();
    }

    public void bpm(float pBPM) {
        final int mPeriod = (int) (60.0f / pBPM * 1000.0f);
        if (mTask != null) {
            mTask.cancel();
        }
        mTask = new BeatTimerTaskP5();
        mTimer.scheduleAtFixedRate(mTask, 1000, mPeriod);
    }

    public static Beat start(PApplet pPApplet, int pBPM) {
        return new Beat(pPApplet, pBPM);
    }

    public static Beat start(PApplet pPApplet) {
        return new Beat(pPApplet);
    }

    private class BeatTimerTaskP5 extends TimerTask {

        @Override
        public void run() {
            try {
                mBeat++;
                mMethod.invoke(mPApplet, mBeat);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
    }
}
