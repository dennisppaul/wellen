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

import java.util.ArrayList;

import static processing.core.PApplet.constrain;

/**
 * implementation of {@link wellen.ToneEngine} using internal DSP audio processing.
 */
public class ToneEngineInternal extends ToneEngine implements AudioBufferRenderer {

    public boolean USE_AMP_FRACTION = false;
    private final ArrayList<InstrumentInternal> mInstruments;
    private final AudioBufferManager mAudioPlayer;
    private final Reverb mReverb;
    private int mCurrentInstrumentID;
    private AudioOutputCallback mAudioblockCallback = null;
    private float[] mCurrentBufferLeft;
    private float[] mCurrentBufferRight;
    private boolean mReverbEnabled;
    private final int mNumberOfInstruments;

    public ToneEngineInternal(int pSamplingRate, int pAudioblockSize, int pOutputDeviceID, int pOutputChannels,
                              int pNumberOfInstruments) {
        mInstruments = new ArrayList<>();
        mNumberOfInstruments = pNumberOfInstruments;
        for (int i = 0; i < mNumberOfInstruments; i++) {
            final InstrumentInternal mInstrument = new InstrumentInternal(i, pSamplingRate);
            mInstruments.add(mInstrument);
        }

        if (pOutputChannels > 0) {
            mAudioPlayer = new AudioBufferManager(this,
                                                  pSamplingRate,
                                                  pAudioblockSize,
                                                  pOutputDeviceID,
                                                  pOutputChannels,
                                                  0,
                                                  0);
        } else {
            mAudioPlayer = null;
        }
        mReverb = new Reverb();
        mReverbEnabled = false;
    }

    public ToneEngineInternal() {
        this(Wellen.DEFAULT_SAMPLING_RATE, Wellen.DEFAULT_AUDIOBLOCK_SIZE, Wellen.DEFAULT_AUDIO_DEVICE, 2, 16);
    }

    public static ToneEngineInternal no_output() {
        return new ToneEngineInternal(Wellen.DEFAULT_SAMPLING_RATE,
                                      Wellen.DEFAULT_AUDIOBLOCK_SIZE,
                                      Wellen.DEFAULT_AUDIO_DEVICE,
                                      Wellen.NO_CHANNELS,
                                      Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS);
    }

    public void enable_reverb(boolean pReverbEnabled) {
        mReverbEnabled = pReverbEnabled;
    }

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
            System.err.println("+++ WARNING @" + getClass().getSimpleName() + ".replace_instrument(Instrument) / " +
                                       "instrument must be" + " of type `InstrumentInternal`");
        }
    }

    public float[] get_buffer_left() {
        return mCurrentBufferLeft;
    }

    public float[] get_buffer_right() {
        return mCurrentBufferRight;
    }

    @Override
    public void audioblock(float[][] pOutputSamples, float[][] pInputSamples) {
        if (pOutputSamples.length == 1) {
            audioblock(pOutputSamples[0]);
        } else if (pOutputSamples.length == 2) {
            audioblock(pOutputSamples[0], pOutputSamples[1]);
        } else {
            System.err.println("+++ WARNING @" + getClass().getSimpleName() + ".audioblock / multiple output " +
                                       "channels" + " are " + "not supported.");
        }
        if (mAudioblockCallback != null) {
            mAudioblockCallback.audioblock(pOutputSamples);
        }
    }

    public void audioblock(float[] pSamplesLeft, float[] pSamplesRight) {
        for (int i = 0; i < pSamplesLeft.length; i++) {
            float mSampleL = 0;
            float mSampleR = 0;
            for (InstrumentInternal mInstrument : mInstruments) {
                final float mSample = mInstrument.output();
                final float mPan = mInstrument.get_pan() * 0.5f + 0.5f;
                mSampleR += mSample * mPan;
                mSampleL += mSample * (1.0f - mPan);
            }
            pSamplesLeft[i] = mSampleL;
            pSamplesRight[i] = mSampleR;
        }
        if (mReverbEnabled) {
            mReverb.process(pSamplesLeft, pSamplesRight, pSamplesLeft, pSamplesRight);
        }
        mCurrentBufferLeft = pSamplesLeft;
        mCurrentBufferRight = pSamplesRight;
    }

    public void audioblock(float[] pSamples) {
        for (int i = 0; i < pSamples.length; i++) {
            float mSample = 0;
            for (InstrumentInternal mInstrument : mInstruments) {
                mSample += mInstrument.output();
            }
            pSamples[i] = mSample;
        }
        if (mReverbEnabled) {
            mReverb.process(pSamples, pSamples, pSamples, pSamples);
        }
        mCurrentBufferLeft = pSamples;
    }

    public void register_audioblock_callback(AudioOutputCallback pAudioblockCallback) {
        mAudioblockCallback = pAudioblockCallback;
    }

    private int getInstrumentID() {
        return Math.max(mCurrentInstrumentID, 0) % mInstruments.size();
    }

    public interface AudioOutputCallback {

        void audioblock(float[][] pOutputSamples);
    }
}
