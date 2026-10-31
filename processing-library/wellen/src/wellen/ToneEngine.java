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
import java.util.Timer;
import java.util.TimerTask;

/**
 * the underlying engine that is used by {@link wellen.Tone} to handle instruments.
 */
public abstract class ToneEngine {

    // @TODO(add javadoc to abstract classes)
    public static final int INSTRUMENT_EMPTY = 0;
    public static final int INSTRUMENT_WITH_OSCILLATOR = 1;
    public static final int INSTRUMENT_WITH_OSCILLATOR_ADSR = 2;
    public static final int INSTRUMENT_WITH_OSCILLATOR_ADSR_FILTER_LFO = 3;
    private final Timer mTimer;
    /**
     * guards the temporary instrument selection in {@link #note_on_instrument(int, int, int)} and
     * {@link #note_off_instrument(int, int)}
     */
    protected final Object fSelectionLock = new Object();

    ToneEngine() {
        mTimer = new Timer("wellen-note-off");
    }

    public static ToneEngine create() {
        return new ToneEngineDSP();
    }

    public static ToneEngine create(String... pName) {
        if (pName.length > 0) {
            if (pName[0].equalsIgnoreCase(Wellen.TONE_ENGINE_INTERNAL)) {
                return new ToneEngineDSP();
            } else if (pName[0].equalsIgnoreCase(Wellen.TONE_ENGINE_MIDI) && pName.length == 2) {
                return new ToneEngineMIDI(pName[1]);
            } else if (pName[0].equalsIgnoreCase(Wellen.TONE_ENGINE_OSC) && pName.length >= 2) {
                if (pName.length == 2) {
                    return new ToneEngineOSC(pName[1]);
                } else if (pName.length == 3) {
                    try {
                        final String mIPTransmit = pName[1];
                        final int mPortTransmit = Integer.parseInt(pName[2]);
                        return new ToneEngineOSC(mIPTransmit, mPortTransmit);
                    } catch (NumberFormatException e) {
                        System.err.println("+++ WARNING @" + ToneEngine.class.getSimpleName() + ".createEngine" + " " + "/" + " could not parse ports");
                    }
                }
                return new ToneEngineOSC();
            }
            System.err.println("+++ WARNING @" + ToneEngine.class.getSimpleName() + ".createEngine" + " / could not " + "find specified tone engine: " + pName[0]);
            System.err.println("+++ hint: check engine name and number of parameters");
        }
        return create();
    }

    public void stop() {
        mTimer.cancel();
    }

    /**
     * play a note
     *
     * @param note     pitch of note ranging from 0 to 127
     * @param velocity volume of note ranging from 0 to 127
     * @param duration duration in seconds before the note is turned off again
     */
    public final void note_on(int note, int velocity, float duration) {
        final int mInstrumentID = instrument().ID();
        note_on_instrument(mInstrumentID, note, velocity);
        /* schedule note-off *after* note-on so that very short durations can not turn the note off before it starts */
        mTimer.schedule(new NoteOffTask(note, mInstrumentID), Math.max(0, (long) (duration * 1000)));
    }

    /**
     * play a note on a specific instrument without changing the currently selected instrument. unlike the combination
     * of <code>instrument(int)</code> and <code>note_on(int, int)</code> this method is safe to call from different
     * threads at the same time ( e.g from a MIDI callback and from <code>draw()</code> ).
     *
     * @param instrument_id ID of instrument
     * @param note          pitch of note ranging from 0 to 127
     * @param velocity      volume of note ranging from 0 to 127
     */
    public void note_on_instrument(int instrument_id, int note, int velocity) {
        synchronized (fSelectionLock) {
            final int mPreviousID = instrument().ID();
            instrument(instrument_id);
            note_on(note, velocity);
            instrument(mPreviousID);
        }
    }

    /**
     * turn off a note on a specific instrument without changing the currently selected instrument.
     *
     * @param instrument_id ID of instrument
     * @param note          pitch of note to turn off
     */
    public void note_off_instrument(int instrument_id, int note) {
        synchronized (fSelectionLock) {
            final int mPreviousID = instrument().ID();
            instrument(instrument_id);
            note_off(note);
            instrument(mPreviousID);
        }
    }

    /**
     * play a note
     *
     * @param note     pitch of note ranging from 0 to 127
     * @param velocity volume of note ranging from 0 to 127
     */
    public abstract void note_on(int note, int velocity);

    /**
     * turn off a note
     *
     * @param note pitch of note to turn off
     */
    public abstract void note_off(int note);

    /**
     * turns off the last played note.
     */
    public abstract void note_off();

    public abstract void control_change(int pCC, int pValue);

    public abstract void pitch_bend(int pValue);

    public abstract boolean is_playing();

    public abstract Instrument instrument(int pInstrumentID);

    public abstract Instrument instrument();

    public abstract ArrayList<? extends Instrument> instruments();

    public abstract void replace_instrument(Instrument pInstrument);

    public float[] get_buffer() {
        return get_buffer_left();
    }

    public float[] get_buffer_left() {
        return null;
    }

    public float[] get_buffer_right() {
        return null;
    }

    public abstract Sampler load_sample(PApplet p, String sampleFilename);

    public abstract Sampler load_sample(byte[] sampleBuffer);

    public abstract boolean remove_sample(Sampler sampler);

    private class NoteOffTask extends TimerTask {

        final int instrument;
        final int note;

        public NoteOffTask(int pNote, int pInstrument) {
            note = pNote;
            instrument = pInstrument;
        }

        public void run() {
            note_off_instrument(instrument, note);
        }
    }
}
