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

import processing.core.PApplet;
import processing.core.PGraphics;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * contains constants and utility methods for the wellen library.
 */
public class Wellen {

    public static final int DEFAULT_AUDIOBLOCK_SIZE = 512;
    public static final int DEFAULT_AUDIO_DEVICE = -1;
    public static final int DEFAULT_NUMBER_OF_INSTRUMENTS = 16;
    public static final int DEFAULT_SAMPLING_RATE = 44100;
    public static final int DEFAULT_WAVETABLE_SIZE = 512;
    public static final float DEFAULT_ATTACK = 0.005f;
    public static final float DEFAULT_DECAY = 0.01f;
    public static final float DEFAULT_RELEASE = 0.075f;
    public static final float DEFAULT_SUSTAIN = 0.5f;
    public static final int DISTORTION_HARD_CLIPPING = 0;
    public static final int DISTORTION_FOLDBACK = 1;
    public static final int DISTORTION_FOLDBACK_SINGLE = 2;
    public static final int DISTORTION_FULL_WAVE_RECTIFICATION = 3;
    public static final int DISTORTION_HALF_WAVE_RECTIFICATION = 4;
    public static final int DISTORTION_INFINITE_CLIPPING = 5;
    public static final int DISTORTION_SOFT_CLIPPING_CUBIC = 6;
    public static final int DISTORTION_SOFT_CLIPPING_ARC_TANGENT = 7;
    public static final int DISTORTION_BIT_CRUSHING = 8;
    public static final int FILTER_MODE_LOWPASS = 0;
    public static final int FILTER_MODE_HIGHPASS = 1;
    public static final int FILTER_MODE_BANDPASS = 2;
    public static final int NOISE_WHITE = 0;
    public static final int NOISE_GAUSSIAN_WHITE = 1;
    public static final int NOISE_GAUSSIAN_WHITE2 = 2;
    public static final int NOISE_PINK = 3;
    public static final int NOISE_PINK2 = 4;
    public static final int NOISE_PINK3 = 5;
    public static final int NOISE_SIMPLEX = 6;
    public static final int NO_CHANNELS = 0;
    public static final String TONE_ENGINE_INTERNAL = "internal";
    public static final String TONE_ENGINE_MIDI = "midi";
    public static final String TONE_ENGINE_OSC = "osc";
    public static final int TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT = -2;
    public static final int WAVESHAPE_SINE = 0;
    public static final int WAVESHAPE_TRIANGLE = 1;
    public static final int WAVESHAPE_SAWTOOTH = 2;
    public static final int WAVESHAPE_SQUARE = 3;
    public static final int WAVESHAPE_NOISE = 4;
    public static final int OSC_NOISE = WAVESHAPE_NOISE;
    public static final int OSC_SAWTOOTH = WAVESHAPE_SAWTOOTH;
    public static final int OSC_SINE = WAVESHAPE_SINE;
    public static final int OSC_SQUARE = WAVESHAPE_SQUARE;
    public static final int OSC_TRIANGLE = WAVESHAPE_TRIANGLE;
    public static final int WAV_FORMAT_IEEE_FLOAT_32BIT = 3;
    public static final int WAV_FORMAT_PCM = 1;
    public static final int EVENT_UNDEFINED = -1;
    public static final int EVENT_NOTE_ON = 0;
    public static final int EVENT_NOTE_OFF = 1;
    public static final int EVENT_CONTROLCHANGE = 2;
    public static final int EVENT_PITCHBEND = 3;
    public static final int EVENT_PROGRAMCHANGE = 4;
    public static final int EVENT_CHANNEL = 0;
    public static final int EVENT_NOTE = 1;
    public static final int EVENT_VELOCITY = 2;
    public static final int PAN_LINEAR = 0;
    public static final int PAN_SQUARE_LAW = 1;
    public static final int PAN_SINE_LAW = 2;
    public static final int SIGNAL_MONO = 0;
    public static final int SIGNAL_LEFT = 0;
    public static final int SIGNAL_RIGHT = 1;

    public static int clamp127(int pValue) {
        return Math.max(0, Math.min(127, pValue));
    }

    public static void dumpMidiOutputDevices() {
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
    }

    public static void dumpMidiInputDevices() {
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
    }

    public static void dumpAudioInputAndOutputDevices() {
        System.out.println("+-------------------------------------------------------+");
        System.out.println("+ AUDIO DEVICES ( Audio System )");
        System.out.println("+-------------------------------------------------------+");

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
                final String mID = i + "\t:";
                final String mName = AudioSystem.getMixerInfo()[i].getName();
                System.out.println(
                "+ " + mID + " ( IN:" + mInputChannels + " / OUT:" + mOutputChannels + " ) : \"" + mName + "\"");
            }
        }
        System.out.println("+-------------------------------------------------------+");
        System.out.println();
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

    public static void draw_buffer(PGraphics g, float pWidth, float pHeight, float[] pBuffer, int pStride) {
        g.line(0, pHeight * 0.5f, pWidth, pHeight * 0.5f);
        if (pBuffer != null) {
            for (int i = 0; i < pBuffer.length - pStride; i += pStride) {
                g.line(PApplet.map(i, 0, pBuffer.length, 0, pWidth),
                       PApplet.map(pBuffer[i], -1.0f, 1.0f, 0, pHeight),
                       PApplet.map(i + pStride, 0, pBuffer.length, 0, pWidth),
                       PApplet.map(pBuffer[i + pStride], -1.0f, 1.0f, 0, pHeight));
            }
        }
    }

    public static void draw_buffer(PGraphics g, float pWidth, float pHeight, float[] pBuffer) {
        draw_buffer(g, pWidth, pHeight, pBuffer, 1);
    }

    public static void draw_buffer(PGraphics g, float pWidth, float pHeight, float[] pBufferLeft,
                                   float[] pBufferRight) {
        g.pushMatrix();
        draw_buffer(g, pWidth, pHeight * 0.5f, pBufferLeft);
        g.translate(0, pHeight * 0.5f);
        draw_buffer(g, pWidth, pHeight * 0.5f, pBufferRight);
        g.popMatrix();
    }

    public static float random(float pMin, float pMax) {
        if (pMin >= pMax) {
            return pMin;
        } else {
            final float mDiff = pMax - pMin;
            final float mValue = (float) Math.random() * mDiff + pMin;
            return mValue;
        }
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

    public static void exportWAV(PApplet p, String pFilepath, float[][] pBuffer, int pBitsPerSignal, int pSignalRate,
                                 int pCompressionType) {
        if (pCompressionType == WAV_FORMAT_IEEE_FLOAT_32BIT && pBitsPerSignal != 32) {
            System.err.println("+++ WARNING @" + Wellen.class.getSimpleName() + ".exportWAV / if WAV format is *IEEE "
                               + "float* 32 bits per sample are required.");
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

    public static void bytes_to_floatIEEEs(byte[] pBytes, float[] pSignal, boolean pLittleEndian) {
        if (pBytes.length / 4 == pSignal.length) {
            for (int i = 0; i < pSignal.length; i++) {
                pSignal[i] = bytes_to_floatIEEE(pBytes, i * 4, (i + 1) * 4, pLittleEndian);
            }
        } else {
            System.err.println(
            "+++ WARNING @ " + Wellen.class.getSimpleName() + " / array sizes do not match. make " + "sure byte array" +
            " is exactly 4 times the size of float array");
        }
    }

    public static float bytes_to_floatIEEE(byte[] b, boolean pLittleEndian) {
        if (b.length != 4) {
            System.err.println("+++ WARNING @ " + Wellen.class.getSimpleName() + " / expected exactly 4 bytes.");
        }
        return ByteBuffer.wrap(b).order(pLittleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).getFloat();
    }

    public static float bytes_to_floatIEEE(byte[] b) {
        return bytes_to_floatIEEE(b, true);
    }

    public static byte[] floatIEEE_to_bytes(float f) {
        return ByteBuffer.allocate(4).putFloat(f).array();
    }

    public static byte[] floatIEEEs_to_bytes(float[] pFloats) {
        return floatIEEEs_to_bytes(pFloats, true);
    }

    public static byte[] floatIEEEs_to_bytes(float[] pFloats, boolean pLittleEndian) {
        ByteBuffer buffer = ByteBuffer.allocate(4 * pFloats.length).order(pLittleEndian ? ByteOrder.LITTLE_ENDIAN :
                                                                          ByteOrder.BIG_ENDIAN);

        for (float value : pFloats) {
            buffer.putFloat(value);
        }
        return buffer.array();
    }

    public static float bytes_to_floatIEEE(byte[] pBytes, int pStart, int pEnd, boolean pLittleEndian) {
        final byte[] mBytes = Arrays.copyOfRange(pBytes, pStart, pEnd);
        return bytes_to_floatIEEE(mBytes, pLittleEndian);
    }

    public static String get_resource_path() {
        return Wellen.class.getResource("").getPath();
    }

    public static void run_sketch_with_resources(Class<? extends PApplet> pSketch) {
        PApplet.runSketch(new String[]{"--sketch-path=" + Wellen.get_resource_path(), pSketch.getName()}, null);
    }

    public static float to_sec(int pMilliSeconds) {
        return pMilliSeconds / 1000.0f;
    }

    public static int to_millis(float pSeconds) {
        return (int) (pSeconds * 1000);
    }
}
