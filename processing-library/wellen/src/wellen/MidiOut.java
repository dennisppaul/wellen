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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import java.util.ArrayList;

import static processing.core.PApplet.constrain;

/**
 * handles communication with MIDI devices
 */
public class MidiOut {

    private final Receiver mMidiOut;

    public MidiOut(String pMidiOutputDevice) {
        mMidiOut = find(pMidiOutputDevice);
    }

    public MidiOut(int pMidiOutputDevice) {
        mMidiOut = find(pMidiOutputDevice);
    }

    public static String[] availableOutputs() {
        ArrayList<String> mMidiOutputs = new ArrayList<>();
        MidiDevice.Info[] mInfos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info mInfo : mInfos) {
            try {
                MidiDevice mDevice = MidiSystem.getMidiDevice(mInfo);
                if (mDevice.getMaxReceivers() != 0) {
                    mMidiOutputs.add(mInfo.getName());
                }
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }
        String[] mMidiOutputsStr = new String[mMidiOutputs.size()];
        return mMidiOutputs.toArray(mMidiOutputsStr);
    }

    public void sendNoteOn(int channel, int pitch, int velocity) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.NOTE_ON,
                               constrain(channel, 0, 15),
                               constrain(pitch, 0, 127),
                               constrain(velocity, 0, 127));
            sendMessage(message);
        } catch (InvalidMidiDataException e) {
            System.err.println("+++ Message not sent, invalid MIDI data");
        }
    }

    public void sendControllerChange(int channel, int number, int value) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.CONTROL_CHANGE,
                               constrain(channel, 0, 15),
                               constrain(number, 0, 127),
                               constrain(value, 0, 127));
            sendMessage(message);
        } catch (InvalidMidiDataException e) {
            System.err.println("+++ Message not sent, invalid MIDI data");
        }
    }

    public void sendNoteOff(int channel, int pitch, int velocity) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.NOTE_OFF,
                               constrain(channel, 0, 15),
                               constrain(pitch, 0, 127),
                               constrain(velocity, 0, 127));
            sendMessage(message);
        } catch (InvalidMidiDataException e) {
            System.err.println("+++ Message not sent, invalid MIDI data");
        }
    }

    public void sendPitchBend(int channel, int pitchbend_lsb, int pitchbend_msb) {
        //        http://home.snafu.de/sicpaul/midi/midi3.htm
        sendMessage(ShortMessage.PITCH_BEND, channel, pitchbend_lsb, pitchbend_msb);
    }

    public void sendMessage(int command, int channel, int data1, int data2) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(command, channel, data1, data2);
            sendMessage(message);
        } catch (InvalidMidiDataException e) {
            System.err.println("+++ Message not sent, invalid MIDI data");
        }
    }

    public void close() {
        mMidiOut.close();
    }

    private Receiver find(String pMidiOutputDevice) {
        MidiDevice.Info[] mInfos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info mInfo : mInfos) {
            try {
                MidiDevice mDevice = MidiSystem.getMidiDevice(mInfo);
                if (mDevice.getMaxReceivers() != 0) {
                    if (pMidiOutputDevice.equals(mInfo.getName())) {
                        if (!mDevice.isOpen()) {
                            mDevice.open();
                        }
                        return mDevice.getReceiver();
                    }
                }
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Receiver find(int pMidiOutputDeviceID) {
        MidiDevice.Info[] mInfos = MidiSystem.getMidiDeviceInfo();
        MidiDevice.Info mInfo = mInfos[pMidiOutputDeviceID];
        try {
            MidiDevice mDevice = MidiSystem.getMidiDevice(mInfo);
            if (mDevice.getMaxReceivers() != 0) {
                if (!mDevice.isOpen()) {
                    mDevice.open();
                }
                return mDevice.getReceiver();
            }
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
        return null;
    }

    private synchronized void sendMessage(MidiMessage message) {
        mMidiOut.send(message, 0);
    }

}