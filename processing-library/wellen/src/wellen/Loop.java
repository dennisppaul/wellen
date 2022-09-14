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

import static wellen.Wellen.NO_LOOP_COUNT;

public class Loop {

    protected int fLength = 0;
    protected int fOffset = 0;

    public int get_length() {
        return fLength;
    }

    public void set_length(int pLength) {
        fLength = pLength;
    }

    public int get_offset() {
        return -fOffset;
    }

    public void set_offset(int pOffset) {
        fOffset = -pOffset;
    }

    public static boolean event(int pTick, int pLoopEvent, int pLoopInterval) {
        if (pLoopInterval == 0) {
            return false;
        }
        return (pTick % pLoopInterval) == pLoopEvent;
    }

    public boolean event(int pTick, int pLoopEvent) {
        if (fLength == 0) {
            return false;
        }
        return (getTickWithOffset(pTick) % fLength) == pLoopEvent;
    }

    public static boolean before(int pTick, int pThreshold, int pLoopInterval) {
        if (pLoopInterval == 0) {
            return false;
        }
        return (pTick % pLoopInterval) < pThreshold;
    }

    public boolean before(int pTick, int pThreshold) {
        if (fLength == 0) {
            return false;
        }
        return (getTickWithOffset(pTick) % fLength) < pThreshold;
    }


    public static boolean after(int pTick, int pThreshold, int pLoopInterval) {
        if (pLoopInterval == 0) {
            return false;
        }
        return (pTick % pLoopInterval) > pThreshold;
    }

    public boolean after(int pTick, int pThreshold) {
        if (fLength == 0) {
            return false;
        }
        return (getTickWithOffset(pTick) % fLength) > pThreshold;
    }

    private int getTickWithOffset(int pTick) {
        return pTick + fOffset;
    }

    public int get_loop_count(int pAbsolutPosition) {
        if (fLength > 0) {
            if (pAbsolutPosition + fOffset >= 0) {
                return (pAbsolutPosition + fOffset) / fLength;
            } else {
                return NO_LOOP_COUNT;
            }
        } else {
            return NO_LOOP_COUNT;
        }
    }

    public static void main(String[] args) {
        Loop l = new Loop();

        System.out.println("CNT\tREL\tEVT\tLOP");

        System.out.println("---");
        l.set_length(3);
        for (int i = 0; i < 10; i++) {
            System.out.print(i + "\t");
            System.out.print("\t");
            System.out.print((l.event(i, 0) ? "+" : "-") + "\t");
            System.out.print(l.get_loop_count(i) + "\t");
            System.out.println();
        }

        System.out.println("---");
        l.set_length(5);
        l.set_offset(2);
        for (int i = 0; i < 10; i++) {
            System.out.print(i + "\t");
            System.out.print("\t");
            System.out.print((l.event(i, 0) ? "+" : "-") + "\t");
            System.out.print(l.get_loop_count(i) + "\t");
            System.out.println();
        }
    }
}
