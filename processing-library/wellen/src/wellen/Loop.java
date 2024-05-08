/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2024 Dennis P Paul.
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

    public static boolean after(int beat, int threshold, int interval) {
        if (interval == 0) {
            return false;
        }
        return (beat % interval) > threshold;
    }

    public static boolean before(int beat, int threshold, int interval) {
        if (interval == 0) {
            return false;
        }
        return (beat % interval) < threshold;
    }

    public static boolean event(int beat, int loop_event, int interval) {
        if (interval == 0) {
            return false;
        }
        return (beat % interval) == loop_event;
    }

    private static void run_test() {
        Loop l = new Loop();

        System.out.println("CNT\tREL\tEVT\tLOP");
        System.out.println("--- LENGTH: 3");
        l.set_length(3);
        for (int i = 0; i < 10; i++) {
            System.out.print(i + "\t");
            System.out.print("\t");
            System.out.print((l.event(i, 0) ? "+" : "-") + "\t");
            System.out.print(l.get_loop_count(i) + "\t");
            System.out.println();
        }

        System.out.println("--- LENGTH: 3 OFFSET: 2");
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

    public int get_length() {
        return fLength;
    }

    public void set_length(int length) {
        fLength = length;
    }

    public int get_offset() {
        return -fOffset;
    }

    public void set_offset(int offset) {
        fOffset = -offset;
    }

    public boolean event(int beat, int event_at) {
        if (fLength == 0) {
            return false;
        }
        return (getTickWithOffset(beat) % fLength) == event_at;
    }

    public boolean before(int beat, int threshold) {
        if (fLength == 0) {
            return false;
        }
        return (getTickWithOffset(beat) % fLength) < threshold;
    }

    public boolean after(int beat, int threshold) {
        if (fLength == 0) {
            return false;
        }
        return (getTickWithOffset(beat) % fLength) > threshold;
    }

    public int get_loop_count(int absolut_position) {
        if (fLength > 0) {
            if (absolut_position + fOffset >= 0) {
                return (absolut_position + fOffset) / fLength;
            } else {
                return NO_LOOP_COUNT;
            }
        } else {
            return NO_LOOP_COUNT;
        }
    }

    private int getTickWithOffset(int beat) {
        return beat + fOffset;
    }

    public static void main(String[] args) {
        run_test();
    }
}
