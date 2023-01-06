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

/**
 * communicates with the underlying audio systems.
 */
public class AudioBufferManager {

    private final AudioDevice fImplementation;

    /**
     * @param sample_renderer renderer to be used
     * @param configuration   configuration to be used
     */
    public AudioBufferManager(AudioBufferRenderer sample_renderer, AudioDeviceConfiguration configuration) {
        if (AndroidProbe.isAndroid()) {
            fImplementation = new AudioDeviceImplAndroid(sample_renderer, configuration);
        } else {
            fImplementation = new AudioDeviceImplDesktop(sample_renderer, configuration);
        }
    }

    /**
     *
     */
    public void exit() {
        fImplementation.exit();
    }

    /**
     * @return sample rate
     */
    public int get_sample_rate() {
        return fImplementation.sample_rate();
    }

    /**
     * @return audio block or buffer size
     */
    public int get_buffer_size() {
        return fImplementation.buffer_size();
    }

    /**
     * pause or resume audio processing
     *
     * @param pause_state <code>true</code> to pause audio processing, <code>false</code> to resume
     */
    public void pause(boolean pause_state) {
        // TODO move this to interface and implement for android as well
        if (fImplementation instanceof AudioDeviceImplDesktop) {
            ((AudioDeviceImplDesktop) fImplementation).pause(pause_state);
        }
    }

    public boolean is_paused() {
        if (fImplementation instanceof AudioDeviceImplDesktop) {
            return ((AudioDeviceImplDesktop) fImplementation).is_paused();
        }
        return false;
    }
}
