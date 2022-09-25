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

import processing.core.PGraphics;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * handles audio signal processing. after <code>start(...)</code> is called the processing continously calls
 * <code>audioblock(...)</code> requesting blocks of audio samples.
 */
public class DSP implements AudioBufferRenderer {

    public boolean COPY_CACHED_BUFFER = false;

    private static final String METHOD_NAME = "audioblock";
    private static AudioBufferManager mAudioPlayer;
    private static DSP mInstance = null;
    private final Object mListener;
    private final int mNumberOutputChannels;
    private final int mNumberInputChannels;
    private Method mMethod = null;
    private static final int OUTPUT_LEFT = 0;
    private static final int OUTPUT_RIGHT = 1;
    private static final int INPUT_LEFT = 2;
    private static final int INPUT_RIGHT = 3;
    private static final int NUM_CACHED_BUFFERS = 4;
    private final float[][] fCachedBuffers = new float[NUM_CACHED_BUFFERS][];

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
                mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME, float[][].class, float[][].class);
            }
        } catch (NoSuchMethodException | SecurityException ex) {
            System.err.println("+++ @" + DSP.class.getSimpleName() + " / could not find callback `" + METHOD_NAME +
                                       "()`.");
            System.err.println("    hint: check the callback method parameters, they must match the number of input" + " and output " + "channels. default is `" + METHOD_NAME + "(float[])` ( = MONO OUTPUT ).");
        }
    }

    public void audioblock(float[][] pOutputSignal, float[][] pInputSignal) {
        try {
            Arrays.fill(fCachedBuffers, null);
            if (mNumberOutputChannels == 1 && mNumberInputChannels == 0) {
                //noinspection PrimitiveArrayArgumentToVarargsMethod
                mMethod.invoke(mListener, pOutputSignal[0]);
                fCachedBuffers[OUTPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(pOutputSignal[0]) : pOutputSignal[0];
            } else if (mNumberOutputChannels == 1 && mNumberInputChannels == 1) {
                mMethod.invoke(mListener, pOutputSignal[0], pInputSignal[0]);
                fCachedBuffers[OUTPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(pOutputSignal[0]) : pOutputSignal[0];
                fCachedBuffers[INPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(pInputSignal[0]) : pInputSignal[0];
            } else if (mNumberOutputChannels == 2 && mNumberInputChannels == 0) {
                mMethod.invoke(mListener, pOutputSignal[0], pOutputSignal[1]);
                fCachedBuffers[OUTPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(pOutputSignal[0]) : pOutputSignal[0];
                fCachedBuffers[OUTPUT_RIGHT] = COPY_CACHED_BUFFER ? Wellen.copy(pOutputSignal[1]) : pOutputSignal[1];
            } else if (mNumberOutputChannels == 2 && mNumberInputChannels == 1) {
                mMethod.invoke(mListener, pOutputSignal[0], pOutputSignal[1], pInputSignal[0]);
                fCachedBuffers[OUTPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(pOutputSignal[0]) : pOutputSignal[0];
                fCachedBuffers[OUTPUT_RIGHT] = COPY_CACHED_BUFFER ? Wellen.copy(pOutputSignal[1]) : pOutputSignal[1];
                fCachedBuffers[INPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(pInputSignal[0]) : pInputSignal[0];
            } else if (mNumberOutputChannels == 2 && mNumberInputChannels == 2) {
                mMethod.invoke(mListener, pOutputSignal[0], pOutputSignal[1], pInputSignal[0], pInputSignal[1]);
                fCachedBuffers[OUTPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(pOutputSignal[0]) : pOutputSignal[0];
                fCachedBuffers[OUTPUT_RIGHT] = COPY_CACHED_BUFFER ? Wellen.copy(pOutputSignal[1]) : pOutputSignal[1];
                fCachedBuffers[INPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(pInputSignal[0]) : pInputSignal[0];
                fCachedBuffers[INPUT_RIGHT] = COPY_CACHED_BUFFER ? Wellen.copy(pInputSignal[1]) : pInputSignal[1];
            } else {
                mMethod.invoke(mListener, pOutputSignal, pInputSignal);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                 NullPointerException ex) {
            System.err.println("+++ @" + DSP.class.getSimpleName() + " / error in audioblock: " + ex.getCause());
            ex.printStackTrace();
        }
    }

    public static void stop() {
        if (mAudioPlayer != null) {
            mAudioPlayer.exit();
        }
        mInstance = null;
        mAudioPlayer = null;
    }

    public static DSP start(Object pObject) {
        return start(pObject, 1, 0);
    }

    public static DSP start(Object pObject, int pNumberOutputChannels) {
        return start(pObject, pNumberOutputChannels, 0);
    }

    public static DSP start(Object pObject, int pNumberOutputChannels, int pNumberInputChannels) {
        return start(pObject,
                     Wellen.DEFAULT_AUDIO_DEVICE,
                     pNumberOutputChannels,
                     Wellen.DEFAULT_AUDIO_DEVICE,
                     pNumberInputChannels);
    }

    public static DSP start(Object pObject,
                            String pOutputDeviceName,
                            int pNumberOutputChannels,
                            String pInputDeviceName,
                            int pNumberInputChannels,
                            int pSamplingRate,
                            int pAudioBlockSize) {
        return start(pObject,
                     Wellen.queryAudioInputAndOutputDevices(pOutputDeviceName, false),
                     pNumberOutputChannels,
                     Wellen.queryAudioInputAndOutputDevices(pInputDeviceName, false),
                     pNumberInputChannels,
                     pSamplingRate,
                     pAudioBlockSize);
    }

    public static DSP start(Object pObject,
                            int pOutputDevice,
                            int pNumberOutputChannels,
                            int pInputDevice,
                            int pNumberInputChannels) {
        return start(pObject,
                     pOutputDevice,
                     pNumberOutputChannels,
                     pInputDevice,
                     pNumberInputChannels,
                     Wellen.DEFAULT_SAMPLING_RATE,
                     Wellen.DEFAULT_AUDIOBLOCK_SIZE);
//        if (mInstance == null) {
//            mInstance = new DSP(pObject, pNumberOutputChannels, pNumberInputChannels);
//            mAudioPlayer = new AudioBufferManager(mInstance,
//                                                  Wellen.DEFAULT_SAMPLING_RATE,
//                                                  Wellen.DEFAULT_AUDIOBLOCK_SIZE,
//                                                  pOutputDevice,
//                                                  pNumberOutputChannels,
//                                                  pInputDevice,
//                                                  pNumberInputChannels);
//        }
//        return mInstance;
    }

    public static DSP start(Object pObject,
                            int pOutputDevice,
                            int pNumberOutputChannels,
                            int pInputDevice,
                            int pNumberInputChannels,
                            int pSamplingRate,
                            int pAudioBlockSize) {
        if (mInstance == null) {
            mInstance = new DSP(pObject, pNumberOutputChannels, pNumberInputChannels);
            mAudioPlayer = new AudioBufferManager(mInstance,
                                                  pSamplingRate,
                                                  pAudioBlockSize,
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

    public static float[] get_output_buffer() {
        return get_output_buffer_left();
    }

    public static float[] get_output_buffer_left() {
        return mInstance == null ? null : mInstance.fCachedBuffers[OUTPUT_LEFT];
    }

    public static float[] get_output_buffer_right() {
        return mInstance == null ? null : mInstance.fCachedBuffers[OUTPUT_RIGHT];
    }

    public static float[] get_input_buffer_left() {
        return mInstance == null ? null : mInstance.fCachedBuffers[INPUT_LEFT];
    }

    public static float[] get_input_buffer_right() {
        return mInstance == null ? null : mInstance.fCachedBuffers[INPUT_RIGHT];
    }

    public static void draw_buffers(PGraphics g, float pWidth, float pHeight) {
        Wellen.draw_buffers(g,
                            pWidth,
                            pHeight,
                            DSP.get_output_buffer_left(),
                            DSP.get_output_buffer_right(),
                            DSP.get_input_buffer_left(),
                            DSP.get_input_buffer_right());
    }

    /* --- UTILITIES --- */

    /**
     * Calculates and returns the root mean square of the signal. Please cache the result since it is calculated every
     * time.
     *
     * @param pBuffer The audio buffer to calculate the RMS for.
     * @return The <a href="http://en.wikipedia.org/wiki/Root_mean_square">RMS</a> of the signal present in the current
     *         buffer.
     */
    public static float calculate_RMS(final float[] pBuffer) {
        float mRMS = 0.0f;
        for (float v : pBuffer) {
            mRMS += v * v;
        }
        mRMS = mRMS / (float) pBuffer.length;
        mRMS = (float) Math.sqrt(mRMS);
        return mRMS;
    }

    /**
     * returns sound pressure level in decibel for given buffer
     *
     * @param pBuffer buffer with signal
     * @return level for buffer in decibel
     */
    public static float sound_pressure_level(final float[] pBuffer) {
        float rms = calculate_RMS(pBuffer);
        return linear_to_decibel(rms);
    }

    /**
     * Converts a linear to a dB value.
     *
     * @param pLinearValue linear value to convert
     * @return converted value in decibel
     */
    public static float linear_to_decibel(final float pLinearValue) {
        return 20.0f * (float) Math.log10(pLinearValue);
    }
}
