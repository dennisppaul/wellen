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

import static wellen.Wellen.LOOP_INFINITE;
import static wellen.Wellen.NO_INPOINT;
import static wellen.Wellen.NO_LOOP;
import static wellen.Wellen.NO_OUTPOINT;

public class Pattern extends Loop implements Loopable {

    @SuppressWarnings("SpellCheckingInspection")
    private static final String TEST_RESULT_IDEAL =
            "CNT\tREL\tEVT\tLOP\n" + "--- LENGTH: 3 LOOP: 2\n" + "0\t0\t+\t0" + "\t\n" + "1\t1\t-\t0\t\n" + "2\t2\t" + "-\t0\t\n" + "3\t0\t+\t1\t\n" + "4\t1\t-\t1\t\n" + "5\t2\t-\t1\t\n" + "6\t0\t-\t-1\t\n" + "7\t1\t" + "-\t-1\t\n" + "8\t2\t-\t-1\t\n" + "9\t0\t-\t-1\t\n" + "10\t1\t-\t-1\t\n" + "11" + "\t2\t-\t-1\t\n" + "--- LENGTH: 3 LOOP: INF IN: 5\n" + "0\t-5\t-\t-1\t\n" + "1\t-4\t-\t-1\t\n" + "2\t-3\t" + "-\t" + "-1\t\n" + "3\t-2\t-\t-1\t\n" + "4\t-1\t-\t-1\t\n" + "5\t0\t-\t0\t\n" + "6\t0\t-\t1\t\n" + "7\t0" + "\t-\t2" + "\t\n" + "8\t0\t-\t3\t\n" + "9\t0\t-\t4\t\n" + "10\t0\t-\t5\t\n" + "11\t0\t-\t6\t\n" + "12\t0\t-\t7\t\n" + "13\t0\t-\t8\t\n" + "14\t0\t-\t9\t\n" + "15\t0\t-\t10\t\n" + "16\t0\t-\t11\t\n" + "17\t0\t-\t12\t\n" + "18\t0\t-\t13\t\n" + "19\t0\t-\t14\t\n" + "---LOOP: NO IN: 7 OUT: 9\n" + "0\t-7\t-\t-1\t\n" + "1\t-6\t-\t-1\t\n" + "2\t-5\t-\t-1\t\n" + "3\t-4\t-\t-1\t\n" + "4\t-3\t-\t-1\t\n" + "5\t-2\t-\t-1\t\n" + "6\t-1\t-\t-1\t\n" + "7\t0\t+\t-1\t\n" + "8\t1\t-\t-1\t\n" + "9\t2\t-\t-1\t\n" + "10\t3\t-\t-1\t\n" + "11\t4\t-\t-1\t\n" + "12\t5\t-\t-1\t\n" + "13\t6\t-\t-1\t\n" + "14\t7\t-\t-1\t\n" + "15\t8\t-\t-1\t\n" + "16\t9\t-\t-1\t\n" + "17\t10\t-\t-1\t\n" + "18\t11\t-\t-1\t\n" + "19\t12\t-\t-1\t\n" + "--- LOOP: 1 IN: 2 OUT: 4\n" + "0\t-2\t-\t-1\t\n" + "1\t-1\t-\t-1\t\n" + "2\t0\t+\t0\t\n" + "3\t1\t-\t0\t\n" + "4\t2\t-\t0\t\n" + "5\t0\t-\t-1\t\n" + "6\t1\t-\t-1\t\n" + "7\t2\t-\t-1\t\n" + "8\t0\t-\t-1\t\n" + "9\t1\t-\t-1\t\n" + "10\t2\t-\t-1\t\n" + "11\t0\t-\t-1\t\n" + "--- IN: 2 OUT: NO\n" + "0\t-2\t-\t-1\t\n" + "1\t-1\t-\t-1\t\n" + "2\t0\t+\t-1\t\n" + "3\t1\t-\t-1\t\n" + "4\t2\t-\t-1\t\n" + "5\t3\t-\t-1\t\n" + "6\t4\t-\t-1\t\n" + "7\t5\t-\t-1\t\n";
    private static String TEST_RESULT = "";
    private int fInPoint;
    private int fLoop;
    private int fOutPoint;

    public Pattern() {
        this(NO_INPOINT, NO_OUTPOINT, LOOP_INFINITE);
    }

    public Pattern(int in_point, int out_point, int loop) {
        fInPoint = in_point;
        fOutPoint = out_point;
        fLoop = loop;
    }

    public static void print(String s) {
        TEST_RESULT += s;
        System.out.print(s);
    }

    public static void println() {
        println("");
    }

    public static void println(String s) {
        TEST_RESULT += s + "\n";
        System.out.println(s);
    }

    public static void run_test() {
        Pattern p = new Pattern();

        println("CNT\tREL\tEVT\tLOP");

        println("--- LENGTH: 3 LOOP: 2");
        p.set_length(3);
        p.set_loop(2);
        for (int i = 0; i < 12; i++) {
            print(i + "\t");
            print(p.get_relative_position(i) + "\t");
            print((p.event(i, 0) ? "+" : "-") + "\t");
            print(p.get_loop_count(i) + "\t");
            println();
        }

        println("--- LENGTH: 3 LOOP: INF IN: 5");
        p.set_length(3);
        p.set_loop(LOOP_INFINITE);
        p.set_in_point(5);
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

        println("---LOOP: NO IN: 7 OUT: 9");
        p.set_loop(NO_LOOP);
        p.set_in_point(7);
        p.set_out_point(9);
        for (int i = 0; i < 20; i++) {
            print(i + "\t");
            print(p.get_relative_position(i) + "\t");
            print((p.event(i, 0) ? "+" : "-") + "\t");
            print(p.get_loop_count(i) + "\t");
            println();
        }

        println("--- LOOP: 1 IN: 2 OUT: 4");
        p.set_loop(1);
        p.set_in_point(2);
        p.set_out_point(4);
        for (int i = 0; i < 12; i++) {
            print(i + "\t");
            print(p.get_relative_position(i) + "\t");
            print((p.event(i, 0) ? "+" : "-") + "\t");
            print(p.get_loop_count(i) + "\t");
            println();
        }

        println("--- IN: 2 OUT: NO");
        p.set_in_point(2);
        p.set_out_point(NO_OUTPOINT);
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

    /**
     * length defines the number of steps before beat is wrapped.
     * <p>
     * e.g an interval of 3 would generate a pattern of 0, 1, 2, 0, 1, 2, 0, 1, …
     * <p>
     * setting interval length affects the out point. e.g an interval of 3 and an in point of N would yield an out point
     * of N + ( 3 - 1 ) and would generate a pattern of 0, 1, 2, 0, 1, 2, 0, 1, …
     *
     * @param length number of steps before the beat is wrapped
     */
    @Override
    public void set_length(int length) {
        fLength = length;
        fOutPoint = fInPoint + fLength - 1;
    }

    @Override
    public int get_loop() {
        return fLoop;
    }

    /**
     * set the number of loops. while the length of a single cycle is set by <code>set_length(int)</code>, the number of
     * cycles or repetitions is defined by <code>set_loop(int)</code>. if number of loops is set to
     * <code>Wellen.LOOP_INFINITE</code> the loop is repearted infinitely. if set to
     * <code>Wellen.NO_LOOP</code> only one cycle is processed.
     *
     * @param pLoop number of loops
     */
    public void set_loop(int pLoop) {
        fLoop = pLoop;
    }

    @Override
    public int get_in_point() {
        return fInPoint;
    }

    /* ---------------------------------- TEST ---------------------------------- */

    /**
     * set in point for pattern. if out point is equal or smaller than in point length is set to 1 and out point
     * accordingly.
     *
     * @param in_point set in point for pattern
     */
    public void set_in_point(int in_point) {
        fInPoint = in_point;
        if (fOutPoint <= fInPoint) {
            set_length(1);
        }
    }

    @Override
    public int get_out_point() {
        return fOutPoint;
    }

    /**
     * set out point for pattern. this method also affects the length of the pattern.
     *
     * @param out_point out point for pattern
     */
    public void set_out_point(int out_point) {
        set_length(out_point - fInPoint + 1);
    }

    /**
     * querry whether an event occured. the first value is the absolute beat ( or beat ) to evalute, the second value
     * specifies the position in to query. e.g if the pattern is of length 3 ( <code>P(3)</code>, has an in&nbsp;point
     * of 0 and loops infinitely, it will generate the following values:
     * <pre><code>
     *     TICK   : 0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16
     *     P(3)   : 0  1  2  0  1  2  0  1  2  0  1  2  0  1  2  0  1
     * </code></pre>
     * <p>
     * if event is now for example tested againt 0 <code>E(0)</code> it will either return true
     * <code>+</code> or false <code>-</code>:
     * <pre><code>
     *     TICK   : 0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16
     *     P(3)   : 0  1  2  0  1  2  0  1  2  0  1  2  0  1  2  0  1
     *     E(0)   : +  -  -  +  -  -  +  -  -  +  -  -  +  -  -  +  -
     * </code></pre>
     * <p>
     * e.g an increasing value will produce an event every 3 beats:
     * <pre><code>
     *     Pattern p = new Pattern();
     *     p.set_length(3);
     *     if (p.event(mTick, 0)) {
     *         println("event");
     *     }
     * </code></pre>
     *
     * @param beat     absolute beat to evaluate
     * @param event_at local position to to test beat against
     * @return true if an event occurs
     */
    @Override
    public boolean event(int beat, int event_at) {
        final int mRelativePosition = get_relative_position(beat);
        final int mLoopCount = get_loop_count(beat);
        final boolean mIsLoopOK =
                mLoopCount < get_loop() && mLoopCount >= 0 || ((get_loop() == NO_LOOP || get_out_point() == NO_OUTPOINT) && mRelativePosition == event_at);
        final boolean mIsInpointOK = mRelativePosition >= 0;
        if (mIsLoopOK && mIsInpointOK) {
            return super.event(mRelativePosition, event_at);
        } else {
            return false;
        }
    }

    /**
     * returns the relative postion in repsect to in- and out point
     *
     * @param absolut_position current absolut position ( i.e beat )
     * @return returns the relative postion. value may be negative.
     */
    public int get_relative_position(int absolut_position) {
        return Loopable.get_relative_position(this, absolut_position);
    }

    /**
     * returns the current loop where 0 is the first loop. this method always returns
     * <code>Wellen.NO_LOOP_COUNT</code> if one of the following criteria is met:
     * <ul>
     *     <li>no out point is specified ( i.e <code>set_out_point(NO_OUTPOINT)</code> )</li>
     *     <li>loop is set to <code>set_loop(NO_LOOP)</code></li>
     *     <li>a number of loops is specified but the position is before the in point</li>
     * </ul>
     *
     * @param absolut_position current absolut position ( i.e beat )
     * @return returns the current loop. value may be negative.
     */
    @Override
    public int get_loop_count(int absolut_position) {
        return Loopable.get_loop_count(this, absolut_position);
    }

    public static void main(String[] args) {
        Pattern.run_test();
    }
}
