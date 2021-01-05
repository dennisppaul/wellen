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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * listens to incoming MIDI messages.
 */
public class EventReceiverMIDI implements MidiInListener {

    private static final String METHOD_NAME = "event_receive";
    private static EventReceiverMIDI mInstance = null;
    private final Object mParent;
    private Method mMethod = null;

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
        sendEvent(Wellen.EVENT_PROGRAMCHANGE, new float[]{channel, number, value});
    }

    @Override
    public void receiveControlChange(int channel, int number, int value) {
        sendEvent(Wellen.EVENT_CONTROLCHANGE, new float[]{channel, number, value});
    }

    @Override
    public void receiveNoteOff(int channel, int pitch) {
        sendEvent(Wellen.EVENT_NOTE_OFF, new float[]{channel, pitch});
    }

    @Override
    public void receiveNoteOn(int channel, int pitch, int velocity) {
        sendEvent(Wellen.EVENT_NOTE_ON, new float[]{channel, pitch, velocity});
    }

    @Override
    public void clock_tick() {
        sendEvent(MIDI.MIDI_CLOCK_TICK, new float[]{});
    }

    @Override
    public void clock_start() {
        sendEvent(MIDI.MIDI_CLOCK_START, new float[]{});
    }

    @Override
    public void clock_continue() {
        sendEvent(MIDI.MIDI_CLOCK_CONTINUE, new float[]{});
    }

    @Override
    public void clock_stop() {
        sendEvent(MIDI.MIDI_CLOCK_STOP, new float[]{});
    }

    @Override
    public void clock_song_position_pointer(int pOffset16th) {
        sendEvent(MIDI.MIDI_SONG_POSITION_POINTER, new float[]{pOffset16th});
    }

    private void sendEvent(int pEvent, float[] pData) {
        try {
            mMethod.invoke(mParent, pEvent, pData);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
