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

import processing.core.PApplet;
import wellen.dsp.DSPNodeOutput;
import wellen.dsp.DSPNodeOutputSignal;
import wellen.dsp.EffectStereo;
import wellen.dsp.Gain;
import wellen.dsp.Reverb;
import wellen.dsp.Signal;

import java.util.ArrayList;

import static wellen.Wellen.NO_AUDIO_DEVICE;
import static wellen.Wellen.clamp;

/**
 * implementation of {@link wellen.ToneEngine} using internal DSP audio processing.
 */
public class ToneEngineDSP extends ToneEngine implements AudioBufferRenderer, DSPNodeOutput, DSPNodeOutputSignal {

    public static boolean VERBOSE = true;
    public boolean USE_AMP_FRACTION = false;
    private final AudioBufferManager fAudioPlayer;
    private AudioOutputCallback fAudioblockCallback = null;
    private float[] fCurrentBufferLeft;
    private int fCurrentBufferCounter;
    private float[] fCurrentBufferRight;
    private int fCurrentInstrumentID;
    private final ArrayList<EffectStereo> fEffects;
    private final Gain fGain;
    private final ArrayList<InstrumentDSP> fInstruments;
    private final int fNumberOfInstruments;
    private final Pan fPan;
    private final Reverb fReverb;
    private boolean fReverbEnabled;

    public ToneEngineDSP(int sampling_rate,
                         int audioblock_size,
                         int output_device_ID,
                         int output_channels,
                         int number_of_instruments) {
        fInstruments = new ArrayList<>();
        fEffects = new ArrayList<>();
        fNumberOfInstruments = number_of_instruments;
        for (int i = 0; i < fNumberOfInstruments; i++) {
            final InstrumentDSP mInstrument = new InstrumentDSP(i, sampling_rate);
            fInstruments.add(mInstrument);
        }

        fGain = new Gain();
        fReverb = new Reverb();
        fReverbEnabled = false;
        fPan = new Pan();
        fPan.set_pan_type(Wellen.PAN_SINE_LAW);

        if (output_device_ID != NO_AUDIO_DEVICE && output_channels > 0) {
            AudioDeviceConfiguration mConfig = new AudioDeviceConfiguration();
            mConfig.sample_rate = sampling_rate;
            mConfig.sample_buffer_size = audioblock_size;
            mConfig.output_device = output_device_ID;
            mConfig.number_of_output_channels = output_channels;
            mConfig.input_device = 0;
            mConfig.number_of_input_channels = 0;
            fAudioPlayer = new AudioBufferManager(this, mConfig);
        } else {
            fAudioPlayer = null;
        }
    }

    public ToneEngineDSP() {
        this(Wellen.DEFAULT_SAMPLING_RATE, Wellen.DEFAULT_AUDIOBLOCK_SIZE, Wellen.DEFAULT_AUDIO_DEVICE, 2, 16);
    }

    public static ToneEngineDSP create_without_audio_output(int number_of_instruments) {
        return new ToneEngineDSP(Wellen.DEFAULT_SAMPLING_RATE,
                                 Wellen.DEFAULT_AUDIOBLOCK_SIZE,
                                 Wellen.NO_AUDIO_DEVICE,
                                 2,
                                 number_of_instruments);
    }

    @Override
    public void stop() {
        super.stop();
        if (fAudioPlayer != null) {
            fAudioPlayer.exit();
        }
    }

    /**
     * @param state enable reverb
     */
    public void enable_reverb(boolean state) {
        fReverbEnabled = state;
    }

    /**
     * @return reference to reverb
     */
    public Reverb get_reverb() {
        return fReverb;
    }

    @Override
    public void note_on(int note, int velocity) {
        if (USE_AMP_FRACTION) {
            velocity /= fNumberOfInstruments;
        }
        fInstruments.get(getInstrumentID()).note_on(note, velocity);
    }

    @Override
    public void note_off(int note) {
        note_off();
    }

    @Override
    public void note_off() {
        fInstruments.get(getInstrumentID()).note_off();
    }

    @Override
    public void control_change(int CC, int value) {
    }

    @Override
    public void pitch_bend(int value) {
        final float mRange = 110;
        final float mValue = mRange * ((float) (PApplet.constrain(value, 0, 16383) - 8192) / 8192.0f);
        fInstruments.get(getInstrumentID()).pitch_bend(mValue);
    }

    @Override
    public boolean is_playing() {
        return fInstruments.get(getInstrumentID()).is_playing();
    }

    @Override
    public Instrument instrument(int instrument_ID) {
        fCurrentInstrumentID = instrument_ID;
        return instrument();
    }

    @Override
    public Instrument instrument() {
        return instruments().get(fCurrentInstrumentID);
    }

    @Override
    public ArrayList<? extends Instrument> instruments() {
        return fInstruments;
    }

    @Override
    public void replace_instrument(Instrument instrument) {
        if (instrument instanceof InstrumentDSP) {
            fInstruments.set(instrument.ID(), (InstrumentDSP) instrument);
        } else {
            System.err.println("+++ WARNING @" + getClass().getSimpleName() + ".replace_instrument(Instrument) / " +
                                       "instrument must " + "be" + " of type `InstrumentInternal`");
        }
    }

    @Override
    public float[] get_buffer_left() {
        return fCurrentBufferLeft;
    }

    @Override
    public float[] get_buffer_right() {
        return fCurrentBufferRight;
    }

    @Override
    public void audioblock(float[][] output_signal, float[][] input_signal) {
        if (output_signal.length == 1) {
            audioblock(output_signal[0]);
        } else if (output_signal.length == 2) {
            audioblock(output_signal[0], output_signal[1]);
        } else {
            System.err.println("+++ WARNING @" + getClass().getSimpleName() + ".audioblock / multiple output " +
                                       "channels are not " + "supported.");
        }
        if (fAudioblockCallback != null) {
            fAudioblockCallback.audioblock(output_signal);
        }
    }

    @Override
    public float output() {
        float mSignal = getNextInstrumentSampleMono();

        if (fReverbEnabled) {
            mSignal = fReverb.process(mSignal);
        }

        mSignal *= fGain.get_gain();

        if (fCurrentBufferLeft == null) {
            fCurrentBufferLeft = new float[Wellen.DEFAULT_AUDIOBLOCK_SIZE];
        }
        fCurrentBufferLeft[fCurrentBufferCounter] = mSignal;
        fCurrentBufferCounter++;
        fCurrentBufferCounter %= fCurrentBufferLeft.length;

        return mSignal;
    }

    @Override
    public Signal output_signal() {
        Signal mSignalSum = getNextInstrumentSampleStereo();

        float[] pSignalLeft = new float[]{mSignalSum.left()};
        float[] pSignalRight = new float[]{mSignalSum.right()};
        for (EffectStereo mEffect : fEffects) {
            mEffect.out(pSignalLeft, pSignalRight);
        }

        fGain.out(pSignalLeft, pSignalRight);

        if (fReverbEnabled) {
            fReverb.process(pSignalLeft, pSignalRight, pSignalLeft, pSignalRight);
        }

        mSignalSum.left(pSignalLeft[0]);
        mSignalSum.right(pSignalRight[0]);

        if (fCurrentBufferLeft == null) {
            fCurrentBufferLeft = new float[Wellen.DEFAULT_AUDIOBLOCK_SIZE];
        }
        if (fCurrentBufferRight == null) {
            fCurrentBufferRight = new float[Wellen.DEFAULT_AUDIOBLOCK_SIZE];
        }
        fCurrentBufferLeft[fCurrentBufferCounter] = mSignalSum.left();
        fCurrentBufferRight[fCurrentBufferCounter] = mSignalSum.right();
        fCurrentBufferCounter++;
        fCurrentBufferCounter %= fCurrentBufferLeft.length;

        return mSignalSum;
    }

    public void audioblock(float[] signal) {
        for (int i = 0; i < signal.length; i++) {
            signal[i] = getNextInstrumentSampleMono();
        }

        if (fReverbEnabled) {
            fReverb.process(signal, signal, signal, signal);
        }

        fGain.out(signal, null);

        fCurrentBufferLeft = signal;
    }

    public void audioblock(float[] signal_left, float[] signal_right) {
        for (int i = 0; i < signal_left.length; i++) {
            Signal mSignalSum = getNextInstrumentSampleStereo();
            signal_left[i] = mSignalSum.left();
            signal_right[i] = mSignalSum.right();
        }

        for (EffectStereo mEffect : fEffects) {
            mEffect.out(signal_left, signal_right);
        }

        fGain.out(signal_left, signal_right);

        if (fReverbEnabled) {
            fReverb.process(signal_left, signal_right, signal_left, signal_right);
        }
        fCurrentBufferLeft = signal_left;
        fCurrentBufferRight = signal_right;
    }

    private float getNextInstrumentSampleMono() {
        float mSignal = 0;
        for (InstrumentDSP mInstrument : fInstruments) {
            final Signal mSignals = mInstrument.output_signal();
            /* if instrument has multiple channels accumulate them into one */
            for (int j = 0; j < mSignals.signal.length; j++) {
                mSignal += mSignals.signal[j];
            }
        }
        mSignal = clamp(mSignal);
        return mSignal;
    }

    private Signal getNextInstrumentSampleStereo() {
        final Signal mSignalSum = new Signal();
        for (InstrumentDSP mInstrument : fInstruments) {
            Signal mSignal = mInstrument.output_signal();
            if (mInstrument.get_channels() == 1) {
                /* convert mono instrument to stereo (default) */
                fPan.set_panning(mInstrument.get_pan());
                /* pan takes only left channel as input */
                mSignal = fPan.process(mSignal.left());
            } else if (mInstrument.get_channels() == 0) {
                mSignal = new Signal();
            } else if (mInstrument.get_channels() > 2) {
                if (VERBOSE) {
                    System.err.println("+++ @WARNING " + getClass().getSimpleName() + ".audioblock(stereo) /" + " " + "instruments with " + "more than 2 channels are " + "not supported in " + "this tone engine. all extra channels are " + "ignored.");
                }
            }
            /* stereo -- more than 2 channels are ignored */
            mSignalSum.left_add(mSignal.left());
            mSignalSum.right_add(mSignal.right());
        }
        return mSignalSum;
    }

    public float get_gain() {
        return fGain.get_gain();
    }

    public void set_gain(float gain) {
        fGain.set_gain(gain);
    }

    public void add_effect(EffectStereo effect) {
        fEffects.add(effect);
    }

    public boolean remove_effect(EffectStereo effect) {
        return fEffects.remove(effect);
    }

    public void register_audioblock_callback(AudioOutputCallback audioblock_callback) {
        fAudioblockCallback = audioblock_callback;
    }

    private int getInstrumentID() {
        return Math.max(fCurrentInstrumentID, 0) % fInstruments.size();
    }

    public static ToneEngineDSP no_output() {
        return new ToneEngineDSP(Wellen.DEFAULT_SAMPLING_RATE,
                                 Wellen.DEFAULT_AUDIOBLOCK_SIZE,
                                 Wellen.DEFAULT_AUDIO_DEVICE,
                                 Wellen.NO_CHANNELS,
                                 Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS);
    }

    public interface AudioOutputCallback {

        void audioblock(float[][] output_signals);
    }
}
