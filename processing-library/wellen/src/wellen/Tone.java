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

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * {@link wellen.Tone} handles instruments and the playing of musical notes.
 */
public abstract class Tone {

    private static ToneEngine instance = null;

    private Tone() {
    }

    public static void start(String... pName) {
        if (instance != null) {
            printAlreadyStartedWarning();
            return;
        }
        instance = ToneEngine.create(pName);
    }

    public static void start(String pName, int pParameter) {
        if (instance != null) {
            printAlreadyStartedWarning();
            return;
        }
        if (pName.equalsIgnoreCase(Wellen.TONE_ENGINE_INTERNAL)) {
            /* specify output channels */
            // ToneEngineInternal(int pSamplingRate, int pAudioblockSize, int pOutputDeviceID, int pOutputChannels)
            instance = new ToneEngineInternal(Wellen.DEFAULT_SAMPLING_RATE,
                                              Wellen.DEFAULT_AUDIOBLOCK_SIZE,
                                              Wellen.DEFAULT_AUDIO_DEVICE,
                                              pParameter,
                                              Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS);
        } else if (pName.equalsIgnoreCase(Wellen.TONE_ENGINE_MIDI)) {
            /* specify output device ID */
            instance = new ToneEngineMIDI(pParameter);
        } else {
            instance = ToneEngine.create(pName);
        }
    }

    public static void start(String pName, int pParameterA, int pParameterB) {
        if (instance != null) {
            printAlreadyStartedWarning();
            return;
        }
        if (pName.equalsIgnoreCase(Wellen.TONE_ENGINE_INTERNAL)) {
            /* specify output device + output channels */
            // ToneEngineInternal(int pSamplingRate, int pAudioblockSize, int pOutputDeviceID, int pOutputChannels)
            instance = new ToneEngineInternal(Wellen.DEFAULT_SAMPLING_RATE,
                                              Wellen.DEFAULT_AUDIOBLOCK_SIZE,
                                              pParameterA,
                                              pParameterB,
                                              Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS);
        } else {
            instance = ToneEngine.create(pName);
        }
    }

    public static void start(String pName, int pParameterA, int pParameterB, int pParameterC) {
        if (instance != null) {
            printAlreadyStartedWarning();
            return;
        }
        if (pName.equalsIgnoreCase(Wellen.TONE_ENGINE_INTERNAL)) {
            /* specify sampling rate + output device + output channels */
            // ToneEngineInternal(int pSamplingRate, int pAudioblockSize, int pOutputDeviceID, int pOutputChannels)
            instance = new ToneEngineInternal(pParameterA,
                                              Wellen.DEFAULT_AUDIOBLOCK_SIZE,
                                              pParameterB,
                                              pParameterC,
                                              Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS);
        } else {
            instance = ToneEngine.create(pName);
        }
    }

    public static ToneEngineInternal start(int pConfiguration) {
        if (instance != null) {
            printAlreadyStartedWarning();
            if (instance instanceof ToneEngineInternal) {
                return (ToneEngineInternal) instance;
            }
        }
        if (pConfiguration == Wellen.TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT) {
            ToneEngineInternal mInstance = new ToneEngineInternal(Wellen.DEFAULT_SAMPLING_RATE,
                                                                  Wellen.DEFAULT_AUDIOBLOCK_SIZE,
                                                                  Wellen.DEFAULT_AUDIO_DEVICE,
                                                                  Wellen.NO_CHANNELS,
                                                                  Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS);
            instance = mInstance;
            return mInstance;
        } else {
            System.err.println("+++ WARNING @" + Tone.class.getSimpleName() + ".start" + " / unknown configuration, " + "using default");
            return new ToneEngineInternal();
        }
    }

    public static void note_on(int pNote, int pVelocity, float pDuration) {
        instance().note_on(pNote, pVelocity, pDuration);
    }

    public static void note_on(int pNote, int pVelocity) {
        instance().note_on(pNote, pVelocity);
    }

    public static void note_off(int pNote) {
        instance().note_off(pNote);
    }

    public static void note_off() {
        instance().note_off();
    }

    public static void control_change(int pCC, int pValue) {
        instance().control_change(pCC, pValue);
    }

    public static void pitch_bend(int pValue) {
        instance().pitch_bend(pValue);
    }

    public static boolean is_playing() {
        return instance().is_playing();
    }

    public static Instrument instrument(int pInstrumentID) {
        return instance().instrument(pInstrumentID);
    }

    public static Instrument instrument() {
        return instance().instrument();
    }

    public static float[] get_buffer() {
        return instance().get_buffer();
    }

    public static float[] get_buffer_left() {
        return instance().get_buffer_left();
    }

    public static float[] get_buffer_right() {
        return instance().get_buffer_right();
    }

    public static void replace_instrument(Class<? extends Instrument> pInstrumentClass, int pID) {
        instance().replace_instrument(create_instrument(pInstrumentClass, pID));
    }

    public static void replace_instrument(Instrument pInstrument) {
        instance().replace_instrument(pInstrument);
    }

    public static ArrayList<? extends Instrument> instruments() {
        return instance().instruments();
    }

    public static <T extends Instrument> T create_instrument(Class<T> pInstrumentClass, int pID) {
        //@TODO(maybe move this to ToneEngine)
        T mInstrument;
        try {
            Constructor<T> c;
            //@TODO(add constructor for `InstrumentInternal(int pID, int pSamplingRate, int pWavetableSize)`)
//            if (InstrumentJSyn.class.isAssignableFrom(pInstrumentClass) && instance() instanceof ToneEngineJSyn) {
//                c = pInstrumentClass.getDeclaredConstructor(ToneEngineJSyn.class, int.class);
//                mInstrument = c.newInstance(instance(), pID);
//            } else if (pInstrumentClass == InstrumentMinim.class && instance() instanceof ToneEngineMinim) {
//                c = pInstrumentClass.getDeclaredConstructor(Minim.class, int.class);
//                mInstrument = c.newInstance((Minim) ((ToneEngineMinim) instance()).minim(), pID);
//            } else {
            c = pInstrumentClass.getDeclaredConstructor(int.class);
            mInstrument = c.newInstance(pID);
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
            mInstrument = null;
        }
        return mInstrument;
    }

    public static ToneEngine instance() {
        if (instance == null) {
            instance = ToneEngine.create();
        }
        return instance;
    }

    public static ToneEngineInternal get_internal_engine() {
        if (instance() instanceof ToneEngineInternal) {
            return (ToneEngineInternal) instance();
        } else {
            return null;
        }
    }

    public static void set_engine(ToneEngine pEngine) {
        instance = pEngine;
    }

    private static void printAlreadyStartedWarning() {
        System.err.println("+++ WARNING @" + Tone.class.getSimpleName() + ".start" + " / tone engine already " +
                                   "initialized. make sure that `start` is the first call to `Ton`. " + "use " +
                                   "`set_engine(ToneEngine)` to switch tone engines.");
    }
}
