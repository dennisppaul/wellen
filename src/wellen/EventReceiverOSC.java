/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2020 Dennis P Paul.
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package wellen;

import oscP5.OscMessage;
import oscP5.OscP5;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * listens to incoming OSC messages.
 */
public class EventReceiverOSC {

    private static final String METHOD_NAME = "event_receive";
    private static final int DEFAULT_RECEIVE_PORT = 7001;
    private static EventReceiverOSC mInstance = null;
    private final Object mParent;
    private final OscP5 mOscP5;
    private Method mMethod = null;

    public EventReceiverOSC(Object pListener, int pPortReceive) {
        mParent = pListener;
        mOscP5 = new OscP5(this, pPortReceive);
        try {
            mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME, int.class, float[].class);
        } catch (NoSuchMethodException | SecurityException ex) {
            ex.printStackTrace();
        }
    }

    public void oscEvent(OscMessage pOSCMessage) {
        try {
            int mEvent = Wellen.EVENT_UNDEFINED;
            int mNumOfArgs = pOSCMessage.typetag().length();
            final float[] mData = new float[mNumOfArgs];
            if (pOSCMessage.checkAddrPattern(ToneEngineOSC.OSC_ADDR_PATTERN_NOTE_ON)) {
                mEvent = Wellen.EVENT_NOTE_ON;
            } else if (pOSCMessage.checkAddrPattern(ToneEngineOSC.OSC_ADDR_PATTERN_NOTE_OFF)) {
                mEvent = Wellen.EVENT_NOTE_OFF;
            } else if (pOSCMessage.checkAddrPattern(ToneEngineOSC.OSC_ADDR_PATTERN_CONTROLCHANGE)) {
                mEvent = Wellen.EVENT_CONTROLCHANGE;
            } else if (pOSCMessage.checkAddrPattern(ToneEngineOSC.OSC_ADDR_PATTERN_PITCHBAND)) {
                mEvent = Wellen.EVENT_PITCHBEND;
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

    public static EventReceiverOSC start(Object pListener, int pPortReceive) {
        if (mInstance == null) {
            mInstance = new EventReceiverOSC(pListener, pPortReceive);
        }
        return mInstance;
    }

    public static EventReceiverOSC start(Object pListener) {
        return start(pListener, DEFAULT_RECEIVE_PORT);
    }
}
