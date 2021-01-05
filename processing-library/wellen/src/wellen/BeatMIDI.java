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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * similar to {@link wellen.Beat} except that beat events are triggered by an external MIDI CLOCK signal.
 */
public class BeatMIDI implements MidiInListener {

    private static final int BPM_SAMPLER_SIZE = 12;
    private static final String METHOD_NAME = "beat";
    public static boolean VERBOSE = false;
    private final Object mListener;
    private final float[] mBPMSampler = new float[BPM_SAMPLER_SIZE];
    private Method mMethod = null;
    private int mTickPPQNCounter = 0;
    private boolean mIsRunning = false;
    private float mBPMEstimate = 0;
    private long mBPMMeasure;
    private int mBPMSamplerCounter = 0;

    private BeatMIDI(Object pListener, int pBPM) {
        this(pListener);
    }

    private BeatMIDI(Object pListener) {
        mListener = pListener;
        mTickPPQNCounter = -1;
        try {
            mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME, Integer.TYPE);
        } catch (NoSuchMethodException | SecurityException ex) {
            System.err.println("+++ @" + getClass().getSimpleName() + " / could not find `" + METHOD_NAME + "(int)`");
        }
        mBPMMeasure = _timer();
        start();
    }

    public static BeatMIDI start(Object pListener, String pMidiInput) {
        final BeatMIDI mBeatMIDI = new BeatMIDI(pListener);
        MidiIn mMidiIn = new MidiIn(pMidiInput);
        mMidiIn.addListener(mBeatMIDI);
        return mBeatMIDI;
    }

    private static long _timer() {
        return System.nanoTime();
    }

    private static double _timer_divider() {
        return 1000000000;
    }

    public boolean running() {
        return mIsRunning;
    }

    public int beat_count() {
        return mTickPPQNCounter;
    }

    /**
     * returns an estimate of the current BPM deduced from the duration between two ticks ( or pulses )
     *
     * @return estimated BPM ( might be imprecise in the first few beats )
     */
    public float bpm() {
        return mBPMEstimate;
    }

    public void invoke() {
        if (mIsRunning) {
            try {
                mMethod.invoke(mListener, mTickPPQNCounter);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                System.err.println("+++ @BeatMIDI / problem calling `" + METHOD_NAME + "`");
                ex.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void receiveProgramChange(int channel, int number, int value) {
    }

    @Override
    public void receiveControlChange(int channel, int number, int value) {
    }

    @Override
    public void receiveNoteOff(int channel, int pitch) {
    }

    @Override
    public void receiveNoteOn(int channel, int pitch, int velocity) {
    }

    @Override
    public void clock_tick() {
        if (mIsRunning) {
            mTickPPQNCounter++;
            estimate_bpm();
            invoke();
        }
    }

    @Override
    public void clock_start() {
        if (VERBOSE) {
            System.out.println("clock_start");
        }
        mTickPPQNCounter = 0;
        start();
//        invoke();
    }

    @Override
    public void clock_continue() {
        if (VERBOSE) {
            System.out.println("clock_continue");
        }
        start();
//        clock_tick();
    }

    @Override
    public void clock_stop() {
        if (VERBOSE) {
            System.out.println("clock_stop");
        }
//        if (mIsRunning) {
//            // @TODO(check if this is desired behavior)
//            mTickCounter++;
//        }
        stop();
    }

    @Override
    public void clock_song_position_pointer(int pOffset16th) {
        final int mPPQN = pOffset16th / 4 * 24;
        mTickPPQNCounter = mPPQN;
        if (VERBOSE) {
            System.out.println("clock_song_position_pointer: " + mTickPPQNCounter + "(" + pOffset16th + ")");
        }
    }

    public void stop() {
        mIsRunning = false;
    }

    public void start() {
        mIsRunning = true;
        mBPMMeasure = System.currentTimeMillis();
    }

    private void estimate_bpm() {
        float mBPMEstimateFragment = 60 / ((float) ((_timer() - mBPMMeasure) / _timer_divider()) * 24); // 24 PPQN *
        // 4 QN * 60 SEC
        mBPMSampler[mBPMSamplerCounter % BPM_SAMPLER_SIZE] = mBPMEstimateFragment;
        mBPMSamplerCounter++;
        mBPMEstimate = 0;
        for (float mBPMSample : mBPMSampler) {
            mBPMEstimate += mBPMSample;
        }
        mBPMEstimate /= BPM_SAMPLER_SIZE;
        mBPMMeasure = _timer();
    }
}
