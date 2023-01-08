/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2023 Dennis P Paul.
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

import static wellen.Wellen.SIGNAL_MONO;
import static wellen.Wellen.SIGNAL_STEREO;

/**
 * a {@link Patch} allows to compose complex DSP configurations. a {@link Patch} may be added to other {@link Patch}es.
 * NOTE this class is not tested yet …
 */
public class Patch implements DSPNodeOutputSignal {

    public static boolean VERBOSE = false;
    private static int oPatchUID;
    public final int ID;
    private final Pan fPan;
    private float fVolume;
    private final ArrayList<Patch> mPatchs = new ArrayList<>();

    public Patch() {
        this(1.0f);
    }

    public Patch(float volume) {
        fVolume = volume;
        fPan = new Pan();
        ID = oPatchUID++;
    }

    private static void addSignalAndVolume(Signal pSignalSum, Patch pPatch, Signal pSignal) {
        pSignalSum.left_add(pSignal.left() * pPatch.get_volume());
        pSignalSum.right_add(pSignal.right() * pPatch.get_volume());
    }

    public ArrayList<Patch> patchs() {
        return mPatchs;
    }

    /**
     * @param index index of patch as stored in <code>patch()</code>. note that this not to be confused with the final
     *              field <code>.ID</code> in {@link Patch} which refers to a unique ID for each patch ever created.
     * @return patch stored at index
     */
    public Patch patch(int index) {
        return mPatchs.get(index);
    }

    /**
     * callback method that accumulates audio signals from child patchs and if applicable maps mono signals into stereo
     * space. this method can be overridden to implement custom behavior, however, if doing so make sure to also call
     * <code>Signal&nbsp;output_signal_update()</code> to collect signals from child patchs.
     *
     * @return signal of this patch
     */
    @Override
    public Signal output_signal() {
        return output_signal_update();
    }

    /**
     * accumulates signals from child patchs and if applicable maps mono signals into stereo space.
     *
     * @return accumulated signal of child patches
     */
    public Signal output_signal_update() {
        final Signal mSignalSum = new Signal();
        for (Patch mPatch : mPatchs) {
            Signal mPatchOutputSignal = mPatch.output_signal();
            if (mPatchOutputSignal.num_channels() == SIGNAL_MONO) {
                /* position mono signal in stereo space */
                final float s = mPatchOutputSignal.mono();
                mPatchOutputSignal = mPatch.pan().process(s);
                addSignalAndVolume(mSignalSum, mPatch, mPatchOutputSignal);
            } else if (mPatchOutputSignal.num_channels() >= SIGNAL_STEREO) {
                /* apply signal with 2 or more channels. additional channels are omitted */
                addSignalAndVolume(mSignalSum, mPatch, mPatchOutputSignal);
                if (VERBOSE && mPatchOutputSignal.num_channels() > SIGNAL_STEREO) {
                    System.out.println("+++ patch ID " + mPatch.ID + " does not emit mono or stereo signal. " +
                                               "number of channels " + "is: " + mPatchOutputSignal.num_channels());
                }
            } else {
                if (VERBOSE) {
                    System.out.println("+++ track ID " + mPatch.ID + " does not emit a signal. number of channels " + "is: 0");
                }
            }
        }
        // @TODO applying volume to itself is redundant. should be handled by parent patch.
        // mSignalSum.mult(volume);
        return mSignalSum;
    }

    public Pan pan() {
        return fPan;
    }

    /**
     * @return patch volume with 0.0 being no output and 1.0 being 100%
     */
    public float get_volume() {
        return fVolume;
    }

    /**
     * volume of patch with 0.0 being no output and 1.0 being 100% of the signal output. note, that this value is
     * interpreted by parent {@link Patch}s.
     *
     * @param volume patch volume with 0.0 being no output and 1.0 being 100%
     */
    public void set_volume(float volume) {
        fVolume = volume;
    }
}
