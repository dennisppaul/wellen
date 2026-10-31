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

/**
 * similar to {@link wellen.Beat} except that beat events are triggered by an external MIDI clock signal.
 * <p>
 * the method <code>beat(int)</code> of the listener is called for every MIDI clock pulse ( 24 pulses per quarter note
 * ). the first pulse after a MIDI <i>start</i> message is beat <code>0</code>. after a <i>song position pointer</i>
 * message the next pulse is the beat at that position ( one sixteenth note = 6 pulses ).
 * <p>
 * by default <code>beat(int)</code> is called on the MIDI thread. use {@link #dispatch_on_draw_thread(boolean)} to
 * call it on the processing animation thread instead.
 */
public class BeatMIDI implements MidiInListener {

    /**
     *
     */
    public static boolean VERBOSE = false;
    /**
     * if <code>true</code> the beat counter behaves like before v0.14: the first beat after a MIDI <i>start</i> message
     * is <code>1</code> instead of <code>0</code>.
     *
     * @deprecated only use this to keep old sketches working that rely on the old ( wrong ) counting.
     */
    @Deprecated
    public static boolean LEGACY_BEAT_COUNT = false;
    private static final String METHOD_NAME = "beat";
    private static final double BPM_SMOOTHING = 0.1;
    private final MidiCallbacks fCallbacks;
    private final Method fMethod;
    private volatile float fBPMEstimate = 0;
    private double fAverageTickIntervalNs = 0;
    private long fLastTickNs = -1;
    private volatile boolean fIsRunning = false;
    private boolean fResetBeatCounterAtStop = true;
    private volatile int fNextTick = 0;
    private volatile int fBeatCount = -1;
    private MidiIn fMidiIn = null;

    private BeatMIDI(Object pListener) {
        fCallbacks = new MidiCallbacks(pListener, getClass().getSimpleName());
        fMethod = fCallbacks.find(METHOD_NAME, Integer.TYPE);
        if (fMethod == null) {
            System.err.println("+++ @" + getClass().getSimpleName() + " / could not find `" + METHOD_NAME + "(int)`");
        }
        /* before v0.14 the counter started at -1 and was incremented before delivery */
        fNextTick = LEGACY_BEAT_COUNT ? -1 : 0;
        start();
    }

    /**
     * @param pListener  object which implements the method `beat(int beatCounter)`
     * @param pMidiInput MIDI input device to listen to
     * @return {@link BeatMIDI}
     */
    public static BeatMIDI start(Object pListener, String pMidiInput) {
        final BeatMIDI mBeatMIDI = new BeatMIDI(pListener);
        mBeatMIDI.fMidiIn = new MidiIn(pMidiInput);
        mBeatMIDI.fMidiIn.add_listener(mBeatMIDI);
        return mBeatMIDI;
    }

    /**
     * @param listener             object which implements the method `beat(int beatCounter)`
     * @param midi_input_device_ID ID of MIDI input device as listed by {@link Wellen#dumpMidiInputDevices()}
     * @return {@link BeatMIDI}
     */
    public static BeatMIDI start(Object listener, int midi_input_device_ID) {
        final BeatMIDI mBeatMIDI = new BeatMIDI(listener);
        mBeatMIDI.fMidiIn = new MidiIn(midi_input_device_ID);
        mBeatMIDI.fMidiIn.add_listener(mBeatMIDI);
        return mBeatMIDI;
    }

    /**
     * @param listener object which implements the method `beat(int beatCounter)`
     * @param midi_in  existing MIDI input ( e.g shared with an {@link EventReceiverMIDI} ). it is not closed by
     *                 {@link #close()}.
     * @return {@link BeatMIDI}
     */
    public static BeatMIDI start(Object listener, MidiIn midi_in) {
        final BeatMIDI mBeatMIDI = new BeatMIDI(listener);
        midi_in.add_listener(mBeatMIDI);
        return mBeatMIDI;
    }

    /**
     * @param pResetBeatCounterAtStop if `true` the beat counter is reset to `0` when the MIDI clock signal stops.
     */
    public void reset_beat_counter_at_stop(boolean pResetBeatCounterAtStop) {
        fResetBeatCounterAtStop = pResetBeatCounterAtStop;
    }

    /**
     * @param dispatch_on_draw_thread if <code>true</code> <code>beat(int)</code> is queued and called on the processing
     *                                animation thread right before <code>draw()</code>. this is safer when drawing or
     *                                changing sketch state in <code>beat(int)</code> but beats are delivered in
     *                                batches once per frame. requires the listener to be a <code>PApplet</code>.
     * @return {@link BeatMIDI}
     */
    public BeatMIDI dispatch_on_draw_thread(boolean dispatch_on_draw_thread) {
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
     * stops listening to the MIDI input and releases the MIDI device ( if it was opened by this instance ).
     */
    public void close() {
        stop();
        fCallbacks.dispose(this);
        if (fMidiIn != null) {
            fMidiIn.remove_listener(this);
            fMidiIn.close();
            fMidiIn = null;
        }
    }

    /**
     * @return true if beat is running
     */
    public boolean running() {
        return fIsRunning;
    }

    /**
     * @return count of the last delivered beat in pulses per quarter notes (PPQN)
     */
    public int beat_count() {
        return fBeatCount;
    }

    /**
     * returns an estimate of the current BPM deduced from the duration between ticks ( or pulses )
     *
     * @return estimated BPM ( <code>0</code> until two ticks were received )
     */
    public float bpm() {
        return fBPMEstimate;
    }

    /**
     * @hidden internal use only
     */
    public void invoke() {
        invokeBeat(fBeatCount);
    }

    @Override
    public void clock_tick() {
        if (!fIsRunning) {
            return;
        }
        estimate_bpm();
        final int mBeat;
        synchronized (this) {
            if (LEGACY_BEAT_COUNT) {
                fNextTick++;
                mBeat = fNextTick;
            } else {
                mBeat = fNextTick;
                fNextTick++;
            }
            fBeatCount = mBeat;
        }
        invokeBeat(mBeat);
    }

    @Override
    public void clock_start() {
        if (VERBOSE) {
            System.out.println("clock_start");
        }
        synchronized (this) {
            fNextTick = 0;
        }
        start();
    }

    @Override
    public void clock_continue() {
        if (VERBOSE) {
            System.out.println("clock_continue");
        }
        start();
    }

    @Override
    public void clock_stop() {
        if (VERBOSE) {
            System.out.println("clock_stop");
        }
        stop();
    }

    @Override
    public void clock_song_position_pointer(int offset_16th) {
        synchronized (this) {
            /* one sixteenth note = 6 clock pulses */
            fNextTick = offset_16th * MIDI.PULSES_PER_SIXTEENTH;
            if (LEGACY_BEAT_COUNT) {
                fNextTick--;
            }
        }
        if (VERBOSE) {
            System.out.println("clock_song_position_pointer: " + fNextTick + "(" + offset_16th + ")");
        }
    }

    public void stop() {
        synchronized (this) {
            if (fResetBeatCounterAtStop) {
                fNextTick = 0;
            }
        }
        fIsRunning = false;
    }

    public void start() {
        synchronized (this) {
            /* first tick after (re)start only provides a time reference for the BPM estimate */
            fLastTickNs = -1;
        }
        fIsRunning = true;
    }

    private void invokeBeat(int beat) {
        if (fMethod == null || !fIsRunning) {
            return;
        }
        fCallbacks.invoke(fMethod, beat);
    }

    private synchronized void estimate_bpm() {
        final long mNow = System.nanoTime();
        if (fLastTickNs >= 0) {
            final long mInterval = mNow - fLastTickNs;
            if (mInterval > 0) {
                if (fAverageTickIntervalNs <= 0) {
                    fAverageTickIntervalNs = mInterval;
                } else {
                    fAverageTickIntervalNs += BPM_SMOOTHING * (mInterval - fAverageTickIntervalNs);
                }
                fBPMEstimate = (float) (60.0e9 / (fAverageTickIntervalNs * MIDI.PPQN));
            }
        }
        fLastTickNs = mNow;
    }
}
