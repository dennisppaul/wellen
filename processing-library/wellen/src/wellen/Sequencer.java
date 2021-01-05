/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2020 Dennis P Paul.
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

/**
 * a container for values ( e.g musical notes ) that can be played back step by step.
 *
 * @param <T>
 */
public class Sequencer<T> {

    // @TODO(should `T` be limited to `Number` e.g `T extends Number` )

    private final T[] mSequence;
    private int mStep;

    public Sequencer(int pSteps) {
        mSequence = (T[]) new Object[pSteps];
        reset();
    }

    @SafeVarargs
    public Sequencer(T... pData) {
        mSequence = pData;
        reset();
    }

    public T[] data() {
        return mSequence;
    }

    public T step() {
        final T mValue = mSequence[mStep];
        mStep++;
        mStep %= mSequence.length;
        return mValue;
    }

    public T get_current() {
        return mSequence[mStep];
    }

    public void set_current(T pValue) {
        set(mStep, pValue);
    }

    public void set(int pStep, T pValue) {
        if (pStep >= 0 && pStep < mSequence.length) {
            mSequence[pStep] = pValue;
        }
    }

    public T get(int pStep) {
        if (pStep >= 0 && pStep < mSequence.length) {
            return mSequence[pStep];
        } else {
            return mSequence[0];
        }
    }

    public void reset() {
        mStep = 0;
    }

    public int get_step() {
        return mStep;
    }
}
