package wellen;

import processing.core.PApplet;

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
}
