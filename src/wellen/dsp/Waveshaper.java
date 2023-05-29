    /*
     * Wellen
     *
     * This file is part of the *wellen* library (https://githubiquad_com/dennisppaul/wellen).
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

    import static java.lang.Math.atan;
    import static java.lang.Math.signum;
    import static java.lang.Math.tanh;
    import static processing.core.PApplet.abs;
    import static processing.core.PApplet.max;
    import static processing.core.PApplet.sin;
    import static processing.core.PConstants.HALF_PI;
    import static wellen.Wellen.WAVESHAPER_ATAN;
    import static wellen.Wellen.WAVESHAPER_CUBIC;
    import static wellen.Wellen.WAVESHAPER_HARDCLIP;
    import static wellen.Wellen.WAVESHAPER_SIN;
    import static wellen.Wellen.WAVESHAPER_TAN_H;
    import static wellen.Wellen.clamp;

    public class Waveshaper {

        private float fAmount;
        private float fOutputGain;
        private float fBias;
        private int fType;
        private float fOneOverAtanAmount;
        private float fOneOverTanhAmount;

        public Waveshaper() {
            fAmount = (1.0f);
            fOutputGain = (1.0f);
            fBias = (0.0f);
            fType = (WAVESHAPER_ATAN);
            fOneOverAtanAmount = (1.f / (float) atan(fAmount));
        }

        public void set_amount(final float amount) {
            // 0.0 means no effect
            fAmount = max(amount, 1.0f);
            fOneOverAtanAmount = 1.0f / (float) atan(fAmount);
            fOneOverTanhAmount = 1.0f / (float) tanh(fAmount);
        }

        public void set_bias(final float bias) {
            fBias = clamp(bias);
        }

        public void set_output_gain(final float gain) {
            fOutputGain = gain;
        }

        public int get_type() {
            return fType;
        }

        public void set_type(final int type) {
            fType = type;
        }

        public void process(float[] signal_buffer) {
            switch (fType) {
                case WAVESHAPER_ATAN:
                    for (int i = 0; i < signal_buffer.length; i++) {
                        signal_buffer[i] = process_atan(signal_buffer[i]);
                    }
                    break;
                case WAVESHAPER_CUBIC:
                    for (int i = 0; i < signal_buffer.length; i++) {
                        signal_buffer[i] = process_cubic(signal_buffer[i]);
                    }
                    break;
                case WAVESHAPER_SIN:
                    for (int i = 0; i < signal_buffer.length; i++) {
                        signal_buffer[i] = process_sin(signal_buffer[i]);
                    }
                    break;
                case WAVESHAPER_HARDCLIP:
                    for (int i = 0; i < signal_buffer.length; i++) {
                        signal_buffer[i] = process_hard_clip(signal_buffer[i]);
                    }
                    break;
                case WAVESHAPER_TAN_H:
                default:
                    for (int i = 0; i < signal_buffer.length; i++) {
                        signal_buffer[i] = process_tanh(signal_buffer[i]);
                    }
                    break;
            }
        }

        public float process(float sample) {
            switch (fType) {
                case WAVESHAPER_ATAN:
                    return process_atan(sample);
                case WAVESHAPER_CUBIC:
                    return process_cubic(sample);
                case WAVESHAPER_SIN:
                    return process_sin(sample);
                case WAVESHAPER_HARDCLIP:
                    return process_hard_clip(sample);
                case WAVESHAPER_TAN_H:
                default:
                    return process_tanh(sample);
            }
        }

        private float process_hard_clip(float signal_buffer) {
            return fOutputGain * clamp(fAmount * (signal_buffer + fBias));
        }

        private float process_tanh(float signal_buffer) {
            return fOutputGain * (float) tanh((signal_buffer + fBias) * fAmount) * fOneOverTanhAmount;
        }

        private float process_atan(float signal_buffer) {
            return fOutputGain * (float) atan((signal_buffer + fBias) * fAmount) * fOneOverAtanAmount;
        }

        private float process_cubic(float signal_buffer) {
            final float CubicMax = 2.f / 3.f;
            final float OneThird = 1.f / 3.f;

            float mSample = (signal_buffer + fBias) * fAmount;

            if (abs(mSample) > 1.f) {
                mSample = signum(mSample) * CubicMax;
            } else {
                mSample = mSample - (mSample * mSample * mSample * OneThird);
            }

            mSample *= fOutputGain;
            return mSample;
        }

        private float process_sin(float signal_buffer) {
            float mSample = (signal_buffer + fBias) * fAmount;

            if (abs(mSample) > HALF_PI) {
                mSample = signum(mSample);
            } else {
                mSample = sin(mSample);
            }

            mSample *= fOutputGain;
            return mSample;
        }
    }
