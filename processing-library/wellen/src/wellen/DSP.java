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

import processing.core.PGraphics;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * handles audio signal processing. after <code>start(...)</code> is called the processing continously calls
 * <code>audioblock(...)</code> requesting blocks of audio samples.
 */
public class DSP implements AudioBufferRenderer {

    private static final String METHOD_NAME = "audioblock";
    private static AudioBufferManager mAudioPlayer;
    private static DSP mInstance = null;
    private final Object mListener;
    private final int mNumberOutputChannels;
    private final int mNumberInputChannels;
    private Method mMethod = null;
    private float[] mCurrentBufferLeft;
    private float[] mCurrentBufferRight;

    public DSP(Object pListener, int pNumberOutputChannels, int pNumberInputChannels) {
        mListener = pListener;
        mNumberOutputChannels = pNumberOutputChannels;
        mNumberInputChannels = pNumberInputChannels;
        try {
            if (mNumberOutputChannels == 2 && mNumberInputChannels == 2) {
                mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME,
                                                                 float[].class,
                                                                 float[].class,
                                                                 float[].class,
                                                                 float[].class);
            } else if (mNumberOutputChannels == 2 && mNumberInputChannels == 0) {
                mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME, float[].class, float[].class);
            } else if (mNumberOutputChannels == 2 && mNumberInputChannels == 1) {
                mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME,
                                                                 float[].class,
                                                                 float[].class,
                                                                 float[].class);
            } else if (mNumberOutputChannels == 1 && mNumberInputChannels == 1) {
                mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME, float[].class, float[].class);
            } else if (mNumberOutputChannels == 1 && mNumberInputChannels == 0) {
                mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME, float[].class);
            } else {
                mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME, float[][].class);
            }
        } catch (NoSuchMethodException | SecurityException ex) {
            System.err.println("+++ @" + DSP.class.getSimpleName() + " / could not find callback `" + METHOD_NAME +
                                       "()`.");
            System.err.println("    hint: check the callback method parameters, they must match the number of input" + " and output channels. default is `" + METHOD_NAME + "(float[])` ( = MONO OUTPUT ).");
        }
    }

    public static DSP start(Object pObject) {
        return start(pObject, 1, 0);
    }

    public static DSP start(Object pObject, int pNumberOutputChannels) {
        return start(pObject, pNumberOutputChannels, 0);
    }

    public static DSP start(Object pObject, int pNumberOutputChannels, int pNumberInputChannels) {
        return start(pObject,
                     AudioBufferManager.DEFAULT,
                     pNumberOutputChannels,
                     AudioBufferManager.DEFAULT,
                     pNumberInputChannels);
    }

    public static DSP start(Object pObject, int pOutputDevice, int pNumberOutputChannels, int pInputDevice,
                            int pNumberInputChannels) {
        if (mInstance == null) {
            mInstance = new DSP(pObject, pNumberOutputChannels, pNumberInputChannels);
            mAudioPlayer = new AudioBufferManager(mInstance,
                                                  Wellen.DEFAULT_SAMPLING_RATE,
                                                  Wellen.DEFAULT_AUDIOBLOCK_SIZE,
                                                  pOutputDevice,
                                                  pNumberOutputChannels,
                                                  pInputDevice,
                                                  pNumberInputChannels);
        }
        return mInstance;
    }

    public static int get_sample_rate() {
        return mAudioPlayer == null ? 0 : mAudioPlayer.sample_rate();
    }

    public static int get_buffer_size() {
        return mAudioPlayer == null ? 0 : mAudioPlayer.buffer_size();
    }

    public static float[] get_buffer() {
        return get_buffer_left();
    }

    public static float[] get_buffer_left() {
        return mInstance == null ? null : mInstance.mCurrentBufferLeft;
    }

    public static float[] get_buffer_right() {
        return mInstance == null ? null : mInstance.mCurrentBufferRight;
    }

    public static void draw_buffer_stereo(PGraphics g, float pWidth, float pHeight) {
        Wellen.draw_buffer(g, pWidth, pHeight, DSP.get_buffer_left(), DSP.get_buffer_right());
    }

    public static void draw_buffer(PGraphics g, float pWidth, float pHeight) {
        Wellen.draw_buffer(g, pWidth, pHeight, DSP.get_buffer());
    }

    public void audioblock(float[][] pOutputSamples, float[][] pInputSamples) {
        try {
            if (mNumberOutputChannels == 1 && mNumberInputChannels == 0) {
                mMethod.invoke(mListener, pOutputSamples[0]);
                mCurrentBufferLeft = pOutputSamples[0];
            } else if (mNumberOutputChannels == 1 && mNumberInputChannels == 1) {
                mMethod.invoke(mListener, pOutputSamples[0], pInputSamples[0]);
                mCurrentBufferLeft = pOutputSamples[0];
            } else if (mNumberOutputChannels == 2 && mNumberInputChannels == 0) {
                mMethod.invoke(mListener, pOutputSamples[0], pOutputSamples[1]);
                mCurrentBufferLeft = pOutputSamples[0];
                mCurrentBufferRight = pOutputSamples[1];
            } else if (mNumberOutputChannels == 2 && mNumberInputChannels == 1) {
                mMethod.invoke(mListener, pOutputSamples[0], pOutputSamples[1], pInputSamples[0]);
                mCurrentBufferLeft = pOutputSamples[0];
                mCurrentBufferRight = pOutputSamples[1];
            } else if (mNumberOutputChannels == 2 && mNumberInputChannels == 2) {
                mMethod.invoke(mListener, pOutputSamples[0], pOutputSamples[1], pInputSamples[0], pInputSamples[1]);
                mCurrentBufferLeft = pOutputSamples[0];
                mCurrentBufferRight = pOutputSamples[1];
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NullPointerException ex) {
            System.err.println("+++ @" + DSP.class.getSimpleName() + " / error in audioblock: " + ex.getCause());
            ex.printStackTrace();
        }
    }
}

