package welle;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public abstract class Tone {

    private static ToneEngine instance = null;

    private Tone() {
    }

    public static void start(String... pName) {
        if (instance != null) {
            printAlreadyStartedWarning();
            return;
        }
        instance = ToneEngine.create(pName);
    }

    public static void start(String pName, int pParameter) {
        if (instance != null) {
            printAlreadyStartedWarning();
            return;
        }
        if (pName.equalsIgnoreCase(Welle.TONE_ENGINE_INTERNAL)) {
            /* specify output channels */
            // ToneEngineInternal(int pSamplingRate, int pOutputDeviceID, int pOutputChannels)
            instance = new ToneEngineInternal(Welle.DEFAULT_SAMPLING_RATE, Welle.DEFAULT_AUDIO_DEVICE, pParameter);
        } else if (pName.equalsIgnoreCase(Welle.TONE_ENGINE_MIDI)) {
            /* specify output device ID */
            instance = new ToneEngineMIDI(pParameter);
        } else {
            instance = ToneEngine.create(pName);
        }
    }

    public static void start(String pName, int pParameterA, int pParameterB) {
        if (instance != null) {
            printAlreadyStartedWarning();
            return;
        }
        if (pName.equalsIgnoreCase(Welle.TONE_ENGINE_INTERNAL)) {
            /* specify output device + output channels */
            // ToneEngineInternal(int pSamplingRate, int pOutputDeviceID, int pOutputChannels)
            instance = new ToneEngineInternal(Welle.DEFAULT_SAMPLING_RATE, pParameterA, pParameterB);
        } else {
            instance = ToneEngine.create(pName);
        }
    }

    public static void start(String pName, int pParameterA, int pParameterB, int pParameterC) {
        if (instance != null) {
            printAlreadyStartedWarning();
            return;
        }
        if (pName.equalsIgnoreCase(Welle.TONE_ENGINE_INTERNAL)) {
            /* specify sampling rate + output device + output channels */
            // ToneEngineInternal(int pSamplingRate, int pOutputDeviceID, int pOutputChannels)
            instance = new ToneEngineInternal(pParameterA, pParameterB, pParameterC);
        } else {
            instance = ToneEngine.create(pName);
        }
    }

    public static ToneEngineInternal start(int pConfiguration) {
        if (instance != null) {
            printAlreadyStartedWarning();
            if (instance instanceof ToneEngineInternal) {
                return (ToneEngineInternal) instance;
            }
        }
        if (pConfiguration == Welle.TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT) {
            ToneEngineInternal mInstance = new ToneEngineInternal(Welle.DEFAULT_SAMPLING_RATE,
                    Welle.DEFAULT_AUDIO_DEVICE,
                    Welle.NO_CHANNELS);
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

    public static ToneEngine instance() {
        if (instance == null) {
            instance = ToneEngine.create();
        }
        return instance;
    }

    public static void set_engine(ToneEngine pEngine) {
        instance = pEngine;
    }

    private static void printAlreadyStartedWarning() {
        System.err.println("+++ WARNING @" + Tone.class.getSimpleName() + ".start" +
                " / tone engine already initialized. make sure that `start` is the first call to `Ton`. " +
                "use `set_engine(ToneEngine)` to switch tone engines.");
    }
}
