package de.hfkbremen.ton;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    public static EventReceiverMIDI start(Object pParent, String pMidiInputDevice) {
        if (mInstance == null) {
            mInstance = new EventReceiverMIDI(pParent);
            MidiIn mMidiIn = new MidiIn(pMidiInputDevice);
            mMidiIn.addListener(mInstance);
        }
        return mInstance;
    }

    @Override
    public void receiveProgramChange(int channel, int number, int value) {
        System.out.println("receiveProgramChange");
        sendEvent(EVENT_PROGRAMCHANGE, new float[]{channel, number, value});
    }

    @Override
    public void receiveControlChange(int channel, int number, int value) {
        System.out.println("receiveControlChange");
        sendEvent(EVENT_CONTROLCHANGE, new float[]{channel, number, value});
    }

    @Override
    public void receiveNoteOff(int channel, int pitch) {
        System.out.println("receiveNoteOff");
        sendEvent(EVENT_NOTE_OFF, new float[]{channel, pitch});
    }

    @Override
    public void receiveNoteOn(int channel, int pitch, int velocity) {
        System.out.println("receiveNoteOn");
        sendEvent(EVENT_NOTE_ON, new float[]{channel, pitch, velocity});
    }

    private void sendEvent(int pEvent, float[] pData) {
        try {
            mMethod.invoke(mParent, pEvent, pData);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }
}
