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

package wellen.dsp;

import processing.core.PApplet;
import processing.core.PGraphics;
import wellen.SamplerListener;
import wellen.Wellen;

import java.util.ArrayList;

import static processing.core.PApplet.cos;
import static processing.core.PApplet.map;
import static processing.core.PApplet.sin;
import static processing.core.PConstants.CLOSE;
import static processing.core.PConstants.TWO_PI;
import static wellen.Note.note_to_frequency;
import static wellen.Wellen.clamp;

/**
 * plays back an array of samples at different speeds.
 */
public class Sampler implements DSPNodeOutput {

    public static final int NO_LOOP_POINT = -1;
    private final ArrayList<SamplerListener> fSamplerListeners;
    private final ArrayList<Float> fRecording;
    private final float fSamplingRate;
    private float fAmplitude;
    private float[] fData;
    private float fDataIndex;
    private boolean fDirectionForward;
    private int fEdgeFadePadding;
    private boolean fEvaluateLoop;
    private float fFrequency;
    private float fFrequencyScale;
    private int fInPoint;
    private boolean fInterpolateSamples;
    private boolean fIsPlaying;
    private int fLoopIn;
    private int fLoopOut;
    private int fOutPoint;
    private float fSpeed;
    private float fStepSize;
    private boolean fIsFlaggedDone;
    private boolean fIsRecording;

    public Sampler() {
        this(0);
    }

    public Sampler(int data_size) {
        this(new float[data_size], Wellen.DEFAULT_SAMPLING_RATE);
    }

    public Sampler(float[] data) {
        this(data, Wellen.DEFAULT_SAMPLING_RATE);
    }

    public Sampler(float[] data, float sampling_rate) {
        fSamplerListeners = new ArrayList<>();
        fSamplingRate = sampling_rate;
        set_data(data);
        fDataIndex = 0;
        fInterpolateSamples = false;
        fEdgeFadePadding = 0;
        fIsPlaying = false;
        fInPoint = 0;
        fOutPoint = 0;
        set_in(0);
        set_out(fData.length - 1);
        fFrequencyScale = 1.0f;
        set_speed(1.0f);
        set_amplitude(1.0f);
        fRecording = new ArrayList<>();
        fIsRecording = false;
    }

    public boolean add_listener(SamplerListener sampler_listener) {
        return fSamplerListeners.add(sampler_listener);
    }

    public boolean remove_listener(SamplerListener sampler_listener) {
        return fSamplerListeners.remove(sampler_listener);
    }

    /**
     * load the sample buffer from *raw* byte data. the method assumes a raw format with 32bit float in a value range
     * from -1.0 to 1.0.
     *
     * @param data raw byte data ( assuming 4 bytes per sample, 32-bit float aka WAVE_FORMAT_IEEE_FLOAT_32BIT )
     * @return instance with data loaded
     */
    public Sampler load(byte[] data) {
        load(data, true);
        return this;
    }

    /**
     * load the sample buffer from *raw* byte data. the method assumes a raw format with 32bit float in a value range
     * from -1.0 to 1.0.
     *
     * @param data          raw byte data ( assuming 4 bytes per sample, 32-bit float aka WAVE_FORMAT_IEEE_FLOAT_32BIT
     *                      )
     * @param little_endian true if byte data is arranged in little endian order
     * @return instance with data loaded
     */
    public Sampler load(byte[] data, boolean little_endian) {
        if (fData == null || fData.length != data.length / 4) {
            fData = new float[data.length / 4];
        }
        set_data(fData);
        Wellen.bytes_to_floatIEEEs(data, get_data(), little_endian);
        rewind();
        return this;
    }

    public int get_in() {
        return fInPoint;
    }

    public void set_in(int in_point) {
        if (in_point > fOutPoint) {
            in_point = fOutPoint;
        }
        fInPoint = in_point;
    }

    public int get_out() {
        return fOutPoint;
    }

    public void set_out(int out_point) {
        fOutPoint = out_point > last_index() ? last_index() : (out_point < fInPoint ? fInPoint : out_point);
    }

    public float get_speed() {
        return fSpeed;
    }

    public void set_speed(float speed) {
        fSpeed = speed;
        fDirectionForward = speed > 0;
        set_frequency(PApplet.abs(speed) * fSamplingRate / fData.length); /* aka `step_size = speed` */
    }

    public void set_frequency(float frequency) {
        if (fFrequency != frequency) {
            fFrequency = frequency;
            fStepSize = fFrequency / fFrequencyScale * ((float) fData.length / fSamplingRate);
        }
    }

    public void set_amplitude(float amplitude) {
        fAmplitude = amplitude;
    }

    public float[] get_data() {
        return fData;
    }

    public void set_data(float[] data) {
        fData = data;
        rewind();
        set_speed(fSpeed);
        set_in(0);
        set_out(fData.length - 1);
        fLoopIn = NO_LOOP_POINT;
        fLoopOut = NO_LOOP_POINT;
    }

    public void interpolate_samples(boolean interpolate_samples) {
        fInterpolateSamples = interpolate_samples;
    }

    public boolean interpolate_samples() {
        return fInterpolateSamples;
    }

    public int get_position() {
        return (int) fDataIndex;
    }

    public float get_position_normalized() {
        return fData.length > 0 ? fDataIndex / fData.length : 0.0f;
    }

    public float get_position_fractional_part() {
        return fDataIndex - get_position();
    }

    public boolean is_playing() {
        return fIsPlaying;
    }

    public float output() {
        if (fData.length == 0) {
            notifyListeners("data is empty");
            return 0.0f;
        }

        if (!fIsPlaying) {
            notifyListeners("not playing");
            return 0.0f;
        }

        validateInOutPoints();

        fDataIndex += fDirectionForward ? fStepSize : -fStepSize;
        final int mRoundedIndex = (int) fDataIndex;

        final float mFrac = fDataIndex - mRoundedIndex;
        final int mCurrentIndex = wrapIndex(mRoundedIndex);
        fDataIndex = mCurrentIndex + mFrac;

        if (fDirectionForward ? (mCurrentIndex >= fOutPoint) : (mCurrentIndex <= fInPoint)) {
            notifyListeners("reached end");
            return 0.0f;
        } else {
            fIsFlaggedDone = false;
        }

        float mSample = fData[mCurrentIndex];

        /* interpolate */
        if (fInterpolateSamples) {
            // TODO evaluate direction?
            final int mNextIndex = wrapIndex(mCurrentIndex + 1);
            final float mNextSample = fData[mNextIndex];
            mSample = mSample * (1.0f - mFrac) + mNextSample * mFrac;
        }
        mSample *= fAmplitude;

        /* fade edges */
        if (fEdgeFadePadding > 0) {
            final int mRelativeIndex = fData.length - mCurrentIndex;
            if (mCurrentIndex < fEdgeFadePadding) {
                final float mFadeInAmount = (float) mCurrentIndex / fEdgeFadePadding;
                mSample *= mFadeInAmount;
            } else if (mRelativeIndex < fEdgeFadePadding) {
                final float mFadeOutAmount = (float) mRelativeIndex / fEdgeFadePadding;
                mSample *= mFadeOutAmount;
            }
        }

        return mSample;
    }

    public int get_edge_fading() {
        return fEdgeFadePadding;
    }

    public void set_edge_fading(int edge_fade_padding) {
        fEdgeFadePadding = edge_fade_padding;
    }

    public void rewind() {
        fDataIndex = fDirectionForward ? fInPoint : fOutPoint;
    }

    public void forward() {
        fDataIndex = fDirectionForward ? fOutPoint : fInPoint;
    }

    public boolean is_looping() {
        return fEvaluateLoop;
    }

    public void enable_loop(boolean loop) {
        fEvaluateLoop = loop;
    }

    public void set_loop_all() {
        fEvaluateLoop = true;
        fLoopIn = 0;
        fLoopOut = fData.length > 0 ? (fData.length - 1) : 0;
    }

    public void start() {
        fIsPlaying = true;
    }

    public void stop() {
        fIsPlaying = false;
    }

    public void start_recording() {
        fIsRecording = true;
    }

    public void record(float sample) {
        fRecording.add(sample);
    }

    public boolean is_recording() {
        return fIsRecording;
    }

    public int end_recording() {
        fIsRecording = false;
        float[] mData = new float[fRecording.size()];
        for (int i = 0; i < fRecording.size(); i++) {
            mData[i] = fRecording.get(i);
        }
        fRecording.clear();
        set_data(mData);
        return mData.length;
    }

    public int get_loop_in() {
        return fLoopIn;
    }

    public void set_loop_in(int loop_in_point) {
        fLoopIn = clamp(loop_in_point, NO_LOOP_POINT, fData.length - 1);
    }

    public float get_loop_in_normalized() {
        if (fData.length < 2) {
            return 0.0f;
        }
        return (float) fLoopIn / (fData.length - 1);
    }

    public void set_loop_in_normalized(float loop_in_point_normalized) {
        set_loop_in((int) (loop_in_point_normalized * fData.length - 1));
    }

    public int get_loop_out() {
        return fLoopOut;
    }

    public void set_loop_out(int loop_out_point) {
        fLoopOut = clamp(loop_out_point, NO_LOOP_POINT, fData.length - 1);
    }

    public float get_loop_out_normalized() {
        if (fData.length < 2) {
            return 0.0f;
        }
        return (float) fLoopOut / (fData.length - 1);
    }

    public void set_loop_out_normalized(float loop_out_point_normalized) {
        set_loop_out((int) (loop_out_point_normalized * fData.length - 1));
    }

    public void note_on() {
        rewind();
        start();
        enable_loop(true);
    }

    public void note_on(int note, int velocity) {
        fIsPlaying = true;
        set_frequency(note_to_frequency(note));
        set_amplitude(Wellen.clamp127(velocity) / 127.0f);
        note_on();
    }

    public void note_off() {
        enable_loop(false);
    }

    /**
     * this function can be used to tune a loaded sample to a specific frequency. after the sampler has been tuned the
     * method <code>set_frequency(float)</code> can be used to play the sample at a desired frequency.
     *
     * @param frequency_scale the assumed frequency of the sampler data in Hz
     */
    public void tune_frequency_to(float frequency_scale) {
        fFrequencyScale = frequency_scale;
    }

    private int last_index() {
        return fData.length - 1;
    }

    private void notifyListeners(String event) {
        if (!fIsFlaggedDone) {
            for (SamplerListener l : fSamplerListeners) {
                l.is_done();
            }
        }
        fIsFlaggedDone = true;
    }

    private void validateInOutPoints() {
        if (fInPoint < 0) {
            fInPoint = 0;
        } else if (fInPoint > fData.length - 1) {
            fInPoint = fData.length - 1;
        }
        if (fOutPoint < 0) {
            fOutPoint = 0;
        } else if (fOutPoint > fData.length - 1) {
            fOutPoint = fData.length - 1;
        }
        if (fOutPoint < fInPoint) {
            fOutPoint = fInPoint;
        }
        if (fLoopIn < fInPoint) {
            fLoopIn = fInPoint;
        }
        if (fLoopOut > fOutPoint) {
            fLoopOut = fOutPoint;
        }
    }

    private int wrapIndex(int i) {
        /* check if in loop concept viable i.e loop in- and output points are set */
        if (fEvaluateLoop) {
            if (fLoopIn != NO_LOOP_POINT && fLoopOut != NO_LOOP_POINT) {
                if (fDirectionForward) {
                    if (i > fLoopOut) {
                        i = fLoopIn;
                    }
                } else {
                    if (i < fLoopIn) {
                        i = fLoopOut;
                    }
                }
            }
        }

        /* check if within bounds */
        if (i > fOutPoint) {
            i = fOutPoint;
        } else if (i < fInPoint) {
            i = fInPoint;
        }
        return i;
    }

    public static void draw_sampler_buffer_circular(Sampler sampler,
                                                    PGraphics g,
                                                    float radius_min,
                                                    float radius_max,
                                                    int step) {
        g.beginShape();
        for (int i = 0; i < sampler.get_data().length; i += step) {
            final float r = TWO_PI * i / sampler.get_data().length;
            final float mData = map(sampler.get_data()[i], -1.0f, 1.0f, radius_min, radius_max);
            final float x = cos(r) * mData;
            final float y = sin(r) * mData;
            g.vertex(x, y);
        }
        g.endShape(CLOSE);
        g.circle(0, 0, radius_min * 2);
        g.circle(0, 0, radius_max * 2);
    }

    public static void draw_sampler_position_circular(Sampler fSampler,
                                                      PGraphics g,
                                                      float radius_min,
                                                      float radius_max) {
        final float r = TWO_PI * fSampler.get_position() / fSampler.get_data().length;
        final float x = cos(r);
        final float y = sin(r);
        g.line(x * radius_min, y * radius_min, x * radius_max, y * radius_max);
    }
}
