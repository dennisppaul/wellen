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
     * setting interval length affects the out point. e.g an interval of 3 and an in point of N would yield an out point
     * of N + ( 3 - 1 ) and would generate a pattern of 0, 1, 2, 0, 1, 2, 0, 1, …
     *
     * @param pLength number of steps before the tick is wrapped
     */
    @Override
    public void set_length(int pLength) {
        fLength = pLength;
        fOutPoint = fInPoint + fLength - 1;
    }

    /**
     * set the number of loops. while the length of a single cycle is set by <pre><code>set_length(int)</code></pre>,
     * the number of cycles or repetitions is defined by <pre><code>set_loop(int)</code></pre>. if number of loops is
     * set to <pre><code>Wellen.LOOP_INFINITE</code></pre> the loop is repearted infinitely. if set to
     * <pre><code>Wellen.NO_LOOP</code></pre> only one cycle is processed.
     *
     * @param pLoop number of loops
     */
    public void set_loop(int pLoop) {
        fLoop = pLoop;
    }

    @Override
    public int get_loop() {
        return fLoop;
    }

    /**
     * set in point for pattern. if out point is equal or smaller than in point length is set to 1 and out point
     * accordingly.
     *
     * @param pInPoint set in point for pattern
     */
    public void set_in_point(int pInPoint) {
        fInPoint = pInPoint;
        if (fOutPoint <= fInPoint) {
            set_length(1);
        }
    }

    @Override
    public int get_in_point() {
        return fInPoint;
    }

    /**
     * set out point for pattern. this method also affects the length of the pattern.
     *
     * @param pOutPoint out point for pattern
     */
    public void set_out_point(int pOutPoint) {
        set_length(pOutPoint - fInPoint + 1);
    }

    @Override
    public int get_out_point() {
        return fOutPoint;
    }

    /**
     * querry whether an event occured. the first value is the absolute tick ( or beat ) to evalute, the second value
     * specifies the position in to query. e.g if the pattern is of length 3 ( <pre><code>P(3)</code></pre>, has an
     * in&nbsp;point of 0 and loops infinitely, it will generate the following values:
     *
     * <pre><code>
     *     TICK   : 0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16
     *     P(3)   : 0  1  2  0  1  2  0  1  2  0  1  2  0  1  2  0  1
     * </code></pre>
     * <p>
     * if event is now for example tested againt 0 <pre><code>E(0)</code></pre> it will either return true
     * <pre><code>+</code></pre> or false <pre><code>-</code></pre>:
     *
     * <pre><code>
     *     TICK   : 0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16
     *     P(3)   : 0  1  2  0  1  2  0  1  2  0  1  2  0  1  2  0  1
     *     E(0)   : +  -  -  +  -  -  +  -  -  +  -  -  +  -  -  +  -
     * </code></pre>
     * <p>
     * e.g an increasing value will produce an event every 3 ticks:
     * <pre><code>
     *     Pattern p = new Pattern();
     *     p.set_length(3);
     *     if (p.event(mTick, 0)) {
     *         println("event");
     *     }
     * </code></pre>
     *
     * @param pTick          absolute tick ( or beat ) to evaluate
     * @param pLocalPosition local position to to test tick against
     * @return true if an event occurs
     */
    @Override
    public boolean event(int pTick, int pLocalPosition) {
        final int mRelativePosition = get_relative_position(pTick);
        final int mLoopCount = get_loop_count(pTick);
        final boolean mIsLoopOK =
        mLoopCount < get_loop() && mLoopCount >= 0 || ((get_loop() == NO_LOOP || get_out_point() == NO_OUTPOINT) && mRelativePosition == pLocalPosition);
        final boolean mIsInpointOK = mRelativePosition >= 0;
        if (mIsLoopOK && mIsInpointOK) {
            return super.event(mRelativePosition, pLocalPosition);
        } else {
            return false;
        }
    }

    /**
     * returns the relative postion in repsect to in- and out point
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
     *     <li>no out point is specified ( i.e <pre><code>set_out_point(NO_OUTPOINT)</code></pre> )</li>
     *     <li>loop is set to <pre><code>set_loop(NO_LOOP)</code></pre></li>
     *     <li>a number of loops is specified but the position is before the in point</li>
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
    private static final String TEST_RESULT_IDEAL = "CNT\tREL\tEVT\tLOP\n" + "---\n" + "0\t0\t+\t0\t\n" + "1\t1\t-\t0" +
                                                    "\t\n" + "2\t2\t-\t0\t\n" + "3\t0\t+\t1\t\n" + "4\t1\t-\t1\t" +
                                                    "\n" + "5\t2\t-\t1\t\n" + "6\t0\t-\t-1\t\n" + "7\t1\t-\t-1\t" +
                                                    "\n" + "8\t2\t-\t-1\t\n" + "9\t0\t-\t-1\t\n" + "10\t1\t-\t-1" +
                                                    "\t\n" + "11\t2\t-\t-1\t\n" + "---\n" + "0\t-5\t-\t-1\t\n" +
                                                    "1\t-4\t-\t-1\t\n" + "2\t-3\t-\t-1\t\n" + "3\t-2\t-\t-1\t\n" + "4" +
                                                    "\t-1\t-\t-1\t\n" + "5\t0\t-\t0\t\n" + "6\t1\t-\t0\t\n" + "7" +
                                                    "\t2\t+\t0\t\n" + "8\t0\t-\t1\t\n" + "9\t1\t-\t1\t\n" + "10" +
                                                    "\t2\t+\t1\t\n" + "11\t0\t-\t2\t\n" + "12\t1\t-\t2\t\n" + "13" +
                                                    "\t2\t+\t2\t\n" + "14\t0\t-\t3\t\n" + "15\t1\t-\t3\t\n" + "16" +
                                                    "\t2\t+\t3\t\n" + "17\t0\t-\t4\t\n" + "18\t1\t-\t4\t\n" + "19" +
                                                    "\t2\t+\t4\t\n" + "---\n" + "0\t-7\t-\t-1\t\n" + "1\t-6\t-\t" +
                                                    "-1\t\n" + "2\t-5\t-\t-1\t\n" + "3\t-4\t-\t-1\t\n" + "4\t-3\t" +
                                                    "-\t-1\t\n" + "5\t-2\t-\t-1\t\n" + "6\t-1\t-\t-1\t\n" + "7\t0" +
                                                    "\t+\t-1\t\n" + "8\t1\t-\t-1\t\n" + "9\t2\t-\t-1\t\n" + "10" +
                                                    "\t3\t-\t-1\t\n" + "11\t4\t-\t-1\t\n" + "12\t5\t-\t-1\t\n" +
                                                    "13\t6\t-\t-1\t\n" + "14\t7\t-\t-1\t\n" + "15\t8\t-\t-1\t\n" +
                                                    "16\t9\t-\t-1\t\n" + "17\t10\t-\t-1\t\n" + "18\t11\t-\t-1\t\n"
                                                    + "19\t12\t-\t-1\t\n" + "---\n" + "0\t-2\t-\t-1\t\n" + "1\t-1" +
                                                    "\t-\t-1\t\n" + "2\t0\t+\t0\t\n" + "3\t1\t-\t0\t\n" + "4\t2\t" +
                                                    "-\t0\t\n" + "5\t0\t-\t-1\t\n" + "6\t1\t-\t-1\t\n" + "7\t2\t" +
                                                    "-\t-1\t\n" + "8\t0\t-\t-1\t\n" + "9\t1\t-\t-1\t\n" + "10\t2" +
                                                    "\t-\t-1\t\n" + "11\t0\t-\t-1\t\n" + "---\n" + "0\t-2\t-\t-1" +
                                                    "\t\n" + "1\t-1\t-\t-1\t\n" + "2\t0\t+\t-1\t\n" + "3\t1\t-\t" +
                                                    "-1\t\n" + "4\t2\t-\t-1\t\n" + "5\t3\t-\t-1\t\n" + "6\t4\t-\t" +
                                                    "-1\t\n" + "7\t5\t-\t-1\t\n";

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
        p.set_in_point(5);
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

        p.set_in_point(7);
        p.set_out_point(9);
        p.set_loop(NO_LOOP);
        for (int i = 0; i < 20; i++) {
            print(i + "\t");
            print(p.get_relative_position(i) + "\t");
            print((p.event(i, 0) ? "+" : "-") + "\t");
            print(p.get_loop_count(i) + "\t");
            println();
        }

        println("---");
        p.set_in_point(2);
        p.set_out_point(4);
        p.set_loop(1);
        for (int i = 0; i < 12; i++) {
            print(i + "\t");
            print(p.get_relative_position(i) + "\t");
            print((p.event(i, 0) ? "+" : "-") + "\t");
            print(p.get_loop_count(i) + "\t");
            println();
        }

        println("---");
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
}
