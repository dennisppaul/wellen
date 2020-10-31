package de.hfkbremen.ton;

import java.util.ArrayList;

public class Ton {

    private static ToneEngine instance = null;

    private Ton() {
    }

    public static void init(String... pName) {
        if (instance != null) {
            System.err.println("+++ @init / tone engine already initialized. make sure that `init` is the first call to `Ton`.");
        }
        instance = ToneEngine.createEngine(pName);
    }

    public static void noteOn(int note, int velocity) {
        instance().noteOn(note, velocity);
    }

    public static void noteOff(int note) {
        instance().noteOff(note);
    }

    public static void noteOff() {
        instance().noteOff();
    }

    public static void control_change(int pCC, int pValue) {
        instance().control_change(pCC, pValue);
    }

    public static void pitch_bend(int pValue) {
        instance().pitch_bend(pValue);
    }

    public static boolean isPlaying() {
        return instance().isPlaying();
    }

    public static Instrument instrument(int pInstrumentID) {
        return instance().instrument(pInstrumentID);
    }

    public static Instrument instrument() {
        return instance().instrument();
    }

    public static ArrayList<? extends Instrument> instruments() {
        return instance().instruments();
    }

    private static ToneEngine instance() {
        if (instance == null) {
            instance = ToneEngine.createEngine();
        }
        return instance;
    }
}
