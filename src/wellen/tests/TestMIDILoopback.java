package wellen.tests;

import wellen.MidiIn;
import wellen.MidiInListener;
import wellen.MidiOut;
import wellen.Wellen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * tests sending and receiving MIDI messages through a real MIDI port. messages are sent with {@link MidiOut} and
 * received with {@link MidiIn} on the same port.
 * <p>
 * a virtual loopback port is required:
 * <ul>
 *     <li>macOS: enable <i>IAC Driver</i> in <i>Audio MIDI Setup › MIDI Studio</i> ( default port name: <code>"IAC
 *     Driver Bus 1"</code> )</li>
 *     <li>windows: create a port with <a href="https://www.tobias-erichsen.de/software/loopmidi.html">loopMIDI</a></li>
 *     <li>linux: <code>sudo modprobe snd-virmidi</code> and connect the ports with <code>aconnect</code></li>
 * </ul>
 * run with <code>java -cp ... wellen.tests.TestMIDILoopback [port name]</code>. exits with code 1 if any message is
 * lost or changed and with code 2 if the port is not available.
 */
public class TestMIDILoopback {

    public static void main(String[] args) throws Exception {
        final String mPort = args.length > 0 ? args[0] : "IAC Driver Bus 1";
        if (!Wellen.isMidiDeviceAvailable(mPort)) {
            System.out.println("+++ port `" + mPort + "` not available, skipping loopback test.");
            Wellen.dumpMidiInputDevices();
            System.exit(2);
        }
        final MidiIn mIn = new MidiIn(mPort);
        final MidiOut mOut = new MidiOut(mPort);
        final List<String> mReceived = Collections.synchronizedList(new ArrayList<>());
        mIn.add_listener(new MidiInListener() {
            public void receive_note_on(int c, int p, int v) {
                mReceived.add("on " + c + " " + p + " " + v);
            }

            public void receive_note_off(int c, int p, int v) {
                mReceived.add("off " + c + " " + p + " " + v);
            }

            public void receive_control_change(int c, int n, int v) {
                mReceived.add("cc " + c + " " + n + " " + v);
            }

            public void receive_program_change(int c, int p) {
                mReceived.add("pc " + c + " " + p);
            }

            public void receive_pitch_bend(int c, int v) {
                mReceived.add("pb " + c + " " + v);
            }

            public void receive_channel_pressure(int c, int p) {
                mReceived.add("cp " + c + " " + p);
            }

            public void receive_poly_pressure(int c, int n, int p) {
                mReceived.add("pp " + c + " " + n + " " + p);
            }

            public void receive_sysex(byte[] d) {
                mReceived.add("sysex " + Arrays.toString(d));
            }

            public void clock_tick() {
                mReceived.add("tick");
            }

            public void clock_start() {
                mReceived.add("start");
            }

            public void clock_continue() {
                mReceived.add("continue");
            }

            public void clock_stop() {
                mReceived.add("stop");
            }

            public void clock_song_position_pointer(int o) {
                mReceived.add("spp " + o);
            }
        });
        System.out.println("+++ testing loopback on `" + mIn.get_device_name() + "`");

        final List<String> mExpected = new ArrayList<>();
        mOut.send_note_on(0, 60, 100);
        mExpected.add("on 0 60 100");
        mOut.send_note_on(0, 60, 0);
        mExpected.add("off 0 60 64");
        mOut.send_note_off(15, 127, 12);
        mExpected.add("off 15 127 12");
        mOut.send_control_change(1, 74, 64);
        mExpected.add("cc 1 74 64");
        mOut.send_program_change(2, 3);
        mExpected.add("pc 2 3");
        mOut.send_pitch_bend(3, 1234);
        mExpected.add("pb 3 1234");
        mOut.send_channel_pressure(4, 5);
        mExpected.add("cp 4 5");
        mOut.send_poly_pressure(5, 6, 7);
        mExpected.add("pp 5 6 7");
        final boolean mSystemMessages = mOut.can_send_clock_and_sysex();
        if (!mSystemMessages) {
            System.out.println("+++ known limitation: this output can not send clock and SysEx messages, skipping them");
        }
        if (mSystemMessages) {
            mOut.send_clock_start();
            mExpected.add("start");
            mOut.send_clock_tick();
            mExpected.add("tick");
            mOut.send_clock_stop();
            mExpected.add("stop");
        }
        mOut.send_song_position_pointer(1000);
        mExpected.add("spp 1000");
        if (mSystemMessages) {
            mOut.send_clock_continue();
            mExpected.add("continue");
        }
        final boolean mTestSysex = mSystemMessages && (args.length < 2 || !args[1].equals("--no-sysex"));
        if (mTestSysex) {
            mOut.send_sysex(new byte[]{(byte) 0xF0, 0x7D, 0x01, 0x02, (byte) 0xF7});
            mExpected.add("sysex [-16, 125, 1, 2, -9]");
        }

        final long mTimeout = System.currentTimeMillis() + 2000;
        while (mReceived.size() < mExpected.size() && System.currentTimeMillis() < mTimeout) {
            Thread.sleep(10);
        }
        mIn.close();
        mOut.close();

        int mFailed = 0;
        for (int i = 0; i < mExpected.size(); i++) {
            final String mGot = i < mReceived.size() ? mReceived.get(i) : "( nothing )";
            final boolean mOK = mExpected.get(i).equals(mGot);
            if (!mOK) {
                mFailed++;
            }
            System.out.println((mOK ? "+ PASS : " : "- FAIL : ") + mExpected.get(i) + (mOK ? "" : " → got " + mGot));
        }
        if (mReceived.size() > mExpected.size()) {
            System.out.println("- unexpected extra messages: " + mReceived.subList(mExpected.size(), mReceived.size()));
            mFailed++;
        }
        System.out.println("+++ " + (mFailed == 0 ? "all messages received" : mFailed + " problems"));
        System.exit(mFailed == 0 ? 0 : 1);
    }
}
