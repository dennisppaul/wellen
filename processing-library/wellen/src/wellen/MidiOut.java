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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

/**
 * sends MIDI messages to a MIDI output device.
 * <p>
 * all values are clamped to their valid ranges ( channel 0–15, data 0–127, pitch bend 0–16383 ).
 */
public class MidiOut {

    /**
     * if <code>true</code> {@link #MidiOut(int)} interprets the ID as an index into <i>all</i> MIDI devices ( behavior
     * before v0.14 ). by default the ID refers to the list printed by {@link Wellen#dumpMidiOutputDevices()}.
     *
     * @deprecated only use this to keep old sketches working that rely on the old ( wrong ) device numbering.
     */
    @Deprecated
    public static boolean USE_LEGACY_DEVICE_INDEX = false;
    private final String fDeviceName;
    private MidiDevice fDevice;
    private volatile Receiver fReceiver;
    private boolean fErrorReported = false;
    private boolean fSystemMessageWarningReported = false;

    private MidiOut(MidiDevice.Info info, String requested_device) {
        MidiDevice mDevice = null;
        Receiver mReceiver = null;
        if (info != null) {
            try {
                mDevice = MidiDevices.acquire(info);
                mReceiver = mDevice.getReceiver();
            } catch (MidiUnavailableException | RuntimeException e) {
                System.err.println("+++ @MidiOut / could not open MIDI output device: " + info.getName() + " ( " + e.getMessage() + " )");
                MidiDevices.release(mDevice);
                mDevice = null;
                mReceiver = null;
            }
        } else {
            System.err.println("+++ @MidiOut / could not find MIDI output device: " + requested_device);
            System.err.println("+++ available outputs are: ");
            Wellen.dumpMidiOutputDevices();
        }
        fDevice = mDevice;
        fReceiver = mReceiver;
        fDeviceName = info != null && mReceiver != null ? info.getName() : "";
    }

    /**
     * @param midi_output_device name of MIDI output device. the name does not need to match exactly: if no exact
     *                           match is found the first device that starts with or contains the name is used.
     */
    public MidiOut(String midi_output_device) {
        this(MidiDevices.resolve(midi_output_device, false), String.valueOf(midi_output_device));
    }

    /**
     * @param midi_output_device_ID ID of MIDI output device as listed by {@link Wellen#dumpMidiOutputDevices()}
     */
    @SuppressWarnings("deprecation")
    public MidiOut(int midi_output_device_ID) {
        this(USE_LEGACY_DEVICE_INDEX ? MidiDevices.resolve_legacy_index(midi_output_device_ID) :
             MidiDevices.resolve(midi_output_device_ID, false), String.valueOf(midi_output_device_ID));
    }

    /**
     * creates a <code>MidiOut</code> that sends all messages to a custom receiver e.g a {@link MidiIn} for loopback
     * tests or a java {@link javax.sound.midi.Synthesizer}.
     *
     * @param receiver receiver that receives all MIDI messages
     */
    public MidiOut(Receiver receiver) {
        fDevice = null;
        fReceiver = receiver;
        fDeviceName = receiver != null ? receiver.getClass().getSimpleName() : "";
    }

    /**
     * @return names of all available MIDI output devices
     */
    public static String[] available_outputs() {
        return MidiDevices.names(false);
    }

    /**
     * @deprecated use {@link #available_outputs()} instead
     */
    @Deprecated
    public static String[] availableOutputs() {
        return available_outputs();
    }

    /* --- channel voice messages --- */

    public void send_note_on(int channel, int pitch, int velocity) {
        sendShort(MIDI.MIDI_NOTE_ON, channel, pitch, velocity);
    }

    public void send_note_off(int channel, int pitch, int velocity) {
        sendShort(MIDI.MIDI_NOTE_OFF, channel, pitch, velocity);
    }

    public void send_note_off(int channel, int pitch) {
        send_note_off(channel, pitch, 0);
    }

    /**
     * @param channel MIDI channel ( 0–15 )
     * @param number  controller number ( 0–127 ), see <code>MIDI.CC_*</code>
     * @param value   controller value ( 0–127 )
     */
    public void send_control_change(int channel, int number, int value) {
        sendShort(MIDI.MIDI_CONTROL_CHANGE, channel, number, value);
    }

    public void send_program_change(int channel, int program) {
        sendShort(MIDI.MIDI_PROGRAM_CHANGE, channel, program, 0);
    }

    public void send_channel_pressure(int channel, int pressure) {
        sendShort(MIDI.MIDI_CHANNEL_PRESSURE, channel, pressure, 0);
    }

    public void send_poly_pressure(int channel, int pitch, int pressure) {
        sendShort(MIDI.MIDI_POLY_PRESSURE, channel, pitch, pressure);
    }

    /**
     * @param channel MIDI channel ( 0–15 )
     * @param value   pitch bend value ( 0–16383 ) with <code>MIDI.PITCH_BEND_CENTER = 8192</code> as center
     */
    public void send_pitch_bend(int channel, int value) {
        final int mValue = clamp(value, MIDI.PITCH_BEND_MIN, MIDI.PITCH_BEND_MAX);
        sendShort(MIDI.MIDI_PITCH_BEND, channel, mValue & 0x7F, (mValue >> 7) & 0x7F);
    }

    /**
     * releases sustain pedal and sends <i>all notes off</i> ( CC 123 ) on one channel
     */
    public void send_all_notes_off(int channel) {
        send_control_change(channel, MIDI.CC_SUSTAIN, 0);
        send_control_change(channel, MIDI.CC_ALL_NOTES_OFF, 0);
    }

    /**
     * releases sustain pedal and sends <i>all notes off</i> ( CC 123 ) on all 16 channels
     */
    public void send_all_notes_off() {
        for (int i = 0; i < MIDI.NUMBER_OF_CHANNELS; i++) {
            send_all_notes_off(i);
        }
    }

    /**
     * sends <i>all sound off</i> ( CC 120 ) i.e mutes all sounding notes immediately ( ignoring release times )
     */
    public void send_all_sound_off(int channel) {
        send_control_change(channel, MIDI.CC_ALL_SOUND_OFF, 0);
    }

    /**
     * sends a <i>note off</i> message for every note ( 0–127 ) on one channel. use this for devices that ignore
     * {@link #send_all_notes_off(int)}.
     */
    public void send_note_off_to_all(int channel) {
        for (int i = 0; i <= 127; i++) {
            send_note_off(channel, i, 0);
        }
    }

    /**
     * sends a raw channel message. data values are clamped to 0–127.
     *
     * @param command status byte without channel e.g <code>MIDI.MIDI_NOTE_ON</code> ( <code>0x90</code> )
     */
    public void send_message(int command, int channel, int data1, int data2) {
        sendShort(command & 0xF0, channel, data1, data2);
    }

    /* --- system messages --- */

    /**
     * @param data complete SysEx message starting with <code>0xF0</code> and ending with <code>0xF7</code>
     */
    public void send_sysex(byte[] data) {
        if (data == null || data.length < 2) {
            return;
        }
        warnIfSystemMessagesUnsupported();
        try {
            final SysexMessage mMessage = new SysexMessage();
            mMessage.setMessage(data, data.length);
            send(mMessage);
        } catch (InvalidMidiDataException e) {
            reportError("invalid SysEx data: " + e.getMessage());
        }
    }

    public void send_clock_tick() {
        sendSystem(MIDI.MIDI_CLOCK_TICK);
    }

    public void send_clock_start() {
        sendSystem(MIDI.MIDI_CLOCK_START);
    }

    public void send_clock_continue() {
        sendSystem(MIDI.MIDI_CLOCK_CONTINUE);
    }

    public void send_clock_stop() {
        sendSystem(MIDI.MIDI_CLOCK_STOP);
    }

    /**
     * @param offset_16th song position in sixteenth notes ( 0–16383 )
     */
    public void send_song_position_pointer(int offset_16th) {
        final int mValue = clamp(offset_16th, 0, 16383);
        try {
            final ShortMessage mMessage = new ShortMessage();
            mMessage.setMessage(MIDI.MIDI_SONG_POSITION_POINTER, mValue & 0x7F, (mValue >> 7) & 0x7F);
            send(mMessage);
        } catch (InvalidMidiDataException e) {
            reportError("invalid MIDI data");
        }
    }

    /**
     * sends any MIDI message.
     */
    public synchronized void send(MidiMessage message) {
        final Receiver mReceiver = fReceiver;
        if (mReceiver != null && message != null) {
            try {
                /* timestamp -1 means "send now" */
                mReceiver.send(message, -1);
            } catch (IllegalStateException e) {
                reportError("could not send message, receiver is closed");
            }
        }
    }

    public boolean is_initialized() {
        return fReceiver != null;
    }

    /**
     * java's built-in MIDI implementation on macOS can not send MIDI clock ( <code>0xF8</code>, <code>0xFA</code>,
     * <code>0xFB</code>, <code>0xFC</code> ) or SysEx messages to MIDI ports. messages are silently dropped. adding
     * <a href="https://github.com/DerekCook/CoreMidi4J">CoreMIDI4J</a> to the classpath ( e.g the sketch's
     * <code>code</code> folder ) fixes this.
     *
     * @return <code>false</code> if this output is known to drop clock and SysEx messages
     */
    public boolean can_send_clock_and_sysex() {
        final MidiDevice mDevice = fDevice;
        if (mDevice == null) {
            return true;
        }
        final boolean mIsMacOS = System.getProperty("os.name", "").toLowerCase().contains("mac");
        return !(mIsMacOS && mDevice.getClass().getName().equals("com.sun.media.sound.MidiOutDevice"));
    }

    public boolean is_open() {
        return fReceiver != null;
    }

    /**
     * @return name of connected device or an empty string if not connected
     */
    public String get_device_name() {
        return fDeviceName;
    }

    /**
     * closes the receiver and closes the device if it was opened by wellen and is not used anymore. messages sent
     * after closing are ignored.
     */
    public synchronized void close() {
        final Receiver mReceiver = fReceiver;
        fReceiver = null;
        if (mReceiver != null && fDevice != null) {
            /* only close receivers obtained from a device, custom receivers belong to the caller */
            try {
                mReceiver.close();
            } catch (RuntimeException ignored) {
            }
        }
        MidiDevices.release(fDevice);
        fDevice = null;
    }

    /* --- deprecated --- */

    /**
     * @deprecated use {@link #send_note_on(int, int, int)} instead
     */
    @Deprecated
    public void sendNoteOn(int channel, int pitch, int velocity) {
        send_note_on(channel, pitch, velocity);
    }

    /**
     * @deprecated use {@link #send_control_change(int, int, int)} instead
     */
    @Deprecated
    public void sendControllerChange(int channel, int number, int value) {
        send_control_change(channel, number, value);
    }

    /**
     * @deprecated use {@link #send_note_off(int, int, int)} instead
     */
    @Deprecated
    public void sendNoteOff(int channel, int pitch, int velocity) {
        send_note_off(channel, pitch, velocity);
    }

    /**
     * @deprecated use {@link #send_pitch_bend(int, int)} instead
     */
    @Deprecated
    public void sendPitchBend(int channel, int pitchbend_lsb, int pitchbend_msb) {
        sendShort(MIDI.MIDI_PITCH_BEND, channel, pitchbend_lsb, pitchbend_msb);
    }

    /**
     * @deprecated use {@link #send_message(int, int, int, int)} instead
     */
    @Deprecated
    public void sendMessage(int command, int channel, int data1, int data2) {
        send_message(command, channel, data1, data2);
    }

    /* --- internal --- */

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void sendShort(int command, int channel, int data1, int data2) {
        try {
            final ShortMessage mMessage = new ShortMessage();
            mMessage.setMessage(command, clamp(channel, 0, 15), clamp(data1, 0, 127), clamp(data2, 0, 127));
            send(mMessage);
        } catch (InvalidMidiDataException e) {
            reportError("invalid MIDI data ( command: " + command + " )");
        }
    }

    private void sendSystem(int status) {
        warnIfSystemMessagesUnsupported();
        try {
            final ShortMessage mMessage = new ShortMessage();
            mMessage.setMessage(status);
            send(mMessage);
        } catch (InvalidMidiDataException e) {
            reportError("invalid MIDI data ( status: " + status + " )");
        }
    }

    private void warnIfSystemMessagesUnsupported() {
        if (!fSystemMessageWarningReported && !can_send_clock_and_sysex()) {
            fSystemMessageWarningReported = true;
            System.err.println("+++ @MidiOut / java's built-in MIDI on macOS can not send MIDI clock or SysEx " +
                                       "messages to `" + fDeviceName + "`. add CoreMIDI4J ( https://github" +
                                       ".com/DerekCook/CoreMidi4J ) to the sketch's `code` folder to fix this.");
        }
    }

    private void reportError(String message) {
        if (!fErrorReported) {
            fErrorReported = true;
            System.err.println("+++ @MidiOut / " + message + " ( further errors are not reported )");
        }
    }
}
