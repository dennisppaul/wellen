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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/**
 * communicates with the underlying audio systems.
 */
public class AudioBufferManager extends Thread {

    /*
     * - @REF([Java Sound Resources: FAQ: Audio Programming](http://jsresources.sourceforge.net/faq_audio
     * .html#sync_playback_recording))
     * - SOURCE == output
     * - TARGET == input
     */

    public static final float MAX_16_BIT = 32768;
    public static final int BITS_PER_SAMPLE = 16;
    public static final int MONO = 1;
    public static final int STEREO = 2;
    public static final boolean LITTLE_ENDIAN = false;
    public static final boolean BIG_ENDIAN = true;
    public static final boolean SIGNED = true;
    public static final boolean UNSIGNED = false;
    public static final int DEFAULT = -1;
    private final AudioBufferRenderer mSampleRenderer;
    private final int mSampleRate;
    private final int mSampleBufferSize;
    private final int mNumOutputChannels;
    private final int mNumInputChannels;
    private SourceDataLine mOutputLine;
    private TargetDataLine mInputLine;
    private byte[] mOutputByteBuffer;
    private byte[] mInputByteBuffer;
    private final boolean mRunBuffer = true;

    public AudioBufferManager(AudioBufferRenderer pSampleRenderer) {
        this(pSampleRenderer,
             Wellen.DEFAULT_SAMPLING_RATE,
             Wellen.DEFAULT_AUDIOBLOCK_SIZE,
             DEFAULT,
             STEREO,
             DEFAULT,
             MONO);
    }

    public AudioBufferManager(AudioBufferRenderer pSampleRenderer, int pSampleRate, int pSampleBufferSize,
                              int pOutputDevice, int pNumOutputChannels, int pInputDevice, int pNumInputChannels) {
        mSampleRenderer = pSampleRenderer;
        mSampleRate = pSampleRate;
        mSampleBufferSize = pSampleBufferSize;
        mNumOutputChannels = pNumOutputChannels;
        mNumInputChannels = pNumInputChannels;

        try {
            /* output */
            final AudioFormat mOutputFormat = new AudioFormat(pSampleRate,
                                                              BITS_PER_SAMPLE,
                                                              pNumOutputChannels,
                                                              SIGNED,
                                                              LITTLE_ENDIAN);
            if (pOutputDevice == DEFAULT) {
                mOutputLine = AudioSystem.getSourceDataLine(mOutputFormat);
            } else {
                System.out.println("+ OUTPUT DEVICE: " + AudioSystem.getMixerInfo()[pOutputDevice]);
                mOutputLine = AudioSystem.getSourceDataLine(mOutputFormat, AudioSystem.getMixerInfo()[pOutputDevice]);
            }
            final int BYTES_PER_SAMPLE = BITS_PER_SAMPLE / 8;
            mOutputByteBuffer = new byte[mSampleBufferSize * BYTES_PER_SAMPLE * pNumOutputChannels];
            mOutputLine.open(mOutputFormat, mOutputByteBuffer.length);

            /* input */
            if (mNumInputChannels > 0) {
                final AudioFormat mInputFormat = new AudioFormat(pSampleRate,
                                                                 BITS_PER_SAMPLE,
                                                                 mNumInputChannels,
                                                                 SIGNED,
                                                                 LITTLE_ENDIAN);
                if (pInputDevice == DEFAULT) {
                    mInputLine = AudioSystem.getTargetDataLine(mInputFormat);
                } else {
                    mInputLine = AudioSystem.getTargetDataLine(mInputFormat, AudioSystem.getMixerInfo()[pInputDevice]);
                    System.out.println("+ INPUT DEVICE: " + AudioSystem.getMixerInfo()[pInputDevice]);
                }
                mInputByteBuffer = new byte[mSampleBufferSize * BYTES_PER_SAMPLE * mNumInputChannels];
                mInputLine.open(mInputFormat, mInputByteBuffer.length);
            }
        } catch (LineUnavailableException e) {
            System.err.println(e.getMessage());
        }
        if (mInputLine != null) {
            mInputLine.start();
        }
        mOutputLine.start();
        start();
    }

    public int sample_rate() {
        return mSampleRate;
    }

    public void run() {
        while (mRunBuffer) {
            /* input */
            float[][] mInputBuffers = new float[mNumInputChannels][];
            for (int j = 0; j < mNumInputChannels; j++) {
                mInputBuffers[j] = new float[mSampleBufferSize];
            }
            if (mInputLine != null) {
                mInputLine.read(mInputByteBuffer, 0, mInputByteBuffer.length);
                final int BYTES_PER_SAMPLE = BITS_PER_SAMPLE / 8;

                for (int i = 0; i < mSampleBufferSize; i++) {
                    final int k = i * mNumInputChannels * BYTES_PER_SAMPLE;
                    for (int j = 0; j < mNumInputChannels; j++) {
                        final int l = k + BYTES_PER_SAMPLE * j;
                        float mSample = readSample16(mInputByteBuffer[l + 0], mInputByteBuffer[l + 1]);
                        mInputBuffers[j][i] = mSample;
                    }
                }

//                final int mStride = BYTES_PER_SAMPLE * mNumInputChannels;
//                for (int i = 0; i < mInputByteBuffer.length; i += mStride) {
//                    for (int j = 0; j < mNumInputChannels; j++) {
//                        final int k = i + j * mNumInputChannels;
//                        final float mSample = readSample16(mInputByteBuffer[k + 0], mInputByteBuffer[k + 1]);
//                        final int l = i / mStride;
//                        mInputBuffers[j][l] = mSample;
//                    }
//                }

                mInputLine.flush();
            }
            /* output */
            float[][] mOutputBuffers = new float[mNumOutputChannels][];
            for (int j = 0; j < mNumOutputChannels; j++) {
                mOutputBuffers[j] = new float[mSampleBufferSize];
            }

            mSampleRenderer.audioblock(mOutputBuffers, mInputBuffers);

            for (int i = 0; i < mSampleBufferSize; i++) {
                for (int j = 0; j < mNumOutputChannels; j++) {
                    writeSample16(mOutputBuffers[j][i], i * mNumOutputChannels + j);
                }
            }
            mOutputLine.write(mOutputByteBuffer, 0, mOutputByteBuffer.length);
        }
    }

    public int buffer_size() {
        return mSampleBufferSize;
    }

    private float readSample16(byte l, byte h) {
        float v = ((int) h << 8) + (int) l;
        return v / MAX_16_BIT;
    }

    private void writeSample16(final float sample, final int i) {
        short s = (short) (MAX_16_BIT * sample);
        if (sample == 1.0) {
            s = Short.MAX_VALUE; // special case since 32768 not a short
        }
        mOutputByteBuffer[i * 2 + 0] = (byte) s;
        mOutputByteBuffer[i * 2 + 1] = (byte) (s >> 8); // little endian
    }
}
