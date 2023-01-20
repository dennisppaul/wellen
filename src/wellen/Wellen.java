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

package wellen;

import processing.core.PApplet;
import processing.core.PGraphics;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import static processing.core.PApplet.day;
import static processing.core.PApplet.hour;
import static processing.core.PApplet.minute;
import static processing.core.PApplet.month;
import static processing.core.PApplet.nf;
import static processing.core.PApplet.second;
import static processing.core.PApplet.year;

/**
 * contains constants and utility methods for the wellen library.
 */
public class Wellen {

    public static final int BITS_PER_SAMPLE_16 = 16;
    public static final int BITS_PER_SAMPLE_24 = 24;
    public static final int BITS_PER_SAMPLE_32 = 32;
    public static final int BITS_PER_SAMPLE_8 = 8;
    public static final float DEFAULT_ATTACK = 0.005f;
    public static final int DEFAULT_AUDIOBLOCK_SIZE = 1024;
    public static final int DEFAULT_AUDIO_DEVICE = -1;
    public static final int DEFAULT_BITS_PER_SAMPLE = BITS_PER_SAMPLE_24;
    public static final float DEFAULT_DECAY = 0.01f;
    public static final float DEFAULT_FILTER_BANDWIDTH = 100.0f;
    public static final float DEFAULT_FILTER_FREQUENCY = 1000.0f;
    public static final int DEFAULT_NUMBER_OF_INSTRUMENTS = 16;
    public static final float DEFAULT_RELEASE = 0.075f;
    public static final int DEFAULT_SAMPLING_RATE = 48000;
    public static final int DEFAULT_INTERPOLATE_AMP_FREQ_DURATION = Wellen.millis_to_samples(5);
    public static final float DEFAULT_SUSTAIN = 0.5f;
    public static final int DEFAULT_WAVETABLE_SIZE = 512;
    public static final int DISTORTION_BIT_CRUSHING = 8;
    public static final int DISTORTION_FOLDBACK = 1;
    public static final int DISTORTION_FOLDBACK_SINGLE = 2;
    public static final int DISTORTION_FULL_WAVE_RECTIFICATION = 3;
    public static final int DISTORTION_HALF_WAVE_RECTIFICATION = 4;
    public static final int DISTORTION_HARD_CLIPPING = 0;
    public static final int DISTORTION_INFINITE_CLIPPING = 5;
    public static final int DISTORTION_SOFT_CLIPPING_ARC_TANGENT = 7;
    public static final int DISTORTION_SOFT_CLIPPING_CUBIC = 6;
    public static final int EVENT_CHANNEL = 0;
    public static final int EVENT_CONTROLCHANGE = 2;
    public static final int EVENT_NOTE = 1;
    public static final int EVENT_NOTE_OFF = 1;
    public static final int EVENT_NOTE_ON = 0;
    public static final int EVENT_PITCHBEND = 3;
    public static final int EVENT_PROGRAMCHANGE = 4;
    public static final int EVENT_UNDEFINED = -1;
    public static final int EVENT_VELOCITY = 2;
    public static final int FILTER_MODE_BAND_PASS = 2;
    public static final int FILTER_MODE_BAND_REJECT = 7;
    public static final int FILTER_MODE_HIGHSHELF = 6;
    public static final int FILTER_MODE_HIGH_PASS = 1;
    public static final int FILTER_MODE_LOWSHELF = 5;
    public static final int FILTER_MODE_LOW_PASS = 0;
    public static final int FILTER_MODE_NOTCH = 3;
    public static final int FILTER_MODE_PEAK = 4;
    public static final int LOOP_INFINITE = Integer.MAX_VALUE;
    public static final int MONO = 1;
    public static final int NOISE_GAUSSIAN_WHITE = 1;
    public static final int NOISE_GAUSSIAN_WHITE2 = 2;
    public static final int NOISE_PINK = 3;
    public static final int NOISE_PINK2 = 4;
    public static final int NOISE_PINK3 = 5;
    public static final int NOISE_SIMPLEX = 6;
    public static final int NOISE_WHITE = 0;
    public static final int NOTE_EIGHTH = 2;
    public static final float NOTE_HALF = 0.5f;
    public static final int NOTE_QUARTER = 1;
    public static final int NOTE_SIXTEENTH = 4;
    public static final int NOTE_THIRTYSECOND = 8;
    public static final float NOTE_WHOLE = 0.25f;
    public static final int NO_AUDIO_DEVICE = -2;
    public static final int NO_CHANNELS = 0;
    public static final int NO_EVENT = -1;
    public static final int NO_INPOINT = 0;
    public static final int NO_LOOP = -2;
    public static final int NO_LOOP_COUNT = -1;
    public static final int NO_OUTPOINT = -1;
    public static final int NO_POSITION = -1;
    public static final int NO_VALUE = -1;
    public static final int PAN_LINEAR = 0;
    public static final int PAN_SINE_LAW = 2;
    public static final int PAN_SQUARE_LAW = 1;
    public static final int SIGNAL_LEFT = 0;
    public static final float SIGNAL_MAX = 1.0f;
    public static final float SIGNAL_MIN = -1.0f;
    public static final int SIGNAL_MONO = 1;
    public static final int SIGNAL_PROCESSING_IGNORE_IN_OUTPOINTS = -3;
    public static final int SIGNAL_RIGHT = 1;
    public static final int SIGNAL_STEREO = 2;
    public static final int SIG_INT16_BIG_ENDIAN = 2;
    public static final int SIG_INT16_LITTLE_ENDIAN = 3;
    public static final int SIG_INT24_3_BIG_ENDIAN = 4;
    public static final int SIG_INT24_3_LITTLE_ENDIAN = 5;
    public static final int SIG_INT24_4_BIG_ENDIAN = 6;
    public static final int SIG_INT24_4_LITTLE_ENDIAN = 7;
    public static final int SIG_INT32_BIG_ENDIAN = 8;
    public static final int SIG_INT32_LITTLE_ENDIAN = 9;
    public static final int SIG_INT8 = 0;
    public static final int SIG_UINT8 = 1;
    public static final int STEREO = 2;
    public static final String TONE_ENGINE_INTERNAL = "internal";
    public static final int TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT = -2;
    public static final String TONE_ENGINE_MIDI = "midi";
    public static final String TONE_ENGINE_OSC = "osc";
    public static final float TWO_PI = PApplet.TWO_PI;
    public static final int VERSION_MAJOR = 0;
    public static final int VERSION_MINOR = 8;
    public static final int WAVEFORM_NOISE = 4;
    /**
     * @deprecated use WAVEFORM_ instead
     */
    @Deprecated
    public static final int OSC_NOISE = WAVEFORM_NOISE;
    public static final int WAVEFORM_SAWTOOTH = 2;
    public static final int OSC_SAWTOOTH = WAVEFORM_SAWTOOTH;
    public static final int WAVEFORM_SINE = 0;
    public static final int OSC_SINE = WAVEFORM_SINE;
    public static final int WAVEFORM_SQUARE = 3;
    public static final int OSC_SQUARE = WAVEFORM_SQUARE;
    public static final int WAVEFORM_TRIANGLE = 1;
    public static final int OSC_TRIANGLE = WAVEFORM_TRIANGLE;
    public static final int WAVESHAPE_INTERPOLATE_CUBIC = 2;
    public static final int WAVESHAPE_INTERPOLATE_LINEAR = 1;
    public static final int WAVESHAPE_INTERPOLATE_NONE = 0;
    /**
     * @deprecated use WAVEFORM_ instead
     */
    @Deprecated
    public static final int WAVESHAPE_NOISE = 4;
    /**
     * @deprecated use WAVEFORM_ instead
     */
    @Deprecated
    public static final int WAVESHAPE_SAWTOOTH = 2;
    /**
     * @deprecated use WAVEFORM_ instead
     */
    @Deprecated()
    public static final int WAVESHAPE_SINE = 0;
    /**
     * @deprecated use WAVEFORM_ instead
     */
    @Deprecated
    public static final int WAVESHAPE_SQUARE = 3;
    /**
     * @deprecated use WAVEFORM_ instead
     */
    @Deprecated
    public static final int WAVESHAPE_TRIANGLE = 1;
    public static final int WAV_FORMAT_IEEE_FLOAT_32BIT = 3;
    public static final int WAV_FORMAT_PCM = 1;
    private static final float SIG_16BIT_MAX = 32768.0f;
    private static final float SIG_16BIT_MAX_INVERSE = 1.0f / SIG_16BIT_MAX;
    private static final float SIG_24BIT_MAX = 8388608.0f;
    private static final float SIG_24BIT_MAX_INVERSE = 1.0f / SIG_24BIT_MAX;
    private static final float SIG_32BIT_MAX = 2147483648.0f;
    private static final float SIG_32BIT_MAX_INVERSE = 1.0f / SIG_32BIT_MAX;
    private static final float SIG_8BIT_MAX = 128.0f;
    private static final float SIG_8BIT_MAX_INVERSE = 1.0f / SIG_8BIT_MAX;

    public static float bytes_to_floatIEEE(byte[] b, boolean pLittleEndian) {
        if (b.length != 4) {
            System.err.println("+++ WARNING @ " + Wellen.class.getSimpleName() + " / expected exactly 4 bytes.");
        }
        return ByteBuffer.wrap(b).order(pLittleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).getFloat();
    }

    public static float bytes_to_floatIEEE(byte[] b) {
        return bytes_to_floatIEEE(b, true);
    }

    public static float bytes_to_floatIEEE(byte[] pBytes, int pStart, int pEnd, boolean pLittleEndian) {
        final byte[] mBytes = Arrays.copyOfRange(pBytes, pStart, pEnd);
        return bytes_to_floatIEEE(mBytes, pLittleEndian);
    }

    public static void bytes_to_floatIEEEs(byte[] pBytes, float[] pSignal, boolean pLittleEndian) {
        if (pBytes.length / 4 == pSignal.length) {
            for (int i = 0; i < pSignal.length; i++) {
                pSignal[i] = bytes_to_floatIEEE(pBytes, i * 4, (i + 1) * 4, pLittleEndian);
            }
        } else {
            System.err.println("+++ WARNING @ " + Wellen.class.getSimpleName() + " / array sizes do not match. make " + "sure byte " + "array" + " is exactly 4 times the size of float array");
        }
    }

    public static void bytes_to_floats(byte[] pBytes, float[] pFloats, int pBitsPerFloat) {
        final int mBytesPerFloat = pBitsPerFloat / 8;
        for (int i = 0; i < pFloats.length; i++) {
            final double mScale = 1.0 / ((1 << (pBitsPerFloat - 1)) - 1);
            long f = 0;
            for (int j = 0; j < mBytesPerFloat; j++) {
                final long mBitShift = j * 8;
                long b = pBytes[i * mBytesPerFloat + j];
                f += b << mBitShift;
            }
            pFloats[i] = (float) (f * mScale);
        }
    }

    public static float bytes_to_floats(int pFormat, byte[] pInput, int pIndex) {
        final float f;
        switch (pFormat) {
            case SIG_INT8:
                f = pInput[pIndex] * SIG_8BIT_MAX_INVERSE;
                break;
            case SIG_UINT8:
                f = ((pInput[pIndex] & 0xFF) - 128) * SIG_8BIT_MAX_INVERSE;
                break;
            case SIG_INT16_BIG_ENDIAN:
                f = ((pInput[pIndex] << 8) | (pInput[pIndex + 1] & 0xFF)) * SIG_16BIT_MAX_INVERSE;
                break;
            case SIG_INT16_LITTLE_ENDIAN:
                f = ((pInput[pIndex + 1] << 8) | (pInput[pIndex] & 0xFF)) * SIG_16BIT_MAX_INVERSE;
                break;
            case SIG_INT24_3_BIG_ENDIAN:
                f = ((pInput[pIndex] << 16) | ((pInput[pIndex + 1] & 0xFF) << 8) | (pInput[pIndex + 2] & 0xFF)) * SIG_24BIT_MAX_INVERSE;
                break;
            case SIG_INT24_3_LITTLE_ENDIAN:
                f = ((pInput[pIndex + 2] << 16) | ((pInput[pIndex + 1] & 0xFF) << 8) | (pInput[pIndex] & 0xFF)) * SIG_24BIT_MAX_INVERSE;
                break;
            case SIG_INT24_4_BIG_ENDIAN:
                f = ((pInput[pIndex + 1] << 16) | ((pInput[pIndex + 2] & 0xFF) << 8) | (pInput[pIndex + 3] & 0xFF)) * SIG_24BIT_MAX_INVERSE;
                break;
            case SIG_INT24_4_LITTLE_ENDIAN:
                f = ((pInput[pIndex + 3] << 16) | ((pInput[pIndex + 2] & 0xFF) << 8) | (pInput[pIndex + 1] & 0xFF)) * SIG_24BIT_MAX_INVERSE;
                break;
            case SIG_INT32_BIG_ENDIAN:
                f = ((pInput[pIndex] << 24) | ((pInput[pIndex + 1] & 0xFF) << 16) | ((pInput[pIndex + 2] & 0xFF) << 8) | (pInput[pIndex + 3] & 0xFF)) * SIG_32BIT_MAX_INVERSE;
                break;
            case SIG_INT32_LITTLE_ENDIAN:
                f = ((pInput[pIndex + 3] << 24) | ((pInput[pIndex + 2] & 0xFF) << 16) | ((pInput[pIndex + 1] & 0xFF) << 8) | (pInput[pIndex] & 0xFF)) * SIG_32BIT_MAX_INVERSE;
                break;
            default:
                f = 0.0f;
        }
        return f;
    }

    public static float clamp(float pValue) {
        if (pValue > 1.0f) {
            return 1.0f;
        } else if (pValue < -1.0f) {
            return -1.0f;
        } else {
            return pValue;
        }
//        return Math.max(pMin, Math.min(pMax, pValue));
    }

    public static float clamp(float pValue, float pMin, float pMax) {
        if (pValue > pMax) {
            return pMax;
        } else if (pValue < pMin) {
            return pMin;
        } else {
            return pValue;
        }
//        return Math.max(pMin, Math.min(pMax, pValue));
    }

    public static int clamp(int pValue, int pMin, int pMax) {
        if (pValue > pMax) {
            return pMax;
        } else if (pValue < pMin) {
            return pMin;
        } else {
            return pValue;
        }
//        return Math.max(pMin, Math.min(pMax, pValue));
    }

    public static int clamp127(int pValue) {
        return Math.max(0, Math.min(127, pValue));
    }

    public static int constrain(int value, int min, int max) {
        if (value > max) {
            value = max;
        }
        if (value < min) {
            value = min;
        }
        return value;
    }

    /**
     * copy the content of one array to another array of the same length
     *
     * @param source      source array
     * @param destination destination array with same length as source array
     */
    public static void copy(float[] source, float[] destination) {
        if (source.length == destination.length) {
            System.arraycopy(source, 0, destination, 0, destination.length);
        }
    }

    /**
     * copy the content of one array to a new array
     *
     * @param source source array
     * @return the new array containing a copy of source array
     */
    public static float[] copy(float[] source) {
        float[] destination = new float[source.length];
        System.arraycopy(source, 0, destination, 0, destination.length);
        return destination;
    }

    public static void draw_buffer(PGraphics g, float pWidth, float pHeight, float[] pBuffer, int pStride) {
        g.line(0, pHeight * 0.5f, pWidth, pHeight * 0.5f);
        if (pBuffer != null) {
            for (int i = 0; i < pBuffer.length - pStride; i += pStride) {
                if (!Float.isNaN(pBuffer[i]) && !Float.isNaN(pBuffer[i + pStride])) {
                    g.line(PApplet.map(i, 0, pBuffer.length, 0, pWidth),
                           PApplet.map(pBuffer[i], -1.0f, 1.0f, 0, pHeight),
                           PApplet.map(i + pStride, 0, pBuffer.length, 0, pWidth),
                           PApplet.map(pBuffer[i + pStride], -1.0f, 1.0f, 0, pHeight));
                }
            }
        }
    }

    public static void draw_buffer(PGraphics g, float pWidth, float pHeight, float[] pBuffer) {
        draw_buffer(g, pWidth, pHeight, pBuffer, 1);
    }

    public static void draw_buffers(PGraphics g, float pWidth, float pHeight, float[]... pBuffers) {
        int mCountValidBuffers = 0;
        for (float[] pBuffer : pBuffers) {
            if (pBuffer != null) {
                mCountValidBuffers++;
            }
        }
        if (mCountValidBuffers == 0) {
            return;
        }
        final float mFraction = 1.0f / mCountValidBuffers;
        g.pushMatrix();
        for (float[] mBuffer : pBuffers) {
            if (mBuffer != null) {
                draw_buffer(g, pWidth, pHeight * mFraction, mBuffer);
                g.translate(0, pHeight * mFraction);
            }
        }
        g.popMatrix();
    }

    public static void draw_tone(PGraphics g, float pWidth, float pHeight) {
        draw_buffer(g, pWidth, pHeight, Tone.get_buffer());
    }

    public static void draw_tone_stereo(PGraphics g, float pWidth, float pHeight) {
        draw_buffers(g, pWidth, pHeight, Tone.get_buffer_left(), Tone.get_buffer_right());
    }

    public static void dumpAudioInputAndOutputDevices(boolean pPrintFormats) {
        queryAudioInputAndOutputDevices(null, true, pPrintFormats);
    }

    public static void dumpAudioInputAndOutputDevices() {
        queryAudioInputAndOutputDevices(null, true, false);
    }

    public static String[] dumpMidiInputDevices() {
        final String[] mInputNames = MidiIn.availableInputs();
        System.out.println("+-------------------------------------------------------+");
        System.out.println("+ MIDI INPUT DEVICES ( aka Ports or Buses )");
        System.out.println("+-------------------------------------------------------+");
        for (int i = 0; i < mInputNames.length; i++) {
            final String mInputName = mInputNames[i];
            System.out.println("+ " + i + "\t: \"" + mInputName + "\"");
        }
        System.out.println("+-------------------------------------------------------+");
        System.out.println();
        return mInputNames;
    }

    public static String[] dumpMidiOutputDevices() {
        final String[] mOutputNames = MidiOut.availableOutputs();
        System.out.println("+-------------------------------------------------------+");
        System.out.println("+ MIDI OUTPUT DEVICES ( aka Ports or Buses )");
        System.out.println("+-------------------------------------------------------+");
        for (int i = 0; i < mOutputNames.length; i++) {
            final String mOutputName = mOutputNames[i];
            System.out.println("+ " + i + "\t: \"" + mOutputName + "\"");
        }
        System.out.println("+-------------------------------------------------------+");
        System.out.println();
        return mOutputNames;
    }

    public static void exportWAV(PApplet p,
                                 String pFilepath,
                                 float[][] pBuffer,
                                 int pBitsPerSignal,
                                 int pSignalRate,
                                 int pCompressionType) {
        if (pCompressionType == WAV_FORMAT_IEEE_FLOAT_32BIT && pBitsPerSignal != 32) {
            System.err.println("+++ WARNING @" + Wellen.class.getSimpleName() + ".exportWAV / if WAV format is *IEEE "
                                       + "float* 32 " + "bits" + " per sample are required.");
            pBitsPerSignal = 32;
        }
        final byte[] mWAVBytes = WAVConverter.convert_samples_to_bytes(pBuffer,
                                                                       pBuffer.length,
                                                                       pBitsPerSignal,
                                                                       pSignalRate,
                                                                       pCompressionType);
        p.saveBytes(pFilepath, mWAVBytes);
    }

    public static void exportWAV(PApplet p, String pFilepath, float[][] pBuffer, int pBitsPerSignal, int pSignalRate) {
        final byte[] mWAVBytes = WAVConverter.convert_samples_to_bytes(pBuffer,
                                                                       pBuffer.length,
                                                                       pBitsPerSignal,
                                                                       pSignalRate);
        p.saveBytes(pFilepath, mWAVBytes);
    }

    public static void exportWAV(PApplet p, String pFilepath, float[] pBuffer, int pBitsPerSignal, int pSignalRate) {
        final byte[] mWAVBytes = WAVConverter.convert_samples_to_bytes(new float[][]{pBuffer},
                                                                       1,
                                                                       pBitsPerSignal,
                                                                       pSignalRate);
        p.saveBytes(pFilepath, mWAVBytes);
    }

    public static void exportWAVInfo(PApplet p, String pFilepath, WAVConverter.Info pWAVInfo) {
        final byte[] mWAVBytes = WAVConverter.convert_samples_to_bytes(pWAVInfo);
        p.saveBytes(pFilepath, mWAVBytes);
    }

    public static int[] find_zero_crossings(float[] pSampleData, final int pInPoint, final int pOutPoint) {
        final int ZERO_CROSSING_EDGE_NONE = 0;
        final int ZERO_CROSSING_EDGE_RISING = 1;
        final int ZERO_CROSSING_EDGE_FALLING = -1;
        int mAdaptedInPoint = pInPoint;
        int mAdaptedOutPoint = pOutPoint;
        if (pInPoint > 0 && pOutPoint > 0 && pInPoint < pSampleData.length - 1 && pOutPoint < pSampleData.length - 1) {
            int mInPointEdgeKind = ZERO_CROSSING_EDGE_NONE;
            {
                float mInValue = pSampleData[pInPoint];
                if (mInValue != 0.0f) {
                    for (int i = pInPoint + 1; i < pSampleData.length; i++) {
                        float v = pSampleData[i];
                        boolean mRisingEdge = (mInValue < 0 && v >= 0);
                        boolean mFallingEdge = (mInValue > 0 && v <= 0);
                        if (mRisingEdge || mFallingEdge) {
                            mAdaptedInPoint = i;
                            mInPointEdgeKind = mRisingEdge ? ZERO_CROSSING_EDGE_RISING : ZERO_CROSSING_EDGE_FALLING;
                            break;
                        }
                    }
                }
            }
            {
                float mOutValue = pSampleData[pOutPoint];
                if (mOutValue != 0.0f && pOutPoint > 0) {
                    for (int i = pOutPoint - 1; i > 0; i--) {
                        float v = pSampleData[i];
                        boolean mRisingEdge = (mOutValue < 0 && v >= 0);
                        boolean mFallingEdge = (mOutValue > 0 && v <= 0);
                        if (mInPointEdgeKind == 0 && (mRisingEdge || mFallingEdge)) {
                            mAdaptedOutPoint = i;
                            break;
                        } else if ((mRisingEdge && mInPointEdgeKind == -1) || (mFallingEdge && mInPointEdgeKind == 1)) {
                            mAdaptedOutPoint = i;
                            break;
                        }
                    }
                }
            }
        }
        return new int[]{mAdaptedInPoint, mAdaptedOutPoint};
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

    public static byte[] floatIEEE_to_bytes(float f) {
        return ByteBuffer.allocate(4).putFloat(f).array();
    }

    public static byte[] floatIEEEs_to_bytes(float[] pFloats) {
        return floatIEEEs_to_bytes(pFloats, true);
    }

    public static byte[] floatIEEEs_to_bytes(float[] pFloats, boolean pLittleEndian) {
        ByteBuffer buffer = ByteBuffer.allocate(4 * pFloats.length)
                                      .order(pLittleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);

        for (float value : pFloats) {
            buffer.putFloat(value);
        }
        return buffer.array();
    }

    public static void floats_to_bytes(byte[] pBytes, float[] pFloats, int pBitsPerFloat) {
        final int mBytesPerFloat = pBitsPerFloat / 8;
        for (int i = 0; i < pFloats.length; i++) {
            final float f = pFloats[i];
            final int mScale = (1 << (pBitsPerFloat - 1)) - 1;
            final long y = (long) (mScale * f);
            for (int j = 0; j < mBytesPerFloat; j++) {
                final int mBitShift = j * 8;
                pBytes[i * mBytesPerFloat + j] = (byte) ((y >>> mBitShift) & 0xFF);
            }
        }
    }

    public static float[] get_extremum(float[] pSignal) {
        float mMaximum = Float.MIN_VALUE;
        float mMinimum = Float.MAX_VALUE;
        for (float f : pSignal) {
            if (f > mMaximum) {
                mMaximum = f;
            }
            if (f < mMinimum) {
                mMinimum = f;
            }
        }
        return new float[]{mMinimum, mMaximum};
    }

    public static String get_resource_path() {
        return Wellen.class.getResource("").getPath();
    }

    public static float[][] importWAV(PApplet p, String pFilepath) {
        byte[] mWAVBytes = p.loadBytes(pFilepath);
        WAVConverter.Info mWAVStruct = WAVConverter.convert_bytes_to_samples(mWAVBytes);
        return mWAVStruct.samples;
    }

    public static WAVConverter.Info importWAVInfo(PApplet p, String pFilepath) {
        byte[] mWAVBytes = p.loadBytes(pFilepath);
        WAVConverter.Info mWAVInfo = WAVConverter.convert_bytes_to_samples(mWAVBytes);
        mWAVInfo.data = mWAVBytes;
        return mWAVInfo;
    }

    public static int millis_to_samples(float pMillis, float pSamplingRate) {
        return (int) (pMillis / 1000.0f * pSamplingRate);
    }

    public static int millis_to_samples(float pMillis) {
        return (int) (pMillis / 1000.0f * (float) DEFAULT_SAMPLING_RATE);
    }

    public static String now() {
        return nf(year(), 4) + nf(month(), 2) + nf(day(), 2) + "_" + nf(hour(), 2) + nf(minute(), 2) + nf(second(), 2);
    }

    public static int queryAudioInputAndOutputDevices(String pDeviceName,
                                                      boolean pPrintDevices,
                                                      boolean pPrintFormats) {
        if (AndroidProbe.isAndroid()) {
            System.out.println("+-------------------------------------------------------+");
            System.out.println("+ AUDIO DEVICES ( Audio System )                         ");
            System.out.println("+ querrying audio input and output devices on android is ");
            System.out.println("+ currently not supported.                               ");
            System.out.println("+-------------------------------------------------------+");
            return Wellen.DEFAULT_AUDIO_DEVICE;
        }

        if (pPrintDevices) {
            System.out.println("+-------------------------------------------------------+");
            System.out.println("+ AUDIO DEVICES ( Audio System )                         ");
            System.out.println("+-------------------------------------------------------+");
        }

        int mSelectedID = Wellen.DEFAULT_AUDIO_DEVICE;
        for (int i = 0; i < AudioSystem.getMixerInfo().length; i++) {
            int mInputChannels = 0;
            int mOutputChannels = 0;

            Mixer mMixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[i]);
            Line.Info[] mSourceLines = mMixer.getSourceLineInfo();
            for (Line.Info li : mSourceLines) {
                try {
                    final Line mLine = mMixer.getLine(li);
                    if (mLine instanceof SourceDataLine) {
                        SourceDataLine mDataLine = (SourceDataLine) mLine;
                        mOutputChannels = mDataLine.getFormat().getChannels();
                    }
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            }

            Line.Info[] targetLines = mMixer.getTargetLineInfo();
            for (Line.Info li : targetLines) {
                try {
                    final Line mLine = mMixer.getLine(li);
                    if (mLine instanceof TargetDataLine) {
                        TargetDataLine mDataLine = (TargetDataLine) mLine;
                        mInputChannels = mDataLine.getFormat().getChannels();
                    }
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            }

            if (mInputChannels + mOutputChannels > 0) {
                final String mID = i + getSpacesFrom(i, 3) + ":";
                final String mName = AudioSystem.getMixerInfo()[i].getName();
                if (pPrintDevices) {
                    System.out.println("+ ID #" + mID + " ( INPUT:" + mInputChannels + " / OUTPUT:" + mOutputChannels + " )" + " : " + "\"" + mName + "\"");
                    if (pPrintFormats) {
                        // @NOTE(only print signed, little endian formats)
                        if (mInputChannels > 0) {
                            printFormats("INPUT", TargetDataLine.class, mMixer.getTargetLineInfo());
                        }
                        if (mOutputChannels > 0) {
                            printFormats("OUTPUT", SourceDataLine.class, mMixer.getSourceLineInfo());
                        }
                    }
                }
                if (pDeviceName != null && pDeviceName.equalsIgnoreCase(mName)) {
                    mSelectedID = i;
                }
            }
        }
        if (pPrintDevices) {
            System.out.println("+-------------------------------------------------------+");
            System.out.println();
        }
        return mSelectedID;
    }

    public static float random(float pMin, float pMax) {
        if (pMin >= pMax) {
            return pMin;
        } else {
            final float mDiff = pMax - pMin;
            return (float) Math.random() * mDiff + pMin;
        }
    }

    public static void run_sketch_with_resources(Class<? extends PApplet> pSketch) {
        PApplet.runSketch(new String[]{"--sketch-path=" + Wellen.get_resource_path(), pSketch.getName()}, null);
    }

    public static float samples_to_millis(int pSamples, float pSamplingRate) {
        return pSamples * 1000.0f / pSamplingRate;
    }

    public static float samples_to_millis(int pSamples) {
        return (float) pSamples * 1000.0f / (float) DEFAULT_SAMPLING_RATE;
    }

    public static float samples_to_seconds(int pSamples, float pSamplingRate) {
        return pSamples / pSamplingRate;
    }

    public static float samples_to_seconds(int pSamples) {
        return (float) pSamples / (float) DEFAULT_SAMPLING_RATE;
    }

    public static int seconds_to_samples(float pSeconds, float pSamplingRate) {
        return (int) (pSeconds * pSamplingRate);
    }

    public static int seconds_to_samples(float pSeconds) {
        return (int) (pSeconds * (float) DEFAULT_SAMPLING_RATE);
    }

    public static int to_millis(float pSeconds) {
        return (int) (pSeconds * 1000);
    }

    public static float to_sec(int pMilliSeconds) {
        return pMilliSeconds / 1000.0f;
    }

    public static String version_string() {
        return VERSION_MAJOR + "." + VERSION_MINOR;
    }

    private static String getSpacesFrom(int pNumbers, int pTotalNumberOfCharacters) {
        int l = pTotalNumberOfCharacters - String.valueOf(pNumbers).length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < l; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private static void printFormats(String mFormatString, Class mDataLineInfoClass, Line.Info[] mLineInfos) {
        final String mIndentionString = "+     ";
        System.out.print(mIndentionString);
        System.out.print(mFormatString);
        System.out.print(" FORMATS:");
        System.out.println();
        for (Line.Info mLineInfo : mLineInfos) {
            if (mLineInfo instanceof DataLine.Info) {
                DataLine.Info mDataLineInfo = (DataLine.Info) mLineInfo;
                AudioFormat[] mFormats = mDataLineInfo.getFormats();
                if (mDataLineInfo.getLineClass() == mDataLineInfoClass) {
                    for (AudioFormat mFormat : mFormats) {
                        if (!mFormat.isBigEndian() && mFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
                            System.out.print(mIndentionString + "- ");
                            System.out.print(mFormat.getSampleSizeInBits() + "-bits, ");
                            System.out.print(mFormat.getChannels() + " channel" + (mFormat.getChannels() == 1 ? "" :
                                    "s") + (mFormat.getSampleRate() > 0 ? ", " : ""));
                            if (mFormat.getSampleRate() > 0) {
                                System.out.print((int) mFormat.getSampleRate() + "Hz");
                            }
                            System.out.println();
                        }
                    }
                }
            }
        }
    }
}
