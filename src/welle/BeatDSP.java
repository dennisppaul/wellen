package welle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BeatDSP implements DSPNodeInput {

    private static final String METHOD_NAME = "beat";
    private final Object mListener;
    private final int mSamplingRate;
    private Method mMethod = null;
    private int mBeat;
    private int mCounter;
    private float mInterval;

    public BeatDSP(Object pListener) {
        this(pListener, Welle.DEFAULT_SAMPLING_RATE);
    }

    public BeatDSP(Object pListener, int pSamplingRate) {
        mListener = pListener;
        mSamplingRate = pSamplingRate;
        mBeat = -1;
        bpm(120);
        try {
            mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME, Integer.TYPE);
        } catch (NoSuchMethodException | SecurityException ex) {
            System.err.println("+++ @" + getClass().getSimpleName() + " / could not find `" + METHOD_NAME + "(int)`");
        }
    }

    public void bpm(float pBPM) {
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

    public static BeatDSP start(Object pListener, int pSamplingRate) {
        return new BeatDSP(pListener);
    }

    public static BeatDSP start(Object pListener) {
        return new BeatDSP(pListener, Welle.DEFAULT_SAMPLING_RATE);
    }
}