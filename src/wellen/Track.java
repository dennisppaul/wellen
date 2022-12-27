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

import wellen.dsp.Signal;

import java.util.ArrayList;

import static wellen.Wellen.LOOP_INFINITE;
import static wellen.Wellen.NO_INPOINT;
import static wellen.Wellen.NO_OUTPOINT;
import static wellen.Wellen.SIGNAL_MONO;
import static wellen.Wellen.SIGNAL_PROCESSING_IGNORE_IN_OUTPOINTS;
import static wellen.Wellen.SIGNAL_STEREO;

/**
 * manages a collection of {@link Module}s. each child module processes audio signals which are accumulated by the
 * track. furthermore, {@link Track} calls its child modules <code>void&nbsp;beat(int)</code> method.
 * <p>
 * note, that since {@link Track} implements {@link Module} it can also be added as a module to another track. if a
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
public class Track extends Module {
    public static boolean VERBOSE = false;

    private final ArrayList<Module> mModules = new ArrayList<>();
    private int mBeat = SIGNAL_PROCESSING_IGNORE_IN_OUTPOINTS;

    public ArrayList<Module> modules() {
        return mModules;
    }

    /**
     * @param index index of track as stored in <pre><code>modules()</code></pre>. note that this not to be confused
     *              with the final field <pre><code>.ID</code></pre> in {@link Module} which refers to a unique ID for
     *              each module ever created.
     * @return module stored at index
     */
    public Module module(int index) {
        return mModules.get(index);
    }

    /**
     * triggered by beat mechanism. this method can be overridden to implement custom behavior, however, if doing so
     * make sure to call <pre><code>void&nbsp;beat_update(int)</code></pre> to pass beat event onto child modules.
     *
     * @param beat current beat count
     */
    @Override
    public void beat(int beat) {
        beat_update(beat);
    }

    /**
     * updates everything related to beat functionality including internal mechanisms and child modules.
     *
     * @param beat current beat count
     */
    public void beat_update(int beat) {
        mBeat = get_relative_position(beat);
        for (Module c : mModules) {
            if (evaluate_in_outpoints(c, mBeat)) {
                c.beat(mBeat);
            }
        }
    }

    /**
     * callback method that accumulates audio signals from child modules and if applicable maps mono signals into stereo
     * space. this method can be overridden to implement custom behavior, however, if doing so make sure to call
     * <pre><code>Signal&nbsp;output_signal_update()</code></pre> to collect signals from child modules.
     *
     * @return Signal of this track
     */
    @Override
    public Signal output_signal() {
        return output_signal_update();
    }

    public Signal output_signal_update() {
        final Signal mSignalSum = new Signal();
        for (Module mModule : mModules) {
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
                        System.out.println("+++ module ID " + mModule.ID + " does not emit mono or stereo signal. " + "number of channels " + "is: " + mModuleOutputSignal.num_channels());
                    }
                }

                if (VERBOSE) {
                    System.out.println("+++ module ID " + mModule.ID + " does not emit a signal. number of channels " + "is: 0");
                }
            }
        }
        // @TODO applying volume to itself is redundant. should be handled by parent track.
        // mSignalSum.mult(volume);
        return mSignalSum;
    }

    private static void addSignalAndVolume(Signal pSignalSum, Module pModule, Signal pSignal) {
        pSignalSum.left_add(pSignal.left() * pModule.get_volume());
        pSignalSum.right_add(pSignal.right() * pModule.get_volume());
    }

    private static boolean evaluate_in_outpoints(Module pTrack, int pBeat) {
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
}
