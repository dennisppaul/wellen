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

public class AudioDeviceConfiguration {
    public int sample_rate = Wellen.DEFAULT_SAMPLING_RATE;
    public int sample_buffer_size = Wellen.DEFAULT_AUDIOBLOCK_SIZE; // i.e audio_block_size
    public int output_device = Wellen.DEFAULT_AUDIO_DEVICE;
    public int number_of_output_channels = Wellen.STEREO;
    public int input_device = Wellen.DEFAULT_AUDIO_DEVICE;
    public int number_of_input_channels = Wellen.MONO;
    public int bits_per_sample = Wellen.DEFAULT_BITS_PER_SAMPLE;

    public static AudioDeviceConfiguration create() {
        return new AudioDeviceConfiguration();
    }

    public AudioDeviceConfiguration set_sample_rate(int pSampleRate) {
        sample_rate = pSampleRate;
        return this;
    }

    public AudioDeviceConfiguration set_sample_buffer_size(int pSampleBufferSize) {
        sample_buffer_size = pSampleBufferSize;
        return this;
    }

    public AudioDeviceConfiguration set_output_device(int pOutputDevice) {
        output_device = pOutputDevice;
        return this;
    }

    public AudioDeviceConfiguration set_number_of_output_channels(int pNumberOfOutputChannels) {
        number_of_output_channels = pNumberOfOutputChannels;
        return this;
    }

    public AudioDeviceConfiguration set_input_device(int pInputDevice) {
        input_device = pInputDevice;
        return this;
    }

    public AudioDeviceConfiguration set_number_of_input_channels(int pNumberOfInputChannels) {
        number_of_input_channels = pNumberOfInputChannels;
        return this;
    }

    public AudioDeviceConfiguration set_bits_per_sample(int pBitsPerSample) {
        bits_per_sample = pBitsPerSample;
        return this;
    }
}
