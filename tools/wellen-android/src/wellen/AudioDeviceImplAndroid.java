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

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Process;

public class AudioDeviceImplAndroid extends Thread implements AudioDevice {
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

        fOutput = createOutputStream(pConfiguration.output_device, mSampleRate);
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
        return 0;
    }

    @Override
    public int buffer_size() {
        return 0;
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

            mSampleRenderer.audioblock(mOutputBuffers, null);

			if (mNumOutputChannels == 1) {
            	fOutput.write(mOutputBuffers[0]);
            } else if (mNumOutputChannels > 1) {
                float[] mOutBufferInterleaved = new float[mSampleBufferSize * mNumOutputChannels];
            	for (int i=0; i < mSampleBufferSize; i++) {
            		for (int j=0; j < mNumOutputChannels; j++) {
	            	    mOutBufferInterleaved[i * mNumOutputChannels + j] = mOutputBuffers[j][i];
					}            		
            	}
            	fOutput.write(mOutBufferInterleaved);
			}

            mFrameCounter++;
        }
    }

    private static class AndroidAudioStream {
        float[] floatBuffer;
        int frameRate;
        int deviceID;
        AudioTrack audioTrack;
        int minBufferSize;
        int bufferSize;

        public AndroidAudioStream(int deviceID, int frameRate) {
            this.deviceID = deviceID;
            this.frameRate = frameRate;
        }
    }

    private static class AndroidAudioOutputStream extends AndroidAudioStream {
        public AndroidAudioOutputStream(int deviceID, int frameRate) {
            super(deviceID, frameRate);
        }

        public void start() {
            Process.setThreadPriority(-5);
            minBufferSize = AudioTrack.getMinBufferSize(frameRate,
                                                        AudioFormat.CHANNEL_OUT_STEREO,
                                                        AudioFormat.ENCODING_PCM_FLOAT);
            bufferSize = (3 * (minBufferSize / 2)) & ~3;
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                                        frameRate,
                                        AudioFormat.CHANNEL_OUT_STEREO,
                                        AudioFormat.ENCODING_PCM_FLOAT,
                                        bufferSize,
                                        AudioTrack.MODE_STREAM);
            audioTrack.play();
        }

        public void write(float[] buffer) {
            write(buffer, 0, buffer.length);
        }

        public void write(float[] buffer, int start, int count) {
            if ((floatBuffer == null) || (floatBuffer.length < count)) {
                floatBuffer = new float[count];
            }
            for (int i = 0; i < count; i++) {
                floatBuffer[i] = buffer[i + start];
            }
            audioTrack.write(floatBuffer, 0, count, AudioTrack.WRITE_BLOCKING);
        }

        public void stop() {
            audioTrack.stop();
            audioTrack.release();
        }

        public void close() {
        }
    }

    private static class AndroidAudioInputStream extends AndroidAudioStream {
        public AndroidAudioInputStream(int deviceID, int frameRate) {
            super(deviceID, frameRate);
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

        public void close() {
        }
    }

    public AndroidAudioOutputStream createOutputStream(int deviceID, int frameRate) {
        return new AndroidAudioOutputStream(deviceID, frameRate);
    }

    public AndroidAudioInputStream createInputStream(int deviceID, int frameRate) {
        if (frameRate > 0) {
            throw new RuntimeException("audio input not implemented");
        }
        return new AndroidAudioInputStream(deviceID, frameRate);
    }
}