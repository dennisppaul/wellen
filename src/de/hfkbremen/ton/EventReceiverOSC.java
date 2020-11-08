package de.hfkbremen.ton;

import oscP5.OscMessage;
import oscP5.OscP5;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static de.hfkbremen.ton.Event.EVENT_CONTROLCHANGE;
import static de.hfkbremen.ton.Event.EVENT_NOTE_OFF;
import static de.hfkbremen.ton.Event.EVENT_NOTE_ON;
import static de.hfkbremen.ton.Event.EVENT_PITCHBAND;
import static de.hfkbremen.ton.Event.EVENT_UNDEFINED;
import static de.hfkbremen.ton.ToneEngineOSC.OSC_ADDR_PATTERN_CONTROLCHANGE;
import static de.hfkbremen.ton.ToneEngineOSC.OSC_ADDR_PATTERN_NOTE_OFF;
import static de.hfkbremen.ton.ToneEngineOSC.OSC_ADDR_PATTERN_NOTE_ON;
import static de.hfkbremen.ton.ToneEngineOSC.OSC_ADDR_PATTERN_PITCHBAND;

public class EventReceiverOSC {

    private static final String METHOD_NAME = "event_receive";
    private static final int DEFAULT_RECEIVE_PORT = 7001;
    private static EventReceiverOSC mInstance = null;
    private final Object mParent;
    private final OscP5 mOscP5;
    private Method mMethod = null;

    public EventReceiverOSC(Object pPApplet, int pPortReceive) {
        mParent = pPApplet;
        mOscP5 = new OscP5(this, pPortReceive);
        try {
            mMethod = pPApplet.getClass().getDeclaredMethod(METHOD_NAME, int.class, float[].class);
        } catch (NoSuchMethodException | SecurityException ex) {
            ex.printStackTrace();
        }
    }

    public static EventReceiverOSC start(Object pParent, int pPortReceive) {
        if (mInstance == null) {
            mInstance = new EventReceiverOSC(pParent, pPortReceive);
        }
        return mInstance;
    }

    public static EventReceiverOSC start(Object pParent) {
        return start(pParent, DEFAULT_RECEIVE_PORT);
    }

    public void oscEvent(OscMessage pOSCMessage) {
        try {
            int mEvent = EVENT_UNDEFINED;
            int mNumOfArgs = pOSCMessage.typetag().length();
            final float[] mData = new float[mNumOfArgs];
            if (pOSCMessage.checkAddrPattern(OSC_ADDR_PATTERN_NOTE_ON)) {
                mEvent = EVENT_NOTE_ON;
            } else if (pOSCMessage.checkAddrPattern(OSC_ADDR_PATTERN_NOTE_OFF)) {
                mEvent = EVENT_NOTE_OFF;
            } else if (pOSCMessage.checkAddrPattern(OSC_ADDR_PATTERN_CONTROLCHANGE)) {
                mEvent = EVENT_CONTROLCHANGE;
            } else if (pOSCMessage.checkAddrPattern(OSC_ADDR_PATTERN_PITCHBAND)) {
                mEvent = EVENT_PITCHBAND;
            }
            for (int i = 0; i < mData.length; i++) {
                if (pOSCMessage.typetag().charAt(i) == 'i') {
                    mData[i] = pOSCMessage.get(i).intValue();
                } else if (pOSCMessage.typetag().charAt(i) == 'f') {
                    mData[i] = pOSCMessage.get(i).floatValue();
                }
            }
            mMethod.invoke(mParent, mEvent, mData);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }
}
