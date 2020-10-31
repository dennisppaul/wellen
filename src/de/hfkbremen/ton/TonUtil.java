package de.hfkbremen.ton;

import com.jsyn.devices.javasound.JavaSoundAudioDevice;
import controlP5.ControlP5;
import controlP5.DropdownList;
import processing.core.PApplet;
import processing.core.PConstants;

public final class TonUtil {


    public static int clamp127(int pValue) {
        return Math.max(0, Math.min(127, pValue));
    }

    public static void dumpMidiOutputDevices() {
        final String[] mOutputNames = MidiOut.availableOutputs();
        System.out.println("+-------------------------------------------------------+");
        System.out.println("+ Midi Output Devices ( aka Ports or Buses )");
        System.out.println("+-------------------------------------------------------+");
        for (String mOutputName : mOutputNames) {
            System.out.println("+ " + mOutputName);
        }
            System.out.println("+-------------------------------------------------------+");
    }

    public static void dumpMidiInputDevices() {
        final String[] mInputNames = MidiIn.availableInputs();
        System.out.println("### Midi Input Devices\n");
        for (String mOutputName : mInputNames) {
            System.out.println("  - " + mOutputName);
        }
    }

    public static void dumpAudioDeviceInfo(final JavaSoundAudioDevice mDevice) {
        System.out.println("+-------------------------------------------------------+");
        System.out.println("AUDIO DEVICE INFO");
        System.out.println("+-------------------------------------------------------+");
        for (int i = 0; i < mDevice.getDeviceCount(); i++) {
            System.out.println("+ " + "ID ................ : " + i);
            System.out.println("+ " + "NAME .............. : " + mDevice.getDeviceName(i));
            System.out.println("+ " + "OUTPUT CHANNELS ... : " + mDevice.getMaxOutputChannels(i));
            System.out.println("+ " + "INPUT CHANNELS .... : " + mDevice.getMaxInputChannels(i));
            System.out.println("+-------------------------------------------------------+");
        }
    }

    public static void buildSelectMidiDeviceMenu(ControlP5 controls) {
        final int mListWidth = 300, mListHeight = 300;

        DropdownList dl = controls.addDropdownList("Please select MIDI Device",
                                                   (controls.papplet.width - mListWidth) / 2,
                                                   (controls.papplet.height - mListHeight) / 2,
                                                   mListWidth,
                                                   mListHeight);

        //        dl.toUpperCase(true);
        dl.setItemHeight(16);
        dl.setBarHeight(16);
        dl.getCaptionLabel().align(PConstants.LEFT, PConstants.CENTER);

        final String[] mOutputNames = MidiOut.availableOutputs();
        for (int i = 0; i < mOutputNames.length; i++) {
            dl.addItem(mOutputNames[i], i);
        }
    }

    public static int constrain(int value, int min, int max) {
        if (value > max) {
            value = max;
        }
        if (value < min) {
            value = min;
        }
        return value;
    }

    public static void run(Class<? extends PApplet> T, String... pArgs) {
        String[] mArgs;
        mArgs = PApplet.concat(new String[]{"--sketch-path=" + System.getProperty("user.dir") + "/simulator"}, pArgs);
        mArgs = PApplet.concat(mArgs, new String[]{T.getName()});
        PApplet.main(mArgs);
    }

    public static void main(String[] args) {
        TonUtil.dumpMidiOutputDevices();
    }
}
