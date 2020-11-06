package de.hfkbremen.ton;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static de.hfkbremen.ton.MIDI.MIDI_CLOCK_CONTINUE;
import static de.hfkbremen.ton.MIDI.MIDI_SONG_POSITION_POINTER;
import static de.hfkbremen.ton.MIDI.MIDI_CLOCK_START;
import static de.hfkbremen.ton.MIDI.MIDI_CLOCK_STOP;
import static de.hfkbremen.ton.MIDI.MIDI_CLOCK_TICK;


public class EventReceiverMIDI implements MidiInListener {

    public static final int EVENT_UNDEFINED = -1;
    public static final int EVENT_NOTE_ON = 0;
    public static final int EVENT_NOTE_OFF = 1;
    public static final int EVENT_CONTROLCHANGE = 2;
    public static final int EVENT_PITCHBAND = 3;
    public static final int EVENT_PROGRAMCHANGE = 4;

    private static final String METHOD_NAME = "event_receive";
    private static EventReceiverMIDI mInstance = null;
    private final Object mParent;
    private Method mMethod = null;

    //    virtual void event_receive(const EVENT_TYPE event, const float* data) {}

    public EventReceiverMIDI(Object pPApplet) {
        mParent = pPApplet;
        try {
            mMethod = pPApplet.getClass().getDeclaredMethod(METHOD_NAME, int.class, float[].class);
        } catch (NoSuchMethodException | SecurityException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void receiveProgramChange(int channel, int number, int value) {
        sendEvent(EVENT_PROGRAMCHANGE, new float[]{channel, number, value});
    }

    @Override
    public void receiveControlChange(int channel, int number, int value) {
        sendEvent(EVENT_CONTROLCHANGE, new float[]{channel, number, value});
    }

    @Override
    public void receiveNoteOff(int channel, int pitch) {
        sendEvent(EVENT_NOTE_OFF, new float[]{channel, pitch});
    }

    @Override
    public void receiveNoteOn(int channel, int pitch, int velocity) {
        sendEvent(EVENT_NOTE_ON, new float[]{channel, pitch, velocity});
    }

    @Override
    public void clock_tick() {
        sendEvent(MIDI_CLOCK_TICK, new float[]{});
    }

    @Override
    public void clock_start() {
        sendEvent(MIDI_CLOCK_START, new float[]{});
    }

    @Override
    public void clock_continue() {
        sendEvent(MIDI_CLOCK_CONTINUE, new float[]{});
    }

    @Override
    public void clock_stop() {
        sendEvent(MIDI_CLOCK_STOP, new float[]{});
    }

    @Override
    public void clock_song_position_pointer(int pOffset16th) {
        sendEvent(MIDI_SONG_POSITION_POINTER, new float[]{pOffset16th});
    }

    private void sendEvent(int pEvent, float[] pData) {
        try {
            mMethod.invoke(mParent, pEvent, pData);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    public static EventReceiverMIDI start(Object pParent, String pMidiInputDevice) {
        if (mInstance == null) {
            mInstance = new EventReceiverMIDI(pParent);
            MidiIn mMidiIn = new MidiIn(pMidiInputDevice);
            mMidiIn.addListener(mInstance);
        }
        return mInstance;
    }
}
