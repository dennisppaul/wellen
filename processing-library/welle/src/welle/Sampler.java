package welle;

import processing.core.PApplet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Sampler implements DSPNodeOutput {

    private final float mSamplingRate;
    private float[] mData;
    private float mFrequency;
    private float mStepSize;
    private float mArrayPtr;
    private float mAmplitude;
    private boolean mLoop = false;
    private boolean mDirectionForward;
    private float mSpeed;

    public Sampler() {
        this(0);
    }

    public Sampler(int pWavetableSize) {
        this(new float[pWavetableSize], Welle.DEFAULT_SAMPLING_RATE);
    }

    public Sampler(float[] pWavetable) {
        this(pWavetable, Welle.DEFAULT_SAMPLING_RATE);
    }

    public Sampler(float[] pWavetable, float pSamplingRate) {
        mData = pWavetable;
        mSamplingRate = pSamplingRate;
        mArrayPtr = 0;
        set_speed(1);
        set_amplitude(0.75f);
    }

    /**
     * load the sample buffer from *raw* byte data. the method assumes a raw format with 32bit float in a value range
     * from -1.0 to 1.0. from -1.0 to 1.0.
     *
     * @param pData raw byte data ( assuming 4 bytes per sample, 32-bit float )
     * @return instance with data loaded
     */
    public Sampler load(byte[] pData) {
        load(pData, true);
        return this;
    }

    /**
     * load the sample buffer from *raw* byte data. the method assumes a raw format with 32bit float in a value range
     * from -1.0 to 1.0.
     *
     * @param pData         raw byte data ( assuming 4 bytes per sample, 32-bit float )
     * @param pLittleEndian true if byte data is arranged in little endian order
     * @return instance with data loaded
     */
    public Sampler load(byte[] pData, boolean pLittleEndian) {
        if (mData == null || mData.length != pData.length / 4) {
            mData = new float[pData.length / 4];
        }
        convertBytesToFloat32(pData, data(), pLittleEndian);
        rewind();
        set_speed(mSpeed);
        return this;
    }

    public void set_speed(float pSpeed) {
        mSpeed = pSpeed;
        mDirectionForward = pSpeed > 0;
        set_frequency(PApplet.abs(pSpeed) * mSamplingRate / data().length); /* aka `mStepSize = pSpeed;` */
    }

    public void set_frequency(float pFrequency) {
        if (mFrequency != pFrequency) {
            mFrequency = pFrequency;
            mStepSize = mFrequency * ((float) mData.length / mSamplingRate);
        }
    }

    public void set_amplitude(float pAmplitude) {
        mAmplitude = pAmplitude;
    }

    public float[] data() {
        return mData;
    }

    public void set_data(float[] pData) {
        mData = pData;
    }

    public float output() {
        mArrayPtr += mStepSize;
        final int i = (int) mArrayPtr;
        if ((i > mData.length - 1 && !mLoop) || mData.length == 0) {
            return 0.0f;
        }
        final float mFrac = mArrayPtr - i;
        final int j = i % mData.length;
        mArrayPtr = j + mFrac;
        final float mSample = mDirectionForward ? mData[j] : mData[(mData.length - 1) - j];
        return mSample * mAmplitude;
    }

    public void rewind() {
        mArrayPtr = 0;
    }

    public void loop(boolean pLoop) {
        mLoop = pLoop;
    }

    public static void convertBytesToFloat32(byte[] pBytes, float[] pWavetable, boolean pLittleEndian) {
        if (pBytes.length / 4 == pWavetable.length) {
            for (int i = 0; i < pWavetable.length; i++) {
                pWavetable[i] = bytesToFloat32(pBytes, i * 4, (i + 1) * 4, pLittleEndian);
            }
        } else {
            System.err.println("+++ WARNING @ Wavetable.from_bytes / array sizes do not match. make sure the byte " +
                    "array is exactly 4 times the size of the float array");
        }
    }

    public static float bytesToFloat32(byte[] b) {
        return bytesToFloat32(b, true);
    }

    public static float bytesToFloat32(byte[] b, boolean pLittleEndian) {
        if (b.length != 4) {
            System.out.println("+++ WARNING @ Sampler.bytesToFloat32(byte[], boolean)");
        }
        return ByteBuffer.wrap(b).order(pLittleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).getFloat();
    }

    public static byte[] float32ToByte(float f) {
        return ByteBuffer.allocate(4).putFloat(f).array();
    }

    private static float bytesToFloat32(byte[] pBytes, int pStart, int pEnd, boolean pLittleEndian) {
        final byte[] mBytes = Arrays.copyOfRange(pBytes, pStart, pEnd);
        return bytesToFloat32(mBytes, pLittleEndian);
    }
}
