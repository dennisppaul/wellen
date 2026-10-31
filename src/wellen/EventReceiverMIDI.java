/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2024 Dennis P Paul.
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * listens to incoming MIDI messages and forwards them to callback methods of a parent object ( e.g a sketch ).
 * <p>
 * the following callback methods are available ( all are optional ):
 * <pre>
 * <code>
 * midi_note_on(int channel, int pitch, int velocity)
 * midi_note_off(int channel, int pitch, int velocity) ( or midi_note_off(int channel, int pitch) )
 * midi_control_change(int channel, int number, int value)
 * midi_program_change(int channel, int program) ( or deprecated midi_program_change(int channel, int number, int value) )
 * midi_pitch_bend(int channel, int value) ( 0–16383, center 8192 )
 * midi_channel_pressure(int channel, int pressure)
 * midi_poly_pressure(int channel, int pitch, int pressure)
 * midi_sysex(byte[] data)
 * midi_clock_tick()
 * midi_clock_start()
 * midi_clock_continue()
 * midi_clock_stop()
 * midi_clock_song_position_pointer(int offset_16th)
 * event_receive(int event, float[] data)
 * </code>
 * </pre>
 * a note-on message with velocity 0 is delivered as note-off.
 * <p>
 * by default callbacks are called on the MIDI thread. use {@link #dispatch_on_draw_thread(boolean)} to call them on
 * the processing animation thread instead ( recommended if callbacks draw or change state used in
 * <code>draw()</code> ).
 */
public class EventReceiverMIDI implements MidiInListener {

    private static final String METHOD_NAME = "event_receive";
    private static final String METHOD_NAME_CHANNEL_PRESSURE = "midi_channel_pressure";
    private static final String METHOD_NAME_CLOCK_CONTINUE = "midi_clock_continue";
    private static final String METHOD_NAME_CLOCK_SONG_POSITION_POINTER = "midi_clock_song_position_pointer";
    private static final String METHOD_NAME_CLOCK_START = "midi_clock_start";
    private static final String METHOD_NAME_CLOCK_STOP = "midi_clock_stop";
    private static final String METHOD_NAME_CLOCK_TICK = "midi_clock_tick";
    private static final String METHOD_NAME_CONTROL_CHANGE = "midi_control_change";
    private static final String METHOD_NAME_NOTE_OFF = "midi_note_off";
    private static final String METHOD_NAME_NOTE_ON = "midi_note_on";
    private static final String METHOD_NAME_PITCH_BEND = "midi_pitch_bend";
    private static final String METHOD_NAME_POLY_PRESSURE = "midi_poly_pressure";
    private static final String METHOD_NAME_PROGRAM_CHANGE = "midi_program_change";
    private static final String METHOD_NAME_SYSEX = "midi_sysex";
    private static final ArrayList<EventReceiverMIDI> fInstances = new ArrayList<>();
    private final MidiCallbacks fCallbacks;
    private final Method fMethod;
    private final Method fMethodChannelPressure;
    private final Method fMethodClockContinue;
    private final Method fMethodClockSongPositionPointer;
    private final Method fMethodClockStart;
    private final Method fMethodClockStop;
    private final Method fMethodClockTick;
    private final Method fMethodControlChange;
    private final Method fMethodNoteOff;
    private final Method fMethodNoteOffWithVelocity;
    private final Method fMethodNoteOn;
    private final Method fMethodPitchBend;
    private final Method fMethodPolyPressure;
    private final Method fMethodProgramChange;
    private final Method fMethodProgramChangeDeprecated;
    private final Method fMethodSysex;
    private MidiIn fMidiIn = null;
    private boolean fOwnsMidiIn = false;
    private String fDeviceKey = null;

    /**
     * @param pReceiver object which will receive MIDI messages
     */
    public EventReceiverMIDI(Object pReceiver) {
        fCallbacks = new MidiCallbacks(pReceiver, getClass().getSimpleName());
        fMethod = fCallbacks.find(METHOD_NAME, int.class, float[].class);
        fMethodNoteOn = fCallbacks.find(METHOD_NAME_NOTE_ON, int.class, int.class, int.class);
        fMethodNoteOffWithVelocity = fCallbacks.find(METHOD_NAME_NOTE_OFF, int.class, int.class, int.class);
        fMethodNoteOff = fCallbacks.find(METHOD_NAME_NOTE_OFF, int.class, int.class);
        fMethodControlChange = fCallbacks.find(METHOD_NAME_CONTROL_CHANGE, int.class, int.class, int.class);
        fMethodProgramChange = fCallbacks.find(METHOD_NAME_PROGRAM_CHANGE, int.class, int.class);
        fMethodProgramChangeDeprecated = fCallbacks.find(METHOD_NAME_PROGRAM_CHANGE, int.class, int.class, int.class);
        fMethodPitchBend = fCallbacks.find(METHOD_NAME_PITCH_BEND, int.class, int.class);
        fMethodChannelPressure = fCallbacks.find(METHOD_NAME_CHANNEL_PRESSURE, int.class, int.class);
        fMethodPolyPressure = fCallbacks.find(METHOD_NAME_POLY_PRESSURE, int.class, int.class, int.class);
        fMethodSysex = fCallbacks.find(METHOD_NAME_SYSEX, byte[].class);
        fMethodClockTick = fCallbacks.find(METHOD_NAME_CLOCK_TICK);
        fMethodClockStart = fCallbacks.find(METHOD_NAME_CLOCK_START);
        fMethodClockContinue = fCallbacks.find(METHOD_NAME_CLOCK_CONTINUE);
        fMethodClockStop = fCallbacks.find(METHOD_NAME_CLOCK_STOP);
        fMethodClockSongPositionPointer = fCallbacks.find(METHOD_NAME_CLOCK_SONG_POSITION_POINTER, int.class);
    }

    /**
     * starts receiving MIDI messages from a device. calling this method again with the same parent and device returns
     * the existing instance.
     *
     * @param pParent          object which will receive MIDI messages
     * @param pMidiInputDevice MIDI input device to use (e.g. "IAC Driver Bus 1")
     * @return instance of {@code EventReceiverMIDI}. use {@link #is_initialized()} to check if the device was found.
     */
    public static EventReceiverMIDI start(Object pParent, String pMidiInputDevice) {
        final javax.sound.midi.MidiDevice.Info mInfo = MidiDevices.resolve(pMidiInputDevice, true);
        final String mKey = "name:" + (mInfo != null ? mInfo.getName() : pMidiInputDevice);
        synchronized (fInstances) {
            final EventReceiverMIDI mExisting = findInstance(pParent, mKey);
            if (mExisting != null) {
                return mExisting;
            }
            final EventReceiverMIDI mInstance = new EventReceiverMIDI(pParent);
            mInstance.connect(new MidiIn(pMidiInputDevice), true, mKey);
            return mInstance;
        }
    }

    /**
     * starts receiving MIDI messages from a device. calling this method again with the same parent and device returns
     * the existing instance.
     *
     * @param pParent            object which will receive MIDI messages
     * @param pMidiInputDeviceID MIDI input device ID to use ( as listed by {@link Wellen#dumpMidiInputDevices()} )
     * @return instance of {@code EventReceiverMIDI}. use {@link #is_initialized()} to check if the device was found.
     */
    public static EventReceiverMIDI start(Object pParent, int pMidiInputDeviceID) {
        final javax.sound.midi.MidiDevice.Info mInfo = MidiDevices.resolve(pMidiInputDeviceID, true);
        final String mKey = "name:" + (mInfo != null ? mInfo.getName() : ("#" + pMidiInputDeviceID));
        synchronized (fInstances) {
            final EventReceiverMIDI mExisting = findInstance(pParent, mKey);
            if (mExisting != null) {
                return mExisting;
            }
            final EventReceiverMIDI mInstance = new EventReceiverMIDI(pParent);
            mInstance.connect(new MidiIn(pMidiInputDeviceID), true, mKey);
            return mInstance;
        }
    }

    /**
     * starts receiving MIDI messages from an existing {@link MidiIn} ( e.g shared with a {@link BeatMIDI} ). the
     * <code>MidiIn</code> is not closed by {@link #stop()}.
     *
     * @param parent  object which will receive MIDI messages
     * @param midi_in MIDI input
     * @return instance of {@code EventReceiverMIDI}
     */
    public static EventReceiverMIDI start(Object parent, MidiIn midi_in) {
        final EventReceiverMIDI mInstance = new EventReceiverMIDI(parent);
        synchronized (fInstances) {
            mInstance.connect(midi_in, false, "midi_in:" + System.identityHashCode(midi_in));
        }
        return mInstance;
    }

    private static EventReceiverMIDI findInstance(Object pParent, String pKey) {
        for (EventReceiverMIDI m : fInstances) {
            if (m.fCallbacks.parent() == pParent && pKey.equals(m.fDeviceKey) && m.is_initialized()) {
                return m;
            }
        }
        return null;
    }

    /**
     * @return <code>true</code> if connected to a MIDI input device
     */
    public boolean is_initialized() {
        return fMidiIn != null && fMidiIn.is_open();
    }

    /**
     * @return MIDI input used by this receiver or <code>null</code> if not started via <code>start(...)</code>
     */
    public MidiIn get_midi_in() {
        return fMidiIn;
    }

    /**
     * @param dispatch_on_draw_thread if <code>true</code> callbacks are queued and called on the processing animation
     *                                thread right before <code>draw()</code>. requires the parent to be a
     *                                <code>PApplet</code>. note that callbacks are then delayed by up to one frame
     *                                and are not called while the sketch is paused with <code>noLoop()</code>.
     * @return this instance
     */
    public EventReceiverMIDI dispatch_on_draw_thread(boolean dispatch_on_draw_thread) {
        fCallbacks.set_dispatch_on_draw_thread(dispatch_on_draw_thread, this);
        return this;
    }

    /**
     * @hidden called by processing before each frame if {@link #dispatch_on_draw_thread(boolean)} is enabled
     */
    public void pre() {
        fCallbacks.drain();
    }

    /**
     * stops receiving MIDI messages and releases the MIDI device ( if it was opened by this instance ).
     */
    public void stop() {
        synchronized (fInstances) {
            final Iterator<EventReceiverMIDI> mIterator = fInstances.iterator();
            while (mIterator.hasNext()) {
                if (mIterator.next() == this) {
                    mIterator.remove();
                }
            }
        }
        if (fMidiIn != null) {
            fMidiIn.remove_listener(this);
            if (fOwnsMidiIn) {
                fMidiIn.close();
            }
            fMidiIn = null;
        }
        fCallbacks.dispose(this);
    }

    /* --- MidiInListener --- */

    @Override
    public void receive_note_on(int channel, int pitch, int velocity) {
        sendEvent(Wellen.EVENT_NOTE_ON, new float[]{channel, pitch, velocity});
        fCallbacks.invoke(fMethodNoteOn, channel, pitch, velocity);
    }

    @Override
    public void receive_note_off(int channel, int pitch, int velocity) {
        sendEvent(Wellen.EVENT_NOTE_OFF, new float[]{channel, pitch});
        if (fMethodNoteOffWithVelocity != null) {
            fCallbacks.invoke(fMethodNoteOffWithVelocity, channel, pitch, velocity);
        } else {
            fCallbacks.invoke(fMethodNoteOff, channel, pitch);
        }
    }

    @Override
    public void receive_control_change(int channel, int number, int value) {
        sendEvent(Wellen.EVENT_CONTROLCHANGE, new float[]{channel, number, value});
        fCallbacks.invoke(fMethodControlChange, channel, number, value);
    }

    @Override
    public void receive_program_change(int channel, int program) {
        /* third value is kept for compatibility, program change messages have no value */
        sendEvent(Wellen.EVENT_PROGRAMCHANGE, new float[]{channel, program, 0});
        if (fMethodProgramChange != null) {
            fCallbacks.invoke(fMethodProgramChange, channel, program);
        } else {
            fCallbacks.invoke(fMethodProgramChangeDeprecated, channel, program, 0);
        }
    }

    @Override
    public void receive_pitch_bend(int channel, int value) {
        sendEvent(Wellen.EVENT_PITCHBEND, new float[]{channel, value});
        fCallbacks.invoke(fMethodPitchBend, channel, value);
    }

    @Override
    public void receive_channel_pressure(int channel, int pressure) {
        sendEvent(MIDI.MIDI_CHANNEL_PRESSURE, new float[]{channel, pressure});
        fCallbacks.invoke(fMethodChannelPressure, channel, pressure);
    }

    @Override
    public void receive_poly_pressure(int channel, int pitch, int pressure) {
        sendEvent(MIDI.MIDI_POLY_PRESSURE, new float[]{channel, pitch, pressure});
        fCallbacks.invoke(fMethodPolyPressure, channel, pitch, pressure);
    }

    @Override
    public void receive_sysex(byte[] data) {
        fCallbacks.invoke(fMethodSysex, (Object) data);
    }

    @Override
    public void clock_tick() {
        sendEvent(MIDI.MIDI_CLOCK_TICK, new float[]{});
        fCallbacks.invoke(fMethodClockTick);
    }

    @Override
    public void clock_start() {
        sendEvent(MIDI.MIDI_CLOCK_START, new float[]{});
        fCallbacks.invoke(fMethodClockStart);
    }

    @Override
    public void clock_continue() {
        sendEvent(MIDI.MIDI_CLOCK_CONTINUE, new float[]{});
        fCallbacks.invoke(fMethodClockContinue);
    }

    @Override
    public void clock_stop() {
        sendEvent(MIDI.MIDI_CLOCK_STOP, new float[]{});
        fCallbacks.invoke(fMethodClockStop);
    }

    /**
     * @param offset_16th offset of the song position in 16th notes
     */
    @Override
    public void clock_song_position_pointer(int offset_16th) {
        sendEvent(MIDI.MIDI_SONG_POSITION_POINTER, new float[]{offset_16th});
        fCallbacks.invoke(fMethodClockSongPositionPointer, offset_16th);
    }

    /* --- deprecated ( kept so that messages can still be injected with the old method names ) --- */

    /**
     * @deprecated use {@link #receive_program_change(int, int)} instead
     */
    @Deprecated
    @Override
    public void receiveProgramChange(int channel, int number, int value) {
        receive_program_change(channel, number);
    }

    /**
     * @deprecated use {@link #receive_control_change(int, int, int)} instead
     */
    @Deprecated
    @Override
    public void receiveControlChange(int channel, int number, int value) {
        receive_control_change(channel, number, value);
    }

    /**
     * @deprecated use {@link #receive_note_off(int, int, int)} instead
     */
    @Deprecated
    @Override
    public void receiveNoteOff(int channel, int pitch) {
        receive_note_off(channel, pitch, 0);
    }

    /**
     * @deprecated use {@link #receive_note_on(int, int, int)} instead
     */
    @Deprecated
    @Override
    public void receiveNoteOn(int channel, int pitch, int velocity) {
        receive_note_on(channel, pitch, velocity);
    }

    /* --- internal --- */

    private void connect(MidiIn pMidiIn, boolean pOwnsMidiIn, String pKey) {
        fMidiIn = pMidiIn;
        fOwnsMidiIn = pOwnsMidiIn;
        fDeviceKey = pKey;
        fMidiIn.add_listener(this);
        fInstances.add(this);
    }

    private void sendEvent(int pEvent, float[] pData) {
        fCallbacks.invoke(fMethod, pEvent, pData);
    }
}
