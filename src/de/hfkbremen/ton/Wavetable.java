package de.hfkbremen.ton;

import processing.core.PApplet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Wavetable {

    private final int mSamplingRate;
    private final float[] mWavetable;
    private float mFrequency;
    private float mStepSize;
    private float mArrayPtr;
    private float mAmplitude;

    public Wavetable(int pWavetableSize) {
        this(pWavetableSize, DSP.DEFAULT_SAMPLING_RATE);
    }

    public Wavetable(int pWavetableSize, int pSamplingRate) {
        mWavetable = new float[pWavetableSize];
        mSamplingRate = pSamplingRate;
        mArrayPtr = 0;
        mAmplitude = 0.75f;
        set_frequency(220);
    }

    public static void from_bytes(byte[] pBytes, float[] pWavetable) {
        if (pBytes.length / 4 == pWavetable.length) {
            for (int i = 0; i < pWavetable.length; i++) {
                pWavetable[i] = byteToFloat32(pBytes, i * 4, (i + 1) * 4);
            }
        } else {
            System.err.println("+++ WARNING @ Wavetable.from_bytes / array sizes do not match. make sure the byte " +
                               "array is exactly 4 times the size of the float array");
        }
    }

    public static float byteToFloat32(byte[] b) {
        ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getFloat();
//        return ByteBuffer.wrap(b).getFloat();
    }

    public static byte[] float32ToByte(float f) {
        return ByteBuffer.allocate(4).putFloat(f).array();
    }

    public static void sine(float[] pWavetable) {
        for (int i = 0; i < pWavetable.length; i++) {
            pWavetable[i] = PApplet.sin(2.0f * PApplet.PI * ((float) i / (float) (pWavetable.length)));
        }
    }

    public static void sawtooth(float[] pWavetable) {
        for (int i = 0; i < pWavetable.length; i++) {
            pWavetable[i] = 2.0f * ((float) i / (float) (pWavetable.length - 1)) - 1.0f;
        }
    }

    public static void triangle(float[] pWavetable) {
        final int q = pWavetable.length / 4;
        final float qf = pWavetable.length * 0.25f;
        for (int i = 0; i < q; i++) {
            pWavetable[i] = i / qf;
            pWavetable[i + (q * 1)] = (qf - i) / qf;
            pWavetable[i + (q * 2)] = -i / qf;
            pWavetable[i + (q * 3)] = -(qf - i) / qf;
        }
    }

    public static void square(float[] pWavetable) {
        for (int i = 0; i < pWavetable.length / 2; i++) {
            pWavetable[i] = 1.0f;
            pWavetable[i + pWavetable.length / 2] = -1.0f;
        }
    }

    public static float byteToFloat32(byte[] pBytes, int pStart, int pEnd) {
        final byte[] mBytes = Arrays.copyOfRange(pBytes, pStart, pEnd);
        return byteToFloat32(mBytes);
    }

    public void set_frequency(float pFrequency) {
        if (mFrequency != pFrequency) {
            mFrequency = pFrequency;
            mStepSize = mFrequency * ((float) mWavetable.length / (float) mSamplingRate);
        }
    }

    public void set_amplitude(float pAmplitude) {
        mAmplitude = pAmplitude;
    }

    public float[] wavetable() {
        return mWavetable;
    }

    public float process() {
        mArrayPtr += mStepSize;
        final int i = (int) mArrayPtr;
        final float mFrac = mArrayPtr - i;
        final int j = i % mWavetable.length;
        mArrayPtr = j + mFrac;
        return mWavetable[j] * mAmplitude;
    }

    public static void main(String[] args) {
        float[] f = new float[4];
        byte[] b = new byte[]{
                64, 73, 6, 37,
                64, 73, 6, 37,
                64, 73, 6, 37,
                64, 73, 6, 37
        };
        from_bytes(b, f);
        PApplet.printArray(f);
    }
}