package de.hfkbremen.ton;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioBufferPlayer extends Thread {

    /*
     * - @TODO(add recording (TargetDataLine))
     * - @TODO(add stereo)
     * - @REF([Java Sound Resources: FAQ: Audio Programming](http://jsresources.sourceforge.net/faq_audio
     * .html#sync_playback_recording))
     * - SOURCE == output
     * - TAARGET == input
     */

    //    public static final int SAMPLE_RATE = 44100;
    //    public static final int BYTES_PER_SAMPLE = 2;       // 16-bit audio
    //    public static final int BITS_PER_SAMPLE = 16;       // 16-bit audio
    public static final double MAX_16_BIT = 32768;

    public static final int MONO = 1;
    public static final int STEREO = 2;
    public static final boolean LITTLE_ENDIAN = false;
    public static final boolean BIG_ENDIAN = true;
    public static final boolean SIGNED = true;
    public static final boolean UNSIGNED = false;
    private final AudioBufferRenderer mSampleRenderer;
    private final int mSampleRate;
    private final int mSampleBufferSize;
    private final int mNumOutputChannels;
    private SourceDataLine mOutputLine;
    private byte[] mByteBuffer;
    private boolean mRunBuffer = true;

    public AudioBufferPlayer(AudioBufferRenderer pSampleRenderer) {
        this(pSampleRenderer, 44100, 512, 16, STEREO);
    }

    public AudioBufferPlayer(AudioBufferRenderer pSampleRenderer,
                             int pSampleRate,
                             int pSampleBufferSize,
                             int pBitsPerSample,
                             int pNumOutputChannels) {
        mSampleRenderer = pSampleRenderer;
        mSampleRate = pSampleRate;
        mSampleBufferSize = pSampleBufferSize;
        mNumOutputChannels = pNumOutputChannels;

        try {
            // 44,100 Hz, 16-bit audio, mono, signed PCM, little endian
            final AudioFormat mFormat = new AudioFormat(pSampleRate,
                                                        pBitsPerSample,
                                                        pNumOutputChannels,
                                                        SIGNED,
                                                        LITTLE_ENDIAN);
            mOutputLine = AudioSystem.getSourceDataLine(mFormat);
            final int BYTES_PER_SAMPLE = 2; // @TODO this probably needs to be adjusted … e.g `pBitsPerSample / 8`
            mByteBuffer = new byte[mSampleBufferSize * BYTES_PER_SAMPLE * pNumOutputChannels];
            mOutputLine.open(mFormat, mByteBuffer.length);
        } catch (LineUnavailableException e) {
            System.err.println(e.getMessage());
        }
        mOutputLine.start();
        start();
    }

    public int sample_rate() {
        return mSampleRate;
    }

    public void run() {
        while (mRunBuffer) {
            float[][] mBuffers = new float[mNumOutputChannels][];
            for (int j = 0; j < mNumOutputChannels; j++) {
                mBuffers[j] = new float[mSampleBufferSize];
            }
            mSampleRenderer.render(mBuffers);
            for (int i = 0; i < mSampleBufferSize; i++) {
                for (int j = 0; j < mNumOutputChannels; j++) {
                    writeSample(mBuffers[j][i], i * 2 + j);
                }
            }
            mOutputLine.write(mByteBuffer, 0, mByteBuffer.length);
        }
    }

    public int buffer_size() {
        return mSampleBufferSize;
    }

    private void writeSample(final float sample, final int i) {
        short s = (short) (MAX_16_BIT * sample);
        if (sample == 1.0) {
            s = Short.MAX_VALUE; // special case since 32768 not a short
        }
        mByteBuffer[i * 2 + 0] = (byte) s;
        mByteBuffer[i * 2 + 1] = (byte) (s >> 8); // little endian
    }
}
