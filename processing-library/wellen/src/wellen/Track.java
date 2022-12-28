/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2022 Dennis P Paul.
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

import wellen.dsp.DSPNodeOutputSignal;
import wellen.dsp.Signal;

import java.util.ArrayList;

import static wellen.Wellen.LOOP_INFINITE;
import static wellen.Wellen.NO_INPOINT;
import static wellen.Wellen.NO_LOOP;
import static wellen.Wellen.NO_OUTPOINT;
import static wellen.Wellen.SIGNAL_MONO;
import static wellen.Wellen.SIGNAL_PROCESSING_IGNORE_IN_OUTPOINTS;
import static wellen.Wellen.SIGNAL_STEREO;

/**
 * manages a collection of {@link Track}s. each child module processes audio signals which are accumulated by the track.
 * furthermore, {@link Track} calls its child tracks <code>void&nbsp;beat(int)</code> method.
 * <p>
 * note, that since {@link Track} implements {@link Module} it can also be added as a track to another track. if a
 * class is derived from {@link Track} and <code>beat(int)</code> is overridden make sure to call
 * <code>beat_update(int)</code> to preserve internal functionality and update for child {@link Module}s.
 * similarly, make sure to call <code>Signal&nbsp;output_signal_update()</code> if
 * <code>Signal&nbsp;output_signal()</code> is overridden.
 * <p>
 * {@link Track} handles mono or stereo {@link Module}s. if a {@link Module} outputs a mono signal the output is
 * positioned via panning ( see {@link Module} <code>pan()</code> ). if a {@link Module} outputs a stereo signal the
 * output ignores panning and just uses the signal unchanged. if a {@link Module} outputs more than channels than a
 * stereo signal all additional channels are ignored.
 *
 * @see Module
 */
public class Track implements DSPNodeOutputSignal, Loopable {

    /*
     * a track allows to compose complex DSP configurations. it is a container that may be managed by a {@link Track}.
     * <p>
     * a track must implement the method <code>void&nbsp;output(Signal)</code> which supplies an audio signal and may
     * implement the method <code>void&nbsp;beat(int)</code> which can be used to receive beat events.
     */

    public static boolean VERBOSE = false;
    public final int ID;

    private float fVolume;
    private int fInPoint;
    private int fOutPoint;
    private int fLoop;
    private final Pan fPan;
    private static int oTrackUID;
    private final ArrayList<Track> mModules = new ArrayList<>();
    private int mBeat = SIGNAL_PROCESSING_IGNORE_IN_OUTPOINTS;

    public Track() {
        this(1.0f, NO_INPOINT, NO_OUTPOINT);
    }

    public Track(float volume, int in_point, int out_point) {
        fVolume = volume;
        fInPoint = in_point;
        fOutPoint = out_point;
        fLoop = NO_LOOP;
        fPan = new Pan();
        ID = oTrackUID++;
    }

    public ArrayList<Track> tracks() {
        return mModules;
    }

    /**
     * @param index index of track as stored in <code>track()</code>. note that this not to be confused with the final
     *              field <code>.ID</code> in {@link Module} which refers to a unique ID for each track ever created.
     * @return track stored at index
     */
    public Track track(int index) {
        return mModules.get(index);
    }

    /**
     * triggered by update mechanism. this method can be overridden to implement custom behavior.
     *
     * @param beat current beat count
     */
    public void beat(int beat) {
    }

    /**
     * updates everything related to beat functionality including internal mechanisms and child tracks. this method
     * also calls this object's own <code>beat(int)</code> method.
     *
     * @param beat current beat count
     */
    public void update(int beat) {
        beat(beat);
        mBeat = get_relative_position(beat);
        for (Track c : mModules) {
            if (evaluate_in_outpoints(c, mBeat)) {
                c.beat(mBeat);
                c.update(mBeat);
            }
        }
    }

    /**
     * callback method that accumulates audio signals from child tracks and if applicable maps mono signals into stereo
     * space. this method can be overridden to implement custom behavior, however, if doing so make sure to call
     * <code>Signal&nbsp;output_signal_update()</code> to collect signals from child tracks.
     *
     * @return Signal of this track
     */
    @Override
    public Signal output_signal() {
        return output_signal_update();
    }

    public Signal output_signal_update() {
        final Signal mSignalSum = new Signal();
        for (Track mModule : mModules) {
            if (mBeat == SIGNAL_PROCESSING_IGNORE_IN_OUTPOINTS || evaluate_in_outpoints(mModule, mBeat)) {
                Signal mModuleOutputSignal = mModule.output_signal();
                if (mModuleOutputSignal.num_channels() == SIGNAL_MONO) {
                    /* position mono signal in stereo space */
                    final float s = mModuleOutputSignal.mono();
                    mModuleOutputSignal = mModule.pan().process(s);
                    addSignalAndVolume(mSignalSum, mModule, mModuleOutputSignal);
                } else if (mModuleOutputSignal.num_channels() >= SIGNAL_STEREO) {
                    /* apply signal with 2 or more channels. additional channels are omitted */
                    addSignalAndVolume(mSignalSum, mModule, mModuleOutputSignal);
                    if (VERBOSE && mModuleOutputSignal.num_channels() > SIGNAL_STEREO) {
                        System.out.println("+++ track ID " + mModule.ID + " does not emit mono or stereo signal. " + "number of channels " + "is: " + mModuleOutputSignal.num_channels());
                    }
                }

                if (VERBOSE) {
                    System.out.println("+++ track ID " + mModule.ID + " does not emit a signal. number of channels " + "is: 0");
                }
            }
        }
        // @TODO applying volume to itself is redundant. should be handled by parent track.
        // mSignalSum.mult(volume);
        return mSignalSum;
    }


    public Pan pan() {
        return fPan;
    }

    /**
     * volume of track with 0.0 being no output and 1.0 being 100% of the signal output. note, that this value is
     * interpreted by parent {@link Track}s.
     *
     * @param volume track volume with 0.0 being no output and 1.0 being 100%
     */
    public void set_volume(float volume) {
        fVolume = volume;
    }

    /**
     * @return track volume with 0.0 being no output and 1.0 being 100%
     */
    public float get_volume() {
        return fVolume;
    }

    public void set_in_point(int in_point) {
        fInPoint = in_point;
    }

    @Override
    public int get_in_point() {
        return fInPoint;
    }

    public void set_out_point(int out_point) {
        fOutPoint = out_point;
    }

    @Override
    public int get_out_point() {
        return fOutPoint;
    }

    public void set_in_out_point(int in_point, int out_point) {
        fInPoint = in_point;
        fOutPoint = out_point;
    }

    public int get_length() {
        return fOutPoint - fInPoint + 1;
    }

    /**
     * set length of a track loop. setting the length affects outpoint. e.g given an inpoint of 3, setting the length
     * to 2 will set output to 4. note that outpoint is inclusive i.e a pattern from in- to outpoint of 3, 4, 5, … is
     * generated.
     *
     * @param length set length of loop
     */
    public void set_length(int length) {
        fOutPoint = fInPoint + length - 1;
    }

    public void set_loop(int loop) {
        fLoop = loop;
    }

    @Override
    public int get_loop() {
        return fLoop;
    }

    public int get_relative_position(int absolut_position) {
        return Loopable.get_relative_position(this, absolut_position);
    }

    public int get_loop_count(int absolut_position) {
        return Loopable.get_loop_count(this, absolut_position);
    }

    private static void addSignalAndVolume(Signal pSignalSum, Track pModule, Signal pSignal) {
        pSignalSum.left_add(pSignal.left() * pModule.get_volume());
        pSignalSum.right_add(pSignal.right() * pModule.get_volume());
    }

    private static boolean evaluate_in_outpoints(Track pTrack, int pBeat) {
        final boolean mNoInOutPoint = (pTrack.get_in_point() == NO_INPOINT && pTrack.get_out_point() == NO_OUTPOINT);
        if (mNoInOutPoint) {
            return true;
        }
        final boolean mIsBeyondInPoint = (pBeat >= pTrack.get_in_point());
        final int mLoopCount = pTrack.get_loop_count(pBeat);
        final boolean mIsBeforeOutPoint =
                (pBeat <= pTrack.get_out_point()) || (pTrack.get_out_point() == NO_OUTPOINT) || (mLoopCount < pTrack.get_loop() || pTrack.get_loop() == LOOP_INFINITE);
        //noinspection UnnecessaryLocalVariable
        final boolean mWithinInOutPoint = mIsBeyondInPoint && mIsBeforeOutPoint;
        return mWithinInOutPoint;
    }


    /* ---------------------------------- TEST ---------------------------------- */

    @SuppressWarnings("SpellCheckingInspection")
    private static final String TEST_RESULT_IDEAL =
            "BEAT\tRELATIV\tLOOP\n" + "---\n" + "2\t\t0\t\t0\n" + "2\t\t0\t" + "\t0\n" + "3\t\t1\t\t0\n" + "3\t\t1\t" + "\t0\n" + "4\t\t2\t\t0\n" + "4\t\t2\t\t0\n" + "5\t\t3\t\t0\n" + "5\t" + "\t3\t\t0\n" + "6\t\t0\t" + "\t1\n" + "6\t\t0\t\t1\n" + "7\t\t1\t\t1\n" + "7\t\t1\t\t1\n" + "8\t\t2\t\t1\n" + "8\t\t2\t\t1\n" + "9\t\t3\t\t1\n" + "9\t\t3\t\t1\n" + "10\t\t0\t\t2\n" + "10\t\t0\t\t2\n" + "11\t\t1\t\t2" + "\n" + "11\t\t1\t\t2\n" + "12\t\t2\t\t2\n" + "12\t\t2\t\t2\n" + "13\t\t3\t\t2\n" + "13\t\t3\t\t2\n" + "14" + "\t\t0\t\t3\n" + "14\t\t0\t\t3\n" + "15\t\t1\t\t3\n" + "15\t\t1\t\t3\n" + "---\n" + "2\t\t0\t\t-1\n" + "2" + "\t\t0\t\t-1\n" + "3\t\t1\t\t-1\n" + "3\t\t1\t\t-1\n" + "4\t\t2\t\t-1\n" + "4\t\t2\t\t-1\n" + "5\t\t3\t\t" + "-1\n" + "5\t\t3\t\t-1\n" + "---\n" + "11\t\t0\t\t-1\n" + "11\t\t0\t\t-1\n" + "12\t\t1\t\t-1\n" + "12\t" + "\t1\t\t-1\n" + "13\t\t2\t\t-1\n" + "13\t\t2\t\t-1\n" + "14\t\t3\t\t-1\n" + "14\t\t3\t\t-1\n" + "15\t\t4" + "\t\t-1\n" + "15\t\t4\t\t-1\n" + "---\n" + "0\t\t0\t\t-1\n" + "0\t\t0\t\t-1\n" + "1\t\t1\t\t-1\n" + "1\t" + "\t1\t\t-1\n" + "2\t\t2\t\t-1\n" + "2\t\t2\t\t-1\n" + "3\t\t3\t\t-1\n" + "3\t\t3\t\t-1\n" + "4\t\t4\t\t-1" + "\n" + "4\t\t4\t\t-1\n";

    private static String TEST_RESULT = "";

    public static void println(String s) {
        TEST_RESULT += s + "\n";
        System.out.println(s);
    }

    public static void main(String[] args) {
        Track.run_test();
    }

    public static void run_test() {
        Track t = new Track();
        Track d = new Track() {
            @Override
            public Signal output_signal() {
                return Signal.create(0);
            }

            public void beat(int beat) {
                println(beat + "\t\t" + get_relative_position(beat) + "\t\t" + get_loop_count(beat));
            }
        };

        t.tracks().add(d);

        //noinspection SpellCheckingInspection
        println("BEAT\tRELATIV\tLOOP");

        println("---");
        d.set_in_out_point(2, 5);
        d.fLoop = LOOP_INFINITE;
        for (int i = 0; i < 16; i++) {
            t.update(i);
        }

        println("---");
        d.set_in_point(2);
        d.set_out_point(5);
        d.fLoop = NO_LOOP;
        for (int i = 0; i < 16; i++) {
            t.update(i);
        }

        println("---");
        d.set_in_out_point(11, NO_OUTPOINT);
        d.fLoop = NO_LOOP;
        for (int i = 0; i < 16; i++) {
            t.update(i);
        }

        println("---");
        d.set_in_point(NO_INPOINT);
        d.set_length(5);
        d.fLoop = NO_LOOP;
        for (int i = 0; i < 16; i++) {
            t.update(i);
        }

        System.out.println("TEST SUCCESS: " + TEST_RESULT.equals(TEST_RESULT_IDEAL));
    }
}
