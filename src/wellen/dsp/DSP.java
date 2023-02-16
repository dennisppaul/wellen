/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2023 Dennis P Paul.
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

    private static final int INPUT_LEFT = 2;
    private static final int INPUT_RIGHT = 3;
    private static final String METHOD_NAME = "audioblock";
    private static final String METHOD_NAME_PER_SAMPLE = "audio";
    private final boolean fProcessPerSample;
    private static final int NUM_CACHED_BUFFERS = 4;
    private static final int OUTPUT_LEFT = 0;
    private static final int OUTPUT_RIGHT = 1;
    private static AudioBufferManager fAudioBufferManager;
    private static DSP fInstance = null;
    /**
     * enable to create a copy of a cached buffer for each call to <code>audioblock(...)</code>. this is useful if the
     * processing of the audio signal is not thread-safe.
     */
    public boolean COPY_CACHED_BUFFER = false;
    private final float[][] fCachedBuffers = new float[NUM_CACHED_BUFFERS][];
    private final Object fListener;
    private Method fMethod = null;
    /* --- UTILITIES --- */
    private final int fNumberInputChannels;
    private final int fNumberOutputChannels;

    /**
     * @param callback                  object which implements the <code>audioblock(...)</code> method
     * @param number_of_output_channels number of output channels
     * @param number_of_input_channels  number of input channels
     */
    public DSP(Object callback, int number_of_output_channels, int number_of_input_channels) {
        fListener = callback;
        fNumberOutputChannels = number_of_output_channels;
        fNumberInputChannels = number_of_input_channels;
        boolean mProcessPerSample = false;
        try {
            if (fNumberOutputChannels == 1 && fNumberInputChannels == 1) {
                fMethod = callback.getClass().getDeclaredMethod(METHOD_NAME_PER_SAMPLE, float.class);
                mProcessPerSample = true;
            } else if (fNumberOutputChannels == 1 && fNumberInputChannels == 0) {
                fMethod = callback.getClass().getDeclaredMethod(METHOD_NAME_PER_SAMPLE);
                mProcessPerSample = true;
            }
            if (fMethod != null) {
                if (fNumberOutputChannels > 1 || fNumberInputChannels > 1 || fMethod.getReturnType() != float.class) {
                    System.err.println("+++ @" + DSP.class.getSimpleName() + " / did find callback `float " + METHOD_NAME_PER_SAMPLE + "(...)` but with wrong signature.");
                    System.err.println("    hint: check the callback method parameters, " + "they must match the " +
                                               "number of input channels.");
                    System.err.println("    also check the return type which needs to be " + "`float`. default is " + "`float " + METHOD_NAME_PER_SAMPLE + "()` ( = NO INPUT, MONO OUTPUT ).");
                    System.exit(-1);
                }
            }
        } catch (NoSuchMethodException | SecurityException ignored) {
        }
        if (!mProcessPerSample) {
            try {
                if (fNumberOutputChannels == 2 && fNumberInputChannels == 2) {
                    fMethod = callback.getClass()
                                      .getDeclaredMethod(METHOD_NAME,
                                                         float[].class,
                                                         float[].class,
                                                         float[].class,
                                                         float[].class);
                } else if (fNumberOutputChannels == 2 && fNumberInputChannels == 0) {
                    fMethod = callback.getClass().getDeclaredMethod(METHOD_NAME, float[].class, float[].class);
                } else if (fNumberOutputChannels == 2 && fNumberInputChannels == 1) {
                    fMethod = callback.getClass()
                                      .getDeclaredMethod(METHOD_NAME, float[].class, float[].class, float[].class);
                } else if (fNumberOutputChannels == 1 && fNumberInputChannels == 1) {
                    fMethod = callback.getClass().getDeclaredMethod(METHOD_NAME, float[].class, float[].class);
                } else if (fNumberOutputChannels == 1 && fNumberInputChannels == 0) {
                    fMethod = callback.getClass().getDeclaredMethod(METHOD_NAME, float[].class);
                } else {
                    fMethod = callback.getClass().getDeclaredMethod(METHOD_NAME, float[][].class, float[][].class);
                }
            } catch (NoSuchMethodException | SecurityException ex) {
                System.err.println("+++ @" + DSP.class.getSimpleName() + " / could not find callback `" + METHOD_NAME + "(...)` or `" + METHOD_NAME_PER_SAMPLE + "(...)`.");
                System.err.println("    hint: check the callback method parameters, they " + "must match the number " + "of input and output channels. " + "also check the return type");
                System.err.print("    default is ( NO INPUT, MONO OUTPUT ): ");
                System.err.println("`void " + METHOD_NAME + "(float[])` or `float " + METHOD_NAME_PER_SAMPLE + "()`");
            }
        }
        fProcessPerSample = mProcessPerSample;
        if (fMethod == null) {
            System.exit(-1);
        }
    }

    /**
     * pause or resume audio processing
     *
     * @param pause_state <code>true</code> to pause audio processing, <code>false</code> to resume
     */
    public static void pause(boolean pause_state) {
        if (fAudioBufferManager != null) {
            fAudioBufferManager.pause(pause_state);
        }
    }

    /**
     * querry the pause state of the audio processing
     *
     * @return <code>true</code> if audio processing is paused, <code>false</code> otherwise
     */
    public static boolean is_paused() {
        if (fAudioBufferManager != null) {
            return fAudioBufferManager.is_paused();
        }
        return false;
    }

    /**
     * Calculates and returns the root mean square of the signal. Please cache the result since it is calculated every
     * time.
     *
     * @param buffer The audio buffer to calculate the RMS for.
     * @return The <a href="http://en.wikipedia.org/wiki/Root_mean_square">RMS</a> of the signal present in the current
     *         buffer.
     */
    public static float calculate_RMS(final float[] buffer) {
        float mRMS = 0.0f;
        for (float v : buffer) {
            mRMS += v * v;
        }
        mRMS = mRMS / (float) buffer.length;
        mRMS = (float) Math.sqrt(mRMS);
        return mRMS;
    }

    /**
     * @param g      graphics context to draw into
     * @param width  visual width of the drawn buffer
     * @param height visual height of the drawn buffer
     */
    public static void draw_buffers(PGraphics g, float width, float height) {
        Wellen.draw_buffers(g,
                            width,
                            height,
                            DSP.get_output_buffer_left(),
                            DSP.get_output_buffer_right(),
                            DSP.get_input_buffer_left(),
                            DSP.get_input_buffer_right());
    }

    /**
     * @return audio block or buffer size
     */
    public static int get_buffer_size() {
        return fAudioBufferManager == null ? Wellen.NO_VALUE : fAudioBufferManager.get_buffer_size();
    }

    /**
     * @return reference to left input buffer
     */
    public static float[] get_input_buffer_left() {
        return fInstance == null ? null : fInstance.fCachedBuffers[INPUT_LEFT];
    }

    /**
     * @return reference to right input buffer
     */
    public static float[] get_input_buffer_right() {
        return fInstance == null ? null : fInstance.fCachedBuffers[INPUT_RIGHT];
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
        return fInstance == null ? null : fInstance.fCachedBuffers[OUTPUT_LEFT];
    }

    /**
     * @return reference to right output buffer
     */
    public static float[] get_output_buffer_right() {
        return fInstance == null ? null : fInstance.fCachedBuffers[OUTPUT_RIGHT];
    }

    /**
     * @return sample rate
     */
    public static int get_sample_rate() {
        return fAudioBufferManager == null ? Wellen.NO_VALUE : fAudioBufferManager.get_sample_rate();
    }

    /**
     * Converts a linear to a dB value.
     *
     * @param linear_value linear value to convert
     * @return converted value in decibel
     */
    public static float linear_to_decibel(final float linear_value) {
        return 20.0f * (float) Math.log10(linear_value);
    }

    /**
     * returns sound pressure level in decibel for given buffer
     *
     * @param buffer buffer with signal
     * @return level for buffer in decibel
     */
    public static float sound_pressure_level(final float[] buffer) {
        float rms = calculate_RMS(buffer);
        return linear_to_decibel(rms);
    }

    /**
     * @param callback object which implements the <code>audioblock(...)</code> method
     * @return reference to DSP instance
     */
    public static DSP start(Object callback) {
        return start(callback, 1, 0);
    }

    /**
     * @param callback                  object which implements the <code>audioblock(...)</code> method
     * @param number_of_output_channels number of output channels
     * @return reference to DSP instance
     */
    public static DSP start(Object callback, int number_of_output_channels) {
        return start(callback, number_of_output_channels, 0);
    }

    /**
     * @param callback                  object which implements the <code>audioblock(...)</code> method
     * @param number_of_output_channels number of output channels
     * @param number_of_input_channels  number of input channels
     * @return reference to DSP instance
     */
    public static DSP start(Object callback, int number_of_output_channels, int number_of_input_channels) {
        return start(callback,
                     Wellen.DEFAULT_AUDIO_DEVICE,
                     number_of_output_channels,
                     Wellen.DEFAULT_AUDIO_DEVICE,
                     number_of_input_channels);
    }

    /**
     * @param callback                  object which implements the <code>audioblock(...)</code> method
     * @param output_device_name        name of output device
     * @param number_of_output_channels number of output channels
     * @param input_device_name         name of input device
     * @param number_of_input_channels  number of input channels
     * @param sampling_rate             sampling rate
     * @param audio_block_size          audio block size
     * @return reference to DSP instance
     */
    public static DSP start(Object callback,
                            String output_device_name,
                            int number_of_output_channels,
                            String input_device_name,
                            int number_of_input_channels,
                            int sampling_rate,
                            int audio_block_size) {
        return start(callback,
                     Wellen.queryAudioInputAndOutputDevices(output_device_name, false, false),
                     number_of_output_channels,
                     Wellen.queryAudioInputAndOutputDevices(input_device_name, false, false),
                     number_of_input_channels,
                     sampling_rate,
                     audio_block_size);
    }

    /**
     * @param callback                  object which implements the <code>audioblock(...)</code> method
     * @param output_device_ID          output device ID as returned by
     *                                  {@link Wellen#queryAudioInputAndOutputDevices(String, boolean, boolean)}
     * @param number_of_output_channels number of output channels
     * @param input_device_ID           input device ID as returned by
     *                                  {@link Wellen#queryAudioInputAndOutputDevices(String, boolean, boolean)}
     * @param number_of_input_channels  number of input channels
     * @return reference to DSP instance
     */
    public static DSP start(Object callback,
                            int output_device_ID,
                            int number_of_output_channels,
                            int input_device_ID,
                            int number_of_input_channels) {
        return start(callback,
                     output_device_ID,
                     number_of_output_channels,
                     input_device_ID,
                     number_of_input_channels,
                     Wellen.DEFAULT_SAMPLING_RATE,
                     Wellen.DEFAULT_AUDIOBLOCK_SIZE);
    }

    /**
     * @param callback                  object which implements the <code>audioblock(...)</code> method
     * @param output_device_ID          output device ID as returned by
     *                                  {@link Wellen#queryAudioInputAndOutputDevices(String, boolean, boolean)}
     * @param number_of_output_channels number of output channels
     * @param input_device_ID           input device ID as returned by
     *                                  {@link Wellen#queryAudioInputAndOutputDevices(String, boolean, boolean)}
     * @param number_of_input_channels  number of input channels
     * @param sampling_rate             sampling rate
     * @param audio_block_size          audio block size
     * @return reference to DSP instance
     */
    public static DSP start(Object callback,
                            int output_device_ID,
                            int number_of_output_channels,
                            int input_device_ID,
                            int number_of_input_channels,
                            int sampling_rate,
                            int audio_block_size) {
        if (fInstance == null) {
            fInstance = new DSP(callback, number_of_output_channels, number_of_input_channels);
            AudioDeviceConfiguration mConfig = new AudioDeviceConfiguration();
            mConfig.sample_rate = sampling_rate;
            mConfig.sample_buffer_size = audio_block_size;
            mConfig.output_device_ID = output_device_ID;
            mConfig.number_of_output_channels = number_of_output_channels;
            mConfig.input_device_ID = input_device_ID;
            mConfig.number_of_input_channels = number_of_input_channels;
            fAudioBufferManager = new AudioBufferManager(fInstance, mConfig);
        }
        return fInstance;
    }

    /**
     * @param callback      object which implements the <code>audioblock(...)</code> method
     * @param configuration audio device configuration
     * @return reference to DSP instance
     */
    public static DSP start(Object callback, AudioDeviceConfiguration configuration) {
        if (fInstance == null) {
            fInstance = new DSP(callback,
                                configuration.number_of_output_channels,
                                configuration.number_of_input_channels);
            fAudioBufferManager = new AudioBufferManager(fInstance, configuration);
        }
        return fInstance;
    }

    /**
     *
     */
    public static void stop() {
        if (fAudioBufferManager != null) {
            fAudioBufferManager.exit();
        }
        fInstance = null;
        fAudioBufferManager = null;
    }

    public void audioblock(float[][] output_signal, float[][] input_signal) {
        try {
            Arrays.fill(fCachedBuffers, null);
            if (fNumberOutputChannels == 1 && fNumberInputChannels == 0) {
                if (fProcessPerSample) {
                    for (int i = 0; i < output_signal[0].length; i++) {
                        output_signal[0][i] = (Float) fMethod.invoke(fListener);
                    }
                } else {
                    //noinspection PrimitiveArrayArgumentToVarargsMethod
                    fMethod.invoke(fListener, output_signal[0]);
                }
                fCachedBuffers[OUTPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(output_signal[0]) : output_signal[0];
            } else if (fNumberOutputChannels == 1 && fNumberInputChannels == 1) {
                if (fProcessPerSample) {
                    for (int i = 0; i < output_signal[0].length; i++) {
                        output_signal[0][i] = (Float) fMethod.invoke(fListener, input_signal[0][i]);
                    }
                } else {
                    fMethod.invoke(fListener, output_signal[0], input_signal[0]);
                }
                fCachedBuffers[OUTPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(output_signal[0]) : output_signal[0];
                fCachedBuffers[INPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(input_signal[0]) : input_signal[0];
            } else if (fNumberOutputChannels == 2 && fNumberInputChannels == 0) {
                fMethod.invoke(fListener, output_signal[0], output_signal[1]);
                fCachedBuffers[OUTPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(output_signal[0]) : output_signal[0];
                fCachedBuffers[OUTPUT_RIGHT] = COPY_CACHED_BUFFER ? Wellen.copy(output_signal[1]) : output_signal[1];
            } else if (fNumberOutputChannels == 2 && fNumberInputChannels == 1) {
                fMethod.invoke(fListener, output_signal[0], output_signal[1], input_signal[0]);
                fCachedBuffers[OUTPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(output_signal[0]) : output_signal[0];
                fCachedBuffers[OUTPUT_RIGHT] = COPY_CACHED_BUFFER ? Wellen.copy(output_signal[1]) : output_signal[1];
                fCachedBuffers[INPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(input_signal[0]) : input_signal[0];
            } else if (fNumberOutputChannels == 2 && fNumberInputChannels == 2) {
                fMethod.invoke(fListener, output_signal[0], output_signal[1], input_signal[0], input_signal[1]);
                fCachedBuffers[OUTPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(output_signal[0]) : output_signal[0];
                fCachedBuffers[OUTPUT_RIGHT] = COPY_CACHED_BUFFER ? Wellen.copy(output_signal[1]) : output_signal[1];
                fCachedBuffers[INPUT_LEFT] = COPY_CACHED_BUFFER ? Wellen.copy(input_signal[0]) : input_signal[0];
                fCachedBuffers[INPUT_RIGHT] = COPY_CACHED_BUFFER ? Wellen.copy(input_signal[1]) : input_signal[1];
            } else {
                fMethod.invoke(fListener, output_signal, input_signal);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                 NullPointerException ex) {
            System.err.println("+++ @" + DSP.class.getSimpleName() + " / error in audioblock: " + ex.getCause());
            ex.printStackTrace();
        }
    }
}
