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

public class Loop {

    private int fLength = 0;
    private int fTick = 0;

    public int get_length() {
        return fLength;
    }

    public void set_length(int pLength) {
        fLength = pLength;
    }

    public int get_tick() {
        return fTick;
    }

    public void set_tick(int pTick) {
        fTick = pTick;
    }

    public static boolean event(int pCounter, int pLoopEvent, int pLoopInterval) {
        if (pLoopInterval == 0) {
            return false;
        }
        return (pCounter % pLoopInterval) == pLoopEvent;
    }

    public boolean event(int pCounter, int pLoopEvent) {
        fTick = pCounter;
        if (fLength == 0) {
            return false;
        }
        return (fTick % fLength) == pLoopEvent;
    }

    public boolean event(int pLoopEvent) {
        if (fLength == 0) {
            return false;
        }
        return (fTick % fLength) == pLoopEvent;
    }

    public static boolean before(int pCounter, int pThreshold, int pLoopInterval) {
        if (pLoopInterval == 0) {
            return false;
        }
        return (pCounter % pLoopInterval) < pThreshold;
    }

    public boolean before(int pCounter, int pThreshold) {
        fTick = pCounter;
        if (fLength == 0) {
            return false;
        }
        return (fTick % fLength) < pThreshold;
    }

    public boolean before(int pThreshold) {
        if (fLength == 0) {
            return false;
        }
        return (fTick % fLength) < pThreshold;
    }

    public static boolean after(int pCounter, int pThreshold, int pLoopInterval) {
        if (pLoopInterval == 0) {
            return false;
        }
        return (pCounter % pLoopInterval) > pThreshold;
    }

    public boolean after(int pCounter, int pThreshold) {
        fTick = pCounter;
        if (fLength == 0) {
            return false;
        }
        return (fTick % fLength) > pThreshold;
    }

    public boolean after(int pThreshold) {
        if (fLength == 0) {
            return false;
        }
        return (fTick % fLength) > pThreshold;
    }
}
