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

import static wellen.Wellen.NO_LOOP;
import static wellen.Wellen.NO_LOOP_COUNT;
import static wellen.Wellen.NO_OUTPOINT;

public interface Loopable {
    int get_loop();
    int get_in_point();
    int get_out_point();

    static int get_relative_position(Loopable p, int pAbsolutPosition) {
        return Loopable.getRelativePositionOrLoopCount(p, pAbsolutPosition, true);
    }

    static int get_loop_count(Loopable p, int pAbsolutPosition) {
        return Loopable.getRelativePositionOrLoopCount(p, pAbsolutPosition, false);
    }

    private static int getRelativePositionOrLoopCount(Loopable p, int pAbsolutPosition, boolean pGetRelativePosition) {
        if (p.get_loop() == NO_LOOP || p.get_out_point() == NO_OUTPOINT) {
            return pGetRelativePosition ? (pAbsolutPosition - p.get_in_point()) : NO_LOOP_COUNT;
        } else {
            //noinspection ManualMinMaxCalculation
            int mSanitizedInPoint = (p.get_in_point() < 0 ? 0 : p.get_in_point());
            int mTrackDuration = 1 + p.get_out_point() - mSanitizedInPoint;
            if (mTrackDuration > 0) {
                int mRelativePosition = (pAbsolutPosition - mSanitizedInPoint);
                if (pGetRelativePosition) {
                    if (mRelativePosition >= 0) {
                        mRelativePosition %= mTrackDuration;
                    }
                    return mRelativePosition;
                } else {
                    if (mRelativePosition < 0) {
                        return NO_LOOP_COUNT;
                    } else {
                        //noinspection UnnecessaryLocalVariable
                        final int mLoopCount = mRelativePosition / mTrackDuration;
                        if (mLoopCount >= p.get_loop()) {
                            return NO_LOOP_COUNT;
                        } else {
                            return mLoopCount;
                        }
                    }
                }
            }
        }
        return pGetRelativePosition ? pAbsolutPosition : 0;
    }
}