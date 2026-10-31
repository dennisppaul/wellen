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

import processing.core.PApplet;
import wellen.dsp.Sampler;

import java.util.ArrayList;

/**
 * implementation of {@link wellen.ToneEngine} sending MIDI messages to external MIDI devices. instrument IDs are used
 * as MIDI channels.
 */
public class ToneEngineMIDI extends ToneEngine {

    /**
     * if <code>true</code> {@link #note_off()} additionally sends a note-off message for every note ( 0–127 ) on the
     * current channel. use this for devices that ignore <i>all notes off</i> ( CC 123 ).
     */
    public static boolean SEND_NOTE_OFF_TO_ALL = false;
    private static final int mNumberOfInstruments = Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS;
    /**
     * @deprecated use {@link #get_midi_out()} instead
     */
    @Deprecated
    public final MidiOut mMidiOut;
    private final ArrayList<InstrumentMIDI> mInstruments;
    private volatile int mCurrentInstrumentID;
    private Thread fShutdownHook;
    private boolean fStopped = false;

    /**
     * @param pMidiOutputDeviceName name of MIDI output device. the name does not need to match exactly: if no exact
     *                              match is found the first device that starts with or contains the name is used.
     */
    public ToneEngineMIDI(String pMidiOutputDeviceName) {
        this(new MidiOut(pMidiOutputDeviceName));
    }

    /**
     * @param pMidiOutputDeviceID ID of MIDI output device as listed by {@link Wellen#dumpMidiOutputDevices()}
     */
    public ToneEngineMIDI(int pMidiOutputDeviceID) {
        this(new MidiOut(pMidiOutputDeviceID));
    }

    /**
     * @param midi_out MIDI output used to send messages
     */
    public ToneEngineMIDI(MidiOut midi_out) {
        mMidiOut = midi_out;
        mInstruments = new ArrayList<>();
        for (int i = 0; i < mNumberOfInstruments; i++) {
            mInstruments.add(new InstrumentMIDI(i, midi_out));
        }
        mCurrentInstrumentID = 0;
        prepareExitHandler();
    }

    /**
     * @param pMidiOutputDeviceName name or part of name of MIDI output device
     * @return full name of first matching MIDI output device or an empty string if no device matches
     */
    public static String get_proper_device_name(String pMidiOutputDeviceName) {
        final javax.sound.midi.MidiDevice.Info mInfo = MidiDevices.resolve(pMidiOutputDeviceName, false);
        if (mInfo != null) {
            return mInfo.getName();
        }
        System.err.println("+++ @" + ToneEngineMIDI.class.getSimpleName() + " / couldn't find MIDI device: " + pMidiOutputDeviceName);
        return "";
    }

    @SuppressWarnings("deprecation")
    public MidiOut get_midi_out() {
        return mMidiOut;
    }

    /**
     * turns off all held notes on all channels, closes the MIDI output and stops scheduled note-offs.
     */
    public void stop() {
        super.stop();
        synchronized (this) {
            if (fStopped) {
                return;
            }
            fStopped = true;
        }
        all_notes_off();
        get_midi_out().close();
        if (fShutdownHook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(fShutdownHook);
            } catch (IllegalStateException ignored) {
                /* JVM is already shutting down */
            }
            fShutdownHook = null;
        }
    }

    public void note_on(int note, int velocity) {
        currentInstrument().note_on(note, velocity);
    }

    public void note_off(int note) {
        currentInstrument().note_off(note);
    }

    /**
     * turns off the last played note on the current channel.
     */
    public void note_off() {
        final InstrumentMIDI mInstrument = currentInstrument();
        mInstrument.note_off();
        if (SEND_NOTE_OFF_TO_ALL) {
            get_midi_out().send_note_off_to_all(mInstrument.channel());
        }
    }

    @Override
    public void note_on_instrument(int instrument_id, int note, int velocity) {
        instrumentByID(instrument_id).note_on(note, velocity);
    }

    @Override
    public void note_off_instrument(int instrument_id, int note) {
        instrumentByID(instrument_id).note_off(note);
    }

    /**
     * turns off all held notes and sends <i>all notes off</i> ( CC 123 ) on all channels.
     */
    public void all_notes_off() {
        for (InstrumentMIDI mInstrument : mInstruments) {
            mInstrument.all_notes_off();
        }
    }

    public void control_change(int pCC, int pValue) {
        currentInstrument().control_change(pCC, pValue);
    }

    /**
     * @param pValue pitch bend value ( 0–16383 ) with <code>MIDI.PITCH_BEND_CENTER = 8192</code> as center
     */
    public void pitch_bend(int pValue) {
        currentInstrument().send_pitch_bend(pValue);
    }

    public boolean is_playing() {
        return currentInstrument().is_playing();
    }

    public Instrument instrument(int pInstrumentID) {
        mCurrentInstrumentID = pInstrumentID < 0 ? 0 : pInstrumentID % mNumberOfInstruments;
        return instrument();
    }

    public Instrument instrument() {
        return currentInstrument();
    }

    public ArrayList<? extends Instrument> instruments() {
        return mInstruments;
    }

    public boolean is_initialized() {
        return get_midi_out().is_initialized();
    }

    public void replace_instrument(Instrument pInstrument) {
        if (pInstrument instanceof InstrumentMIDI) {
            final InstrumentMIDI mInstrument = (InstrumentMIDI) pInstrument;
            if (mInstrument.ID() < 0 || mInstrument.ID() >= mInstruments.size()) {
                System.err.println("+++ WARNING @" + getClass().getSimpleName() + ".replace_instrument(Instrument) / " +
                                           "instrument ID must be between 0 and " + (mInstruments.size() - 1));
                return;
            }
            if (mInstrument.get_midi_out() == null) {
                mInstrument.set_midi_out(get_midi_out());
            }
            mInstruments.set(mInstrument.ID(), mInstrument);
        } else {
            System.err.println("+++ WARNING @" + getClass().getSimpleName() + ".replace_instrument(Instrument) / " +
                                       "instrument must be" + " of type `" + InstrumentMIDI.class.getSimpleName() +
                                       "`");
        }
    }

    @Override
    public Sampler load_sample(PApplet p, String sampleFilename) {
        System.out.println("+++ @" + getClass().getSimpleName() + ".load_sample(PApplet, String) / not implemented");
        return null;
    }

    @Override
    public Sampler load_sample(byte[] sampleBuffer) {
        System.out.println("+++ @" + getClass().getSimpleName() + ".load_sample(byte[]) / not implemented");
        return null;
    }

    @Override
    public boolean remove_sample(Sampler sampler) {
        return false;
    }

    private InstrumentMIDI currentInstrument() {
        return mInstruments.get(mCurrentInstrumentID);
    }

    private InstrumentMIDI instrumentByID(int pID) {
        return mInstruments.get(pID < 0 ? 0 : pID % mNumberOfInstruments);
    }

    private void prepareExitHandler() {
        fShutdownHook = new Thread(() -> {
            synchronized (ToneEngineMIDI.this) {
                if (fStopped) {
                    return;
                }
                fStopped = true;
            }
            try {
                all_notes_off();
                get_midi_out().close();
            } catch (RuntimeException ignored) {
                /* never fail during JVM shutdown */
            }
        }, "wellen-midi-shutdown");
        try {
            Runtime.getRuntime().addShutdownHook(fShutdownHook);
        } catch (IllegalStateException e) {
            fShutdownHook = null;
        }
    }
}
