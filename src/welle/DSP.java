package welle;

import processing.core.PApplet;
import processing.core.PGraphics;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DSP implements AudioBufferRenderer {

    private static final String METHOD_NAME = "audioblock";
    private static AudioBufferManager mAudioPlayer;
    private static DSP mInstance = null;
    private final Object mListener;
    private final int mNumberOutputChannels;
    private final int mNumberInputChannels;
    private Method mMethod = null;
    private float[] mCurrentBufferLeft;
    private float[] mCurrentBufferRight;

    public DSP(Object pListener, int pNumberOutputChannels, int pNumberInputChannels) {
        mListener = pListener;
        mNumberOutputChannels = pNumberOutputChannels;
        mNumberInputChannels = pNumberInputChannels;
        try {
            if (mNumberOutputChannels == 2 && mNumberInputChannels == 2) {
                mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME,
                        float[].class,
                        float[].class,
                        float[].class,
                        float[].class);
            } else if (mNumberOutputChannels == 2 && mNumberInputChannels == 0) {
                mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME,
                        float[].class,
                        float[].class);
            } else if (mNumberOutputChannels == 1 && mNumberInputChannels == 1) {
                mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME,
                        float[].class,
                        float[].class);
            } else if (mNumberOutputChannels == 1 && mNumberInputChannels == 0) {
                mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME,
                        float[].class);
            } else {
                mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME,
                        float[][].class);
            }
        } catch (NoSuchMethodException | SecurityException ex) {
            System.err.println("+++ @DSP / could not find callback `" + METHOD_NAME + "()`. hint: check the callback " +
                    "method paramters, they must match the number of input and ouput channels. default is `" + METHOD_NAME + "(float[])` ( = MONO OUTPUT ).");
        }
    }

    public void audioblock(float[][] pOutputSamples, float[][] pInputSamples) {
        try {
            if (mNumberOutputChannels == 1 && mNumberInputChannels == 0) {
                mMethod.invoke(mListener, pOutputSamples[0]);
                mCurrentBufferLeft = pOutputSamples[0];
            } else if (mNumberOutputChannels == 1 && mNumberInputChannels == 1) {
                mMethod.invoke(mListener, pOutputSamples[0], pInputSamples[0]);
                mCurrentBufferLeft = pOutputSamples[0];
            } else if (mNumberOutputChannels == 2 && mNumberInputChannels == 0) {
                mMethod.invoke(mListener, pOutputSamples[0], pOutputSamples[1]);
                mCurrentBufferLeft = pOutputSamples[0];
                mCurrentBufferRight = pOutputSamples[1];
            } else if (mNumberOutputChannels == 2 && mNumberInputChannels == 2) {
                mMethod.invoke(mListener, pOutputSamples[0], pOutputSamples[1], pInputSamples[0], pInputSamples[1]);
                mCurrentBufferLeft = pOutputSamples[0];
                mCurrentBufferRight = pOutputSamples[1];
            } else {
                mMethod.invoke(mListener, pOutputSamples);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    public static DSP start(PApplet pPApplet) {
        return start(pPApplet, 1, 0);
    }

    public static DSP start(PApplet pPApplet, int pNumberOutputChannels) {
        return start(pPApplet, pNumberOutputChannels, 0);
    }

    public static DSP start(PApplet pPApplet, int pNumberOutputChannels, int pNumberInputChannels) {
        return start(pPApplet, AudioBufferManager.DEFAULT, pNumberOutputChannels, AudioBufferManager.DEFAULT,
                pNumberInputChannels);
    }

    public static DSP start(PApplet pPApplet,
            int pOutputDevice,
            int pNumberOutputChannels,
            int pInputDevice,
            int pNumberInputChannels) {
        if (mInstance == null) {
            mInstance = new DSP(pPApplet, pNumberOutputChannels, pNumberInputChannels);
            mAudioPlayer = new AudioBufferManager(mInstance,
                    Welle.DEFAULT_SAMPLING_RATE,
                    Welle.DEFAULT_AUDIOBLOCK_SIZE,
                    pOutputDevice,
                    pNumberOutputChannels,
                    pInputDevice,
                    pNumberInputChannels);
        }
        return mInstance;
    }

    public static int get_sample_rate() {
        return mAudioPlayer == null ? 0 : mAudioPlayer.sample_rate();
    }

    public static int get_buffer_size() {
        return mAudioPlayer == null ? 0 : mAudioPlayer.buffer_size();
    }

    public static float[] get_buffer() {
        return get_buffer_left();
    }

    public static float[] get_buffer_left() {
        return mInstance == null ? null : mInstance.mCurrentBufferLeft;
    }

    public static float[] get_buffer_right() {
        return mInstance == null ? null : mInstance.mCurrentBufferRight;
    }


    public static void draw_buffer(PGraphics g, int pWidth, int pHeight) {
        final int mBufferSize = DSP.get_buffer_size();
        if (DSP.get_buffer() != null) {
//            g.beginShape(PConstants.LINES);
            for (int i = 0; i < mBufferSize - 1; i++) {
//                final float x = PApplet.map(i, 0, mBufferSize, 0, pWidth);
//                g.vertex(x, PApplet.map(DSP.buffer()[i], -1, 1, 0, pHeight));
                g.line(PApplet.map(i, 0, mBufferSize, 0, pWidth),
                        PApplet.map(DSP.get_buffer()[i], -1, 1, 0, pHeight),
                        PApplet.map(i + 1, 0, mBufferSize, 0, pWidth),
                        PApplet.map(DSP.get_buffer()[i + 1], -1, 1, 0, pHeight));
            }
//            g.endShape();
        }
    }
}

