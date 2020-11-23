package de.hfkbremen.ton;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Sampler {

    private float[] mData;
    private float mFrequency;
    private float mStepSize;
    private float mArrayPtr;
    private float mAmplitude;
    private boolean mLoop = false;

    public Sampler() {
        this(0);
    }

    public Sampler(int pWavetableSize) {
        mData = new float[pWavetableSize];
        mArrayPtr = 0;
        set_frequency(220);
        set_amplitude(0.75f);
    }

    public void load(byte[] pData) {
        load(pData, true);
    }

    public void load(byte[] pData, boolean pLittleEndian) {
        if (mData == null || mData.length != pData.length / 4) {
            mData = new float[pData.length / 4];
        }
        convertBytesToFloat32(pData, data(), pLittleEndian);
        rewind();
    }

    public void set_speed(float pSpeed) {
        if (pSpeed > 0) {
            set_frequency(pSpeed * DSP.DEFAULT_SAMPLING_RATE / data().length); /* aka `mStepSize = pSpeed;` */
        } else {
            set_frequency(0);
        }
    }

    public void set_frequency(float pFrequency) {
        if (mFrequency != pFrequency) {
            mFrequency = pFrequency;
            mStepSize = mFrequency * ((float) mData.length / (float) DSP.DEFAULT_SAMPLING_RATE);
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

    public float process() {
        mArrayPtr += mStepSize;
        final int i = (int) mArrayPtr;
        if ((i > mData.length - 1 && !mLoop) || mData.length == 0) {
            return 0.0f;
        }
        final float mFrac = mArrayPtr - i;
        final int j = i % mData.length;
        mArrayPtr = j + mFrac;
        return mData[j] * mAmplitude;
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

    private static float bytesToFloat32(byte[] b, boolean pLittleEndian) {
        return ByteBuffer.wrap(b).order(pLittleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).getFloat();
    }

    private static byte[] float32ToByte(float f) {
        return ByteBuffer.allocate(4).putFloat(f).array();
    }

    private static float bytesToFloat32(byte[] pBytes, int pStart, int pEnd, boolean pLittleEndian) {
        final byte[] mBytes = Arrays.copyOfRange(pBytes, pStart, pEnd);
        return bytesToFloat32(mBytes, pLittleEndian);
    }
}
