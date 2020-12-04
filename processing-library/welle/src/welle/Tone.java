package welle;

import processing.core.PApplet;

import javax.sound.sampled.AudioSystem;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

public abstract class Tone {

    public static final int OSC_SINE = 0;
    public static final int OSC_TRIANGLE = 1;
    public static final int OSC_SAWTOOTH = 2;
    public static final int OSC_SQUARE = 3;
    public static final int OSC_NOISE = 4;
    public static final int NUMBER_OF_OSCILLATORS = 5;
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

    private static ToneEngine instance = null;

    private Tone() {
    }

    public static void start(String... pName) {
        if (instance != null) {
            printAlreadyStartedWarning();
            return;
        }
        instance = ToneEngine.createEngine(pName);
    }

    public static void start(String pName, int pParameter) {
        if (instance != null) {
            printAlreadyStartedWarning();
            return;
        }
        if (pName.equalsIgnoreCase(TONE_ENGINE_INTERNAL)) {
            /* specify output channels */
            // ToneEngineInternal(int pSamplingRate, int pOutputDeviceID, int pOutputChannels)
            instance = new ToneEngineInternal(DEFAULT_SAMPLING_RATE, DEFAULT_AUDIO_DEVICE, pParameter);
        } else if (pName.equalsIgnoreCase(TONE_ENGINE_MIDI)) {
            /* specify output device ID */
            instance = new ToneEngineMIDI(pParameter);
        } else {
            instance = ToneEngine.createEngine(pName);
        }
    }

    public static void start(String pName, int pParameterA, int pParameterB) {
        if (instance != null) {
            printAlreadyStartedWarning();
            return;
        }
        if (pName.equalsIgnoreCase(TONE_ENGINE_INTERNAL)) {
            /* specify output device + output channels */
            // ToneEngineInternal(int pSamplingRate, int pOutputDeviceID, int pOutputChannels)
            instance = new ToneEngineInternal(DEFAULT_SAMPLING_RATE, pParameterA, pParameterB);
        } else {
            instance = ToneEngine.createEngine(pName);
        }
    }

    public static void start(String pName, int pParameterA, int pParameterB, int pParameterC) {
        if (instance != null) {
            printAlreadyStartedWarning();
            return;
        }
        if (pName.equalsIgnoreCase(TONE_ENGINE_INTERNAL)) {
            /* specify sampling rate + output device + output channels */
            // ToneEngineInternal(int pSamplingRate, int pOutputDeviceID, int pOutputChannels)
            instance = new ToneEngineInternal(pParameterA, pParameterB, pParameterC);
        } else {
            instance = ToneEngine.createEngine(pName);
        }
    }

    public static ToneEngineInternal start(int pConfiguration) {
        if (instance != null) {
            printAlreadyStartedWarning();
            if (instance instanceof ToneEngineInternal) {
                return (ToneEngineInternal) instance;
            }
        }
        if (pConfiguration == TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT) {
            ToneEngineInternal mInstance = new ToneEngineInternal(DEFAULT_SAMPLING_RATE,
                    DEFAULT_AUDIO_DEVICE,
                    NO_CHANNELS);
            instance = mInstance;
            return mInstance;
        } else {
            System.err.println("+++ WARNING @" + Tone.class.getSimpleName() + ".start" +
                    " / unknown configuration, using default");
            return new ToneEngineInternal();
        }
    }

    public static void note_on(int pNote, int pVelocity, float pDuration) {
        instance().note_on(pNote, pVelocity, pDuration);
    }

    public static void note_on(int pNote, int pVelocity) {
        instance().note_on(pNote, pVelocity);
    }

    public static void note_off(int pNote) {
        instance().note_off(pNote);
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

    public static boolean is_playing() {
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
        //@TODO(maybe move this to ToneEngine)
        T mInstrument;
        try {
            Constructor<T> c;
            //@TODO(add constructor for `InstrumentInternal(int pID, int pSamplingRate, int pWavetableSize)`)
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

    private static void printAlreadyStartedWarning() {
        System.err.println("+++ WARNING @" + Tone.class.getSimpleName() + ".start" +
                " / tone engine already initialized. make sure that `start` is the first call to `Ton`. " +
                "use `set_engine(ToneEngine)` to switch tone engines.");
    }
}
