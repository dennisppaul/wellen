package de.hfkbremen.ton;

import processing.core.PApplet;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public abstract class Ton {

    public static final int OSC_SINE = 0;
    public static final int OSC_TRIANGLE = 1;
    public static final int OSC_SAWTOOTH = 2;
    public static final int OSC_SQUARE = 3;
    public static final int OSC_NOISE = 4;
    public static final int NUMBER_OF_OSCILLATORS = 5;
    public static final String TONE_ENGINE_SOFTWARE = "software";
    public static final String TONE_ENGINE_MIDI = "midi";
    public static final String TONE_ENGINE_OSC = "osc";
    public static final float DEFAULT_ATTACK = 0.005f;
    public static final float DEFAULT_DECAY = 0.01f;
    public static final float DEFAULT_RELEASE = 0.075f;
    public static final float DEFAULT_SUSTAIN = 0.5f;
    public static final int DEFAULT_SAMPLING_RATE = 44100;
    public static final int DEFAULT_AUDIOBLOCK_SIZE = 512;
    private static ToneEngine instance = null;

    private Ton() {
    }

    public static void start(String... pName) {
        if (instance != null) {
            System.err.println(
                    "+++ @start / tone engine already initialized. make sure that `start` is the first call to `Ton`.");
        }
        instance = ToneEngine.createEngine(pName);
    }

    public static void start(String pName, int pParameter) {
        if (pName.equalsIgnoreCase(TONE_ENGINE_MIDI)) {
            instance = new ToneEngineMIDI(pParameter);
        } else {
            instance = ToneEngine.createEngine(pName);
        }
    }

    public static void start(String pName, int pParameterA, int pParameterB) {
        if (instance != null) {
            System.err.println(
                    "+++ @start / tone engine already initialized. make sure that `start` is the first call to `Ton`.");
        }
        if (pName.equalsIgnoreCase(TONE_ENGINE_SOFTWARE)) {
            /* specify output device */
            //     public ToneEngineSoftware(int pSamplingRate,
            //                              int pOutputDeviceID,
            //                              int pOutputChannels)
            instance = new ToneEngineSoftware(DEFAULT_SAMPLING_RATE, pParameterA, pParameterB);
        } else {
            instance = ToneEngine.createEngine(pName);
        }
    }

    public static void note_on(int note, int velocity, float duration) {
        instance().note_on(note, velocity, duration);
    }

    public static void note_on(int note, int velocity) {
        instance().note_on(note, velocity);
    }

    public static void note_off(int note) {
        instance().note_off(note);
    }

    public static void note_off() {
        instance().note_off();
    }

    public static void control_change(int pCC, int pValue) {
        instance().control_change(pCC, pValue);
    }

    public static void pitch_bend(int pValue) {
        instance().pitch_bend(pValue);
    }

    public static boolean isPlaying() {
        return instance().is_playing();
    }

    public static Instrument instrument(int pInstrumentID) {
        return instance().instrument(pInstrumentID);
    }

    public static Instrument instrument() {
        return instance().instrument();
    }

    public static void replace_instrument(Class<? extends Instrument> pInstrumentClass, int pID) {
        instance().replace_instrument(create_instrument(pInstrumentClass, pID));
    }

    public static void replace_instrument(Instrument pInstrument) {
        instance().replace_instrument(pInstrument);
    }

    public static ArrayList<? extends Instrument> instruments() {
        return instance().instruments();
    }

    public static int clamp127(int pValue) {
        return Math.max(0, Math.min(127, pValue));
    }

    public static <T extends Instrument> T create_instrument(Class<T> pInstrumentClass, int pID) {
        T mInstrument;
        try {
            Constructor<T> c;
//            if (InstrumentJSyn.class.isAssignableFrom(pInstrumentClass) && instance() instanceof ToneEngineJSyn) {
//                c = pInstrumentClass.getDeclaredConstructor(ToneEngineJSyn.class, int.class);
//                mInstrument = c.newInstance(instance(), pID);
//            } else if (pInstrumentClass == InstrumentMinim.class && instance() instanceof ToneEngineMinim) {
//                c = pInstrumentClass.getDeclaredConstructor(Minim.class, int.class);
//                mInstrument = c.newInstance((Minim) ((ToneEngineMinim) instance()).minim(), pID);
//            } else {
            c = pInstrumentClass.getDeclaredConstructor(int.class);
            mInstrument = c.newInstance(pID);
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
            mInstrument = null;
        }
        return mInstrument;
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
    }

    public static void dumpMidiInputDevices() {
        final String[] mInputNames = MidiIn.availableInputs();
        System.out.println("+-------------------------------------------------------+");
        System.out.println("+ MIDI INPUT DEVICES");
        System.out.println("+-------------------------------------------------------+");
        for (String mOutputName : mInputNames) {
            System.out.println("  - " + mOutputName);
        }
    }

//    public static void dumpAudioDevices() {
//        final JavaSoundAudioDevice mDevice = new JavaSoundAudioDevice();
//        System.out.println("+-------------------------------------------------------+");
//        System.out.println("AUDIO DEVICES ( Java Sound )");
//        System.out.println("+-------------------------------------------------------+");
//        for (int i = 0; i < mDevice.getDeviceCount(); i++) {
//            System.out.println("+ " + "ID ................ : " + i);
//            System.out.println("+ " + "NAME .............. : " + mDevice.getDeviceName(i));
//            System.out.println("+ " + "OUTPUT CHANNELS ... : " + mDevice.getMaxOutputChannels(i));
//            System.out.println("+ " + "INPUT CHANNELS .... : " + mDevice.getMaxInputChannels(i));
//            System.out.println("+-------------------------------------------------------+");
//        }
//    }

//    public static void buildSelectMidiDeviceMenu(ControlP5 controls) {
//        final int mListWidth = 300, mListHeight = 300;
//
//        DropdownList dl = controls.addDropdownList("Please select MIDI Device",
//                                                   (controls.papplet.width - mListWidth) / 2,
//                                                   (controls.papplet.height - mListHeight) / 2,
//                                                   mListWidth,
//                                                   mListHeight);
//
//        //        dl.toUpperCase(true);
//        dl.setItemHeight(16);
//        dl.setBarHeight(16);
//        dl.getCaptionLabel().align(PConstants.LEFT, PConstants.CENTER);
//
//        final String[] mOutputNames = MidiOut.availableOutputs();
//        for (int i = 0; i < mOutputNames.length; i++) {
//            dl.addItem(mOutputNames[i], i);
//        }
//    }

    public static int constrain(int value, int min, int max) {
        if (value > max) {
            value = max;
        }
        if (value < min) {
            value = min;
        }
        return value;
    }

    public static void run(Class<? extends PApplet> T, String... pArgs) {
        String[] mArgs;
        mArgs = PApplet.concat(new String[]{"--sketch-path=" + System.getProperty("user.dir") + "/simulator"},
                               pArgs);
        mArgs = PApplet.concat(mArgs, new String[]{T.getName()});
        PApplet.main(mArgs);
    }

    public static ToneEngine instance() {
        if (instance == null) {
            instance = ToneEngine.createEngine();
        }
        return instance;
    }

    public static void set_engine(ToneEngine pEngine) {
        instance = pEngine;
    }

    public static float clamp(float pValue, float pMin, float pMax) {
        return Math.max(pMin, Math.min(pMax, pValue));
    }
}
