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

import java.util.ArrayList;

import static processing.core.PApplet.constrain;

/**
 * implementation of {@link wellen.ToneEngine} sending MIDI messages to external MIDI devices.
 */
public class ToneEngineMIDI extends ToneEngine {

    public static boolean SEND_NOTE_OFF_TO_ALL = false;
    private static final int mNumberOfInstruments = Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS;
    public final MidiOut mMidiOut;
    private int mCurrentInstrumentID;
    private final ArrayList<InstrumentMIDI> mInstruments;

    public ToneEngineMIDI(String pMidiOutputDeviceName) {
        this(new MidiOut(get_proper_device_name(pMidiOutputDeviceName)));
    }

    public ToneEngineMIDI(int pMidiOutputDeviceID) {
        this(new MidiOut(pMidiOutputDeviceID));
    }

    private ToneEngineMIDI(MidiOut pMidiOut) {
        mMidiOut = pMidiOut;
        mInstruments = new ArrayList<>();
        prepareExitHandler();
        for (int i = 0; i < mNumberOfInstruments; i++) {
            final InstrumentMIDI mInstrument = new InstrumentMIDI(i);
            mInstruments.add(mInstrument);
        }
        mCurrentInstrumentID = 0;
    }

    public static String get_proper_device_name(String pMidiOutputDeviceName) {
        String[] mDevices = MidiOut.availableOutputs();
        for (String mDevice : mDevices) {
            if (mDevice.startsWith(pMidiOutputDeviceName)) {
                return mDevice;
            }
        }
        System.err.println("+++ @" + ToneEngineMIDI.class.getSimpleName() + " / couldn't find MIDI device: " + pMidiOutputDeviceName);
        return "";
    }

    public void stop() {
        super.stop();
        if (mMidiOut != null) {
            mMidiOut.close();
        }
    }

    public void note_on(int note, int velocity) {
        mMidiOut.sendNoteOn(mCurrentInstrumentID, note, velocity);
        mInstruments.get(mCurrentInstrumentID).note_on(note, velocity);
    }

    public void note_off(int note) {
        mMidiOut.sendNoteOff(mCurrentInstrumentID, note, 0);
        mInstruments.get(mCurrentInstrumentID).note_off();
    }

    public void note_off() {
        note_off(0);
        if (SEND_NOTE_OFF_TO_ALL) {
            for (int i = 0; i < 127; i++) {
                mMidiOut.sendNoteOff(mCurrentInstrumentID, i, 0);
            }
        }
    }

    public void control_change(int pCC, int pValue) {
        mMidiOut.sendControllerChange(mCurrentInstrumentID, pCC, pValue);
    }

    public void pitch_bend(int pValue) {
        final int mValue = constrain(pValue, 0, 16383);
        final int LSB_MASK = 0b00000001111111;
        final int MSB_MASK = 0b11111110000000;
        final int msb = (mValue & MSB_MASK) / 128;
        final int lsb = mValue & LSB_MASK;
        mMidiOut.sendPitchBend(mCurrentInstrumentID, lsb, msb);
    }

    public boolean is_playing() {
        return mInstruments.get(mCurrentInstrumentID).is_playing();
    }

    public Instrument instrument(int pInstrumentID) {
        mCurrentInstrumentID = pInstrumentID < 0 ? 0 : pInstrumentID % mNumberOfInstruments;
        return instrument();
    }

    public Instrument instrument() {
        return instruments().get(mCurrentInstrumentID);
    }

    public ArrayList<? extends Instrument> instruments() {
        return mInstruments;
    }

    public boolean is_initialized() {
        return mMidiOut.is_initialized();
    }

    public void replace_instrument(Instrument pInstrument) {
        if (pInstrument instanceof InstrumentMIDI) {
            mInstruments.set(pInstrument.ID(), (InstrumentMIDI) pInstrument);
        } else {
            System.err.println("+++ WARNING @" + getClass().getSimpleName() + ".replace_instrument(Instrument) / " +
                                       "instrument must be" + " of type `" + InstrumentMIDI.class.getSimpleName() +
                                       "`");
        }
    }

    private void prepareExitHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                if (mMidiOut != null) {
                    for (int j = 0; j < mNumberOfInstruments; j++) {
                        for (int i = 0; i < 127; i++) {
                            mMidiOut.sendNoteOff(j, i, 0);
                        }
                    }
                    mMidiOut.close();
                }
            }
        }));
    }
}
