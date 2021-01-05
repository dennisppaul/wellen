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

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import java.util.ArrayList;

/**
 * handles communication with MIDI devices
 */
public class MidiIn implements Receiver {

    private static final int SYSEX_START = 0xF0;
    private static final int SYSEX_END = 0xF7;
    private static final int NOTE_OFF = 0x80;
    private static final int NOTE_ON = 0x90;
    private static final int CONTROL_CHANGE = 0xB0;
    private static final int PROGRAM_CHANGE = 0xC0;
    private static final int SYSTEM_REALTIME_MESSAGE = 0xF0;
    public static boolean VERBOSE = false;

    private final ArrayList<MidiInListener> mListener;

    public MidiIn(String pMidiOutputDevice) {
        final Transmitter mMidiIn = find(pMidiOutputDevice);
        if (mMidiIn != null) {
            mMidiIn.setReceiver(this);
        } else {
            System.err.println("+++ Error @ MidiIn / could not find midi device: " + pMidiOutputDevice);
            System.err.println("+++ available inputs are: ");
            Wellen.dumpMidiInputDevices();
        }
        mListener = new ArrayList<>();
    }

    public void addListener(MidiInListener pMidiInListener) {
        mListener.add(pMidiInListener);
    }

    public void removeListener(MidiInListener pMidiInListener) {
        mListener.remove(pMidiInListener);
    }

    @Override
    public void send(MidiMessage pMessage, long pTimeStamp) {
        if (pMessage instanceof ShortMessage) {
            ShortMessage mShortMessage = (ShortMessage) pMessage;
            final int mChannel = mShortMessage.getChannel();
            final int midiData1 = mShortMessage.getData1();
            final int midiData2 = mShortMessage.getData2();

            switch (mShortMessage.getCommand()) {
                case NOTE_ON:
                    receiveNoteOn(mChannel, midiData1, midiData2);
                    break;
                case NOTE_OFF:
                    receiveNoteOff(mChannel, midiData1);
                    break;
                case CONTROL_CHANGE:
                    receiveControlChange(mChannel, midiData1, midiData2);
                    break;
                case PROGRAM_CHANGE:
                    receiveProgramChange(mChannel, midiData1, midiData2);
                    break;
                case SYSTEM_REALTIME_MESSAGE:
                    parseSystemMessage(mShortMessage);
                    break;
                default:
                    if (VERBOSE) {
                        // int POLY_PRESSURE = 0xA0; // Polyphonic Key Pressure (Aftertouch)
                        System.err.println("+++ MidiIn / could not parse command: " + mShortMessage.getCommand() + " " +
                                ": " + mShortMessage);
                    }
            }
        } else {
            if (VERBOSE) {
                System.err.println("+++ MidiIn / could not parse midi message as `ShortMessage`: " + pMessage);
            }
        }
    }

    @Override
    public void close() {
    }

    private void parseSystemMessage(ShortMessage mShortMessage) {
        if (mShortMessage.getLength() == 1) {
            final int mClockMessage = parse_byte(mShortMessage.getMessage()[0]);
            switch (mClockMessage) {
                case MIDI.MIDI_CLOCK_TICK:
                    clock_tick();
                    break;
                case MIDI.MIDI_CLOCK_START:
                    clock_start();
                    break;
                case MIDI.MIDI_CLOCK_CONTINUE:
                    clock_continue();
                    break;
                case MIDI.MIDI_CLOCK_STOP:
                    clock_stop();
                    break;
            }
        } else if (mShortMessage.getLength() == 3) {
            final int mClockMessage = parse_byte(mShortMessage.getMessage()[0]);
            if (mClockMessage == MIDI.MIDI_SONG_POSITION_POINTER) {
                final int mOffset16th = mShortMessage.getMessage()[1] + mShortMessage.getMessage()[2] * 128;
                if (VERBOSE) {
                    System.out.println("SONG_POSITION_POINTER: " + mOffset16th);
                }
                clock_song_position_pointer(mOffset16th);
            }
        } else {
            if (VERBOSE) {
                System.err.println("+++ MidiIn / unrecognized system message: " + mShortMessage + " (" + mShortMessage.getLength() + ")");
            }
        }
    }

    private void clock_song_position_pointer(int pOffset16th) {
        for (MidiInListener m : mListener) {
            m.clock_song_position_pointer(pOffset16th);
        }
    }

    private void clock_stop() {
        for (MidiInListener m : mListener) {
            m.clock_stop();
        }
    }

    private void clock_continue() {
        for (MidiInListener m : mListener) {
            m.clock_continue();
        }
    }

    private void clock_start() {
        for (MidiInListener m : mListener) {
            m.clock_start();
        }
    }

    private void clock_tick() {
        for (MidiInListener m : mListener) {
            m.clock_tick();
        }
    }

    private int parse_byte(byte b) {
        return (b & 0xFF);
    }

    private Transmitter find(String pMidiOutputDevice) {
        MidiDevice.Info[] mInfos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info mInfo : mInfos) {
            try {
                MidiDevice mDevice = MidiSystem.getMidiDevice(mInfo);
                if (mDevice.getMaxTransmitters() != 0) {
                    if (pMidiOutputDevice.equals(mInfo.getName())) {
                        if (!mDevice.isOpen()) {
                            mDevice.open();
                        }
                        return mDevice.getTransmitter();
                    }
                }
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void receiveProgramChange(int channel, int number, int value) {
        for (MidiInListener m : mListener) {
            m.receiveProgramChange(channel, number, value);
        }
    }

    private void receiveControlChange(int channel, int number, int value) {
        for (MidiInListener m : mListener) {
            m.receiveControlChange(channel, number, value);
        }
    }

    private void receiveNoteOff(int channel, int pitch) {
        for (MidiInListener m : mListener) {
            m.receiveNoteOff(channel, pitch);
        }
    }

    private void receiveNoteOn(int channel, int pitch, int velocity) {
        for (MidiInListener m : mListener) {
            m.receiveNoteOn(channel, pitch, velocity);
        }
    }

    public static String[] availableInputs() {
        ArrayList<String> mMidiInputs = new ArrayList<>();
        MidiDevice.Info[] mInfos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info mInfo : mInfos) {
            try {
                MidiDevice mDevice = MidiSystem.getMidiDevice(mInfo);
                if (mDevice.getMaxTransmitters() != 0) {
                    mMidiInputs.add(mInfo.getName());
                }
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }
        String[] mMidiOutputsStr = new String[mMidiInputs.size()];
        return mMidiInputs.toArray(mMidiOutputsStr);
    }
}
