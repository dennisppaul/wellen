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

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Transmitter;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * receives MIDI messages from a MIDI input device and forwards them to {@link MidiInListener}s.
 * <p>
 * listeners are called on the MIDI thread. a note-on message with velocity 0 is forwarded as note-off ( as defined in
 * the MIDI 1.0 specification ).
 */
public class MidiIn implements Receiver {

    public static boolean VERBOSE = false;
    private final CopyOnWriteArrayList<MidiInListener> fListeners = new CopyOnWriteArrayList<>();
    private final String fDeviceName;
    private MidiDevice fDevice;
    private Transmitter fTransmitter;
    private volatile boolean fClosed = false;
    private boolean fListenerErrorReported = false;

    private MidiIn(MidiDevice.Info info, String requested_device) {
        MidiDevice mDevice = null;
        Transmitter mTransmitter = null;
        if (info != null) {
            try {
                mDevice = MidiDevices.acquire(info);
                mTransmitter = mDevice.getTransmitter();
                mTransmitter.setReceiver(this);
                if (VERBOSE) {
                    System.out.println("+++ @MidiIn / opened MIDI input device: " + info.getName());
                }
            } catch (MidiUnavailableException | RuntimeException e) {
                System.err.println("+++ @MidiIn / could not open MIDI input device: " + info.getName() + " ( " + e.getMessage() + " )");
                if (mTransmitter != null) {
                    mTransmitter.close();
                }
                MidiDevices.release(mDevice);
                mDevice = null;
                mTransmitter = null;
            }
        } else if (requested_device != null) {
            System.err.println("+++ @MidiIn / could not find MIDI input device: " + requested_device);
            System.err.println("+++ available inputs are: ");
            Wellen.dumpMidiInputDevices();
        }
        fDevice = mDevice;
        fTransmitter = mTransmitter;
        fDeviceName = info != null && mDevice != null ? info.getName() : "";
    }

    /**
     * @param midi_input_device_ID ID of MIDI input device as listed by {@link Wellen#dumpMidiInputDevices()}
     */
    public MidiIn(int midi_input_device_ID) {
        this(MidiDevices.resolve(midi_input_device_ID, true), String.valueOf(midi_input_device_ID));
    }

    /**
     * @param midi_input_device name of MIDI input device. the name does not need to match exactly: if no exact match
     *                          is found the first device that starts with or contains the name is used.
     */
    public MidiIn(String midi_input_device) {
        this(MidiDevices.resolve(midi_input_device, true), String.valueOf(midi_input_device));
    }

    /**
     * creates a <code>MidiIn</code> that is not connected to any device. MIDI messages can be injected with
     * {@link #send(MidiMessage, long)} e.g for testing or to forward messages from another source.
     *
     * @return unconnected MIDI input
     */
    public static MidiIn create_unconnected() {
        return new MidiIn((MidiDevice.Info) null, null);
    }

    /**
     * @return names of all available MIDI input devices
     */
    public static String[] available_inputs() {
        return MidiDevices.names(true);
    }

    /**
     * @deprecated use {@link #available_inputs()} instead
     */
    @Deprecated
    public static String[] availableInputs() {
        return available_inputs();
    }

    public void add_listener(MidiInListener listener) {
        if (listener != null) {
            fListeners.addIfAbsent(listener);
        }
    }

    public boolean remove_listener(MidiInListener listener) {
        return fListeners.remove(listener);
    }

    /**
     * @deprecated use {@link #add_listener(MidiInListener)} instead
     */
    @Deprecated
    public void addListener(MidiInListener pMidiInListener) {
        add_listener(pMidiInListener);
    }

    /**
     * @deprecated use {@link #remove_listener(MidiInListener)} instead
     */
    @Deprecated
    public void removeListener(MidiInListener pMidiInListener) {
        remove_listener(pMidiInListener);
    }

    /**
     * @return <code>true</code> if connected to a MIDI input device
     */
    public boolean is_open() {
        return fTransmitter != null && !fClosed;
    }

    /**
     * @return <code>true</code> if connected to a MIDI input device
     */
    public boolean is_initialized() {
        return is_open();
    }

    /**
     * @return name of connected device or an empty string if not connected
     */
    public String get_device_name() {
        return fDeviceName;
    }

    /**
     * parses a MIDI message and forwards it to all listeners. this method is called by the MIDI system but may also be
     * called directly to inject messages.
     */
    @Override
    public void send(MidiMessage message, long timestamp) {
        if (message instanceof ShortMessage) {
            final ShortMessage m = (ShortMessage) message;
            if (m.getStatus() >= 0xF0) {
                parseSystemMessage(m);
                return;
            }
            final int ch = m.getChannel();
            final int d1 = m.getData1();
            final int d2 = m.getData2();
            switch (m.getCommand()) {
                case MIDI.MIDI_NOTE_ON:
                    if (d2 == 0) {
                        /* note-on with velocity 0 is note-off with release velocity 64 */
                        dispatchNoteOff(ch, d1, 64);
                    } else {
                        dispatchNoteOn(ch, d1, d2);
                    }
                    break;
                case MIDI.MIDI_NOTE_OFF:
                    dispatchNoteOff(ch, d1, d2);
                    break;
                case MIDI.MIDI_CONTROL_CHANGE:
                    for (MidiInListener l : fListeners) {
                        try {
                            l.receive_control_change(ch, d1, d2);
                        } catch (RuntimeException e) {
                            reportListenerError(e);
                        }
                    }
                    break;
                case MIDI.MIDI_PROGRAM_CHANGE:
                    for (MidiInListener l : fListeners) {
                        try {
                            l.receive_program_change(ch, d1);
                        } catch (RuntimeException e) {
                            reportListenerError(e);
                        }
                    }
                    break;
                case MIDI.MIDI_PITCH_BEND:
                    final int mValue = (d2 << 7) | d1;
                    for (MidiInListener l : fListeners) {
                        try {
                            l.receive_pitch_bend(ch, mValue);
                        } catch (RuntimeException e) {
                            reportListenerError(e);
                        }
                    }
                    break;
                case MIDI.MIDI_CHANNEL_PRESSURE:
                    for (MidiInListener l : fListeners) {
                        try {
                            l.receive_channel_pressure(ch, d1);
                        } catch (RuntimeException e) {
                            reportListenerError(e);
                        }
                    }
                    break;
                case MIDI.MIDI_POLY_PRESSURE:
                    for (MidiInListener l : fListeners) {
                        try {
                            l.receive_poly_pressure(ch, d1, d2);
                        } catch (RuntimeException e) {
                            reportListenerError(e);
                        }
                    }
                    break;
                default:
                    if (VERBOSE) {
                        System.err.println("+++ @MidiIn / could not parse command: " + m.getCommand());
                    }
            }
        } else if (message instanceof SysexMessage) {
            final byte[] mData = message.getMessage();
            for (MidiInListener l : fListeners) {
                try {
                    l.receive_sysex(mData.clone());
                } catch (RuntimeException e) {
                    reportListenerError(e);
                }
            }
        } else if (VERBOSE && message != null) {
            System.err.println("+++ @MidiIn / could not parse MIDI message: " + message);
        }
    }

    /**
     * disconnects from MIDI device and closes the device if it was opened by wellen and is not used anymore.
     */
    @Override
    public void close() {
        if (fClosed) {
            return;
        }
        fClosed = true;
        if (fTransmitter != null) {
            try {
                fTransmitter.close();
            } catch (RuntimeException ignored) {
            }
            fTransmitter = null;
        }
        MidiDevices.release(fDevice);
        fDevice = null;
    }

    private void dispatchNoteOn(int ch, int pitch, int velocity) {
        for (MidiInListener l : fListeners) {
            try {
                l.receive_note_on(ch, pitch, velocity);
            } catch (RuntimeException e) {
                reportListenerError(e);
            }
        }
    }

    private void dispatchNoteOff(int ch, int pitch, int velocity) {
        for (MidiInListener l : fListeners) {
            try {
                l.receive_note_off(ch, pitch, velocity);
            } catch (RuntimeException e) {
                reportListenerError(e);
            }
        }
    }

    private void parseSystemMessage(ShortMessage m) {
        final int mStatus = m.getStatus();
        switch (mStatus) {
            case MIDI.MIDI_CLOCK_TICK:
                for (MidiInListener l : fListeners) {
                    try {
                        l.clock_tick();
                    } catch (RuntimeException e) {
                        reportListenerError(e);
                    }
                }
                break;
            case MIDI.MIDI_CLOCK_START:
                for (MidiInListener l : fListeners) {
                    try {
                        l.clock_start();
                    } catch (RuntimeException e) {
                        reportListenerError(e);
                    }
                }
                break;
            case MIDI.MIDI_CLOCK_CONTINUE:
                for (MidiInListener l : fListeners) {
                    try {
                        l.clock_continue();
                    } catch (RuntimeException e) {
                        reportListenerError(e);
                    }
                }
                break;
            case MIDI.MIDI_CLOCK_STOP:
                for (MidiInListener l : fListeners) {
                    try {
                        l.clock_stop();
                    } catch (RuntimeException e) {
                        reportListenerError(e);
                    }
                }
                break;
            case MIDI.MIDI_SONG_POSITION_POINTER:
                final int mOffset16th = m.getData1() | (m.getData2() << 7);
                if (VERBOSE) {
                    System.out.println("+++ @MidiIn / song position pointer: " + mOffset16th);
                }
                for (MidiInListener l : fListeners) {
                    try {
                        l.clock_song_position_pointer(mOffset16th);
                    } catch (RuntimeException e) {
                        reportListenerError(e);
                    }
                }
                break;
            case MIDI.MIDI_SONG_SELECT:
                for (MidiInListener l : fListeners) {
                    try {
                        l.receive_song_select(m.getData1());
                    } catch (RuntimeException e) {
                        reportListenerError(e);
                    }
                }
                break;
            case MIDI.MIDI_TIME_CODE_QUARTER_FRAME:
                for (MidiInListener l : fListeners) {
                    try {
                        l.receive_time_code_quarter_frame(m.getData1());
                    } catch (RuntimeException e) {
                        reportListenerError(e);
                    }
                }
                break;
            case MIDI.MIDI_ACTIVE_SENSING:
            case MIDI.MIDI_RESET:
            case MIDI.MIDI_TUNE_REQUEST:
                /* ignored */
                break;
            default:
                if (VERBOSE) {
                    System.err.println("+++ @MidiIn / unrecognized system message: " + mStatus + " (" + m.getLength() + ")");
                }
        }
    }

    private void reportListenerError(RuntimeException e) {
        if (!fListenerErrorReported) {
            fListenerErrorReported = true;
            System.err.println("+++ @MidiIn / exception in listener ( further exceptions are not reported ):");
            e.printStackTrace();
        }
    }
}
