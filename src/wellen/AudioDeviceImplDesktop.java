/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2024 Dennis P Paul.
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
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import static wellen.Wellen.CHECK_DEFAULT_AUDIO_DEVICE_SAMPLE_RATE;
import static wellen.Wellen.ENCODING_ALAW;
import static wellen.Wellen.ENCODING_PCM_FLOAT;
import static wellen.Wellen.ENCODING_PCM_SIGNED;
import static wellen.Wellen.ENCODING_PCM_UNSIGNED;
import static wellen.Wellen.ENCODING_ULAW;

public class AudioDeviceImplDesktop extends Thread implements AudioDevice {

    /*
     * - @REF([Java Sound Resources: FAQ: Audio Programming](http://jsresources.sourceforge.net/faq_audio
     * .html#sync_playback_recording))
     * - SOURCE == output
     * - TARGET == input
     */

//    public void playAudio(float[][] samples) {
//        try {
//            // Set up audio format
//            AudioFormat audioFormat = new AudioFormat(44100, 32, 2, true, false);
//
//            // Open audio output
//            SourceDataLine line = AudioSystem.getSourceDataLine(audioFormat);
//            line.open(audioFormat, 44100 * 8);
//            line.start();
//
//            // Write samples to audio output
//            for (float[] sample : samples) {
//                // Convert float samples to 32-bit PCM
//                int pcmLeft = Float.floatToIntBits(sample[0]);
//                int pcmRight = Float.floatToIntBits(sample[1]);
//
//                // Write PCM samples to audio output
//                line.write(new byte[]{(byte) (pcmLeft & 0xff), (byte) ((pcmLeft >> 8) & 0xff),
//                                      (byte) ((pcmLeft >> 16) & 0xff), (byte) ((pcmLeft >> 24) & 0xff)}, 0, 4);
//                line.write(new byte[]{(byte) (pcmRight & 0xff), (byte) ((pcmRight >> 8) & 0xff),
//                                      (byte) ((pcmRight >> 16) & 0xff), (byte) ((pcmRight >> 24) & 0xff)}, 0, 4);
//            }
//
//            // Close audio output
//            line.drain();
//            line.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public float[][] readAudio() {
//        float[][] samples = null;
//
//        try {
//            // Set up audio format
//            AudioFormat audioFormat = new AudioFormat(44100, 32, 2, true, false);
//
//            // Open audio input
//            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
//            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
//            line.open(audioFormat);
//            line.start();
//
//            // Read samples from audio input
//            int numSamples = (int) (line.getMicrosecondPosition() / 1000000.0 * audioFormat.getSampleRate()) *
//            audioFormat.getChannels();
//            samples = new float[numSamples][2];
//            for (int i = 0; i < numSamples; i++) {
//                // Read PCM samples from audio input
//                byte[] pcm = new byte[8];
//                line.read(pcm, 0, 8);
//
//                // Convert PCM samples to float
//                int pcmLeft = ((pcm[3] & 0xff) << 24) | ((pcm[2] & 0xff) << 16) | ((pcm[1] & 0xff) << 8) | (pcm[0]
//                & 0xff);
//                int pcmRight = ((pcm[7] & 0xff) << 24) | ((pcm[6] & 0xff) << 16) | ((pcm[5] & 0xff) << 8) | (pcm[4]
//                & 0xff);
//                samples[i][0] = Float.intBitsToFloat(pcmLeft);
//                samples[i][1] = Float.intBitsToFloat(pcmRight);
//            }
//
//            // Close audio input
//            line.stop();
//            line.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return samples;
//    }

//    // Convert 32-bit PCM samples to float
//    int pcmLeft = ((pcm[3] & 0xff) << 24) | ((pcm[2] & 0xff) << 16) | ((pcm[1] & 0xff) << 8) | (pcm[0] & 0xff);
//    int pcmRight = ((pcm[7] & 0xff) << 24) | ((pcm[6] & 0xff) << 16) | ((pcm[5] & 0xff) << 8) | (pcm[4] & 0xff);
//    samples[i][0] = Float.intBitsToFloat(pcmLeft);
//    samples[i][1] = Float.intBitsToFloat(pcmRight);
//
//    // Convert float samples to 32-bit PCM
//    int pcmLeft = Float.floatToIntBits(sample[0]);
//    int pcmRight = Float.floatToIntBits(sample[1]);
//
//    // Write PCM samples to audio output
//    line.write(new byte[]{(byte) (pcmLeft & 0xff), (byte) ((pcmLeft >> 8) & 0xff), (byte) ((pcmLeft >> 16) & 0xff),
//    (byte) ((pcmLeft >> 24) & 0xff)}, 0, 4);
//    line.write(new byte[]{(byte) (pcmRight & 0xff), (byte) ((pcmRight >> 8) & 0xff), (byte) ((pcmRight >> 16) &
//    0xff), (byte) ((pcmRight >> 24) & 0xff)}, 0, 4);

    public static boolean VERBOSE = false;
    private static final float SIG_16BIT_MAX = 32768.0f;
    private static final float SIG_16BIT_MAX_INVERSE = 1.0f / SIG_16BIT_MAX;
    private static final float SIG_24BIT_MAX = 8388608.0f;
    private static final float SIG_24BIT_MAX_INVERSE = 1.0f / SIG_24BIT_MAX;
    private static final float SIG_32BIT_MAX = 2147483648.0f;
    private static final float SIG_32BIT_MAX_INVERSE = 1.0f / SIG_32BIT_MAX;
    private static final float SIG_8BIT_MAX = 128.0f;
    private static final float SIG_8BIT_MAX_INVERSE = 1.0f / SIG_8BIT_MAX;
    private final int fBitsPerSample;
    /* --- */
    private final int fBytesPerSample;
    private final int mNumInputChannels;
    private final int mNumOutputChannels;
    private final int mSampleBufferSize;
    private final int mSampleRate;
    private final AudioBufferRenderer mSampleRenderer;
    private volatile boolean fThreadSuspended = false;
    private int mFrameCounter = 0;
    private byte[] mInputByteBuffer;
    private TargetDataLine mInputLine;
    private byte[] mOutputByteBuffer;
    private SourceDataLine mOutputLine;
    private boolean mRunBuffer = true;

    public AudioDeviceImplDesktop(AudioBufferRenderer pSampleRenderer, AudioDeviceConfiguration pConfiguration) {
        mSampleRenderer = pSampleRenderer;
        mSampleRate = pConfiguration.sample_rate;
        mSampleBufferSize = pConfiguration.sample_buffer_size;
        mNumOutputChannels = pConfiguration.number_of_output_channels;
        mNumInputChannels = pConfiguration.number_of_input_channels;
        fBitsPerSample = pConfiguration.bits_per_sample;
        fBytesPerSample = fBitsPerSample / 8;

        try {
            /* output */
            // TODO check if there is any java implementation that allows PCM_FLOAT
            AudioFormat mOutputFormat;
            mOutputFormat = new AudioFormat(getEncoding(pConfiguration.encoding),
                                            pConfiguration.sample_rate,
                                            pConfiguration.bits_per_sample,
                                            pConfiguration.number_of_output_channels,
                                            getFrameSize(pConfiguration.bits_per_sample,
                                                         pConfiguration.number_of_output_channels),
                                            pConfiguration.sample_rate,
                                            pConfiguration.is_big_endian);
            if (pConfiguration.output_device_ID == Wellen.DEFAULT_AUDIO_DEVICE) {
                if (CHECK_DEFAULT_AUDIO_DEVICE_SAMPLE_RATE) {
                    System.out.println("+-------------------------------------------------------+");
                    System.out.println("+ checking default output device sample rate ... ");
                    System.out.println("+");
                    boolean mSampleRateMatch = true;
                    Mixer mDefaultMixer = AudioSystem.getMixer(null);
                    Line.Info[] mDefaultSourceDataLine = mDefaultMixer.getSourceLineInfo();
                    for (Line.Info li : mDefaultSourceDataLine) {
                        final Line mLine = mDefaultMixer.getLine(li);
                        if (mLine instanceof SourceDataLine) {
                            AudioFormat mDefaultAudioFormat = ((SourceDataLine) mLine).getFormat();
                            if (mSampleRate != mDefaultAudioFormat.getSampleRate()) {
                                System.out.println("+ AVAILABLE DEFAULT OUTPUT DEVICE CAPABILITIES");
                                System.out.println("+ - sample rate ........... : " + mDefaultAudioFormat.getSampleRate());
                                System.out.println("+ - channels .............. : " + mDefaultAudioFormat.getChannels());
                                System.out.println("+ - general info .......... : " + mDefaultAudioFormat);
                                System.out.print("+ WARNING desired sample rate '" + mSampleRate + "' ");
                                System.out.println("and DEFAULT OUTPUT DEVICE sample rate '" + mDefaultAudioFormat.getSampleRate() + "' do not match.");
                                System.out.println("+ setting sample rate to '" + mDefaultAudioFormat.getSampleRate() + "' ( be aware that this might cause problems later ).");
                                mOutputFormat = new AudioFormat(getEncoding(pConfiguration.encoding),
                                                                pConfiguration.sample_rate,
                                                                pConfiguration.bits_per_sample,
                                                                pConfiguration.number_of_output_channels,
                                                                getFrameSize(pConfiguration.bits_per_sample,
                                                                             pConfiguration.number_of_output_channels),
                                                                mDefaultAudioFormat.getSampleRate(),
                                                                pConfiguration.is_big_endian);
                                mSampleRateMatch = false;
                                System.out.println("+");
                                System.out.println("+ try setting the sample rate manually in `setup()` e.g:");
                                System.out.println(
                                        "+     Tone.start(\"internal\", 48000, Wellen.DEFAULT_AUDIO_DEVICE, 2);");
                                System.out.println("+");
                            }
                        }
                    }
                    if (mSampleRateMatch) {
                        System.out.println("+ sample rate '" + mSampleRate + "' OK.");
                    }
                    System.out.println("+-------------------------------------------------------+");

//                    try {
//                        DataLine.Info mDefaultDataLineInfo = new DataLine.Info(SourceDataLine.class, null);
//                        SourceDataLine mDefaultSourceDataLine = (SourceDataLine) AudioSystem.getLine
//                        (mDefaultDataLineInfo);
//                        AudioFormat mDefaultAudioFormat = mDefaultSourceDataLine.getFormat();
//                        if (mSampleRate != mDefaultAudioFormat.getSampleRate()) {
//                            System.out.print("+++ WARNING desired sample rate '" + mSampleRate + "' ");
//                            System.out.println("and DEFAULT OUTPUT DEVICE sample rate '" + mDefaultAudioFormat
//                            .getSampleRate() + "' do not match.");
//                            mOutputFormat = new AudioFormat((SIGNED ? Encoding.PCM_SIGNED : Encoding.PCM_UNSIGNED),
//                                                            mSampleRate,
//                                                            fBitsPerSample,
//                                                            mNumOutputChannels,
//                                                            ((fBitsPerSample + 7) / 8) * mNumOutputChannels,
//                                                            mDefaultAudioFormat.getSampleRate(),
//                                                            LITTLE_ENDIAN);
//                        }
//                    } catch (LineUnavailableException e) {
//                        e.printStackTrace();
//                    }
                }

                mOutputLine = AudioSystem.getSourceDataLine(mOutputFormat);
                if (VERBOSE) {
                    System.out.println("+-------------------------------------------------------+");
                    System.out.println("+ DEFAULT OUTPUT DEVICE CAPABILITIES");
                    System.out.println("+ - number of channels .... : " + mOutputLine.getFormat().getChannels());
                    System.out.println("+ - sample size in bits ... : " + mOutputLine.getFormat()
                                                                                     .getSampleSizeInBits());
                    System.out.println("+ - sample rate ........... : " + mOutputLine.getFormat().getSampleRate());
                    System.out.println("+ - encoding .............. : " + mOutputLine.getFormat().getEncoding());
                    System.out.println("+ - endianess ............. : " + (mOutputLine.getFormat()
                                                                                      .isBigEndian() ? "big-endian" :
                            "little-endian"));
                    System.out.println("+-------------------------------------------------------+");
                }
            } else {
                if (VERBOSE) {
                    System.out.println("+ OUTPUT DEVICE: " + AudioSystem.getMixerInfo()[pConfiguration.output_device_ID]);
                }
                mOutputLine = AudioSystem.getSourceDataLine(mOutputFormat,
                                                            AudioSystem.getMixerInfo()[pConfiguration.output_device_ID]);
                if (mNumOutputChannels != mOutputLine.getFormat().getChannels()) {
                    System.err.println("+++ @" + getClass().getSimpleName() + " / output line 'channel numbers' do " + "not match: REQUESTED: " + mNumOutputChannels + " RECEIVED: " + mOutputLine.getFormat()
                                                                                                                                                                                                  .getChannels());
                }
                if (fBitsPerSample != mOutputLine.getFormat().getSampleSizeInBits()) {
                    System.err.println("+++ @" + getClass().getSimpleName() + " / output line 'bits per sample' do " + "not match: REQUESTED: " + fBitsPerSample + " RECEIVED: " + mOutputLine.getFormat()
                                                                                                                                                                                              .getSampleSizeInBits());
                }
                if (mSampleRate != mOutputLine.getFormat().getSampleRate()) {
                    System.err.println("+++ @" + getClass().getSimpleName() + " / output line 'sample rates' do not " + "match: REQUESTED: " + mSampleRate + " RECEIVED: " + mOutputLine.getFormat()
                                                                                                                                                                                        .getSampleRate());
                }
            }
            mOutputByteBuffer =
                    new byte[mSampleBufferSize * fBytesPerSample * pConfiguration.number_of_output_channels];
            mOutputLine.open(mOutputFormat, mOutputByteBuffer.length);

            /* input */
            if (mNumInputChannels > 0) {
                final AudioFormat mInputFormat = new AudioFormat(getEncoding(pConfiguration.encoding),
                                                                 pConfiguration.sample_rate,
                                                                 pConfiguration.bits_per_sample,
                                                                 pConfiguration.number_of_input_channels,
                                                                 getFrameSize(pConfiguration.bits_per_sample,
                                                                              pConfiguration.number_of_input_channels),
                                                                 pConfiguration.sample_rate,
                                                                 pConfiguration.is_big_endian);
                if (pConfiguration.input_device_ID == Wellen.DEFAULT_AUDIO_DEVICE) {
                    mInputLine = AudioSystem.getTargetDataLine(mInputFormat);
                    if (mNumInputChannels != mInputLine.getFormat().getChannels()) {
                        System.err.println("+++ @" + getClass().getSimpleName() + " / input line 'channel numbers' " + "do" + " not match: REQUESTED: " + mNumInputChannels + " RECEIVED:" + " " + mInputLine.getFormat()
                                                                                                                                                                                                             .getChannels());
                    }
                    if (fBitsPerSample != mInputLine.getFormat().getSampleSizeInBits()) {
                        System.err.println("+++ @" + getClass().getSimpleName() + " / input line 'bits per sample' " + "do" + " not match: REQUESTED: " + fBitsPerSample + " RECEIVED: " + mInputLine.getFormat()
                                                                                                                                                                                                     .getSampleSizeInBits());
                    }
                    if (mSampleRate != mInputLine.getFormat().getSampleRate()) {
                        System.err.println("+++ @" + getClass().getSimpleName() + " / input line 'sample rates' do " + "not match: REQUESTED: " + mSampleRate + " RECEIVED: " + mInputLine.getFormat()
                                                                                                                                                                                          .getSampleRate());
                    }
                } else {
                    mInputLine = AudioSystem.getTargetDataLine(mInputFormat,
                                                               AudioSystem.getMixerInfo()[pConfiguration.input_device_ID]);
                    if (VERBOSE) {
                        System.out.println("+ INPUT DEVICE: " + AudioSystem.getMixerInfo()[pConfiguration.input_device_ID]);
                    }
                }
                mInputByteBuffer = new byte[mSampleBufferSize * fBytesPerSample * mNumInputChannels];
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

    AudioFormat.Encoding getEncoding(int encoding) {
        switch (encoding) {
            case ENCODING_PCM_SIGNED:
                return new AudioFormat.Encoding("PCM_SIGNED");
            case ENCODING_PCM_UNSIGNED:
                return new AudioFormat.Encoding("PCM_UNSIGNED");
            case ENCODING_PCM_FLOAT:
                return new AudioFormat.Encoding("PCM_FLOAT");
            case ENCODING_ULAW:
                return new AudioFormat.Encoding("ULAW");
            case ENCODING_ALAW:
                return new AudioFormat.Encoding("ALAW");
        }
        return new AudioFormat.Encoding("PCM_SIGNED");
    }

    private int getFrameSize(int bits_per_sample, int number_of_channels) {
        return (number_of_channels == AudioSystem.NOT_SPECIFIED || bits_per_sample == AudioSystem.NOT_SPECIFIED) ?
                AudioSystem.NOT_SPECIFIED : ((bits_per_sample + 7) / 8) * number_of_channels;
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

    public synchronized void pause(boolean pause_state) {
        fThreadSuspended = pause_state;

        if (!fThreadSuspended) {
            notify();
        }
    }

    public boolean is_paused() {
        return fThreadSuspended;
    }

    @Override
    public void run() {
        while (mRunBuffer) {
            try {
                if (fThreadSuspended) {
                    synchronized (this) {
                        while (fThreadSuspended) wait();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

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
                for (int i = 0; i < mSampleBufferSize; i++) {
                    final int k = i * mNumInputChannels * fBytesPerSample;
                    for (int j = 0; j < mNumInputChannels; j++) {
                        final int l = k + fBytesPerSample * j;
                        final float mSample;
                        if (fBitsPerSample == Wellen.BITS_PER_SAMPLE_16) {
                            mSample = readSample16(mInputByteBuffer, l);
                        } else if (fBitsPerSample == Wellen.BITS_PER_SAMPLE_24) {
                            mSample = readSample24(mInputByteBuffer, l);
                        } else if (fBitsPerSample == Wellen.BITS_PER_SAMPLE_32) {
                            mSample = readSample32(mInputByteBuffer, l);
                        } else if (fBitsPerSample == Wellen.BITS_PER_SAMPLE_8) {
                            mSample = readSample8(mInputByteBuffer, l);
                        } else {
                            mSample = 0;
                        }
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
                    final float mSample = Wellen.clamp(mOutputBuffers[j][i]);
                    if (fBitsPerSample == Wellen.BITS_PER_SAMPLE_16) {
                        writeSample16(mSample, i * mNumOutputChannels + j);
                    } else if (fBitsPerSample == Wellen.BITS_PER_SAMPLE_24) {
                        writeSample24(mSample, i * mNumOutputChannels + j);
                    } else if (fBitsPerSample == Wellen.BITS_PER_SAMPLE_32) {
                        writeSample32(mSample, i * mNumOutputChannels + j);
                    } else if (fBitsPerSample == Wellen.BITS_PER_SAMPLE_8) {
                        writeSample8(mSample, i * mNumOutputChannels + j);
                    }
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

//    private AudioFormat getAudioFormat(AudioDeviceConfiguration pConfiguration) {
//        final int mFrameSize =
//                (mNumInputChannels == AudioSystem.NOT_SPECIFIED || fBitsPerSample == AudioSystem.NOT_SPECIFIED) ?
//                        AudioSystem.NOT_SPECIFIED : ((fBitsPerSample + 7) / 8) * mNumInputChannels;
//        final float mFrameRate = mSampleRate;
//        final AudioFormat mInputFormat = new AudioFormat(pConfiguration.encoding,
//                                                         mSampleRate,
//                                                         fBitsPerSample,
//                                                         mNumInputChannels,
//                                                         mFrameSize,
//                                                         mFrameRate,
//                                                         pConfiguration.is_big_endian);
//        return mInputFormat;
//    }

    private float readSample16(byte[] b, int offset) { // low+high
        final float v = ((b[offset + 1] << 8) | (b[offset + 0] & 0xFF));
        return v * SIG_16BIT_MAX_INVERSE;
    }

    private float readSample24(byte[] b, int offset) {
        final float v = ((b[offset + 2] << 16) | ((b[offset + 1] & 0xFF) << 8) | (b[offset + 0] & 0xFF));
        return v * SIG_24BIT_MAX_INVERSE;
    }

    private float readSample32(byte[] b, int offset) {
        final float v =
                ((b[offset + 3] << 24) | ((b[offset + 2] & 0xFF) << 16) | ((b[offset + 1] & 0xFF) << 8) | (b[offset + 0] & 0xFF));
        return v * SIG_32BIT_MAX_INVERSE;
    }

    private float readSample8(byte[] b, int offset) {
        final float v = b[offset];
        return v * SIG_8BIT_MAX_INVERSE;
    }

    private void writeSample16(final float sample, final int i) {
        final short v;
        if (sample == 1.0f) {
            v = Short.MAX_VALUE; // special case since 32768 not a short
        } else {
            v = (short) (SIG_16BIT_MAX * sample);
        }
        mOutputByteBuffer[i * fBytesPerSample + 0] = (byte) (v & 0xff);
        mOutputByteBuffer[i * fBytesPerSample + 1] = (byte) (v >> 8 & 0xff); // little endian
    }

    private void writeSample24(final float sample, final int i) {
        final int v;
        if (sample == 1.0f) {
            v = Integer.MAX_VALUE;
        } else {
            v = (int) (SIG_24BIT_MAX * sample);
        }
        mOutputByteBuffer[i * fBytesPerSample + 0] = (byte) (v & 0xff);
        mOutputByteBuffer[i * fBytesPerSample + 1] = (byte) (v >> 8 & 0xff); // little endian
        mOutputByteBuffer[i * fBytesPerSample + 2] = (byte) (v >> 16 & 0xff);
    }

    private void writeSample32(final float sample, final int i) {
        final long v;
        if (sample == 1.0f) {
            v = Long.MAX_VALUE; // special case
        } else {
            v = (long) (SIG_32BIT_MAX * sample);
        }
        mOutputByteBuffer[i * fBytesPerSample + 0] = (byte) (v & 0xff);
        mOutputByteBuffer[i * fBytesPerSample + 1] = (byte) (v >> 8 & 0xff); // little endian
        mOutputByteBuffer[i * fBytesPerSample + 2] = (byte) (v >> 16 & 0xff);
        mOutputByteBuffer[i * fBytesPerSample + 3] = (byte) (v >> 24 & 0xff);
    }

    private void writeSample8(final float sample, final int i) {
        final byte v;
        if (sample == 1.0f) {
            v = Byte.MAX_VALUE;
        } else {
            v = (byte) (SIG_8BIT_MAX * sample);
        }
        mOutputByteBuffer[i * fBytesPerSample + 0] = (byte) (v & 0xff);
    }

    private void writeSamplePCM_FLOAT32(final float sample, final int i) {
        int mPCM = Float.floatToIntBits(sample);
        mOutputByteBuffer[i * fBytesPerSample + 0] = (byte) (mPCM & 0xff);
        mOutputByteBuffer[i * fBytesPerSample + 1] = (byte) ((mPCM >> 8) & 0xff);
        mOutputByteBuffer[i * fBytesPerSample + 2] = (byte) ((mPCM >> 16) & 0xff);
        mOutputByteBuffer[i * fBytesPerSample + 3] = (byte) ((mPCM >> 24) & 0xff);
    }
}
