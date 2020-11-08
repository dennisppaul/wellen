package de.hfkbremen.ton;

import java.util.ArrayList;
import java.util.Timer;

import static processing.core.PApplet.constrain;

public class ToneEngineMIDI extends ToneEngine {

    public static final int CC_MODULATION = 1;
    public final MidiOut mMidiOut;
    private final Timer mTimer;
    private int mLastPlayedNote = -1;
    private int mChannel;

    public ToneEngineMIDI(String pMidiOutputDeviceName) {
        mTimer = new Timer();
        mMidiOut = new MidiOut(getProperDeviceName(pMidiOutputDeviceName));
        prepareExitHandler();
    }

    public static String getProperDeviceName(String pMidiOutputDeviceName) {
        String[] mDevices = MidiOut.availableOutputs();
        for (String mDevice : mDevices) {
            if (mDevice.startsWith(pMidiOutputDeviceName)) {
                return mDevice;
            }
        }
        System.err.println("### couldn t find midi device");
        return null;
    }

    public void note_on(int note, int velocity, float duration) {
        mTimer.schedule(new MidiTimerNoteOffTask(mMidiOut, mChannel, note, velocity), (long) duration * 1000);
        note_on(note, velocity);
    }

    public void note_on(int note, int velocity) {
        mMidiOut.sendNoteOn(mChannel, note, velocity);
        mLastPlayedNote = note;
    }

    public void note_off(int note) {
        mMidiOut.sendNoteOff(mChannel, note, 0);
        mLastPlayedNote = -1;
    }

    public void note_off() {
        note_off(mLastPlayedNote);
    }

    public void control_change(int pCC, int pValue) {
        mMidiOut.sendControllerChange(mChannel, pCC, pValue);
    }

    public void pitch_bend(int pValue) {
        final int mValue = constrain(pValue, 0, 16383);
        final int LSB_MASK = 0b00000001111111;
        final int MSB_MASK = 0b11111110000000;
        final int msb = (mValue & MSB_MASK) / 128;
        final int lsb = mValue & LSB_MASK;
        mMidiOut.sendPitchBend(mChannel, lsb, msb);
    }

    public boolean isPlaying() {
        return (mLastPlayedNote != -1);
    }

    public Instrument instrument(int pInstrumentID) {
        mChannel = pInstrumentID;
        return null;
    }

    public Instrument instrument() {
        return null;
    }

    public ArrayList<? extends Instrument> instruments() {
        return null;
    }

    private void prepareExitHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < 127; i++) {
                    mMidiOut.sendNoteOff(mChannel, i, 0);
                    mLastPlayedNote = -1;
                }
                mMidiOut.close();
            }
        }));
    }
}
