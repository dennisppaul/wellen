package de.hfkbremen.ton;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import javax.swing.*;
import java.util.ArrayList;

import static de.hfkbremen.ton.TonUtil.dumpMidiInputDevices;

public class MidiIn implements Receiver {

    private static final int SYSEX_START = 0xF0;
    private static final int SYSEX_END = 0xF7;
    private static final int NOTE_OFF = 0x80;
    private static final int NOTE_ON = 0x90;
    private static final int CONTROL_CHANGE = 0xB0;
    private static final int PROGRAM_CHANGE = 0xC0;
    private static boolean DUMP_MESSAGES = false;
    private final ArrayList<MidiInListener> mListener;

    public MidiIn(String pMidiOutputDevice) {
        final Transmitter mMidiIn = find(pMidiOutputDevice);
        if (mMidiIn != null) {
            mMidiIn.setReceiver(this);
        } else {
            System.err.println("### Error @ MidiIn / could not find midi device: " + pMidiOutputDevice);
            System.err.println("### availabel inputs are: ");
            dumpMidiInputDevices();
        }
        mListener = new ArrayList<>();
    }

    public void addListener(MidiInListener pMidiInListener) {
        mListener.add(pMidiInListener);
    }

    public void removeListener(MidiInListener pMidiInListener) {
        mListener.remove(pMidiInListener);
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

    @Override
    public void send(MidiMessage message, long timeStamp) {
        if (message instanceof ShortMessage) {
            ShortMessage mShortMessage = (ShortMessage) message;
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
                default:
                    if (DUMP_MESSAGES) {
                        System.err.println("### MidiIn / could not parse midi message: " + mShortMessage);
                    }
            }
        } else {
            if (DUMP_MESSAGES) {
                System.err.println("### MidiIn / could not parse midi message: " + message);
            }
        }
    }

    @Override
    public void close() {
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

    private static class TestMidiIn implements MidiInListener {

        public void receiveProgramChange(int channel, int number, int value) {
            System.out.println("programChange: " + channel + " / " + number + " + " + value);
        }

        public void receiveControlChange(int channel, int number, int value) {
            System.out.println("controlChange: " + channel + " / " + number + " + " + value);
        }

        public void receiveNoteOff(int channel, int pitch) {
            System.out.println("noteOff      : " + channel + " / " + pitch);
        }

        public void receiveNoteOn(int channel, int pitch, int velocity) {
            System.out.println("noteOn       : " + channel + " / " + pitch + " + " + velocity);
        }

    }

    public static void main(String[] args) {
        MidiIn mMidiIn = new MidiIn("IAC-Driver");
        mMidiIn.addListener(new TestMidiIn());
        new JFrame().setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
