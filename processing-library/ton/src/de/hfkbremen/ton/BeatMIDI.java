package de.hfkbremen.ton;

import processing.core.PApplet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BeatMIDI implements MidiInListener {

    private static final boolean VERBOSE = true;
    private final PApplet mPApplet;
    private Method mMethod = null;
    private int mTickCounter = 0;
    private boolean mIsRunning = false;
    private static final String METHOD_NAME = "beat";

    public BeatMIDI(PApplet pPApplet, int pBPM) {
        this(pPApplet);
    }

    public BeatMIDI(PApplet pPApplet) {
        mPApplet = pPApplet;
        try {
            mMethod = pPApplet.getClass().getDeclaredMethod(METHOD_NAME, Integer.TYPE);
        } catch (NoSuchMethodException | SecurityException ex) {
            ex.printStackTrace();
        }
    }

    public boolean running() {
        return mIsRunning;
    }

    public int beat_count() {
        return mTickCounter;
    }

    public void invoke() {
        if (mIsRunning) {
            try {
                mMethod.invoke(mPApplet, mTickCounter);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void receiveProgramChange(int channel, int number, int value) {
    }

    @Override
    public void receiveControlChange(int channel, int number, int value) {
    }

    @Override
    public void receiveNoteOff(int channel, int pitch) {
    }

    @Override
    public void receiveNoteOn(int channel, int pitch, int velocity) {
    }

    @Override
    public void clock_tick() {
        if (mIsRunning) {
            mTickCounter++;
            invoke();
        }
    }

    @Override
    public void clock_start() {
        if (VERBOSE) {
            System.out.println("clock_start");
        }
        mTickCounter = 0;
        mIsRunning = true;
//        invoke();
    }

    @Override
    public void clock_continue() {
        if (VERBOSE) {
            System.out.println("clock_continue");
        }
        mIsRunning = true;
//        clock_tick();
    }

    @Override
    public void clock_stop() {
        if (VERBOSE) {
            System.out.println("clock_stop");
        }
//        if (mIsRunning) {
//            // @TODO(check if this is desired behavior)
//            mTickCounter++;
//        }
        mIsRunning = false;
    }

    public static BeatMIDI start(PApplet pPApplet, String pMidiInput) {
        final BeatMIDI mBeatMIDI = new BeatMIDI(pPApplet);
        MidiIn mMidiIn = new MidiIn(pMidiInput);
        mMidiIn.addListener(mBeatMIDI);
        return mBeatMIDI;
    }
}
