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

import java.util.ArrayList;

import static wellen.Wellen.LOOP_INFINITE;
import static wellen.Wellen.NO_INPOINT;
import static wellen.Wellen.NO_OUTPOINT;
import static wellen.Wellen.SIGNAL_MONO;
import static wellen.Wellen.SIGNAL_PROCESSING_IGNORE_IN_OUTPOINTS;
import static wellen.Wellen.SIGNAL_STEREO;

/**
 * manages a collection of {@link DSPModule}s. each child module processes audio signals which are accumulated by the
 * track. furthermore, {@link DSPTrack} calls its child modules <pre><code>void&nbsp;beat(int)</code></pre> method.
 * <p>
 * note, that since {@link DSPTrack} implements {@link DSPModule} it can also be added as a module to another track. if
 * a class is derived from {@link DSPTrack} and <pre><code>beat(int)</code></pre> is overridden make sure to call
 * <pre><code>beat_update(int)</code></pre> to preserve internal functionality and update for child {@link DSPModule}s.
 * similarly, make sure to call <pre><code>Signal&nbsp;output_signal_update()</code></pre> if
 * <pre><code>Signal&nbsp;output_signal()</code></pre> is overridden.
 * <p>
 * {@link DSPTrack} handles mono or stereo {@link DSPModule}s. if a {@link DSPModule} outputs a mono signal the output
 * is positioned via panning ( see {@link DSPModule} <pre><code>pan()</code></pre> ). if a {@link DSPModule} outputs a
 * stereo signal the output ignores panning and just uses the signal unchanged. if a {@link DSPModule} outputs more than
 * channels than a stereo signal all additional channels are ignored.
 *
 * @see wellen.DSPModule
 */
public class DSPTrack extends DSPModule {
    public static boolean VERBOSE = false;

    private final ArrayList<DSPModule> mModules = new ArrayList<>();
    private int mBeat = SIGNAL_PROCESSING_IGNORE_IN_OUTPOINTS;

    public ArrayList<DSPModule> modules() {
        return mModules;
    }

    /**
     * @param pIndex index of track as stored in <pre><code>modules()</code></pre>. note that this not to be confused
     *               with the final field <pre><code>.ID</code></pre> in {@link DSPModule} which refers to a unique ID
     *               for each module ever created.
     * @return module stored at index
     */
    public DSPModule module(int pIndex) {
        return mModules.get(pIndex);
    }

    /**
     * triggered by beat mechanism. this method can be overridden to implement custom behavior, however, if doing so
     * make sure to call <pre><code>void&nbsp;beat_update(int)</code></pre> to pass beat event onto child modules.
     *
     * @param pBeat current beat count
     */
    @Override
    public void beat(int pBeat) {
        beat_update(pBeat);
    }

    /**
     * updates everything related to beat functionality including internal mechanisms and child modules.
     *
     * @param pBeat current beat count
     */
    public void beat_update(int pBeat) {
        mBeat = get_relative_position(pBeat);
        for (DSPModule c : mModules) {
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
        for (DSPModule mModule : mModules) {
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
                        System.out.println(
                        "+++ module ID " + mModule.ID + " does not emit mono or stereo signal. number of channels " + "is: " + mModuleOutputSignal.num_channels());
                    }
                }

                if (VERBOSE) {
                    System.out.println(
                    "+++ module ID " + mModule.ID + " does not emit a signal. number of channels is: 0");
                }
            }
        }
        // @TODO applying volume to itself is redundant. should be handled by parent track.
        // mSignalSum.mult(volume);
        return mSignalSum;
    }

    private static void addSignalAndVolume(Signal pSignalSum, DSPModule pModule, Signal pSignal) {
        pSignalSum.left_add(pSignal.left() * pModule.get_volume());
        pSignalSum.right_add(pSignal.right() * pModule.get_volume());
    }

    private static boolean evaluate_in_outpoints(DSPModule pTrack, int pBeat) {
        final boolean mNoInOutPoint = (pTrack.get_inpoint() == NO_INPOINT && pTrack.get_outpoint() == NO_OUTPOINT);
        if (mNoInOutPoint) {
            return true;
        }
        final boolean mIsBeyondInPoint = (pBeat >= pTrack.get_inpoint());
        final int mLoopCount = pTrack.get_loop_count(pBeat);
        final boolean mIsBeforeOutPoint =
        (pBeat <= pTrack.get_outpoint()) || (pTrack.get_outpoint() == NO_OUTPOINT) || (mLoopCount < pTrack.get_loop() || pTrack.get_loop() == LOOP_INFINITE);
        //noinspection UnnecessaryLocalVariable
        final boolean mWithinInOutPoint = mIsBeyondInPoint && mIsBeforeOutPoint;
        return mWithinInOutPoint;
    }
}
