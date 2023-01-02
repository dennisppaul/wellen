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
 * a {@link Track} allows to compose complex compositional and DSP configurations. a {@link Track} may be added to other
 * {@link Track}s.
 * <p>
 * {@link Track} must implement the method <code>void&nbsp;output(Signal)</code> which delivers an audio signal and it
 * may implement the method <code>void&nbsp;beat(int)</code> which can be used to receive beat events. a {@link Track}
 * may manage a collection of child {@link Track}s. each child track processes audio signals which are accumulated by
 * the track. furthermore, {@link Track} calls its child tracks <code>void&nbsp;beat(int)</code> method.
 * <p>
 * if a class is derived from {@link Track} and <code>update(int)</code> is overridden make sure to call
 * <code>beat(int)</code> to preserve internal functionality and update for child {@link Track}s.
 * similarly, make sure to call <code>Signal&nbsp;output_signal_update()</code> if
 * <code>Signal&nbsp;output_signal()</code> is overridden.
 * <p>
 * {@link Track} may handle other mono or stereo {@link Track}s. if a {@link Track} outputs a mono signal the output is
 * positioned via panning ( see {@link Track} <code>pan()</code> ). if a {@link Track} outputs a stereo signal the
 * output ignores panning and just uses the signal unchanged. if a {@link Track} outputs more than channels than a
 * stereo signal all additional channels are ignored.
 */
public class Track implements DSPNodeOutputSignal, Loopable {

    public static boolean VERBOSE = false;
    public final int ID;

    private float fVolume;
    private int fInPoint;
    private int fOutPoint;
    private int fLoop;
    private final Pan fPan;
    private static int oTrackUID;
    private final ArrayList<Track> mTracks = new ArrayList<>();
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
        return mTracks;
    }

    public void add_track(Track track) {
        mTracks.add(track);
    }

    /**
     * @param index index of track as stored in <code>track()</code>. note that this not to be confused with the final
     *              field <code>.ID</code> in {@link Track} which refers to a unique ID for each track ever created.
     * @return track stored at index
     */
    public Track track(int index) {
        return mTracks.get(index);
    }

    /**
     * triggered by update mechanism. this method can be overridden to implement custom behavior.
     *
     * @param beat_absolute current absolute beat count
     * @param beat_relative current relative beat count
     */
    public void beat(int beat_absolute, int beat_relative) {
    }

    /**
     * updates everything related to beat functionality including internal mechanisms and child tracks. this method also
     * calls this object's own <code>beat(int)</code> method. the order in which methods are called is as follows:
     * <p>
     * <ul>
     *     <li><code>beat(int)</code></li>
     *     <li>child <code>beat(int)</code></li>
     *     <li>child <code>update(int)</code></li>
     * </ul>
     *
     * @param beat_absolute current absolute beat count. by default this is the global beat count that is passed to the
     *                      very first {@link Track} in the composition.
     * @param beat_relative current relative beat count
     */
    public void update(int beat_absolute, int beat_relative) {
        beat(beat_absolute, beat_relative);
        mBeat = get_relative_position(beat_relative);
        for (Track c : mTracks) {
            if (evaluate_in_outpoints(c, mBeat)) {
                c.update(beat_absolute, mBeat);
            }
        }
    }

    public void update(int beat) {
        update(beat, beat);
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
        for (Track mTrack : mTracks) {
            if (mBeat == SIGNAL_PROCESSING_IGNORE_IN_OUTPOINTS || evaluate_in_outpoints(mTrack, mBeat)) {
                Signal mTrackOutputSignal = mTrack.output_signal();
                if (mTrackOutputSignal.num_channels() == SIGNAL_MONO) {
                    /* position mono signal in stereo space */
                    final float s = mTrackOutputSignal.mono();
                    mTrackOutputSignal = mTrack.pan().process(s);
                    addSignalAndVolume(mSignalSum, mTrack, mTrackOutputSignal);
                } else if (mTrackOutputSignal.num_channels() >= SIGNAL_STEREO) {
                    /* apply signal with 2 or more channels. additional channels are omitted */
                    addSignalAndVolume(mSignalSum, mTrack, mTrackOutputSignal);
                    if (VERBOSE && mTrackOutputSignal.num_channels() > SIGNAL_STEREO) {
                        System.out.println("+++ track ID " + mTrack.ID + " does not emit mono or stereo signal. " +
                                                   "number of channels " + "is: " + mTrackOutputSignal.num_channels());
                    }
                } else {
                    if (VERBOSE) {
                        System.out.println("+++ track ID " + mTrack.ID + " does not emit a signal. number of " +
                                                   "channels" + " " + "is: 0");
                    }
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

    /**
     * @param in_point in point in beats. included in length count i.e an in point of 2 and an out point of 5 will *
     *                 result in a duration of 4 beats ( 2, 3, 4, 5 ).
     */
    public void set_in_point(int in_point) {
        fInPoint = in_point;
    }

    @Override
    public int get_in_point() {
        return fInPoint;
    }

    /**
     * @param out_point out point in beats. included in length count i.e an in point of 2 and an out point of 5 will
     *                  result in a duration of 4 beats ( 2, 3, 4, 5 ).
     */
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
     * set length of a track loop. setting the length affects outpoint. e.g given an inpoint of 3, setting the length to
     * 2 will set output to 4. note that outpoint is inclusive i.e a pattern from in- to outpoint of 3, 4, 5, … is
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

    private static void addSignalAndVolume(Signal pSignalSum, Track pTrack, Signal pSignal) {
        pSignalSum.left_add(pSignal.left() * pTrack.get_volume());
        pSignalSum.right_add(pSignal.right() * pTrack.get_volume());
    }

    private static boolean evaluate_in_outpoints(Track pTrack, int beat) {
        final boolean mNoInOutPoint = (pTrack.get_in_point() == NO_INPOINT && pTrack.get_out_point() == NO_OUTPOINT);
        if (mNoInOutPoint) {
            return true;
        }
        final boolean mIsBeyondInPoint = (beat >= pTrack.get_in_point());
        final int mLoopCount = pTrack.get_loop_count(beat);
        final boolean mIsBeforeOutPoint =
                (beat <= pTrack.get_out_point()) || (pTrack.get_out_point() == NO_OUTPOINT) || (mLoopCount < pTrack.get_loop() || pTrack.get_loop() == LOOP_INFINITE);
        //noinspection UnnecessaryLocalVariable
        final boolean mWithinInOutPoint = mIsBeyondInPoint && mIsBeforeOutPoint;
        return mWithinInOutPoint;
    }


    /* ---------------------------------- TEST ---------------------------------- */

    @SuppressWarnings("SpellCheckingInspection")
    private static final String TEST_RESULT_IDEAL =
            "ROLE\tGLOBAL\tBEAT\tRELATIV\tLOOP\n" + "--- LOOP:INFINITE, IN: " + "2, OUT:5 ---\n" + "SERVER\t0\n" +
                    "SERVER\t1\n" + "SERVER\t2\n" + "CHILD\t2\t\t2\t\t0\t\t0\n" + "SERVER" + "\t3\n" + "CHILD\t3\t\t3"
                    + "\t\t1\t\t0\n" + "SERVER\t4\n" + "CHILD\t4\t\t4\t\t2\t\t0\n" + "SERVER\t5\n" + "CHILD\t5\t\t5\t"
                    + "\t3\t\t0\n" + "SERVER\t6\n" + "CHILD\t6\t\t6\t\t0\t\t1\n" + "SERVER\t7\n" + "CHILD\t7\t" +
                    "\t7" + "\t\t1\t\t1\n" + "SERVER\t8\n" + "CHILD\t8\t\t8\t\t2\t\t1\n" + "SERVER\t9\n" + "CHILD\t9" + "\t\t9\t" + "\t3\t" + "\t1\n" + "SERVER\t10\n" + "CHILD\t10\t\t10\t\t0\t\t2\n" + "SERVER\t11\n" + "CHILD\t11" + "\t\t11\t\t1\t\t2\n" + "SERVER\t12\n" + "CHILD\t12\t\t12\t\t2\t\t2\n" + "SERVER\t13\n" + "CHILD" + "\t13\t\t13\t\t3\t\t2\n" + "SERVER\t14\n" + "CHILD\t14\t\t14\t\t0\t\t3\n" + "SERVER\t15\n" + "CHILD\t15\t\t15\t\t1\t\t3\n" + "--- LOOP:NO, IN:2, OUT:5 ---\n" + "SERVER\t0\n" + "SERVER\t1\n" + "SERVER\t2\n" + "CHILD\t2\t\t2\t\t0\t\t-1\n" + "SERVER\t3\n" + "CHILD\t3\t\t3\t\t1\t\t-1\n" + "SERVER\t4\n" + "CHILD\t4\t\t4\t\t2\t\t-1\n" + "SERVER\t5\n" + "CHILD\t5\t\t5\t\t3\t\t-1\n" + "SERVER\t6\n" + "SERVER\t7\n" + "SERVER\t8\n" + "SERVER\t9\n" + "SERVER\t10\n" + "SERVER\t11\n" + "SERVER\t12\n" + "SERVER\t13\n" + "SERVER\t14\n" + "SERVER\t15\n" + "--- LOOP:NO, IN:11, OUT:NO ---\n" + "SERVER\t0\n" + "SERVER\t1\n" + "SERVER\t2\n" + "SERVER\t3\n" + "SERVER\t4\n" + "SERVER\t5\n" + "SERVER\t6\n" + "SERVER\t7\n" + "SERVER\t8\n" + "SERVER\t9\n" + "SERVER\t10\n" + "SERVER\t11\n" + "CHILD\t11\t\t11\t\t0\t\t-1\n" + "SERVER\t12\n" + "CHILD\t12\t\t12\t\t1\t\t-1\n" + "SERVER\t13\n" + "CHILD\t13\t\t13\t\t2\t\t-1\n" + "SERVER\t14\n" + "CHILD\t14\t\t14\t\t3\t\t-1\n" + "SERVER\t15\n" + "CHILD\t15\t\t15\t\t4\t\t-1\n" + "--- LOOP:NO, IN:0, OUT:4 ---\n" + "SERVER\t0\n" + "CHILD\t0\t\t0\t\t0\t\t-1\n" + "SERVER\t1\n" + "CHILD\t1\t\t1\t\t1\t\t-1\n" + "SERVER\t2\n" + "CHILD\t2\t\t2\t\t2\t\t-1\n" + "GRANDCH\t2\t\t2\t\t0\t\t-1\n" + "SERVER\t3\n" + "CHILD\t3\t\t3\t\t3\t\t-1\n" + "GRANDCH\t3\t\t3\t\t1\t\t-1\n" + "SERVER\t4\n" + "CHILD\t4\t\t4\t\t4\t\t-1\n" + "GRANDCH\t4\t\t4\t\t2\t\t-1\n" + "SERVER\t5\n" + "SERVER\t6\n" + "SERVER\t7\n" + "SERVER\t8\n" + "SERVER\t9\n" + "SERVER\t10\n" + "SERVER\t11\n" + "SERVER\t12\n" + "SERVER\t13\n" + "SERVER\t14\n" + "SERVER\t15\n";

    private static String TEST_RESULT = "";

    public static void println(String s) {
        TEST_RESULT += s + "\n";
        System.out.println(s);
    }

    public static void run_test() {
        Track mServerTrack = new Track() {
            public void beat(int beat_absolute, int beat_relative) {
                println("SERVER\t" + beat_absolute);
            }
        };
        Track mChildTrack = new Track() {
            @Override
            public Signal output_signal() {
                return Signal.create(0);
            }

            public void beat(int beat_absolute, int beat_relative) {
                println("CHILD\t" + beat_absolute + "\t\t" + beat_relative + "\t\t" + get_relative_position(
                        beat_relative) + "\t\t" + get_loop_count(beat_relative));
            }
        };

        Track mGrandChildTrack = new Track() {
            @Override
            public Signal output_signal() {
                return Signal.create(0);
            }

            public void beat(int beat_absolute, int beat_relative) {
                println("GRANDCH\t" + beat_absolute + "\t\t" + beat_relative + "\t\t" + get_relative_position(
                        beat_relative) + "\t\t" + get_loop_count(beat_relative));
            }
        };

        mServerTrack.tracks().add(mChildTrack);

        //noinspection SpellCheckingInspection
        println("ROLE\tGLOBAL\tBEAT\tRELATIV\tLOOP");

        println("--- LOOP:INFINITE, IN: 2, OUT:5 ---");
        mChildTrack.set_in_out_point(2, 5);
        mChildTrack.fLoop = LOOP_INFINITE;
        for (int i = 0; i < 16; i++) {
            mServerTrack.update(i);
        }

        println("--- LOOP:NO, IN:2, OUT:5 ---");
        mChildTrack.set_in_point(2);
        mChildTrack.set_out_point(5);
        mChildTrack.fLoop = NO_LOOP;
        for (int i = 0; i < 16; i++) {
            mServerTrack.update(i);
        }

        println("--- LOOP:NO, IN:11, OUT:NO ---");
        mChildTrack.set_in_out_point(11, NO_OUTPOINT);
        mChildTrack.fLoop = NO_LOOP;
        for (int i = 0; i < 16; i++) {
            mServerTrack.update(i);
        }

        println("--- LOOP:NO, IN:0, OUT:4 ---");
        mChildTrack.set_in_point(NO_INPOINT);
        mChildTrack.set_length(5);
        mChildTrack.fLoop = NO_LOOP;
        mChildTrack.add_track(mGrandChildTrack);
        mGrandChildTrack.set_in_out_point(2, 5);
        for (int i = 0; i < 16; i++) {
            mServerTrack.update(i);
        }

        System.out.println("TEST SUCCESS: " + TEST_RESULT.equals(TEST_RESULT_IDEAL));
    }

//    public static void main(String[] args) {
//        Track.run_test();
//    }
}
