/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2024 Dennis P Paul.
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

    public static final int                        NO_LOOP_POINT = -1;
    private final       ArrayList<SamplerListener> fSamplerListeners;
    private final       ArrayList<Float>           fRecording;
    private final       float                      fSamplingRate;
    private             float                      fAmplitude;
    private             float[]                    fBuffer;
    private             double                     fBufferIndex;
    private             boolean                    fDirectionForward;
    private             int                        fEdgeFadePadding;
    private             boolean                    fEvaluateLoop;
    private             float                      fFrequency;
    private             float                      fFrequencyScale;
    private             int                        fInPoint;
    private             boolean                    fInterpolateSamples;
    private             boolean                    fIsPlaying;
    private             int                        fLoopIn;
    private             int                        fLoopOut;
    private             int                        fOutPoint;
    private             float                      fSpeed;
    private             float                      fStepSize;
    private             boolean                    fIsFlaggedDone;
    private             boolean                    fIsRecording;

    public Sampler() {
        this(0);
    }

    public Sampler(int buffer_size) {
        this(new float[buffer_size], Wellen.DEFAULT_SAMPLING_RATE);
    }

    public Sampler(float[] buffer) {
        this(buffer, Wellen.DEFAULT_SAMPLING_RATE);
    }

    public Sampler(float[] buffer, float sampling_rate) {
        fSamplerListeners = new ArrayList<>();
        fSamplingRate     = sampling_rate;
        set_buffer(buffer);
        fBufferIndex        = 0;
        fInterpolateSamples = false;
        fEdgeFadePadding    = 0;
        fIsPlaying          = false;
        fEvaluateLoop       = false;
        fInPoint            = 0;
        fOutPoint           = 0;
        set_in(0);
        set_out(fBuffer.length - 1);
        fFrequencyScale = 1.0f;
        set_speed(1.0f);
        set_amplitude(1.0f);
        fRecording   = new ArrayList<>();
        fIsRecording = false;
    }

    public boolean add_listener(SamplerListener sampler_listener) {
        return fSamplerListeners.add(sampler_listener);
    }

    public boolean remove_listener(SamplerListener sampler_listener) {
        return fSamplerListeners.remove(sampler_listener);
    }

    /**
     * load the sample data from *raw* byte data. the method assumes a raw format with 32bit float in a value range from
     * -1.0 to 1.0.
     *
     * @param buffer raw byte data ( assuming 4 bytes per sample, 32-bit float aka WAVE_FORMAT_IEEE_FLOAT_32BIT )
     * @return instance with buffer loaded
     */
    public Sampler load(byte[] buffer) {
        load(buffer, true);
        return this;
    }

    /**
     * load the sample data from *raw* byte data. the method assumes a raw format with 32bit float in a value range from
     * -1.0 to 1.0.
     *
     * @param buffer        raw byte data ( assuming 4 bytes per sample, 32-bit float aka WAVE_FORMAT_IEEE_FLOAT_32BIT )
     * @param little_endian true if byte data is arranged in little endian order
     * @return instance with buffer loaded
     */
    public Sampler load(byte[] buffer, boolean little_endian) {
        if (fBuffer == null || fBuffer.length != buffer.length / 4) {
            fBuffer = new float[buffer.length / 4];
        }
        set_buffer(fBuffer);
        Wellen.bytes_to_floatIEEEs(buffer, get_buffer(), little_endian);
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
        fSpeed            = speed;
        fDirectionForward = speed > 0;
        set_frequency(PApplet.abs(speed) * fSamplingRate / fBuffer.length); /* aka `step_size = speed` */
    }

    public void set_frequency(float frequency) {
        if (fFrequency != frequency) {
            fFrequency = frequency;
            fStepSize  = fFrequency / fFrequencyScale * ((float) fBuffer.length / fSamplingRate);
        }
    }

    public void set_amplitude(float amplitude) {
        fAmplitude = amplitude;
    }

    public float[] get_buffer() {
        return fBuffer;
    }

    public void set_buffer(float[] buffer) {
        fBuffer = buffer;
        rewind();
        set_speed(fSpeed);
        set_in(0);
        set_out(fBuffer.length - 1);
        fLoopIn  = NO_LOOP_POINT;
        fLoopOut = NO_LOOP_POINT;
    }

    public void interpolate_samples(boolean interpolate_samples) {
        fInterpolateSamples = interpolate_samples;
    }

    public boolean interpolate_samples() {
        return fInterpolateSamples;
    }

    public int get_position() {
        return (int) fBufferIndex;
    }

    public float get_position_normalized() {
        return fBuffer.length > 0 ? (float) fBufferIndex / fBuffer.length : 0.0f;
    }

    public float get_position_fractional_part() {
        return (float) fBufferIndex - get_position();
    }

    public boolean is_playing() {
        return fIsPlaying;
    }

    public void set_duration(float seconds) {
        if (fBuffer == null || fBuffer.length == 0 || seconds == 0.0f) {
            return;
        }
        final float mNormDurationSec = (fBuffer.length / fSamplingRate);
        final float mSpeed           = mNormDurationSec / seconds;
        set_speed(mSpeed);
    }

    public float get_duration() {
        if (fBuffer == null || fBuffer.length == 0 || fSpeed == 0.0f) {
            return 0;
        }
        final float mNormDurationSec = (fBuffer.length / fSamplingRate);
        return mNormDurationSec / fSpeed;
    }

    public float output() {
        if (fBuffer.length == 0) {
            notifyListeners("buffer is empty");
            return 0.0f;
        }

        if (!fIsPlaying) {
            notifyListeners("not playing");
            return 0.0f;
        }

        validateInOutPoints();

        fBufferIndex += fDirectionForward ? fStepSize : -fStepSize;
        final int mRoundedIndex = (int) fBufferIndex;

        final double mFrac         = fBufferIndex - mRoundedIndex;
        final int    mCurrentIndex = wrapIndex(mRoundedIndex);
        fBufferIndex = mCurrentIndex + mFrac;

        if (fDirectionForward ? (mCurrentIndex >= fOutPoint) : (mCurrentIndex <= fInPoint)) {
            notifyListeners("reached end");
            fIsPlaying = false;
            return 0.0f;
        } else {
            fIsFlaggedDone = false;
        }

        return getSample(mCurrentIndex, mFrac);
    }

    private float getSample(int mCurrentIndex, double mFrac) {
        double mSample = fBuffer[mCurrentIndex];

        /* interpolate */
        if (fInterpolateSamples) {
            // TODO evaluate direction?
            final int    mNextIndex  = wrapIndex(mCurrentIndex + 1);
            final double mNextSample = fBuffer[mNextIndex];
            mSample = mSample * (1.0 - mFrac) + mNextSample * mFrac;
        }
        mSample *= fAmplitude;

        /* fade edges */
        if (fEdgeFadePadding > 0) {
            // TODO ignores in- and outpoints
            final int mRelativeIndex = fBuffer.length - mCurrentIndex;
            if (mCurrentIndex < fEdgeFadePadding) {
                final float mFadeInAmount = (float) mCurrentIndex / fEdgeFadePadding;
                mSample *= mFadeInAmount;
            } else if (mRelativeIndex < fEdgeFadePadding) {
                final float mFadeOutAmount = (float) mRelativeIndex / fEdgeFadePadding;
                mSample *= mFadeOutAmount;
            }
        }
        return (float) mSample;
    }

    public int get_edge_fading() {
        return fEdgeFadePadding;
    }

    public void set_edge_fading(int edge_fade_padding) {
        fEdgeFadePadding = edge_fade_padding;
    }

    public void rewind() {
        fBufferIndex = fDirectionForward ? fInPoint : fOutPoint;
    }

    public void forward() {
        fBufferIndex = fDirectionForward ? fOutPoint : fInPoint;
    }

    public boolean is_looping() {
        return fEvaluateLoop;
    }

    public void enable_loop(boolean loop) {
        fEvaluateLoop = loop;
    }

    public void set_loop_all() {
        fEvaluateLoop = true;
        fLoopIn       = 0;
        fLoopOut      = fBuffer.length > 0 ? (fBuffer.length - 1) : 0;
    }

    public void play() {
        fIsPlaying = true;
        fRecording.clear();
    }

    public void stop() {
        fIsPlaying = false;
    }

    public void start_recording() {
        fIsRecording = true;
    }

    public void resume_recording() {
        fIsRecording = true;
    }

    public void pause_recording() {
        fIsRecording = false;
    }

    public void delete_recording() {
        fRecording.clear();
    }

    public void record(float sample) {
        if (fIsRecording) {
            fRecording.add(sample);
        }
    }

    public void record(float[] samples) {
        if (fIsRecording) {
            for (float sample : samples) {
                fRecording.add(sample);
            }
        }
    }

    public boolean is_recording() {
        return fIsRecording;
    }

    public int get_length_recording() {
        return fRecording.size();
    }

    public int end_recording() {
        fIsRecording = false;
        float[] mBuffer = new float[fRecording.size()];
        for (int i = 0; i < fRecording.size(); i++) {
            mBuffer[i] = fRecording.get(i);
        }
        fRecording.clear();
        set_buffer(mBuffer);
        return mBuffer.length;
    }

    public int get_loop_in() {
        return fLoopIn;
    }

    public void set_loop_in(int loop_in_point) {
        fLoopIn = clamp(loop_in_point, NO_LOOP_POINT, fBuffer.length - 1);
    }

    public float get_loop_in_normalized() {
        if (fBuffer.length < 2) {
            return 0.0f;
        }
        return (float) fLoopIn / (fBuffer.length - 1);
    }

    public void set_loop_in_normalized(float loop_in_point_normalized) {
        set_loop_in((int) (loop_in_point_normalized * fBuffer.length - 1));
    }

    public int get_loop_out() {
        return fLoopOut;
    }

    public void set_loop_out(int loop_out_point) {
        fLoopOut = clamp(loop_out_point, NO_LOOP_POINT, fBuffer.length - 1);
    }

    public float get_loop_out_normalized() {
        if (fBuffer.length < 2) {
            return 0.0f;
        }
        return (float) fLoopOut / (fBuffer.length - 1);
    }

    public void set_loop_out_normalized(float loop_out_point_normalized) {
        set_loop_out((int) (loop_out_point_normalized * fBuffer.length - 1));
    }

    public void note_on() {
        rewind();
        play();
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
     * @param frequency_scale the assumed frequency of the sampler buffer in Hz
     */
    public void tune_frequency_to(float frequency_scale) {
        fFrequencyScale = frequency_scale;
    }

    private int last_index() {
        return fBuffer.length - 1;
    }

    private void notifyListeners(String event) {
        if (!fIsFlaggedDone) {
            for (SamplerListener l : fSamplerListeners) {
                l.is_done(this);
            }
        }
        fIsFlaggedDone = true;
    }

    private void validateInOutPoints() {
        if (fInPoint < 0) {
            fInPoint = 0;
        } else if (fInPoint > fBuffer.length - 1) {
            fInPoint = fBuffer.length - 1;
        }
        if (fOutPoint < 0) {
            fOutPoint = 0;
        } else if (fOutPoint > fBuffer.length - 1) {
            fOutPoint = fBuffer.length - 1;
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
        for (int i = 0; i < sampler.get_buffer().length; i += step) {
            final float r       = TWO_PI * i / sampler.get_buffer().length;
            final float mSample = map(sampler.get_buffer()[i], -1.0f, 1.0f, radius_min, radius_max);
            final float x       = cos(r) * mSample;
            final float y       = sin(r) * mSample;
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
        final float r = TWO_PI * fSampler.get_position() / fSampler.get_buffer().length;
        final float x = cos(r);
        final float y = sin(r);
        g.line(x * radius_min, y * radius_min, x * radius_max, y * radius_max);
    }
}
