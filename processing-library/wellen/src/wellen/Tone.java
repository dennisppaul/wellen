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

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * {@link wellen.Tone} handles instruments and the playing of musical notes.
 */
public abstract class Tone {

    private static ToneEngine mInstance = null;

    private Tone() {
    }

    public static void control_change(int CC, int value) {
        instance().control_change(CC, value);
    }

    public static <T extends Instrument> T create_instrument(Class<T> instrument_class, int ID) {
        //@TODO(maybe move this to ToneEngine)
        T mInstrument;
        try {
            Constructor<T> c;
            //@TODO(add constructor for `InstrumentInternal(int ID, int pSamplingRate, int pWavetableSize)`)
//            if (InstrumentJSyn.class.isAssignableFrom(instrument_class) && instance() instanceof ToneEngineJSyn) {
//                c = instrument_class.getDeclaredConstructor(ToneEngineJSyn.class, int.class);
//                mInstrument = c.newInstance(instance(), ID);
//            } else if (instrument_class == InstrumentMinim.class && instance() instanceof ToneEngineMinim) {
//                c = instrument_class.getDeclaredConstructor(Minim.class, int.class);
//                mInstrument = c.newInstance((Minim) ((ToneEngineMinim) instance()).minim(), ID);
//            } else {
            c = instrument_class.getDeclaredConstructor(int.class);
            mInstrument = c.newInstance(ID);
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
            mInstrument = null;
        }
        return mInstrument;
    }

    /**
     * @param damping  default: 0.5
     * @param roomsize default: 0.5
     * @param wet      default: 0.33
     */
    public static void enable_reverb(float damping, float roomsize, float wet) {
        if (get_DSP_engine() != null) {
            get_DSP_engine().enable_reverb(true);
            get_DSP_engine().get_reverb().set_damp(damping);
            get_DSP_engine().get_reverb().set_roomsize(roomsize);
            get_DSP_engine().get_reverb().set_wet(wet);
        }
    }

    public static ToneEngineDSP get_DSP_engine() {
        if (instance() instanceof ToneEngineDSP) {
            return (ToneEngineDSP) instance();
        } else {
            return null;
        }
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

    public static ToneEngineMIDI get_midi_engine() {
        if (instance() instanceof ToneEngineMIDI) {
            return (ToneEngineMIDI) instance();
        } else {
            return null;
        }
    }

    public static ToneEngine instance() {
        if (mInstance == null) {
            mInstance = ToneEngine.create();
        }
        return mInstance;
    }

    public static Instrument instrument(int ID) {
        return instance().instrument(ID);
    }

    public static Instrument instrument() {
        return instance().instrument();
    }

    public static ArrayList<? extends Instrument> instruments() {
        return instance().instruments();
    }

    public static boolean is_playing() {
        return instance().is_playing();
    }

    public static void note_off(int note) {
        instance().note_off(note);
    }

    public static void note_off() {
        instance().note_off();
    }

    public static void note_on(int note, int velocity, float duration) {
        instance().note_on(note, velocity, duration);
    }

    public static void note_on(int note, int velocity) {
        instance().note_on(note, velocity);
    }

    public static void pitch_bend(int value) {
        instance().pitch_bend(value);
    }

    public static void replace_instrument(Class<? extends Instrument> instrument_class, int ID) {
        instance().replace_instrument(create_instrument(instrument_class, ID));
    }

    public static void replace_instrument(Instrument instrument) {
        instance().replace_instrument(instrument);
    }

    public static void set_engine(ToneEngine tone_engine) {
        mInstance = tone_engine;
    }

    public static void start(String... tone_enginge_name) {
        if (mInstance != null) {
            printAlreadyStartedWarning();
            return;
        }
        mInstance = ToneEngine.create(tone_enginge_name);
    }

    public static void start(String tone_enginge_name, int parameter) {
        if (mInstance != null) {
            printAlreadyStartedWarning();
            return;
        }
        if (tone_enginge_name.equalsIgnoreCase(Wellen.TONE_ENGINE_INTERNAL)) {
            /* specify output channels */
            // ToneEngineInternal(int pSamplingRate, int pAudioblockSize, int pOutputDeviceID, int pOutputChannels)
            mInstance = new ToneEngineDSP(Wellen.DEFAULT_SAMPLING_RATE,
                                          Wellen.DEFAULT_AUDIOBLOCK_SIZE,
                                          Wellen.DEFAULT_AUDIO_DEVICE,
                                          parameter,
                                          Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS);
        } else if (tone_enginge_name.equalsIgnoreCase(Wellen.TONE_ENGINE_MIDI)) {
            /* specify output device ID */
            mInstance = new ToneEngineMIDI(parameter);
        } else {
            mInstance = ToneEngine.create(tone_enginge_name);
        }
    }

    public static void start(String tone_enginge_name, int parameterA, int parameterB) {
        if (mInstance != null) {
            printAlreadyStartedWarning();
            return;
        }
        if (tone_enginge_name.equalsIgnoreCase(Wellen.TONE_ENGINE_INTERNAL)) {
            /* specify output device + output channels */
            // ToneEngineInternal(int pSamplingRate, int pAudioblockSize, int pOutputDeviceID, int pOutputChannels)
            mInstance = new ToneEngineDSP(Wellen.DEFAULT_SAMPLING_RATE,
                                          Wellen.DEFAULT_AUDIOBLOCK_SIZE,
                                          parameterA,
                                          parameterB,
                                          Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS);
        } else {
            mInstance = ToneEngine.create(tone_enginge_name);
        }
    }

    public static void start(String tone_enginge_name, int parameterA, int parameterB, int parameterC) {
        if (mInstance != null) {
            printAlreadyStartedWarning();
            return;
        }
        if (tone_enginge_name.equalsIgnoreCase(Wellen.TONE_ENGINE_INTERNAL)) {
            /* specify sampling rate + output device + output channels */
            // ToneEngineInternal(int pSamplingRate, int pAudioblockSize, int pOutputDeviceID, int pOutputChannels)
            mInstance = new ToneEngineDSP(parameterA,
                                          Wellen.DEFAULT_AUDIOBLOCK_SIZE,
                                          parameterB,
                                          parameterC,
                                          Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS);
        } else {
            mInstance = ToneEngine.create(tone_enginge_name);
        }
    }

    public static ToneEngineDSP start(int configuration) {
        if (mInstance != null) {
            printAlreadyStartedWarning();
            if (mInstance instanceof ToneEngineDSP) {
                return (ToneEngineDSP) mInstance;
            }
        }
        if (configuration == Wellen.TONE_ENGINE_INTERNAL_WITH_NO_OUTPUT) {
            ToneEngineDSP mInstance = new ToneEngineDSP(Wellen.DEFAULT_SAMPLING_RATE,
                                                        Wellen.DEFAULT_AUDIOBLOCK_SIZE,
                                                        Wellen.DEFAULT_AUDIO_DEVICE,
                                                        Wellen.NO_CHANNELS,
                                                        Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS);
            Tone.mInstance = mInstance;
            return mInstance;
        } else {
            System.err.println("+++ WARNING @" + Tone.class.getSimpleName() + ".start" + " / unknown configuration, " + "using default");
            return new ToneEngineDSP();
        }
    }

    public static void stop() {
        if (mInstance != null) {
            mInstance.stop();
        }
        mInstance = null;
    }

    private static void printAlreadyStartedWarning() {
        System.err.println("+++ WARNING @" + Tone.class.getSimpleName() + ".start" + " / tone engine already " +
                                   "initialized. make sure that `start` is the first call to `Ton`. " + "use " +
                                   "`set_engine(ToneEngine)` to switch tone engines.");
    }
}
