package wellen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

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

    public void set_bpm(float pBPM) {
        final int mPeriod = (int) (60.0f / pBPM * 1000.0f);
        if (mTask != null) {
            mTask.cancel();
        }
        mTask = new BeatTimerTaskP5();
        mTimer.scheduleAtFixedRate(mTask, 1000, mPeriod);
    }

    public static Beat start(Object pListener, int pBPM) {
        return new Beat(pListener, pBPM);
    }

    public static Beat start(Object pListener) {
        return new Beat(pListener);
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
