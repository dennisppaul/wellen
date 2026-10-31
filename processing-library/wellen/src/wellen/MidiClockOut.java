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

import java.util.concurrent.locks.LockSupport;

/**
 * sends MIDI clock signals ( 24 pulses per quarter note ) to a MIDI output i.e makes wellen act as MIDI clock master
 * for other applications or devices ( e.g a {@link BeatMIDI} in another sketch, a DAW or a drum machine ).
 * <p>
 * pulses are scheduled on absolute times on a dedicated thread, so the tempo does not drift. tempo changes take effect
 * with the next pulse.
 * <p>
 * note that on macOS java's built-in MIDI implementation can not send MIDI clock messages to MIDI ports, see
 * {@link MidiOut#can_send_clock_and_sysex()}.
 * <pre>
 * <code>
 * MidiClockOut mClock = new MidiClockOut(new MidiOut("IAC Driver Bus 1"), 120);
 * mClock.start();
 * </code>
 * </pre>
 */
public class MidiClockOut {

    private static final long SPIN_THRESHOLD_NS = 1_000_000L; // busy-wait during the last millisecond
    private final MidiOut fMidiOut;
    private volatile double fBPM;
    private volatile boolean fRunning = false;
    private volatile boolean fDisposed = false;
    private volatile int fTickCount = 0;
    private Thread fThread;
    private long fNextTickNs;

    /**
     * @param midi_out MIDI output to send clock signals to
     * @param BPM      tempo in beats ( quarter notes ) per minute
     */
    public MidiClockOut(MidiOut midi_out, float BPM) {
        fMidiOut = midi_out;
        set_bpm(BPM);
    }

    /**
     * @param BPM tempo in beats ( quarter notes ) per minute ( clamped to 1–999 )
     */
    public void set_bpm(float BPM) {
        fBPM = Math.max(1.0, Math.min(999.0, BPM));
    }

    public float get_bpm() {
        return (float) fBPM;
    }

    /**
     * @return number of pulses sent since the last {@link #start()} or {@link #set_song_position(int)}
     */
    public int get_tick_count() {
        return fTickCount;
    }

    public boolean is_running() {
        return fRunning;
    }

    /**
     * sends MIDI <i>start</i> and starts sending pulses from the beginning of the song.
     */
    public synchronized void start() {
        stopThread();
        fTickCount = 0;
        fMidiOut.send_clock_start();
        startThread();
    }

    /**
     * sends MIDI <i>continue</i> and resumes sending pulses from the current position.
     */
    public synchronized void resume() {
        stopThread();
        fMidiOut.send_clock_continue();
        startThread();
    }

    /**
     * stops sending pulses and sends MIDI <i>stop</i>.
     */
    public synchronized void stop() {
        stopThread();
        fMidiOut.send_clock_stop();
    }

    /**
     * sends a <i>song position pointer</i>. should be sent while the clock is stopped.
     *
     * @param offset_16th song position in sixteenth notes
     */
    public synchronized void set_song_position(int offset_16th) {
        fTickCount = offset_16th * MIDI.PULSES_PER_SIXTEENTH;
        fMidiOut.send_song_position_pointer(offset_16th);
    }

    /**
     * stops the clock. the MIDI output is <i>not</i> closed.
     */
    public synchronized void dispose() {
        stop();
        fDisposed = true;
    }

    private void startThread() {
        if (fDisposed) {
            return;
        }
        fRunning = true;
        fNextTickNs = System.nanoTime();
        fThread = new Thread(this::run, "wellen-midi-clock-out");
        fThread.setDaemon(true);
        fThread.setPriority(Thread.MAX_PRIORITY);
        fThread.start();
    }

    private void stopThread() {
        fRunning = false;
        if (fThread != null) {
            fThread.interrupt();
            if (fThread != Thread.currentThread()) {
                try {
                    fThread.join(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            fThread = null;
        }
    }

    private void run() {
        final Thread mSelf = Thread.currentThread();
        long mNextTickNs = fNextTickNs;
        while (fRunning && !mSelf.isInterrupted()) {
            final long mWait = mNextTickNs - System.nanoTime();
            if (mWait > SPIN_THRESHOLD_NS) {
                LockSupport.parkNanos(mWait - SPIN_THRESHOLD_NS);
                continue;
            }
            if (mWait > 0) {
                Thread.onSpinWait();
                continue;
            }
            if (!fRunning) {
                break;
            }
            fMidiOut.send_clock_tick();
            fTickCount++;
            mNextTickNs += (long) (60.0e9 / (fBPM * MIDI.PPQN));
        }
    }
}
