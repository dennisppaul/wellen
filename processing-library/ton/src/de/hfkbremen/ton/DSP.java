package de.hfkbremen.ton;

import processing.core.PApplet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DSP implements AudioBufferRenderer {

    //    void audioblock(float[] pOutputLeft,
    //                    float[] pOutputRight,
    //                    float[] pInputLeft,
    //                    float[] pInputRight) {}
    private static final String METHOD_NAME = "audioblock";
    private static AudioBufferPlayer mAudioPlayer;
    private static DSP mInstance = null;
    private final PApplet mPApplet;
    private Method mMethod = null;
    private float[] mCurrentBuffer;

    public DSP(PApplet pPApplet) {
        mPApplet = pPApplet;
        try {
            mMethod = pPApplet.getClass().getDeclaredMethod(METHOD_NAME, float[].class);
        } catch (NoSuchMethodException | SecurityException ex) {
            ex.printStackTrace();
        }
    }

    public static DSP start(PApplet pPApplet) {
        if (mInstance == null) {
            mInstance = new DSP(pPApplet);
            mAudioPlayer = new AudioBufferPlayer(mInstance);
        }
        return mInstance;
    }

    public static int sample_rate() {
        return mAudioPlayer == null ? 0 : mAudioPlayer.sample_rate();
    }

    public static float[] buffer() {
        return mInstance == null ? null : mInstance.mCurrentBuffer;
    }

    public void render(float[] pSamples) {
        try {
            mMethod.invoke(mPApplet, pSamples);
            mCurrentBuffer = pSamples;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }
}

