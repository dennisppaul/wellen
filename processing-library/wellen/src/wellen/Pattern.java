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

import static wellen.Wellen.LOOP_INFINITE;
import static wellen.Wellen.NO_INPOINT;
import static wellen.Wellen.NO_LOOP;
import static wellen.Wellen.NO_OUTPOINT;

public class Pattern extends Loop implements Loopable {

    private int fLoop = LOOP_INFINITE;
    private int fInPoint = NO_INPOINT;
    private int fOutPoint = NO_OUTPOINT;

    /**
     * length defines the number of steps before tick is wrapped.
     * <p>
     * e.g an interval of 3 would generate a pattern of 0, 1, 2, 0, 1, 2, 0, 1, …
     * <p>
     * setting interval length affects the outpoint. e.g an interval of 3 and an inpoint of N would yield an outpoint of
     * N + ( 3 - 1 ) and would generate a pattern of 0, 1, 2, 0, 1, 2, 0, 1, …
     *
     * @param pInterval number of steps before the tick is wrapped
     */
    @Override
    public void set_length(int pInterval) {
        super.set_length(pInterval);
        fOutPoint = fInPoint + fInterval - 1;
    }

    public void set_loop(int pLoop) {
        fLoop = pLoop;
    }

    @Override
    public int get_loop() {
        return fLoop;
    }

    /**
     * set inpoint for pattern. if outpoint is equal or smaller than inpoint length is set to 1 and output set to
     * inpoint.
     *
     * @param pIn set inpoint for pattern
     */
    public void set_inpoint(int pIn) {
        fInPoint = pIn;
        if (fOutPoint <= fInPoint) {
            fOutPoint = fInPoint;
            fInterval = 1;
        }
    }

    @Override
    public int get_inpoint() {
        return fInPoint;
    }

    public void set_outpoint(int pOut) {
        set_length(pOut - fInPoint + 1);
    }

    @Override
    public int get_outpoint() {
        return fOutPoint;
    }

    @Override
    public boolean event(int pTick, int pLoopEvent) {
        final int mRelativePosition = get_relative_position(pTick);
        final int mLoopCount = get_loop_count(pTick);
        final boolean mIsLoopOK = mLoopCount < get_loop() && mLoopCount >= 0 ||
                                  ((get_loop() == NO_LOOP || get_outpoint() == NO_OUTPOINT) && mRelativePosition == pLoopEvent);
        final boolean mIsInpointOK = mRelativePosition >= 0;
        if (mIsLoopOK && mIsInpointOK) {
            return super.event(mRelativePosition, pLoopEvent);
        } else {
            return false;
        }
    }

    @Override
    public boolean before(int pTick, int pThreshold) {
        return super.before(pTick, pThreshold);
    }

    @Override
    public boolean after(int pTick, int pThreshold) {
        return super.after(pTick, pThreshold);
    }

    /**
     * returns the relative postion in repsect to in- and outpoint
     *
     * @param pAbsolutPosition current absolut position ( i.e tick )
     * @return returns the relative postion. value may be negative.
     */
    public int get_relative_position(int pAbsolutPosition) {
        return Loopable.get_relative_position(this, pAbsolutPosition);
    }

    /**
     * returns the current loop where 0 is the first loop. this method always returns
     * <pre><code>Wellen.NO_LOOP_COUNT</code></pre> if one of the following criteria is met:
     *
     * <ul>
     *     <li>no outpoint is specified ( i.e <pre><code>set_outpoint(NO_OUTPOINT)</code></pre> )</li>
     *     <li>loop is set to <pre><code>set_loop(NO_LOOP)</code></pre></li>
     *     <li>a number of loops is specified but the position is before the inpoint</li>
     * </ul>
     *
     * @param pAbsolutPosition current absolut position ( i.e tick )
     * @return returns the current loop. value may be negative.
     */
    @Override
    public int get_loop_count(int pAbsolutPosition) {
        return Loopable.get_loop_count(this, pAbsolutPosition);
    }

    /* ---------------------------------- TEST ---------------------------------- */

    @SuppressWarnings("SpellCheckingInspection")
    private static final String TEST_RESULT_IDEAL = "CNT\tREL\tEVT\tLOP\n" +
                                                    "---\n" +
                                                    "0\t0\t+\t0\t\n" +
                                                    "1\t1\t-\t0\t\n" +
                                                    "2\t2\t-\t0\t\n" +
                                                    "3\t0\t+\t1\t\n" +
                                                    "4\t1\t-\t1\t\n" +
                                                    "5\t2\t-\t1\t\n" +
                                                    "6\t0\t-\t-1\t\n" +
                                                    "7\t1\t-\t-1\t\n" +
                                                    "8\t2\t-\t-1\t\n" +
                                                    "9\t0\t-\t-1\t\n" +
                                                    "10\t1\t-\t-1\t\n" +
                                                    "11\t2\t-\t-1\t\n" +
                                                    "---\n" +
                                                    "0\t-5\t-\t-1\t\n" +
                                                    "1\t-4\t-\t-1\t\n" +
                                                    "2\t-3\t-\t-1\t\n" +
                                                    "3\t-2\t-\t-1\t\n" +
                                                    "4\t-1\t-\t-1\t\n" +
                                                    "5\t0\t-\t0\t\n" +
                                                    "6\t1\t-\t0\t\n" +
                                                    "7\t2\t+\t0\t\n" +
                                                    "8\t0\t-\t1\t\n" +
                                                    "9\t1\t-\t1\t\n" +
                                                    "10\t2\t+\t1\t\n" +
                                                    "11\t0\t-\t2\t\n" +
                                                    "12\t1\t-\t2\t\n" +
                                                    "13\t2\t+\t2\t\n" +
                                                    "14\t0\t-\t3\t\n" +
                                                    "15\t1\t-\t3\t\n" +
                                                    "16\t2\t+\t3\t\n" +
                                                    "17\t0\t-\t4\t\n" +
                                                    "18\t1\t-\t4\t\n" +
                                                    "19\t2\t+\t4\t\n" +
                                                    "---\n" +
                                                    "0\t-7\t-\t-1\t\n" +
                                                    "1\t-6\t-\t-1\t\n" +
                                                    "2\t-5\t-\t-1\t\n" +
                                                    "3\t-4\t-\t-1\t\n" +
                                                    "4\t-3\t-\t-1\t\n" +
                                                    "5\t-2\t-\t-1\t\n" +
                                                    "6\t-1\t-\t-1\t\n" +
                                                    "7\t0\t+\t-1\t\n" +
                                                    "8\t1\t-\t-1\t\n" +
                                                    "9\t2\t-\t-1\t\n" +
                                                    "10\t3\t-\t-1\t\n" +
                                                    "11\t4\t-\t-1\t\n" +
                                                    "12\t5\t-\t-1\t\n" +
                                                    "13\t6\t-\t-1\t\n" +
                                                    "14\t7\t-\t-1\t\n" +
                                                    "15\t8\t-\t-1\t\n" +
                                                    "16\t9\t-\t-1\t\n" +
                                                    "17\t10\t-\t-1\t\n" +
                                                    "18\t11\t-\t-1\t\n" +
                                                    "19\t12\t-\t-1\t\n" +
                                                    "---\n" +
                                                    "0\t-2\t-\t-1\t\n" +
                                                    "1\t-1\t-\t-1\t\n" +
                                                    "2\t0\t+\t0\t\n" +
                                                    "3\t1\t-\t0\t\n" +
                                                    "4\t2\t-\t0\t\n" +
                                                    "5\t0\t-\t-1\t\n" +
                                                    "6\t1\t-\t-1\t\n" +
                                                    "7\t2\t-\t-1\t\n" +
                                                    "8\t0\t-\t-1\t\n" +
                                                    "9\t1\t-\t-1\t\n" +
                                                    "10\t2\t-\t-1\t\n" +
                                                    "11\t0\t-\t-1\t\n" +
                                                    "---\n" +
                                                    "0\t-2\t-\t-1\t\n" +
                                                    "1\t-1\t-\t-1\t\n" +
                                                    "2\t0\t+\t-1\t\n" +
                                                    "3\t1\t-\t-1\t\n" +
                                                    "4\t2\t-\t-1\t\n" +
                                                    "5\t3\t-\t-1\t\n" +
                                                    "6\t4\t-\t-1\t\n" +
                                                    "7\t5\t-\t-1\t\n";

    private static String TEST_RESULT = "";

    public static void println() {
        println("");
    }

    public static void print(String s) {
        TEST_RESULT += s;
        System.out.print(s);
    }

    public static void println(String s) {
        TEST_RESULT += s + "\n";
        System.out.println(s);
    }

    public static void main(String[] args) {
        Pattern.run_test();
    }

    public static void run_test() {
        Pattern p = new Pattern();

        println("CNT\tREL\tEVT\tLOP");

        println("---");
        p.set_length(3);
        p.set_loop(2);
        for (int i = 0; i < 12; i++) {
            print(i + "\t");
            print(p.get_relative_position(i) + "\t");
            print((p.event(i, 0) ? "+" : "-") + "\t");
            print(p.get_loop_count(i) + "\t");
            println();
        }

        println("---");
        p.set_inpoint(5);
        p.set_length(3);
        p.set_loop(LOOP_INFINITE);
        for (int i = 0; i < 20; i++) {
            print(i + "\t");
            print(p.get_relative_position(i) + "\t");
            if (p.event(i, 2)) {
                print("+" + "\t");
            } else {
                print("-" + "\t");
            }
            print(p.get_loop_count(i) + "\t");
            println();
        }

        println("---");

        p.set_inpoint(7);
        p.set_outpoint(9);
        p.set_loop(NO_LOOP);
        for (int i = 0; i < 20; i++) {
            print(i + "\t");
            print(p.get_relative_position(i) + "\t");
            print((p.event(i, 0) ? "+" : "-") + "\t");
            print(p.get_loop_count(i) + "\t");
            println();
        }

        println("---");
        p.set_inpoint(2);
        p.set_outpoint(4);
        p.set_loop(1);
        for (int i = 0; i < 12; i++) {
            print(i + "\t");
            print(p.get_relative_position(i) + "\t");
            print((p.event(i, 0) ? "+" : "-") + "\t");
            print(p.get_loop_count(i) + "\t");
            println();
        }

        println("---");
        p.set_inpoint(2);
        p.set_outpoint(NO_OUTPOINT);
        for (int i = 0; i < 8; i++) {
            print(i + "\t");
            print(p.get_relative_position(i) + "\t");
            print((p.event(i, 0) ? "+" : "-") + "\t");
            print(p.get_loop_count(i) + "\t");
            println();
        }

        System.out.println("---");
        System.out.println("TEST SUCCESS: " + TEST_RESULT.equals(TEST_RESULT_IDEAL));
    }
}
