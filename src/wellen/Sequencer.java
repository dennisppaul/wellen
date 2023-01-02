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

/**
 * a container for values ( e.g musical notes ) that can be played back step by step.
 *
 * @param <T> type of sequenced data. usually, <code>int</code> or <code>float</code> but could be also be a complex
 *            data types.
 */
public class Sequencer<T> {

    // @TODO(should `T` be limited to `Number` e.g `T extends Number` )

    private final T[] fSequence;
    private int fStep;

    public Sequencer(int steps) {
        Float f;
        fSequence = (T[]) new Object[steps];
        reset();
    }

    @SafeVarargs
    public Sequencer(T... data) {
        fSequence = data;
        reset();
    }

    public T[] data() {
        return fSequence;
    }

    public T step() {
        fStep++;
        fStep %= fSequence.length;
        final T mValue = fSequence[fStep];
        return mValue;
    }

    public T get_current() {
        if (fStep >= 0 && fStep < fSequence.length) {
            return fSequence[fStep];
        } else {
            return fSequence[0];
        }
    }

    public void set_current(T value) {
        set(fStep, value);
    }

    public void set(int step, T value) {
        if (step >= 0 && step < fSequence.length) {
            fSequence[step] = value;
        }
    }

    public T get(int step) {
        if (step >= 0 && step < fSequence.length) {
            return fSequence[step];
        } else {
            return fSequence[0];
        }
    }

    public void reset() {
        fStep = -1;
    }

    public void set_step(int step) {
        fStep = step;
    }

    public int get_step() {
        return fStep;
    }
}
