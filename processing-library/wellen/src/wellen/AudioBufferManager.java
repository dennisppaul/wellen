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

    public AudioBufferManager(AudioBufferRenderer pSampleRenderer, AudioDeviceConfiguration pConfiguration) {
        if (AndroidProbe.isAndroid()) {
            fImplementation = new AudioDeviceImplAndroid(pSampleRenderer, pConfiguration);
        } else {
            fImplementation = new AudioDeviceImplDesktop(pSampleRenderer, pConfiguration);
        }
    }

    public void exit() {
        fImplementation.exit();
    }

    public int sample_rate() {
        return fImplementation.sample_rate();
    }

    public int buffer_size() {
        return fImplementation.buffer_size();
    }
}
