package wellen;

import processing.core.PApplet;
import processing.core.PGraphics;

import javax.sound.sampled.AudioSystem;

public class Wellen {

    public static final int OSC_SINE = 0;
    public static final int OSC_TRIANGLE = 1;
    public static final int OSC_SAWTOOTH = 2;
    public static final int OSC_SQUARE = 3;
    public static final int OSC_NOISE = 4;
    public static final int TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT = -2;
    public static final String TONE_ENGINE_INTERNAL = "internal";
    public static final String TONE_ENGINE_MIDI = "midi";
    public static final String TONE_ENGINE_OSC = "osc";
    public static final float DEFAULT_ATTACK = 0.005f;
    public static final float DEFAULT_DECAY = 0.01f;
    public static final float DEFAULT_RELEASE = 0.075f;
    public static final float DEFAULT_SUSTAIN = 0.5f;
    public static final int DEFAULT_SAMPLING_RATE = 44100;
    public static final int DEFAULT_AUDIOBLOCK_SIZE = 512;
    public static final int DEFAULT_AUDIO_DEVICE = -1;
    public static final int DEFAULT_WAVETABLE_SIZE = 512;
    public static final int NO_CHANNELS = 0;

    public static int clamp127(int pValue) {
        return Math.max(0, Math.min(127, pValue));
    }

    public static void dumpMidiOutputDevices() {
        final String[] mOutputNames = MidiOut.availableOutputs();
        System.out.println("+-------------------------------------------------------+");
        System.out.println("+ MIDI OUTPUT DEVICES ( aka Ports or Buses )");
        System.out.println("+-------------------------------------------------------+");
        for (String mOutputName : mOutputNames) {
            System.out.println("+ " + mOutputName);
        }
        System.out.println("+-------------------------------------------------------+");
        System.out.println();
    }

    public static void dumpMidiInputDevices() {
        final String[] mInputNames = MidiIn.availableInputs();
        System.out.println("+-------------------------------------------------------+");
        System.out.println("+ MIDI INPUT DEVICES");
        System.out.println("+-------------------------------------------------------+");
        for (String mOutputName : mInputNames) {
            System.out.println("+ " + mOutputName);
        }
        System.out.println("+-------------------------------------------------------+");
        System.out.println();
    }

    public static void dumpAudioInputAndOutputDevices() {
        System.out.println("+-------------------------------------------------------+");
        System.out.println("+ AUDIO DEVICES ( Audio System )");
        System.out.println("+-------------------------------------------------------+");
        for (int i = 0; i < AudioSystem.getMixerInfo().length; i++) {
            System.out.println("+ " + i + "\t: " + AudioSystem.getMixerInfo()[i].getName());
        }
        System.out.println("+-------------------------------------------------------+");
        System.out.println();
    }

    public static int constrain(int value, int min, int max) {
        if (value > max) {
            value = max;
        }
        if (value < min) {
            value = min;
        }
        return value;
    }

    public static float clamp(float pValue, float pMin, float pMax) {
        if (pValue > pMax) {
            return pMax;
        } else if (pValue < pMin) {
            return pMin;
        } else {
            return pValue;
        }
//        return Math.max(pMin, Math.min(pMax, pValue));
    }

    public static float[] get_extremum(float[] pSignal) {
        float mMaximum = Float.MIN_VALUE;
        float mMinimum = Float.MAX_VALUE;
        for (float f : pSignal) {
            if (f > mMaximum) {
                mMaximum = f;
            }
            if (f < mMinimum) {
                mMinimum = f;
            }
        }
        return new float[]{mMinimum, mMaximum};
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

    public static void draw_buffer(PGraphics g, float pWidth, float pHeight, float[] pBuffer) {
        g.line(0, pHeight * 0.5f, pWidth, pHeight * 0.5f);
        if (pBuffer != null) {
            for (int i = 0; i < pBuffer.length - 1; i++) {
                g.line(PApplet.map(i, 0, pBuffer.length, 0, pWidth),
                       PApplet.map(pBuffer[i], -1.0f, 1.0f, 0, pHeight),
                       PApplet.map(i + 1, 0, pBuffer.length, 0, pWidth),
                       PApplet.map(pBuffer[i + 1], -1.0f, 1.0f, 0, pHeight));
            }
        }
    }

    public static void draw_buffer(PGraphics g,
                                   float pWidth,
                                   float pHeight,
                                   float[] pBufferLeft,
                                   float[] pBufferRight) {
        g.pushMatrix();
        draw_buffer(g, pWidth, pHeight * 0.5f, pBufferLeft);
        g.translate(0, pHeight * 0.5f);
        draw_buffer(g, pWidth, pHeight * 0.5f, pBufferRight);
        g.popMatrix();
    }

    public static float random(float pMin, float pMax) {
        if (pMin >= pMax) {
            return pMin;
        } else {
            final float mDiff = pMax - pMin;
            final float mValue = (float) Math.random() * mDiff + pMin;
            return mValue;
        }
    }
}
