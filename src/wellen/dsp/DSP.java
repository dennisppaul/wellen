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

package wellen.dsp;

import processing.core.PGraphics;
import wellen.AudioBufferManager;
import wellen.AudioBufferRenderer;
import wellen.AudioDeviceConfiguration;
import wellen.Wellen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * handles audio signal processing. after <code>start(...)</code> is called the processing continously calls
 * <code>audioblock(...)</code> requesting blocks of audio samples.
 */
public class DSP implements AudioBufferRenderer {

    /**
     * enable to create a copy of a cached buffer for each call to <code>audioblock(...)</code>. this is useful if the
     * processing of the audio signal is not thread-safe.
     */
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

    /**
     * @param pListener             object which implements the <code>audioblock(...)</code> method
     * @param pNumberOutputChannels number of output channels
     * @param pNumberInputChannels  number of input channels
     */
    public DSP(Object pListener, int pNumberOutputChannels, int pNumberInputChannels) {
        mListener = pListener;
        mNumberOutputChannels = pNumberOutputChannels;
        mNumberInputChannels = pNumberInputChannels;
        try {
            if (mNumberOutputChannels == 2 && mNumberInputChannels == 2) {
                mMethod = pListener.getClass()
                                   .getDeclaredMethod(METHOD_NAME,
                                                      float[].class,
                                                      float[].class,
                                                      float[].class,
                                                      float[].class);
            } else if (mNumberOutputChannels == 2 && mNumberInputChannels == 0) {
                mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME, float[].class, float[].class);
            } else if (mNumberOutputChannels == 2 && mNumberInputChannels == 1) {
                mMethod = pListener.getClass()
                                   .getDeclaredMethod(METHOD_NAME, float[].class, float[].class, float[].class);
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

    public void audioblock(float[][] output_signal, float[][] pInputSignal) {
        try {
            Arrays.fill(fCachedBuffers, null);
            if (mNumberOutputChannels == 1 && mNumberInputChannels == 0) {
                //noinspection PrimitiveArrayArgumentToVarargsMethod
                mMethod.invoke(mListener, output_signal[0]);
                fCachedBuffers[OUTPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(output_signal[0]) : output_signal[0];
            } else if (mNumberOutputChannels == 1 && mNumberInputChannels == 1) {
                mMethod.invoke(mListener, output_signal[0], pInputSignal[0]);
                fCachedBuffers[OUTPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(output_signal[0]) : output_signal[0];
                fCachedBuffers[INPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(pInputSignal[0]) : pInputSignal[0];
            } else if (mNumberOutputChannels == 2 && mNumberInputChannels == 0) {
                mMethod.invoke(mListener, output_signal[0], output_signal[1]);
                fCachedBuffers[OUTPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(output_signal[0]) : output_signal[0];
                fCachedBuffers[OUTPUT_RIGHT] = COPY_CACHED_BUFFER ? Wellen.copy(output_signal[1]) : output_signal[1];
            } else if (mNumberOutputChannels == 2 && mNumberInputChannels == 1) {
                mMethod.invoke(mListener, output_signal[0], output_signal[1], pInputSignal[0]);
                fCachedBuffers[OUTPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(output_signal[0]) : output_signal[0];
                fCachedBuffers[OUTPUT_RIGHT] = COPY_CACHED_BUFFER ? Wellen.copy(output_signal[1]) : output_signal[1];
                fCachedBuffers[INPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(pInputSignal[0]) : pInputSignal[0];
            } else if (mNumberOutputChannels == 2 && mNumberInputChannels == 2) {
                mMethod.invoke(mListener, output_signal[0], output_signal[1], pInputSignal[0], pInputSignal[1]);
                fCachedBuffers[OUTPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(output_signal[0]) : output_signal[0];
                fCachedBuffers[OUTPUT_RIGHT] = COPY_CACHED_BUFFER ? Wellen.copy(output_signal[1]) : output_signal[1];
                fCachedBuffers[INPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(pInputSignal[0]) : pInputSignal[0];
                fCachedBuffers[INPUT_RIGHT] = COPY_CACHED_BUFFER ? Wellen.copy(pInputSignal[1]) : pInputSignal[1];
            } else {
                mMethod.invoke(mListener, output_signal, pInputSignal);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                 NullPointerException ex) {
            System.err.println("+++ @" + DSP.class.getSimpleName() + " / error in audioblock: " + ex.getCause());
            ex.printStackTrace();
        }
    }

    /**
     *
     */
    public static void stop() {
        if (mAudioPlayer != null) {
            mAudioPlayer.exit();
        }
        mInstance = null;
        mAudioPlayer = null;
    }

    /**
     * @param pObject object which implements the <code>audioblock(...)</code> method
     * @return reference to DSP instance
     */
    public static DSP start(Object pObject) {
        return start(pObject, 1, 0);
    }

    /**
     * @param pObject               object which implements the <code>audioblock(...)</code> method
     * @param pNumberOutputChannels number of output channels
     * @return reference to DSP instance
     */
    public static DSP start(Object pObject, int pNumberOutputChannels) {
        return start(pObject, pNumberOutputChannels, 0);
    }

    /**
     * @param pObject               object which implements the <code>audioblock(...)</code> method
     * @param pNumberOutputChannels number of output channels
     * @param pNumberInputChannels  number of input channels
     * @return reference to DSP instance
     */
    public static DSP start(Object pObject, int pNumberOutputChannels, int pNumberInputChannels) {
        return start(pObject,
                     Wellen.DEFAULT_AUDIO_DEVICE,
                     pNumberOutputChannels,
                     Wellen.DEFAULT_AUDIO_DEVICE,
                     pNumberInputChannels);
    }

    /**
     * @param pObject               object which implements the <code>audioblock(...)</code> method
     * @param pOutputDeviceName     name of output device
     * @param pNumberOutputChannels number of output channels
     * @param pInputDeviceName      name of input device
     * @param pNumberInputChannels  number of input channels
     * @param pSamplingRate         sampling rate
     * @param pAudioBlockSize       audio block size
     * @return reference to DSP instance
     */

    public static DSP start(Object pObject,
                            String pOutputDeviceName,
                            int pNumberOutputChannels,
                            String pInputDeviceName,
                            int pNumberInputChannels,
                            int pSamplingRate,
                            int pAudioBlockSize) {
        return start(pObject,
                     Wellen.queryAudioInputAndOutputDevices(pOutputDeviceName, false, false),
                     pNumberOutputChannels,
                     Wellen.queryAudioInputAndOutputDevices(pInputDeviceName, false, false),
                     pNumberInputChannels,
                     pSamplingRate,
                     pAudioBlockSize);
    }

    /**
     * @param pObject               object which implements the <code>audioblock(...)</code> method
     * @param pOutputDevice         output device ID as returned by
     *                              {@link Wellen#queryAudioInputAndOutputDevices(String, boolean, boolean)}
     * @param pNumberOutputChannels number of output channels
     * @param pInputDevice          input device ID as returned by
     *                              {@link Wellen#queryAudioInputAndOutputDevices(String, boolean, boolean)}
     * @param pNumberInputChannels  number of input channels
     * @return reference to DSP instance
     */
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
    }

    /**
     * @param pObject               object which implements the <code>audioblock(...)</code> method
     * @param pOutputDevice         output device ID as returned by
     *                              {@link Wellen#queryAudioInputAndOutputDevices(String, boolean, boolean)}
     * @param pNumberOutputChannels number of output channels
     * @param pInputDevice          input device ID as returned by
     *                              {@link Wellen#queryAudioInputAndOutputDevices(String, boolean, boolean)}
     * @param pNumberInputChannels  number of input channels
     * @param pSamplingRate         sampling rate
     * @param pAudioBlockSize       audio block size
     * @return reference to DSP instance
     */
    public static DSP start(Object pObject,
                            int pOutputDevice,
                            int pNumberOutputChannels,
                            int pInputDevice,
                            int pNumberInputChannels,
                            int pSamplingRate,
                            int pAudioBlockSize) {
        if (mInstance == null) {
            mInstance = new DSP(pObject, pNumberOutputChannels, pNumberInputChannels);
            AudioDeviceConfiguration mConfig = new AudioDeviceConfiguration();
            mConfig.sample_rate = pSamplingRate;
            mConfig.sample_buffer_size = pAudioBlockSize;
            mConfig.output_device = pOutputDevice;
            mConfig.number_of_output_channels = pNumberOutputChannels;
            mConfig.input_device = pInputDevice;
            mConfig.number_of_input_channels = pNumberInputChannels;
            mAudioPlayer = new AudioBufferManager(mInstance, mConfig);
        }
        return mInstance;
    }

    /**
     * @param pObject object which implements the <code>audioblock(...)</code> method
     * @param pConfig audio device configuration
     * @return reference to DSP instance
     */
    public static DSP start(Object pObject, AudioDeviceConfiguration pConfig) {
        if (mInstance == null) {
            mInstance = new DSP(pObject, pConfig.number_of_output_channels, pConfig.number_of_input_channels);
            mAudioPlayer = new AudioBufferManager(mInstance, pConfig);
        }
        return mInstance;
    }

    /**
     * @return sample rate
     */
    public static int get_sample_rate() {
        return mAudioPlayer == null ? Wellen.NO_VALUE : mAudioPlayer.get_sample_rate();
    }

    /**
     * @return audio block or buffer size
     */
    public static int get_buffer_size() {
        return mAudioPlayer == null ? Wellen.NO_VALUE : mAudioPlayer.get_buffer_size();
    }

    /**
     * @return reference to output buffer
     */
    public static float[] get_output_buffer() {
        return get_output_buffer_left();
    }

    /**
     * @return reference to left output buffer
     */
    public static float[] get_output_buffer_left() {
        return mInstance == null ? null : mInstance.fCachedBuffers[OUTPUT_LEFT];
    }

    /**
     * @return reference to right output buffer
     */
    public static float[] get_output_buffer_right() {
        return mInstance == null ? null : mInstance.fCachedBuffers[OUTPUT_RIGHT];
    }

    /**
     * @return reference to left input buffer
     */
    public static float[] get_input_buffer_left() {
        return mInstance == null ? null : mInstance.fCachedBuffers[INPUT_LEFT];
    }

    /**
     * @return reference to right input buffer
     */
    public static float[] get_input_buffer_right() {
        return mInstance == null ? null : mInstance.fCachedBuffers[INPUT_RIGHT];
    }

    /**
     * @param g       graphics context to draw into
     * @param pWidth  visual width of the drawn buffer
     * @param pHeight visual height of the drawn buffer
     */
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
