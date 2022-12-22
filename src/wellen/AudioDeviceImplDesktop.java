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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class AudioDeviceImplDesktop extends Thread implements AudioDevice {

    /*
     * - @REF([Java Sound Resources: FAQ: Audio Programming](http://jsresources.sourceforge.net/faq_audio
     * .html#sync_playback_recording))
     * - SOURCE == output
     * - TARGET == input
     */

    //@TODO(make BITS_PER_SAMPLE more flexible)

    public static final int BITS_PER_SAMPLE = 16;
    public static final boolean LITTLE_ENDIAN = false;
    public static final boolean BIG_ENDIAN = true;
    public static final boolean SIGNED = true;
    public static final boolean UNSIGNED = false;
    public static boolean VERBOSE = false;
    private int mFrameCounter = 0;
    private byte[] mInputByteBuffer;
    private TargetDataLine mInputLine;
    private byte[] mOutputByteBuffer;
    private SourceDataLine mOutputLine;
    private boolean mRunBuffer = true;

    private final AudioBufferRenderer mSampleRenderer;
    private final int mSampleRate;
    private final int mSampleBufferSize;
    private final int mNumOutputChannels;
    private final int mNumInputChannels;

    public AudioDeviceImplDesktop(AudioBufferRenderer pSampleRenderer, AudioDeviceConfiguration pConfiguration) {
        mSampleRenderer = pSampleRenderer;
        mSampleRate = pConfiguration.sample_rate;
        mSampleBufferSize = pConfiguration.sample_buffer_size;
        mNumOutputChannels = pConfiguration.number_of_output_channels;
        mNumInputChannels = pConfiguration.number_of_input_channels;

        try {
            /* output */
            final AudioFormat mOutputFormat = new AudioFormat(mSampleRate,
                                                              BITS_PER_SAMPLE,
                                                              mNumOutputChannels,
                                                              SIGNED,
                                                              LITTLE_ENDIAN);
            if (pConfiguration.output_device == Wellen.DEFAULT_AUDIO_DEVICE) {
                mOutputLine = AudioSystem.getSourceDataLine(mOutputFormat);
            } else {
                System.out.println("+ OUTPUT DEVICE: " + AudioSystem.getMixerInfo()[pConfiguration.output_device]);
                mOutputLine = AudioSystem.getSourceDataLine(mOutputFormat,
                                                            AudioSystem.getMixerInfo()[pConfiguration.output_device]);
            }
            final int BYTES_PER_SAMPLE = BITS_PER_SAMPLE / 8;
            mOutputByteBuffer =
                    new byte[mSampleBufferSize * BYTES_PER_SAMPLE * pConfiguration.number_of_output_channels];
            mOutputLine.open(mOutputFormat, mOutputByteBuffer.length);

            /* input */
            if (mNumInputChannels > 0) {
                final AudioFormat mInputFormat = new AudioFormat(mSampleRate,
                                                                 BITS_PER_SAMPLE,
                                                                 mNumInputChannels,
                                                                 SIGNED,
                                                                 LITTLE_ENDIAN);
                if (pConfiguration.input_device == Wellen.DEFAULT_AUDIO_DEVICE) {
                    mInputLine = AudioSystem.getTargetDataLine(mInputFormat);
                    if (mNumInputChannels != mInputLine.getFormat().getChannels()) {
                        System.err.println("+++ @" + getClass().getSimpleName() + " / input line channel numbers do " + "not match: " + "REQUESTED: " + mNumInputChannels + " RECEIVED: " + mInputLine.getFormat().getChannels());
                    }
                    if (BITS_PER_SAMPLE != mInputLine.getFormat().getSampleSizeInBits()) {
                        System.err.println("+++ @" + getClass().getSimpleName() + " / input line BITS_PER_SAMPLE do " + "not match: " + "REQUESTED: " + BITS_PER_SAMPLE + " RECEIVED: " + mInputLine.getFormat().getSampleSizeInBits());
                    }
                    if (mSampleRate != mInputLine.getFormat().getSampleRate()) {
                        System.err.println("+++ @" + getClass().getSimpleName() + " / sample rates do not match: " +
                                                   "REQUESTED: " + mSampleRate + " RECEIVED: " + mInputLine.getFormat().getSampleRate());
                    }
                } else {
                    mInputLine = AudioSystem.getTargetDataLine(mInputFormat,
                                                               AudioSystem.getMixerInfo()[pConfiguration.input_device]);
                    System.out.println("+ INPUT DEVICE: " + AudioSystem.getMixerInfo()[pConfiguration.input_device]);
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

    @Override
    public int buffer_size() {
        return mSampleBufferSize;
    }

    @Override
    public int sample_rate() {
        return mSampleRate;
    }

    @Override
    public void exit() {
        mRunBuffer = false;
        if (mInputLine != null) {
            mInputLine.flush();
            mInputLine.stop();
            mInputLine.close();
        }
        if (mOutputLine != null) {
            mOutputLine.flush();
            mOutputLine.stop();
            mOutputLine.close();
        }
    }

    @Override
    public void run() {
        while (mRunBuffer) {
            boolean mLockAudioBlock;
            /* input */
            float[][] mInputBuffers = new float[mNumInputChannels][];
            for (int j = 0; j < mNumInputChannels; j++) {
                mInputBuffers[j] = new float[mSampleBufferSize];
            }
            if (mInputLine != null) {
                final int mBytesRead = mInputLine.read(mInputByteBuffer, 0, mInputByteBuffer.length);
                if (VERBOSE) {
                    if (mBytesRead != mInputByteBuffer.length) {
                        System.err.println("+++ @" + getClass().getSimpleName() + " / input buffer underrun.");
                    }
                }
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

//                mInputLine.flush();
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

            /* detect buffer underrun */
            if (VERBOSE) {
                if (mFrameCounter > 0) {
                    // SourceDataLine
                    if (mOutputLine.available() == mOutputLine.getBufferSize()) {
                        System.out.println("+++ @" + getClass().getSimpleName() + " / buffer underrun in " +
                                                   "SourceDataLine `mOutputLine" + ".available() == mOutputLine" +
                                                   ".getBufferSize()`" + "(" + mFrameCounter + ")");
                    }
//                    if (!mOutputLine.isRunning()) {
//                        System.out.println(
//                        "+++ @" + getClass().getSimpleName() + " / buffer underrun in SourceDataLine `!mOutputLine" +
//                        ".isRunning()`" + "(" + mFrameCounter + ")");
//                    }
                }
            }

            final int mNumOfBytesWritten = mOutputLine.write(mOutputByteBuffer, 0, mOutputByteBuffer.length);
            if (VERBOSE && mNumOfBytesWritten != mOutputByteBuffer.length) {
                System.out.println("+++ number of bytes written: " + mNumOfBytesWritten + "( expected " + mOutputByteBuffer.length + " )");
            }
            mFrameCounter++;
        }
    }

    private static final float SIG_16BIT_MAX = 32768.0f;
    private static final float SIG_16BIT_MAX_INVERSE = 1.0f / SIG_16BIT_MAX;

    private float readSample16(byte l, byte h) {
        final float v = ((h << 8) | (l & 0xFF));
        return v * SIG_16BIT_MAX_INVERSE;
    }

    private void writeSample16(final float sample, final int i) {
        short s = (short) (SIG_16BIT_MAX * sample);
        if (sample == 1.0) {
            s = Short.MAX_VALUE; // special case since 32768 not a short
        }
        mOutputByteBuffer[i * 2 + 0] = (byte) s;
        mOutputByteBuffer[i * 2 + 1] = (byte) (s >> 8); // little endian
    }
}
