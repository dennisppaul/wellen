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

import java.util.ArrayList;

import static processing.core.PApplet.constrain;
import static wellen.Wellen.clamp;

/**
 * implementation of {@link wellen.ToneEngine} using internal DSP audio processing.
 */
public class ToneEngineInternal extends ToneEngine implements AudioBufferRenderer {

    public static boolean VERBOSE = true;
    public boolean USE_AMP_FRACTION = false;
    private final AudioBufferManager mAudioPlayer;
    private AudioOutputCallback mAudioblockCallback = null;
    private float[] mCurrentBufferLeft;
    private float[] mCurrentBufferRight;
    private int mCurrentInstrumentID;
    private final ArrayList<EffectStereo> mEffects;
    private Gain mGain;
    private final ArrayList<InstrumentInternal> mInstruments;
    private final int mNumberOfInstruments;
    private final Pan mPan;
    private final Reverb mReverb;
    private boolean mReverbEnabled;

    public ToneEngineInternal(int pSamplingRate, int pAudioblockSize, int pOutputDeviceID, int pOutputChannels,
                              int pNumberOfInstruments) {
        mInstruments = new ArrayList<>();
        mEffects = new ArrayList<>();
        mNumberOfInstruments = pNumberOfInstruments;
        for (int i = 0; i < mNumberOfInstruments; i++) {
            final InstrumentInternal mInstrument = new InstrumentInternal(i, pSamplingRate);
            mInstruments.add(mInstrument);
        }

        mGain = new Gain();
        mReverb = new Reverb();
        mReverbEnabled = false;
        mPan = new Pan();
        mPan.set_pan_type(Wellen.PAN_SINE_LAW);

        if (pOutputChannels > 0) {
            mAudioPlayer = new AudioBufferManager(this, pSamplingRate, pAudioblockSize, pOutputDeviceID,
                                                  pOutputChannels, 0, 0);
        } else {
            mAudioPlayer = null;
        }
    }

    public ToneEngineInternal() {
        this(Wellen.DEFAULT_SAMPLING_RATE, Wellen.DEFAULT_AUDIOBLOCK_SIZE, Wellen.DEFAULT_AUDIO_DEVICE, 2, 16);
    }

    public void stop() {
        super.stop();
        if (mAudioPlayer != null) {
            mAudioPlayer.exit();
        }
    }

    /**
     * @deprecated
     * @param pReverbEnabled enable reverb
     */
    public void enable_reverb(boolean pReverbEnabled) {
        mReverbEnabled = pReverbEnabled;
    }

    /**
     * @deprecated
     * @return reference to reverb
     */
    public Reverb get_reverb() {
        return mReverb;
    }

    @Override
    public void note_on(int note, int velocity) {
        if (USE_AMP_FRACTION) {
            velocity /= mNumberOfInstruments;
        }
        mInstruments.get(getInstrumentID()).note_on(note, velocity);
    }

    @Override
    public void note_off(int note) {
        note_off();
    }

    @Override
    public void note_off() {
        mInstruments.get(getInstrumentID()).note_off();
    }

    @Override
    public void control_change(int pCC, int pValue) {
    }

    @Override
    public void pitch_bend(int pValue) {
        final float mRange = 110;
        final float mValue = mRange * ((float) (constrain(pValue, 0, 16383) - 8192) / 8192.0f);
        mInstruments.get(getInstrumentID()).pitch_bend(mValue);
    }

    @Override
    public boolean is_playing() {
        return mInstruments.get(getInstrumentID()).is_playing();
    }

    @Override
    public Instrument instrument(int pInstrumentID) {
        mCurrentInstrumentID = pInstrumentID;
        return instrument();
    }

    @Override
    public Instrument instrument() {
        return instruments().get(mCurrentInstrumentID);
    }

    @Override
    public ArrayList<? extends Instrument> instruments() {
        return mInstruments;
    }

    @Override
    public void replace_instrument(Instrument pInstrument) {
        if (pInstrument instanceof InstrumentInternal) {
            mInstruments.set(pInstrument.ID(), (InstrumentInternal) pInstrument);
        } else {
            System.err.println(
            "+++ WARNING @" + getClass().getSimpleName() + ".replace_instrument(Instrument) / " + "instrument must " +
            "be" + " of type `InstrumentInternal`");
        }
    }

    public float[] get_buffer_left() {
        return mCurrentBufferLeft;
    }

    public float[] get_buffer_right() {
        return mCurrentBufferRight;
    }

    @Override
    public void audioblock(float[][] pOutputSignal, float[][] pInputSignal) {
        if (pOutputSignal.length == 1) {
            audioblock(pOutputSignal[0]);
        } else if (pOutputSignal.length == 2) {
            audioblock(pOutputSignal[0], pOutputSignal[1]);
        } else {
            System.err.println(
            "+++ WARNING @" + getClass().getSimpleName() + ".audioblock / multiple output " + "channels are not " +
            "supported.");
        }
        if (mAudioblockCallback != null) {
            mAudioblockCallback.audioblock(pOutputSignal);
        }
    }

    public void audioblock(float[] pSignalLeft, float[] pSignalRight) {
        for (int i = 0; i < pSignalLeft.length; i++) {
            float mSignalL = 0;
            float mSignalR = 0;
            for (InstrumentInternal mInstrument : mInstruments) {
                final Signal mSignals = new Signal(mInstrument.get_channels());
                mInstrument.output(mSignals);
                if (mInstrument.get_channels() == 1) {
                    /* mono (default) */
                    final float mSignal = mSignals.signal[0];
                    mPan.set_panning(mInstrument.get_pan());
                    Signal mStereoSignal = mPan.process(mSignal);
                    mSignalL += mStereoSignal.signal[Wellen.SIGNAL_LEFT];
                    mSignalR += mStereoSignal.signal[Wellen.SIGNAL_RIGHT];
                } else if (mInstrument.get_channels() == 2) {
                    /* stereo */
                    mSignalL += mSignals.signal[0];
                    mSignalR += mSignals.signal[1];
                } else {
                    if (VERBOSE) {
                        System.err.println(
                        "+++ @WARNING " + getClass().getSimpleName() + ".audioblock(stereo) / instruments with more " +
                        "than 2 channels are " + "not supported in this tone engine.");
                    }
                }
            }
            pSignalLeft[i] = mSignalL;
            pSignalRight[i] = mSignalR;
        }

        for (EffectStereo mEffect : mEffects) {
            mEffect.out(pSignalLeft, pSignalRight);
        }

        mGain.out(pSignalLeft, pSignalRight);

        if (mReverbEnabled) {
            mReverb.process(pSignalLeft, pSignalRight, pSignalLeft, pSignalRight);
        }
        mCurrentBufferLeft = pSignalLeft;
        mCurrentBufferRight = pSignalRight;
    }

    public float get_gain() {
        return mGain.get_gain();
    }

    public void set_gain(float pGain) {
        mGain.set_gain(pGain);
    }

    public void add_effect(EffectStereo pEffect) {
        mEffects.add(pEffect);
    }

    public boolean remove_effect(EffectStereo pEffect) {
        return mEffects.remove(pEffect);
    }

    public void audioblock(float[] pSignal) {
        for (int i = 0; i < pSignal.length; i++) {
            float mSignal = 0;
            for (InstrumentInternal mInstrument : mInstruments) {
                final Signal mSignals = new Signal(mInstrument.get_channels());
                mInstrument.output(mSignals);
                /* if instrument has multiple channels accumulate them into one */
                for (int j = 0; j < mSignals.signal.length; j++) {
                    mSignal += mSignals.signal[j];
                }
            }
            pSignal[i] = clamp(mSignal);
        }
        if (mReverbEnabled) {
            mReverb.process(pSignal, pSignal, pSignal, pSignal);
        }
        mCurrentBufferLeft = pSignal;
    }

    public void register_audioblock_callback(AudioOutputCallback pAudioblockCallback) {
        mAudioblockCallback = pAudioblockCallback;
    }

    private int getInstrumentID() {
        return Math.max(mCurrentInstrumentID, 0) % mInstruments.size();
    }

    public static ToneEngineInternal no_output() {
        return new ToneEngineInternal(Wellen.DEFAULT_SAMPLING_RATE, Wellen.DEFAULT_AUDIOBLOCK_SIZE,
                                      Wellen.DEFAULT_AUDIO_DEVICE, Wellen.NO_CHANNELS,
                                      Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS);
    }

    public interface AudioOutputCallback {

        void audioblock(float[][] pOutputSignals);
    }
}
