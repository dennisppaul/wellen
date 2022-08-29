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

/**
 * a module allows to compose complex DSP modules. it is a container that may be managed by a {@link DSPTrack}.
 * <p>
 * a module must implement the method <pre><code>void&nbsp;output(Signal)</code></pre> which supplies an audio signal
 * and may implement the method <pre><code>void&nbsp;beat(int)</code></pre> which can be used to receive beat events.
 */
public abstract class DSPModule implements DSPNodeOutputSignal, Loopable {
    public final int ID;

    private float fVolume;
    private int fInPoint;
    private int fOutPoint;
    private int fLoop;
    private final Pan fPan;
    private static int oTrackUID;

    public DSPModule() {
        this(1.0f, NO_INPOINT, NO_OUTPOINT);
    }

    public DSPModule(float pVolume, int pIn, int pOut) {
        fVolume = pVolume;
        fInPoint = pIn;
        fOutPoint = pOut;
        fLoop = NO_LOOP;
        fPan = new Pan();
        ID = oTrackUID++;
    }

    public void beat(int pBeat) {
    }

    public Pan pan() {
        return fPan;
    }

    /**
     * volume of module with 0.0 being no output and 1.0 being 100% of the signal output. note, that this value is
     * interpreted by parent {@link DSPTrack}s.
     *
     * @param pVolume module volume with 0.0 being no output and 1.0 being 100%
     */
    public void set_volume(float pVolume) {
        fVolume = pVolume;
    }

    /**
     * @return module volume with 0.0 being no output and 1.0 being 100%
     */
    public float get_volume() {
        return fVolume;
    }

    public void set_inpoint(int pIn) {
        fInPoint = pIn;
    }

    @Override
    public int get_inpoint() {
        return fInPoint;
    }

    public void set_outpoint(int pOut) {
        fOutPoint = pOut;
    }

    @Override
    public int get_outpoint() {
        return fOutPoint;
    }

    public void set_in_outpoint(int pIn, int pOut) {
        fInPoint = pIn;
        fOutPoint = pOut;
    }

    public int get_length() {
        return fOutPoint - fInPoint + 1;
    }

    /**
     * set length of a module loop. setting the length affects outpoint. e.g given an inpoint of 3, setting the length
     * to 2 will set output to 4. note that outpoint is inclusive i.e a pattern from in- to outpoint of 3, 4, 5, … is
     * generated.
     *
     * @param pLength set length of loop
     */
    public void set_length(int pLength) {
        fOutPoint = fInPoint + pLength - 1;
    }

    public void set_loop(int pLoop) {
        fLoop = pLoop;
    }

    @Override
    public int get_loop() {
        return fLoop;
    }

    public int get_relative_position(int pAbsolutPosition) {
        return Loopable.get_relative_position(this, pAbsolutPosition);
    }

    public int get_loop_count(int pAbsolutPosition) {
        return Loopable.get_loop_count(this, pAbsolutPosition);
    }

    /* ---------------------------------- TEST ---------------------------------- */

    @SuppressWarnings("SpellCheckingInspection")
    private static final String TEST_RESULT_IDEAL = "BEAT\tRELATIV\tLOOP\n" +
                                                    "---\n" +
                                                    "2\t\t0\t\t0\n" +
                                                    "3\t\t1\t\t0\n" +
                                                    "4\t\t2\t\t0\n" +
                                                    "5\t\t3\t\t0\n" +
                                                    "6\t\t0\t\t1\n" +
                                                    "7\t\t1\t\t1\n" +
                                                    "8\t\t2\t\t1\n" +
                                                    "9\t\t3\t\t1\n" +
                                                    "10\t\t0\t\t2\n" +
                                                    "11\t\t1\t\t2\n" +
                                                    "12\t\t2\t\t2\n" +
                                                    "13\t\t3\t\t2\n" +
                                                    "14\t\t0\t\t3\n" +
                                                    "15\t\t1\t\t3\n" +
                                                    "---\n" +
                                                    "2\t\t0\t\t0\n" +
                                                    "3\t\t1\t\t0\n" +
                                                    "4\t\t2\t\t0\n" +
                                                    "5\t\t3\t\t0\n" +
                                                    "---\n" +
                                                    "11\t\t0\t\t0\n" +
                                                    "12\t\t1\t\t0\n" +
                                                    "13\t\t2\t\t0\n" +
                                                    "14\t\t3\t\t0\n" +
                                                    "15\t\t4\t\t0\n" +
                                                    "---\n" +
                                                    "0\t\t0\t\t0\n" +
                                                    "1\t\t1\t\t0\n" +
                                                    "2\t\t2\t\t0\n" +
                                                    "3\t\t3\t\t0\n" +
                                                    "4\t\t4\t\t0\n";

    private static String TEST_RESULT = "";

    public static void println(String s) {
        TEST_RESULT += s + "\n";
        System.out.println(s);
    }

    public static void main(String[] args) {
        DSPModule.run_test();
    }

    public static void run_test() {
        DSPTrack t = new DSPTrack();
        DSPModule d = new DSPModule() {
            @Override
            public Signal output_signal() {
                return Signal.create(0);
            }

            public void beat(int pBeat) {
                println(pBeat + "\t\t" + get_relative_position(pBeat) + "\t\t" + get_loop_count(pBeat));
            }
        };

        t.modules().add(d);

        //noinspection SpellCheckingInspection
        println("BEAT\tRELATIV\tLOOP");

        println("---");
        d.set_in_outpoint(2, 5);
        d.fLoop = LOOP_INFINITE;
        for (int i = 0; i < 16; i++) {
            t.beat(i);
        }

        println("---");
        d.set_inpoint(2);
        d.set_outpoint(5);
        d.fLoop = NO_LOOP;
        for (int i = 0; i < 16; i++) {
            t.beat(i);
        }

        println("---");
        d.set_in_outpoint(11, NO_OUTPOINT);
        d.fLoop = NO_LOOP;
        for (int i = 0; i < 16; i++) {
            t.beat(i);
        }

        println("---");
        d.set_inpoint(NO_INPOINT);
        d.set_length(5);
        d.fLoop = NO_LOOP;
        for (int i = 0; i < 16; i++) {
            t.beat(i);
        }

        System.out.println("TEST SUCCESS: " + TEST_RESULT.equals(TEST_RESULT_IDEAL));
    }
}
