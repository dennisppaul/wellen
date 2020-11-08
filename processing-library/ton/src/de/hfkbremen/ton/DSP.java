package de.hfkbremen.ton;

import processing.core.PApplet;

import javax.sound.sampled.AudioSystem;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static de.hfkbremen.ton.AudioBufferManager.DEFAULT;

public class DSP implements AudioBufferRenderer {

    private static final String METHOD_NAME = "audioblock";
    private static AudioBufferManager mAudioPlayer;
    private static DSP mInstance = null;
    private final PApplet mPApplet;
    private final int mNumberOutputChannels;
    private final int mNumberInputChannels;
    private Method mMethod = null;
    private float[] mCurrentBufferLeft;
    private float[] mCurrentBufferRight;

    public DSP(PApplet pPApplet, int pNumberOutputChannels, int pNumberInputChannels) {
        mPApplet = pPApplet;
        mNumberOutputChannels = pNumberOutputChannels;
        mNumberInputChannels = pNumberInputChannels;
        try {
            if (mNumberOutputChannels == 2 && mNumberInputChannels == 2) {
                mMethod = pPApplet.getClass().getDeclaredMethod(METHOD_NAME,
                                                                float[].class,
                                                                float[].class,
                                                                float[].class,
                                                                float[].class);
            } else if (mNumberOutputChannels == 2 && mNumberInputChannels == 0) {
                mMethod = pPApplet.getClass().getDeclaredMethod(METHOD_NAME,
                                                                float[].class,
                                                                float[].class);
            } else if (mNumberOutputChannels == 1 && mNumberInputChannels == 1) {
                mMethod = pPApplet.getClass().getDeclaredMethod(METHOD_NAME,
                                                                float[].class,
                                                                float[].class);
            } else if (mNumberOutputChannels == 1 && mNumberInputChannels == 0) {
                mMethod = pPApplet.getClass().getDeclaredMethod(METHOD_NAME,
                                                                float[].class);
            } else {
                mMethod = pPApplet.getClass().getDeclaredMethod(METHOD_NAME,
                                                                float[][].class);
            }
        } catch (NoSuchMethodException | SecurityException ex) {
            System.err.println("+++ @DSP / could not find callback `" + METHOD_NAME + "()`. hint: check the callback " +
                               "method paramters, they must match the sum of input and ouput channels. default is `" + METHOD_NAME + "(float[])` ( = MONO OUTPUT ).");
        }
    }

    public static DSP start(PApplet pPApplet) {
        return start(pPApplet, 1, 0);
    }

    public static DSP start(PApplet pPApplet, int pNumberOutputChannels) {
        return start(pPApplet, pNumberOutputChannels, 0);
    }

    public static DSP start(PApplet pPApplet, int pNumberOutputChannels, int pNumberInputChannels) {
        return start(pPApplet, DEFAULT, pNumberOutputChannels, DEFAULT, pNumberInputChannels);
    }

    public static DSP start(PApplet pPApplet,
                            int pOutputDevice,
                            int pNumberOutputChannels,
                            int pInputDevice,
                            int pNumberInputChannels) {
        if (mInstance == null) {
            mInstance = new DSP(pPApplet, pNumberOutputChannels, pNumberInputChannels);
            mAudioPlayer = new AudioBufferManager(mInstance,
                                                  44100,
                                                  512,
                                                  pOutputDevice,
                                                  pNumberOutputChannels,
                                                  pInputDevice,
                                                  pNumberInputChannels);
        }
        return mInstance;
    }

    public static int sample_rate() {
        return mAudioPlayer == null ? 0 : mAudioPlayer.sample_rate();
    }

    public static int buffer_size() {
        return mAudioPlayer == null ? 0 : mAudioPlayer.buffer_size();
    }

    public static float[] buffer() {
        return buffer_left();
    }

    public static float[] buffer_left() {
        return mInstance == null ? null : mInstance.mCurrentBufferLeft;
    }

    public static float[] buffer_right() {
        return mInstance == null ? null : mInstance.mCurrentBufferRight;
    }

    public static void dumpAudioDevices() {
        System.out.println("+-------------------------------------------------------+");
        System.out.println("AUDIO DEVICES ( Audio System )");
        System.out.println("+-------------------------------------------------------+");
        for (int i = 0; i < AudioSystem.getMixerInfo().length; i++) {
            System.out.println(i + "\t: " + AudioSystem.getMixerInfo()[i].getName());
        }
        System.out.println("+-------------------------------------------------------+");
    }

    public static float clamp(float pValue, float pMin, float pMax) {
        if (pValue > pMax) {
            return pMax;
        } else if (pValue < pMin) {
            return pMin;
        } else {
            return pValue;
        }
    }

    public static float flip(float pValue) {
        float pMin = -1.0f;
        float pMax = 1.0f;
        if (pValue > pMax) {
            return pValue - PApplet.floor(pValue);
        } else if (pValue < pMin) {
            return -PApplet.ceil(pValue) + pValue;
        } else {
            return pValue;
        }
    }

    public void audioblock(float[][] pOutputSamples, float[][] pInputSamples) {
        try {
            if (mNumberOutputChannels == 1 && mNumberInputChannels == 0) {
                mMethod.invoke(mPApplet, pOutputSamples[0]);
                mCurrentBufferLeft = pOutputSamples[0];
            } else if (mNumberOutputChannels == 1 && mNumberInputChannels == 1) {
                mMethod.invoke(mPApplet, pOutputSamples[0], pInputSamples[0]);
                mCurrentBufferLeft = pOutputSamples[0];
            } else if (mNumberOutputChannels == 2 && mNumberInputChannels == 0) {
                mMethod.invoke(mPApplet, pOutputSamples[0], pOutputSamples[1]);
                mCurrentBufferLeft = pOutputSamples[0];
                mCurrentBufferRight = pOutputSamples[1];
            } else if (mNumberOutputChannels == 2 && mNumberInputChannels == 2) {
                mMethod.invoke(mPApplet, pOutputSamples[0], pOutputSamples[1], pInputSamples[0], pInputSamples[1]);
                mCurrentBufferLeft = pOutputSamples[0];
                mCurrentBufferRight = pOutputSamples[1];
            } else {
                mMethod.invoke(mPApplet, pOutputSamples);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }
}

