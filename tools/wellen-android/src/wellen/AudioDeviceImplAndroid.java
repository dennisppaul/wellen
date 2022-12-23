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

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

public class AudioDeviceImplAndroid extends Thread implements AudioDevice {
    private static final boolean VERBOSE = true;

    private boolean mRunBuffer = true;
    private int mFrameCounter = 0;
    private final AndroidAudioOutputStream fOutput;

    private final AudioBufferRenderer mSampleRenderer;
    private final int mSampleRate;
    private final int mSampleBufferSize;
    private final int mNumOutputChannels;
    private final int mNumInputChannels;

    public AudioDeviceImplAndroid(AudioBufferRenderer pSampleRenderer, AudioDeviceConfiguration pConfiguration) {
        mSampleRenderer = pSampleRenderer;
        mSampleRate = pConfiguration.sample_rate;
        mSampleBufferSize = pConfiguration.sample_buffer_size;
        mNumOutputChannels = pConfiguration.number_of_output_channels;
        mNumInputChannels = pConfiguration.number_of_input_channels;

        fOutput = createOutputStream(pConfiguration.output_device, mSampleRate, mNumOutputChannels);
        fOutput.start();
        // TODO ignore input channels for now
        start();
    }

    @Override
    public void exit() {
        mRunBuffer = false;
    }

    @Override
    public int sample_rate() {
        return mSampleRate;
    }

    @Override
    public int buffer_size() {
        return mSampleBufferSize;
    }

    @Override
    public void run() {
        while (mRunBuffer) {
            boolean mLockAudioBlock;

            /* output */
            float[][] mOutputBuffers = new float[mNumOutputChannels][];
            for (int j = 0; j < mNumOutputChannels; j++) {
                mOutputBuffers[j] = new float[mSampleBufferSize];
            }

            // TODO ignore input channels for now
            mSampleRenderer.audioblock(mOutputBuffers, null);

            if (mNumOutputChannels == Wellen.MONO) {
                fOutput.write(mOutputBuffers[0]);
            } else if (mNumOutputChannels > Wellen.MONO) {
                float[] mOutBufferInterleaved = new float[mSampleBufferSize * mNumOutputChannels];
                for (int i = 0; i < mSampleBufferSize; i++) {
                    for (int j = 0; j < mNumOutputChannels; j++) {
                        mOutBufferInterleaved[i * mNumOutputChannels + j] = mOutputBuffers[j][i];
                    }
                }
                fOutput.write(mOutBufferInterleaved);
            }

            mFrameCounter++;
        }
    }

    private static class AndroidAudioStream {
        protected AudioTrack audioTrack;
        protected float[] floatBuffer;
        protected int minBufferSize;

        protected final int fDeviceID;
        protected final int fSampleRate;
        protected final int fNumOutChannels;

        public AndroidAudioStream(int pDeviceID, int pSampleRate, int pNumChannels) {
            fDeviceID = pDeviceID;
            fSampleRate = pSampleRate;
            fNumOutChannels = pNumChannels;
        }
    }

    private static class AndroidAudioOutputStream extends AndroidAudioStream {
        public AndroidAudioOutputStream(int pDeviceID, int pSampleRate, int pNumChannels) {
            super(pDeviceID, pSampleRate, pNumChannels);
        }

        public void start() {
//            Process.setThreadPriority(-5);
            // TODO only evaluate MONO and STEREO for now
            final int ANDROID_ENUM_NUM_CHANNELS = fNumOutChannels == Wellen.MONO ? AudioFormat.CHANNEL_OUT_MONO :
                    AudioFormat.CHANNEL_OUT_STEREO;
            final int ANDROID_ENUM_ENCODING = AudioFormat.ENCODING_PCM_FLOAT;
            minBufferSize = AudioTrack.getMinBufferSize(fSampleRate, ANDROID_ENUM_NUM_CHANNELS, ANDROID_ENUM_ENCODING);
            // TODO replace with `AudioAttributes` constructor e.g
            // see https://developer.android.com/reference/android/media/AudioAttributes.Builder
            AudioAttributes mAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                                                                       .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                                                       // or `AudioAttributes.CONTENT_TYPE_SONIFICATION`
                                                                       .build();
            // see https://developer.android.com/reference/android/media/AudioFormat.Builder
            AudioFormat mFormat = new AudioFormat.Builder().setEncoding(ANDROID_ENUM_ENCODING)
                                                           .setSampleRate(fSampleRate)
                                                           .setChannelMask(ANDROID_ENUM_NUM_CHANNELS).build();
            audioTrack = new AudioTrack.Builder().setAudioAttributes(mAttributes).setAudioFormat(mFormat)
                                                 .setBufferSizeInBytes(minBufferSize).build();

//            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
//                                        fSampleRate,
//                                        AudioFormat.CHANNEL_OUT_STEREO,
//                                        AudioFormat.ENCODING_PCM_FLOAT,
//                                        bufferSize,
//                                        AudioTrack.MODE_STREAM);

            if (VERBOSE) {
                System.out.println("+++ Android Output Stream Information");
                System.out.println("+++ ");
                System.out.println("+++ num channels ........................... : " + fNumOutChannels);
                System.out.println("+++ min buffer size ........................ : " + minBufferSize);
                System.out.println("+++ calc'd buffer size ( not used ) ........ : " + ((3 * (minBufferSize / 2)) & ~3));
                System.out.println("+++ actual buffer size ( in frames ) ....... : " + audioTrack.getBufferSizeInFrames());
                System.out.println("+++ latency ................................ : " + (minBufferSize / fNumOutChannels));
            }
            audioTrack.play();
        }

        public void write(float[] buffer) {
            write(buffer, 0, buffer.length);
        }

        public void write(float[] buffer, int start, int count) {
            if ((floatBuffer == null) || (floatBuffer.length < count)) {
                floatBuffer = new float[count];
            }
            if (count >= 0) {
                System.arraycopy(buffer, start, floatBuffer, 0, count);
            }
            int mResult = audioTrack.write(floatBuffer, 0, count, AudioTrack.WRITE_BLOCKING);
            if (VERBOSE) {
                if (mResult < 0) {
                    System.err.println("### ERROR: AudioTrack.write() returned " + mResult);
                } else if (mResult != count) {
                    System.err.println("### ERROR: AudioTrack.write() returned " + mResult + " instead of " + count);
                }
            }
        }

        public void stop() {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
    }

    private static class AndroidAudioInputStream extends AndroidAudioStream {
        public AndroidAudioInputStream(int deviceID, int frameRate, int pNumChannels) {
            super(deviceID, frameRate, pNumChannels);
        }

        public void start() {
        }

        public float read() {
            float[] buffer = new float[1];
            read(buffer, 0, 1);
            return buffer[0];
        }

        public int read(float[] buffer) {
            return read(buffer, 0, buffer.length);
        }

        public int read(float[] buffer, int start, int count) {
            return 0;
        }

        public void stop() {
        }

        public int available() {
            return 0;
        }
    }

    private AndroidAudioOutputStream createOutputStream(int pDeviceID, int pSampleRate, int pNumOutChannels) {
        return new AndroidAudioOutputStream(pDeviceID, pSampleRate, pNumOutChannels);
    }

    private AndroidAudioInputStream createInputStream(int pDeviceID, int pSampleRate, int pNumInChannels) {
        if (pSampleRate > 0) {
            throw new RuntimeException("audio input not implemented");
        }
        return new AndroidAudioInputStream(pDeviceID, pSampleRate, pNumInChannels);
    }
}