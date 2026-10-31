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

package wellen;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * internal helper that lists, resolves, opens and closes MIDI devices. it is shared by {@link MidiIn} and
 * {@link MidiOut} so that device IDs and names are resolved the same way everywhere.
 * <p>
 * device IDs always refer to the position in the list printed by {@link Wellen#dumpMidiInputDevices()} and
 * {@link Wellen#dumpMidiOutputDevices()}.
 * <p>
 * device names are matched in this order: exact, case-insensitive, case-insensitive prefix, case-insensitive
 * substring. this makes names like <code>"KeyStep"</code> work on macOS ( <code>"Arturia KeyStep 37"</code> ), linux
 * ( <code>"KeyStep37 [hw:1,0,0]"</code> ) and windows ( <code>"2- Arturia KeyStep 37"</code> ).
 * <p>
 * on macOS the built-in java MIDI implementation does not detect devices that are connected after the program started
 * and handles SysEx messages poorly. if <a href="https://github.com/DerekCook/CoreMidi4J">CoreMIDI4J</a> is found on
 * the classpath ( e.g by adding <code>CoreMIDI4J.jar</code> to the sketch's <code>code</code> folder ) its devices are
 * used instead.
 */
final class MidiDevices {

    private static final String COREMIDI4J_PROVIDER = "uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider";
    private static final Map<MidiDevice, Integer> fOpenedByWellen = new IdentityHashMap<>();
    private static boolean fCoreMidi4JChecked = false;
    private static Method fCoreMidi4JGetInfo = null;

    private MidiDevices() {
    }

    /**
     * @param input <code>true</code> for input devices ( devices that transmit MIDI messages to wellen ),
     *              <code>false</code> for output devices
     * @return list of available devices in the same order as printed by the <code>dump</code> functions
     */
    static List<MidiDevice.Info> list(boolean input) {
        final ArrayList<MidiDevice.Info> mList = new ArrayList<>();
        for (MidiDevice.Info mInfo : all_device_infos()) {
            try {
                final MidiDevice mDevice = MidiSystem.getMidiDevice(mInfo);
                final int mMax = input ? mDevice.getMaxTransmitters() : mDevice.getMaxReceivers();
                if (mMax != 0) {
                    mList.add(mInfo);
                }
            } catch (MidiUnavailableException | IllegalArgumentException ignored) {
                /* device is unavailable or vanished, skip quietly */
            }
        }
        return mList;
    }

    static String[] names(boolean input) {
        final List<MidiDevice.Info> mList = list(input);
        final String[] mNames = new String[mList.size()];
        for (int i = 0; i < mNames.length; i++) {
            mNames[i] = mList.get(i).getName();
        }
        return mNames;
    }

    /**
     * @return <code>true</code> if device is provided by java itself ( e.g "Real Time Sequencer" or "Gervill" )
     * instead of being a hardware or virtual MIDI port
     */
    static boolean is_java_virtual_device(MidiDevice.Info info) {
        final String mName = info.getName();
        return "Real Time Sequencer".equals(mName) || "Gervill".equals(mName);
    }

    static MidiDevice.Info resolve(String name, boolean input) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        final List<MidiDevice.Info> mList = list(input);
        for (MidiDevice.Info mInfo : mList) {
            if (mInfo.getName().equals(name)) {
                return mInfo;
            }
        }
        for (MidiDevice.Info mInfo : mList) {
            if (mInfo.getName().equalsIgnoreCase(name)) {
                return mInfo;
            }
        }
        final String mLowerName = name.toLowerCase(Locale.ROOT);
        for (MidiDevice.Info mInfo : mList) {
            if (mInfo.getName().toLowerCase(Locale.ROOT).startsWith(mLowerName)) {
                return mInfo;
            }
        }
        for (MidiDevice.Info mInfo : mList) {
            if (mInfo.getName().toLowerCase(Locale.ROOT).contains(mLowerName)) {
                return mInfo;
            }
        }
        return null;
    }

    static MidiDevice.Info resolve(int index, boolean input) {
        final List<MidiDevice.Info> mList = list(input);
        if (index < 0 || index >= mList.size()) {
            return null;
        }
        return mList.get(index);
    }

    /**
     * resolves an index the way {@link MidiOut} did before v0.14 i.e as an index into <i>all</i> MIDI devices
     */
    static MidiDevice.Info resolve_legacy_index(int index) {
        final MidiDevice.Info[] mInfos = all_device_infos();
        if (index < 0 || index >= mInfos.length) {
            return null;
        }
        return mInfos[index];
    }

    /**
     * opens device if it is not already open. devices opened by wellen are reference counted and closed by
     * {@link #release(MidiDevice)} once the last user releases it.
     */
    static synchronized MidiDevice acquire(MidiDevice.Info info) throws MidiUnavailableException {
        final MidiDevice mDevice = MidiSystem.getMidiDevice(info);
        final Integer mCount = fOpenedByWellen.get(mDevice);
        if (mCount != null) {
            fOpenedByWellen.put(mDevice, mCount + 1);
            if (!mDevice.isOpen()) {
                mDevice.open();
            }
        } else if (!mDevice.isOpen()) {
            mDevice.open();
            fOpenedByWellen.put(mDevice, 1);
        }
        return mDevice;
    }

    static synchronized void release(MidiDevice device) {
        if (device == null) {
            return;
        }
        final Integer mCount = fOpenedByWellen.get(device);
        if (mCount == null) {
            return; /* not opened by wellen, leave it alone */
        }
        if (mCount <= 1) {
            fOpenedByWellen.remove(device);
            try {
                device.close();
            } catch (RuntimeException ignored) {
            }
        } else {
            fOpenedByWellen.put(device, mCount - 1);
        }
    }

    static void print_devices(boolean input) {
        final List<MidiDevice.Info> mList = list(input);
        System.out.println("+-------------------------------------------------------+");
        System.out.println("+ MIDI " + (input ? "INPUT" : "OUTPUT") + " DEVICES ( aka Ports or Buses )");
        System.out.println("+-------------------------------------------------------+");
        for (int i = 0; i < mList.size(); i++) {
            final MidiDevice.Info mInfo = mList.get(i);
            System.out.println("+ " + i + "\t: \"" + mInfo.getName() + "\"" + (is_java_virtual_device(mInfo) ? " " +
                    "( java built-in )" : ""));
        }
        System.out.println("+-------------------------------------------------------+");
        System.out.println();
    }

    private static MidiDevice.Info[] all_device_infos() {
        final MidiDevice.Info[] mCoreMidi4J = core_midi4j_device_infos();
        if (mCoreMidi4J != null) {
            return mCoreMidi4J;
        }
        return MidiSystem.getMidiDeviceInfo();
    }

    private static synchronized MidiDevice.Info[] core_midi4j_device_infos() {
        if (!fCoreMidi4JChecked) {
            fCoreMidi4JChecked = true;
            try {
                final Class<?> mProvider = Class.forName(COREMIDI4J_PROVIDER);
                fCoreMidi4JGetInfo = mProvider.getMethod("getMidiDeviceInfo");
                System.out.println("+++ @MIDI / using CoreMIDI4J");
            } catch (ClassNotFoundException | NoSuchMethodException | LinkageError ignored) {
                fCoreMidi4JGetInfo = null;
            }
        }
        if (fCoreMidi4JGetInfo == null) {
            return null;
        }
        try {
            /* CoreMIDI4J lists its own devices + all non-CoreMIDI java devices */
            return (MidiDevice.Info[]) fCoreMidi4JGetInfo.invoke(null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }
}
