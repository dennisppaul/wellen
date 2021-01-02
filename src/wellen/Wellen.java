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
import java.util.ArrayList;

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
    public static final int DISTORTION_CLIP = 0;
    public static final int DISTORTION_FOLDBACK = 1;
    public static final int DISTORTION_FOLDBACK_SINGLE = 2;
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
            System.out.println("+ " + PApplet.nf(i, 2) + " : " + mOutputName);
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
            System.out.println("+ " + PApplet.nf(i, 2) + " : " + mInputName);
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
                final String mID = PApplet.nf(i, 2);
                final String mName = AudioSystem.getMixerInfo()[i].getName();
                System.out.println("+ " + mID + " ( IN:" + mInputChannels + " / OUT:" + mOutputChannels + " ) : " + mName);
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

    public static void draw_buffer(PGraphics g, float pWidth, float pHeight, float[] pBuffer) {
        g.line(0, pHeight * 0.5f, pWidth, pHeight * 0.5f);
        if (pBuffer != null) {
            for (int i = 0; i < pBuffer.length - 1; i++) {
                g.line(PApplet.map(i, 0, pBuffer.length, 0, pWidth),
                       PApplet.map(pBuffer[i], -1.0f, 1.0f, 0, pHeight),
                       PApplet.map(i + 1, 0, pBuffer.length, 0, pWidth),
                       PApplet.map(pBuffer[i + 1], -1.0f, 1.0f, 0, pHeight));
            }
        }
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

//    public static void bytes_to_float32s(byte[] pBytes, float[] pFloats, boolean pLittleEndian) {
//        if (pBytes.length / 4 == pFloats.length) {
//            for (int i = 0; i < pFloats.length; i++) {
//                pFloats[i] = bytes_to_float32(pBytes, i * 4, (i + 1) * 4, pLittleEndian);
//            }
//        } else {
//            System.err.println("+++ WARNING @ Wavetable.from_bytes / array sizes do not match. make sure the byte "
//            + "array is exactly 4 times the size of the float array");
//        }
//    }

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

    public static class TestWAVWriter extends PApplet {

        WAVStruct mWAVStruct;

        public void settings() {
            size(640, 480);
        }

        public void setup() {
            int size = 44100;
            final float[][] buf = new float[2][size];
            for (int i = 0; i < size; i++) {
                final float r = 110.0f * PApplet.TWO_PI * i / 44100;
                buf[0][i] = PApplet.sin(r);
                buf[1][i] = PApplet.cos(r);
            }
            byte[] mWAVStereo = WAVConverter.convert_samples_to_bytes(buf, 2, 16, 44100);
            saveBytes("/Users/dennisppaul/Desktop/foobar/test-stereo.wav", mWAVStereo);
            byte[] mWAVMono = WAVConverter.convert_samples_to_bytes(new float[][]{buf[0]}, 1, 24, 44100);
            saveBytes("/Users/dennisppaul/Desktop/foobar/test-mono.wav", mWAVMono);
//            WAVConverter.convert_bytes_to_samples(mWAVMono);
//            WAVConverter.convert_bytes_to_samples(mWAVStereo);
            System.out.println("------------- loading wave from disk --------------");
            byte[] mWAVMonoF = loadBytes("/Users/dennisppaul/Desktop/foobar/test-mono_exp.wav");
            mWAVStruct = WAVConverter.convert_bytes_to_samples(mWAVMonoF);
            /*
             * CHUNK: RIFF
             *     file length: 88236
             *     RIFF type  : WAVE
             * CHUNK: fmt
             *     chunk size : 16
             *     comp code  : 1
             *     channels   : 1
             *     sample rate: 44100
             *     byte/sec   : 88200
             *     block align: 2
             *     bits/sample: 16
             * CHUNK: data
             *     chunk size       : 88200
             *     (samples/channel): 44100
             */
        }

        public void draw() {
            background(255);
            stroke(0);
            float[] mSamples = mWAVStruct.samples[0];
            for (int i = 0; i < mSamples.length; i++) {
                float x = map(i, 0, mSamples.length, 0, width * map(mouseX, 0, width, 1, 1000));
                float y = map(mSamples[i], -1, 1, 0, height);
                point(x, y);
            }
        }
    }

    static class WAVConverter {

        private static final int COMPRESSION_CODE_WAVE_FORMAT_PCM = 1;
        public static boolean VERBOSE = true;
        private final int mChannels;
        private final int mBitsPerSample;
        private final int mSampleRate;
        private final ArrayList<Byte> mHeader;
        private final ArrayList<Byte> mData;

        private WAVConverter(int pChannels, int pBitsPerSample, int pSampleRate) {
            mChannels = pChannels;
            mBitsPerSample = pBitsPerSample;
            mSampleRate = pSampleRate;
            mData = new ArrayList<>();
            mHeader = new ArrayList<>();
        }

        public static byte[] convert_samples_to_bytes(float[][] pBuffer, int pChannels, int pBitsPerSample,
                                                      int pSampleRate) {
            WAVConverter mWAVConverter = new WAVConverter(pChannels, pBitsPerSample, pSampleRate);
            mWAVConverter.appendData(pBuffer);
            mWAVConverter.writeHeader();
            return mWAVConverter.getByteData();
        }

        public static WAVStruct convert_bytes_to_samples(byte[] pHeader) {
            final WAVStruct mWAVStruct = new WAVStruct();
            // from https://sites.google.com/site/musicgapi/technical-documents/wav-file-format
            /* RIFF Chunk */
            int mOffset = 0x00;
            int mFileLength = read__int32(pHeader, mOffset + 0x04);
            if (VERBOSE) {
                System.out.println("CHUNK: " + WAVConverter.read_string(pHeader, 0, 4));
                System.out.println("    file length: " + mFileLength);
                System.out.println("    RIFF type  : " + WAVConverter.read_string(pHeader, mOffset + 0x08, 4));
            }

            /* format chunk */
            mOffset = 0x0C;
            final int mFormatChunkSize = WAVConverter.read__int32(pHeader, mOffset + 0x04);
            final int mCompressionCode = WAVConverter.read__int16(pHeader,
                                                                  mOffset + 0x08); // assert WAVE_FORMAT_PCM(=1)
            mWAVStruct.channels = WAVConverter.read__int16(pHeader, mOffset + 0x0A);
            mWAVStruct.sample_rate = WAVConverter.read__int32(pHeader, mOffset + 0x0C);
            mWAVStruct.bits_per_sample = WAVConverter.read__int16(pHeader, mOffset + 0x16);
            if (VERBOSE) {
                System.out.println("CHUNK: " + WAVConverter.read_string(pHeader, mOffset + 0x00, 4));
                System.out.println("    chunk size : " + mFormatChunkSize);
                System.out.println("    comp code  : " + mCompressionCode);
                System.out.println("    channels   : " + mWAVStruct.channels);
                System.out.println("    sample rate: " + mWAVStruct.sample_rate);
                System.out.println("    byte/sec   : " + WAVConverter.read__int32(pHeader, mOffset + 0x10));
                System.out.println("    block align: " + WAVConverter.read__int16(pHeader, mOffset + 0x14));
                System.out.println("    bits/sample: " + mWAVStruct.bits_per_sample);
            }
            if (mCompressionCode != COMPRESSION_CODE_WAVE_FORMAT_PCM) {
                System.out.println(
                        "+++ WARNING @ / compression code not supported. currently only `WAVE_FORMAT_PCM` works. (" + mCompressionCode + ")");
            }

            /* data chunk */
            mOffset = 0x0C + 0x18;
            if (WAVConverter.read_string(pHeader, mOffset + 0x00, 4).equalsIgnoreCase("fact")) {
                // @TODO(hack! skipping `fact` chunk … handle this a bit more elegantly)
                System.out.println("+++ skipping `fact` chunk");
                final int mFactChunkSize = WAVConverter.read__int32(pHeader, mOffset + 0x04);
                mOffset += 0x04 + mFactChunkSize * 0x08; // Chunk ID + Chunk Data Size + Format Dependant Data ( 4
                // bytes )
            }
            final int mDataChunkSize = WAVConverter.read__int32(pHeader, mOffset + 0x04);
            byte[] mInterlacedByteBuffer = WAVConverter.read__bytes(pHeader, mOffset + 0x08, mDataChunkSize);
            int mDataSize = mInterlacedByteBuffer.length / mWAVStruct.channels / (mWAVStruct.bits_per_sample / 8);
            if (VERBOSE) {
                System.out.println("CHUNK: " + WAVConverter.read_string(pHeader, mOffset + 0x00, 4));
                System.out.println("    chunk size       : " + mDataChunkSize);
                System.out.println("    (samples/channel): " + mDataSize);
            }

            mWAVStruct.samples = new float[mWAVStruct.channels][mDataSize];
            final int mBytesPerSample = mWAVStruct.bits_per_sample / 8;
            final int mStride = mWAVStruct.channels * mBytesPerSample;
            for (int j = 0; j < mWAVStruct.channels; j++) {
                byte[] mByteSamples = new byte[mBytesPerSample * mDataSize];
                int c = 0;
                for (int i = 0; i < mInterlacedByteBuffer.length; i += mStride) {
                    for (int l = 0; l < mBytesPerSample; l++) {
                        byte b = mInterlacedByteBuffer[i + j * mBytesPerSample + l];
                        mByteSamples[c] = b;
                        c++;
                    }
                }
                float[] mFloatSamples = mWAVStruct.samples[j];
                bytes_to_floats(mByteSamples, mFloatSamples, mWAVStruct.bits_per_sample);
            }
            return mWAVStruct;
        }

        private static byte[] read__bytes(byte[] pBuffer, int pStart, int pLength) {
            return PApplet.subset(pBuffer, pStart, pLength);
        }

        private static String read_string(byte[] pBuffer, int pStart, int pLength) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < pLength; i++) {
                sb.append((char) pBuffer[pStart + i]);
            }
            return sb.toString();
        }

        private static int read__int32(byte[] pBuffer, int pStart) {
            return read____int(pBuffer, pStart, 4);
        }

        private static int read__int16(byte[] pBuffer, int pStart) {
            return read____int(pBuffer, pStart, 2);
        }

        private static int read____int(byte[] pBuffer, int pStart, int pBytes) {
            int v = 0;
            for (int i = 0; i < pBytes; i++) {
                v |= (pBuffer[i + pStart] & 0xFF) << (i * 8);
            }
            return v;
        }

        private static int findSingleBufferLength(float[][] pBuffer) {
            if (pBuffer == null || pBuffer.length == 0) {
                return -1;
            } else if (pBuffer.length == 1) {
                return pBuffer[0].length;
            } else {
                int mBufferLength = pBuffer[0].length;
                for (int i = 1; i < pBuffer.length; i++) {
                    if (mBufferLength != pBuffer[i].length) {
                        System.err.println("+++ ERROR @" + Wellen.class.getSimpleName() + " / sample buffers have " + "different length.");
                        return mBufferLength;
                    }
                }
                return mBufferLength;
            }
        }

        private static void write___byte(ArrayList<Byte> pBuffer, int b) {
            pBuffer.add((byte) b);
        }

        private static void write__bytes(ArrayList<Byte> pBuffer, byte[] b) {
            for (byte value : b) {
                pBuffer.add(value);
            }
        }

        private static void write__int16(ArrayList<Byte> pBuffer, int s) {
            int b0, b1;
            b0 = (s >>> 0) & 0xff;
            b1 = (s >>> 8) & 0xff;
            write_bytes2(pBuffer, b0, b1);
        }

        private static void write__int32(ArrayList<Byte> pBuffer, int i) {
            int b0, b1, b2, b3;
            b0 = (i >>> 0) & 0xff;
            b1 = (i >>> 8) & 0xff;
            b2 = (i >>> 16) & 0xff;
            b3 = (i >>> 24) & 0xff;
            write_bytes4(pBuffer, b0, b1, b2, b3);
        }

        private static void write_bytes2(ArrayList<Byte> pBuffer, int b0, int b1) {
            write___byte(pBuffer, b0);
            write___byte(pBuffer, b1);
        }

        private static void write_bytes4(ArrayList<Byte> pBuffer, int b0, int b1, int b2, int b3) {
            write_bytes2(pBuffer, b0, b1);
            write_bytes2(pBuffer, b2, b3);
        }

        private static void write_string(ArrayList<Byte> pBuffer, String s) {
            final byte[] b = s.getBytes();
            for (byte value : b) {
                pBuffer.add(value);
            }
        }

        public void appendData(float[][] pFloatBuffer) {
            int mNumberOfFrames = findSingleBufferLength(pFloatBuffer);
            float[] mInterleavedFloatBuffer = new float[mNumberOfFrames * mChannels];
            byte[] mByteBuffer = new byte[mNumberOfFrames * mChannels * mBitsPerSample / 8];
            for (int i = 0; i < mNumberOfFrames; i++) {
                for (int mChannel = 0; mChannel < mChannels; mChannel++) {
                    mInterleavedFloatBuffer[i * mChannels + mChannel] = pFloatBuffer[mChannel][i];
                }
            }
            floats_to_bytes(mByteBuffer, mInterleavedFloatBuffer, mBitsPerSample);
            write__bytes(mData, mByteBuffer);
        }

        public void writeHeader() {
            mHeader.clear();
            /* RIFF Chunk */
            write_string(mHeader, "RIFF");
            write__int32(mHeader, mData.size()); // file length ( without header )
            write_string(mHeader, "WAVE");
            /* format chunk */
            write_string(mHeader, "fmt ");
            write__int32(mHeader, 16); // chunk length
            write__int16(mHeader, COMPRESSION_CODE_WAVE_FORMAT_PCM);
            write__int16(mHeader, mChannels);
            write__int32(mHeader, mSampleRate);
            write__int32(mHeader, (mSampleRate * mChannels * mBitsPerSample / 8)); // bytes per second
            write__int16(mHeader, (mChannels * mBitsPerSample / 8)); // block align
            write__int16(mHeader, mBitsPerSample);
            /* data chunk */
            write_string(mHeader, "data");
            write__int32(mHeader, mData.size()); // data length
        }

        public byte[] getByteData() {
            byte[] mBuffer = new byte[mHeader.size() + mData.size()];
            for (int i = 0; i < mHeader.size(); i++) {
                mBuffer[i] = mHeader.get(i);
            }
            for (int i = 0; i < mData.size(); i++) {
                mBuffer[i + mHeader.size()] = mData.get(i);
            }
            return mBuffer;
        }
    }

    public static class WAVStruct {
        int channels;
        int bits_per_sample;
        int sample_rate;
        byte[] data;
        float[][] samples;
    }

    public static void main(String[] args) {
//        float[] f = {0.45f, -0.5f, 2.0f, -0.99f};
//        byte[] b = new byte[8];
//        floats_to_bytes(b, f, 16);
//        for (byte v : b) {
//            System.out.println(v);
//        }
//
//        float[] _f = new float[f.length];
//        bytes_to_floats(b, _f, 16);
//        for (float v : _f) {
//            System.out.println(v);
//        }

        PApplet.main(TestWAVWriter.class.getName());
    }
}
