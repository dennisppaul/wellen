/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2022 Dennis P Paul.
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
 * <p>
 * the following callback methods are available:
 * <pre>
 * <code>
 * midi_note_on(int channel, int pitch, int velocity)
 * midi_note_off(int channel, int pitch)
 * midi_control_change(int channel, int number, int value)
 * midi_program_change(int channel, int number, int value)
 * midi_clock_tick()
 * midi_clock_start()
 * midi_clock_continue()
 * midi_clock_stop()
 * event_receive(int command, float[] data)
 * </code>
 * </pre>
 */
public class EventReceiverMIDI implements MidiInListener {

    private static final String METHOD_NAME = "event_receive";
    private static final String METHOD_NAME_CC = "midi_cc";
    private static final String METHOD_NAME_CLOCK_CONTINUE = "midi_clock_continue";
    private static final String METHOD_NAME_CLOCK_SONG_POSITION_POINTER = "midi_clock_song_position_pointer";
    private static final String METHOD_NAME_CLOCK_START = "midi_clock_start";
    private static final String METHOD_NAME_CLOCK_STOP = "midi_clock_stop";
    private static final String METHOD_NAME_CLOCK_TICK = "midi_clock_tick";
    private static final String METHOD_NAME_CONTROL_CHANGE = "midi_control_change";
    private static final String METHOD_NAME_NOTE_OFF = "midi_note_off";
    private static final String METHOD_NAME_NOTE_ON = "midi_note_on";
    private static final String METHOD_NAME_PROGRAM_CHANGE = "midi_program_change";
    private static EventReceiverMIDI mInstance = null;
    private final boolean VERBOSE = false;
    private Method mMethod = null;
    private Method mMethodClockContinue = null;
    private Method mMethodClockSongPositionPointer = null;
    private Method mMethodClockStart = null;
    private Method mMethodClockStop = null;
    private Method mMethodClockTick = null;
    private Method mMethodControlChange = null;
    private Method mMethodNoteOff = null;
    private Method mMethodNoteOn = null;
    private Method mMethodProgramChange = null;
    private final Object mParent;

    /**
     * @param pReceiver object which will receive MIDI messages
     */
    public EventReceiverMIDI(Object pReceiver) {
        mParent = pReceiver;
        try {
            mMethod = pReceiver.getClass().getDeclaredMethod(METHOD_NAME, int.class, float[].class);
        } catch (NoSuchMethodException | SecurityException ex) {
            if (VERBOSE) {
                ex.printStackTrace();
            }
        }
        try {
            mMethodNoteOn = pReceiver.getClass()
                                     .getDeclaredMethod(METHOD_NAME_NOTE_ON, int.class, int.class, int.class);
        } catch (NoSuchMethodException | SecurityException ex) {
            if (VERBOSE) {
                ex.printStackTrace();
            }
        }
        try {
            mMethodNoteOff = pReceiver.getClass().getDeclaredMethod(METHOD_NAME_NOTE_OFF, int.class, int.class);
        } catch (NoSuchMethodException | SecurityException ex) {
            if (VERBOSE) {
                ex.printStackTrace();
            }
        }
        try {
            mMethodControlChange = pReceiver.getClass()
                                            .getDeclaredMethod(METHOD_NAME_CONTROL_CHANGE,
                                                               int.class,
                                                               int.class,
                                                               int.class);
        } catch (NoSuchMethodException | SecurityException ex) {
            if (VERBOSE) {
                ex.printStackTrace();
            }
        }
        try {
            mMethodProgramChange = pReceiver.getClass()
                                            .getDeclaredMethod(METHOD_NAME_PROGRAM_CHANGE,
                                                               int.class,
                                                               int.class,
                                                               int.class);
        } catch (NoSuchMethodException | SecurityException ex) {
            if (VERBOSE) {
                ex.printStackTrace();
            }
        }

        try {
            mMethodClockTick = pReceiver.getClass().getDeclaredMethod(METHOD_NAME_CLOCK_TICK);
        } catch (NoSuchMethodException | SecurityException ex) {
            if (VERBOSE) {
                ex.printStackTrace();
            }
        }
        try {
            mMethodClockStart = pReceiver.getClass().getDeclaredMethod(METHOD_NAME_CLOCK_START);
        } catch (NoSuchMethodException | SecurityException ex) {
            if (VERBOSE) {
                ex.printStackTrace();
            }
        }
        try {
            mMethodClockContinue = pReceiver.getClass().getDeclaredMethod(METHOD_NAME_CLOCK_CONTINUE);
        } catch (NoSuchMethodException | SecurityException ex) {
            if (VERBOSE) {
                ex.printStackTrace();
            }
        }
        try {
            mMethodClockStop = pReceiver.getClass().getDeclaredMethod(METHOD_NAME_CLOCK_STOP);
        } catch (NoSuchMethodException | SecurityException ex) {
            if (VERBOSE) {
                ex.printStackTrace();
            }
        }
        try {
            mMethodClockSongPositionPointer = pReceiver.getClass()
                                                       .getDeclaredMethod(METHOD_NAME_CLOCK_SONG_POSITION_POINTER,
                                                                          int.class);
        } catch (NoSuchMethodException | SecurityException ex) {
            if (VERBOSE) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * @param pParent          object which will receive MIDI messages
     * @param pMidiInputDevice MIDI input device to use (e.g. "IAC Driver Bus 1")
     * @return instance of {@code MIDIInput} or {@code null} if the device is not available.
     */
    public static EventReceiverMIDI start(Object pParent, String pMidiInputDevice) {
        if (mInstance == null) {
            mInstance = new EventReceiverMIDI(pParent);
            MidiIn mMidiIn = new MidiIn(pMidiInputDevice);
            mMidiIn.addListener(mInstance);
        }
        return mInstance;
    }

    /**
     * @param pParent            object which will receive MIDI messages
     * @param pMidiInputDeviceID MIDI input device ID to use
     * @return instance of {@code MIDIInput} or {@code null} if the device is not available.
     */
    public static EventReceiverMIDI start(Object pParent, int pMidiInputDeviceID) {
        if (mInstance == null) {
            mInstance = new EventReceiverMIDI(pParent);
            MidiIn mMidiIn = new MidiIn(pMidiInputDeviceID);
            mMidiIn.addListener(mInstance);
        }
        return mInstance;
    }

    /**
     * @param channel channel
     * @param number  number
     * @param value   value
     */
    @Override
    public void receiveProgramChange(int channel, int number, int value) {
        sendEvent(Wellen.EVENT_PROGRAMCHANGE, new float[]{channel, number, value});
        sendEventProgramChange(channel, number, value);
    }

    /**
     * @param channel channel
     * @param number  number
     * @param value   value
     */
    @Override
    public void receiveControlChange(int channel, int number, int value) {
        sendEvent(Wellen.EVENT_CONTROLCHANGE, new float[]{channel, number, value});
        sendEventControlChange(channel, number, value);
    }

    /**
     * @param channel channel
     * @param pitch   pitch
     */
    @Override
    public void receiveNoteOff(int channel, int pitch) {
        sendEvent(Wellen.EVENT_NOTE_OFF, new float[]{channel, pitch});
        sendEventNoteOff(channel, pitch);
    }

    /**
     * @param channel  channel
     * @param pitch    pitch
     * @param velocity velocity
     */
    @Override
    public void receiveNoteOn(int channel, int pitch, int velocity) {
        sendEvent(Wellen.EVENT_NOTE_ON, new float[]{channel, pitch, velocity});
        sendEventNoteOn(channel, pitch, velocity);
    }

    /**
     *
     */
    @Override
    public void clock_tick() {
        sendEvent(MIDI.MIDI_CLOCK_TICK, new float[]{});
        sendEventClockTick();
    }

    /**
     *
     */
    @Override
    public void clock_start() {
        sendEvent(MIDI.MIDI_CLOCK_START, new float[]{});
        sendEventClockStart();
    }

    /**
     *
     */
    @Override
    public void clock_continue() {
        sendEvent(MIDI.MIDI_CLOCK_CONTINUE, new float[]{});
        sendEventClockContinue();
    }

    /**
     *
     */
    @Override
    public void clock_stop() {
        sendEvent(MIDI.MIDI_CLOCK_STOP, new float[]{});
        sendEventClockStop();
    }

    /**
     * @param pOffset16th offset of the song position in 16th notes
     */
    @Override
    public void clock_song_position_pointer(int pOffset16th) {
        sendEvent(MIDI.MIDI_SONG_POSITION_POINTER, new float[]{pOffset16th});
        sendEventClockSongPositionPointer(pOffset16th);
    }

    private void sendEvent(int pEvent, float[] pData) {
        if (mMethod != null) {
            try {
                mMethod.invoke(mParent, pEvent, pData);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendEventClockContinue() {
        if (mMethodClockContinue != null) {
            try {
                mMethodClockContinue.invoke(mParent);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendEventClockSongPositionPointer(int pOffset16th) {
        if (mMethodClockSongPositionPointer != null) {
            try {
                mMethodClockSongPositionPointer.invoke(mParent, pOffset16th);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendEventClockStart() {
        if (mMethodClockStart != null) {
            try {
                mMethodClockStart.invoke(mParent);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendEventClockStop() {
        if (mMethodClockStop != null) {
            try {
                mMethodClockStop.invoke(mParent);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendEventClockTick() {
        if (mMethodClockTick != null) {
            try {
                mMethodClockTick.invoke(mParent);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendEventControlChange(int channel, int number, int value) {
        if (mMethodControlChange != null) {
            try {
                mMethodControlChange.invoke(mParent, channel, number, value);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendEventNoteOff(int channel, int pitch) {
        if (mMethodNoteOff != null) {
            try {
                mMethodNoteOff.invoke(mParent, channel, pitch);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendEventNoteOn(int channel, int pitch, int velocity) {
        if (mMethodNoteOn != null) {
            try {
                mMethodNoteOn.invoke(mParent, channel, pitch, velocity);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendEventProgramChange(int channel, int number, int value) {
        if (mMethodProgramChange != null) {
            try {
                mMethodProgramChange.invoke(mParent, channel, number, value);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
    }
}
