package de.hfkbremen.ton;

import processing.core.PApplet;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioBufferPlayer extends Thread {

    /*
     * - @TODO(add recording (TargetDataLine))
     * - @TODO(add stereo)
     * - @REF([Java Sound Resources: FAQ: Audio Programming](http://jsresources.sourceforge.net/faq_audio
     * .html#sync_playback_recording))
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
    private SourceDataLine mOutputLine;
    private byte[] mByteBuffer;
    private boolean mRunBuffer = true;

    public AudioBufferPlayer(AudioBufferRenderer pSampleRenderer) {
        this(pSampleRenderer, 44100, 512, 16, MONO);
    }

    public AudioBufferPlayer(AudioBufferRenderer pSampleRenderer,
                             int pSampleRate,
                             int pSampleBufferSize,
                             int pBitsPerSample,
                             int pNumOutputChannels) {
        mSampleRenderer = pSampleRenderer;
        mSampleRate = pSampleRate;
        mSampleBufferSize = pSampleBufferSize;

        try {
            // 44,100 Hz, 16-bit audio, mono, signed PCM, little endian
            AudioFormat mFormat = new AudioFormat(pSampleRate,
                                                  pBitsPerSample,
                                                  pNumOutputChannels,
                                                  SIGNED,
                                                  LITTLE_ENDIAN);
            DataLine.Info mInfo = new DataLine.Info(SourceDataLine.class, mFormat);

            final int BYTES_PER_SAMPLE = 2; // @TODO this probably needs to be adjusted … e.g `pBitsPerSample / 8`

            mOutputLine = (SourceDataLine) AudioSystem.getLine(mInfo);
            mOutputLine.open(mFormat, mSampleBufferSize * BYTES_PER_SAMPLE);

            mByteBuffer = new byte[mSampleBufferSize * BYTES_PER_SAMPLE];
        } catch (LineUnavailableException e) {
            System.err.println(e.getMessage());
        }

        mOutputLine.start();
        start();
    }

    public static float clamp(float pValue, float pMin, float pMax) {
        if (pValue > pMax) {
            return pMax;
        } else if (pValue < pMin) {
            return pMin;
        } else {
            return pValue;
        }
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

    public int sample_rate() {
        return mSampleRate;
    }

    public void run() {
        while (mRunBuffer) {
            final float[] mBuffer = new float[mSampleBufferSize];
            mSampleRenderer.render(mBuffer);
            for (int i = 0; i < mBuffer.length; i++) {
                writeSample(mBuffer[i], i);
            }
            mOutputLine.write(mByteBuffer, 0, mByteBuffer.length);
        }
    }

    public void close() {
        mRunBuffer = false;
        mOutputLine.drain();
        mOutputLine.stop();
        mOutputLine.close();
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
