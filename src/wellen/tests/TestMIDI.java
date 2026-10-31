package wellen.tests;

import wellen.BeatMIDI;
import wellen.EventReceiverMIDI;
import wellen.InstrumentMIDI;
import wellen.MIDI;
import wellen.MidiClockOut;
import wellen.MidiIn;
import wellen.MidiInListener;
import wellen.MidiOut;
import wellen.MidiUtilities;
import wellen.ToneEngineMIDI;
import wellen.Wellen;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

/**
 * headless test suite for the MIDI implementation. no MIDI hardware is required: messages are injected into unconnected
 * {@link MidiIn}s and recorded from {@link MidiOut}s that send into a custom {@link Receiver}.
 * <p>
 * run with <code>java -cp ... wellen.tests.TestMIDI</code>. exits with code 1 if any test fails.
 * <p>
 * see {@link TestMIDILoopback} for a test with real ( or virtual ) MIDI ports.
 */
public class TestMIDI {

    private static int fPassed = 0;
    private static final List<String> fFailed = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        test_note_on_velocity_zero_is_note_off();
        test_legacy_listener_still_receives_messages();
        test_new_message_types();
        test_system_messages();
        test_listener_exception_does_not_stop_dispatch();
        test_listener_can_be_added_during_dispatch();
        test_beat_midi_counting();
        test_beat_midi_legacy_counting();
        test_beat_midi_without_callback_is_quiet();
        test_beat_midi_bpm_estimate();
        test_event_receiver_callbacks();
        test_event_receiver_dispatch_on_draw_thread_requires_papplet();
        test_midi_out_messages();
        test_midi_out_after_close();
        test_midi_out_to_midi_in_loopback();
        test_tone_engine_note_off_turns_off_last_note();
        test_tone_engine_held_notes_and_all_notes_off();
        test_tone_engine_explicit_instrument();
        test_tone_engine_timed_note_off_does_not_change_selection();
        test_tone_engine_stop_is_safe();
        test_instrument_midi_cc_mapping();
        test_midi_utilities_and_constants();
        test_device_resolution();
        test_midi_clock_out();

        System.out.println();
        System.out.println("+++ passed: " + fPassed + " / failed: " + fFailed.size());
        for (String f : fFailed) {
            System.out.println("    FAILED: " + f);
        }
        System.exit(fFailed.isEmpty() ? 0 : 1);
    }

    /* --- MidiIn --- */

    static void test_note_on_velocity_zero_is_note_off() throws Exception {
        final MidiIn mIn = MidiIn.create_unconnected();
        final Recorder r = new Recorder();
        mIn.add_listener(r);
        mIn.send(new ShortMessage(ShortMessage.NOTE_ON, 2, 60, 0), -1);
        check("M1 note-on velocity 0 → note-off", r.events.equals(List.of("off 2 60 64")), r.events);
        r.events.clear();
        mIn.send(new ShortMessage(ShortMessage.NOTE_ON, 2, 60, 100), -1);
        mIn.send(new ShortMessage(ShortMessage.NOTE_OFF, 2, 60, 33), -1);
        check("M1 note-on / note-off with release velocity", r.events.equals(List.of("on 2 60 100", "off 2 60 33")),
              r.events);
    }

    @SuppressWarnings("deprecation")
    static void test_legacy_listener_still_receives_messages() throws Exception {
        final MidiIn mIn = MidiIn.create_unconnected();
        final List<String> mEvents = Collections.synchronizedList(new ArrayList<>());
        /* implements only the old ( deprecated ) camelCase methods, like sketches written before v0.14 */
        mIn.addListener(new MidiInListener() {
            public void receiveProgramChange(int channel, int number, int value) {
                mEvents.add("pc " + channel + " " + number + " " + value);
            }

            public void receiveControlChange(int channel, int number, int value) {
                mEvents.add("cc " + channel + " " + number + " " + value);
            }

            public void receiveNoteOff(int channel, int pitch) {
                mEvents.add("off " + channel + " " + pitch);
            }

            public void receiveNoteOn(int channel, int pitch, int velocity) {
                mEvents.add("on " + channel + " " + pitch + " " + velocity);
            }

            public void clock_tick() {
                mEvents.add("tick");
            }

            public void clock_start() {
            }

            public void clock_continue() {
            }

            public void clock_stop() {
            }

            public void clock_song_position_pointer(int pOffset16th) {
                mEvents.add("spp " + pOffset16th);
            }
        });
        mIn.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 48, 90), -1);
        mIn.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 48, 0), -1);
        mIn.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 1, 7, 100), -1);
        mIn.send(new ShortMessage(ShortMessage.PROGRAM_CHANGE, 3, 12, 0), -1);
        mIn.send(new ShortMessage(ShortMessage.TIMING_CLOCK), -1);
        mIn.send(new ShortMessage(ShortMessage.SONG_POSITION_POINTER, 3, 1), -1);
        check("M18 deprecated listener methods are still called",
              mEvents.equals(List.of("on 0 48 90", "off 0 48", "cc 1 7 100", "pc 3 12 0", "tick", "spp 131")),
              mEvents);
    }

    static void test_new_message_types() throws Exception {
        final MidiIn mIn = MidiIn.create_unconnected();
        final Recorder r = new Recorder();
        mIn.add_listener(r);
        mIn.send(new ShortMessage(ShortMessage.PITCH_BEND, 4, 0x00, 0x40), -1); // center
        mIn.send(new ShortMessage(ShortMessage.PITCH_BEND, 4, 0x7F, 0x7F), -1); // max
        mIn.send(new ShortMessage(ShortMessage.CHANNEL_PRESSURE, 5, 99, 0), -1);
        mIn.send(new ShortMessage(ShortMessage.POLY_PRESSURE, 6, 64, 77), -1);
        mIn.send(new ShortMessage(ShortMessage.PROGRAM_CHANGE, 7, 42, 0), -1);
        final byte[] mSysex = {(byte) 0xF0, 0x7E, 0x7F, 0x06, 0x01, (byte) 0xF7};
        mIn.send(new SysexMessage(mSysex, mSysex.length), -1);
        check("M10 pitch bend, pressure, poly pressure, program change, sysex", r.events.equals(List.of("pb 4 8192",
                                                                                                        "pb 4 16383",
                                                                                                        "cp 5 99",
                                                                                                        "pp 6 64 77",
                                                                                                        "pc 7 42",
                                                                                                        "sysex " + Arrays.toString(mSysex))), r.events);
    }

    static void test_system_messages() throws Exception {
        final MidiIn mIn = MidiIn.create_unconnected();
        final Recorder r = new Recorder();
        mIn.add_listener(r);
        mIn.send(new ShortMessage(ShortMessage.START), -1);
        mIn.send(new ShortMessage(ShortMessage.TIMING_CLOCK), -1);
        mIn.send(new ShortMessage(ShortMessage.STOP), -1);
        mIn.send(new ShortMessage(ShortMessage.SONG_POSITION_POINTER, 0x7F, 0x7F), -1);
        mIn.send(new ShortMessage(ShortMessage.CONTINUE), -1);
        mIn.send(new ShortMessage(ShortMessage.SONG_SELECT, 5, 0), -1);
        mIn.send(new ShortMessage(ShortMessage.MIDI_TIME_CODE, 0x23, 0), -1);
        mIn.send(new ShortMessage(ShortMessage.ACTIVE_SENSING), -1);
        mIn.send(new ShortMessage(ShortMessage.SYSTEM_RESET), -1);
        check("system realtime + common messages", r.events.equals(List.of("start", "tick", "stop", "spp 16383",
                                                                           "continue", "song 5", "mtc 35")), r.events);
    }

    static void test_listener_exception_does_not_stop_dispatch() throws Exception {
        final MidiIn mIn = MidiIn.create_unconnected();
        mIn.add_listener(new MidiInListener() {
            public void receive_note_on(int channel, int pitch, int velocity) {
                throw new RuntimeException("expected test exception ( only reported once )");
            }
        });
        final Recorder r = new Recorder();
        mIn.add_listener(r);
        mIn.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 100), -1);
        mIn.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 61, 100), -1);
        check("M7 exception in one listener does not block others", r.events.size() == 2, r.events);
    }

    static void test_listener_can_be_added_during_dispatch() throws Exception {
        final MidiIn mIn = MidiIn.create_unconnected();
        final Recorder r = new Recorder();
        mIn.add_listener(new MidiInListener() {
            public void receive_note_on(int channel, int pitch, int velocity) {
                mIn.add_listener(r);
            }
        });
        boolean mOK = true;
        try {
            mIn.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 100), -1);
            mIn.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 61, 100), -1);
        } catch (RuntimeException e) {
            mOK = false;
        }
        check("M7 adding listener during dispatch is safe", mOK && r.events.size() == 1, r.events);
    }

    /* --- BeatMIDI --- */

    public static class BeatSketch {
        final List<Integer> beats = Collections.synchronizedList(new ArrayList<>());

        public void beat(int beat) {
            beats.add(beat);
        }
    }

    static void test_beat_midi_counting() throws Exception {
        final MidiIn mIn = MidiIn.create_unconnected();
        final BeatSketch s = new BeatSketch();
        final BeatMIDI b = BeatMIDI.start(s, mIn);
        mIn.send(new ShortMessage(ShortMessage.START), -1);
        tick(mIn, 3);
        check("M3 first beat after start is 0", s.beats.equals(List.of(0, 1, 2)), s.beats);
        check("M3 beat_count returns last delivered beat", b.beat_count() == 2, b.beat_count());
        s.beats.clear();
        mIn.send(new ShortMessage(ShortMessage.STOP), -1);
        tick(mIn, 2); // ignored while stopped
        mIn.send(new ShortMessage(ShortMessage.SONG_POSITION_POINTER, 3, 0), -1);
        mIn.send(new ShortMessage(ShortMessage.CONTINUE), -1);
        tick(mIn, 2);
        check("M3 song position 3 ( 4th sixteenth ) → beat 18", s.beats.equals(List.of(18, 19)), s.beats);
        s.beats.clear();
        mIn.send(new ShortMessage(ShortMessage.SONG_POSITION_POINTER, 5, 2), -1); // 5 + 2 * 128 = 261
        mIn.send(new ShortMessage(ShortMessage.CONTINUE), -1);
        tick(mIn, 1);
        check("M3 song position uses 14 bit value", s.beats.equals(List.of(261 * 6)), s.beats);
        s.beats.clear();
        mIn.send(new ShortMessage(ShortMessage.STOP), -1);
        mIn.send(new ShortMessage(ShortMessage.CONTINUE), -1);
        tick(mIn, 1);
        check("stop resets counter by default", s.beats.equals(List.of(0)), s.beats);
        b.close();
    }

    @SuppressWarnings("deprecation")
    static void test_beat_midi_legacy_counting() throws Exception {
        BeatMIDI.LEGACY_BEAT_COUNT = true;
        try {
            final MidiIn mIn = MidiIn.create_unconnected();
            final BeatSketch s = new BeatSketch();
            BeatMIDI.start(s, mIn);
            tick(mIn, 1);
            mIn.send(new ShortMessage(ShortMessage.START), -1);
            tick(mIn, 2);
            check("LEGACY_BEAT_COUNT restores old counting", s.beats.equals(List.of(0, 1, 2)), s.beats);
        } finally {
            BeatMIDI.LEGACY_BEAT_COUNT = false;
        }
    }

    static void test_beat_midi_without_callback_is_quiet() throws Exception {
        final MidiIn mIn = MidiIn.create_unconnected();
        BeatMIDI.start(new Object(), mIn); // prints one error at construction
        boolean mOK = true;
        try {
            tick(mIn, 48);
        } catch (RuntimeException e) {
            mOK = false;
        }
        check("M6 missing beat(int) does not throw on ticks", mOK, "");
    }

    static void test_beat_midi_bpm_estimate() throws Exception {
        final MidiIn mIn = MidiIn.create_unconnected();
        final BeatMIDI b = BeatMIDI.start(new BeatSketch(), mIn);
        mIn.send(new ShortMessage(ShortMessage.START), -1);
        check("M5 bpm is 0 before two ticks", b.bpm() == 0, b.bpm());
        final long mIntervalNs = (long) (60.0e9 / (120 * 24));
        long mNext = System.nanoTime();
        for (int i = 0; i < 72; i++) {
            while (System.nanoTime() < mNext) {
                LockSupport.parkNanos(200_000);
            }
            mIn.send(new ShortMessage(ShortMessage.TIMING_CLOCK), -1);
            mNext += mIntervalNs;
        }
        check("M5 bpm estimate at 120 BPM within ±5%", Math.abs(b.bpm() - 120) < 6, b.bpm());
    }

    /* --- EventReceiverMIDI --- */

    public static class ReceiverSketch {
        final List<String> events = Collections.synchronizedList(new ArrayList<>());

        public void midi_note_on(int channel, int pitch, int velocity) {
            events.add("on " + channel + " " + pitch + " " + velocity);
        }

        public void midi_note_off(int channel, int pitch) {
            events.add("off2 " + channel + " " + pitch);
        }

        public void midi_note_off(int channel, int pitch, int velocity) {
            events.add("off3 " + channel + " " + pitch + " " + velocity);
        }

        public void midi_program_change(int channel, int number, int value) {
            events.add("pc3 " + channel + " " + number);
        }

        public void midi_pitch_bend(int channel, int value) {
            events.add("pb " + channel + " " + value);
        }

        public void event_receive(int event, float[] data) {
            events.add("event " + event + " " + data.length);
        }

        /* private callbacks must work too */
        private void midi_clock_tick() {
            events.add("tick");
        }
    }

    static void test_event_receiver_callbacks() throws Exception {
        final MidiIn mIn = MidiIn.create_unconnected();
        final ReceiverSketch s = new ReceiverSketch();
        final EventReceiverMIDI e = EventReceiverMIDI.start(s, mIn);
        mIn.send(new ShortMessage(ShortMessage.NOTE_ON, 1, 60, 100), -1);
        mIn.send(new ShortMessage(ShortMessage.NOTE_ON, 1, 60, 0), -1);
        mIn.send(new ShortMessage(ShortMessage.PROGRAM_CHANGE, 1, 9, 0), -1);
        mIn.send(new ShortMessage(ShortMessage.PITCH_BEND, 1, 0, 0x40), -1);
        mIn.send(new ShortMessage(ShortMessage.TIMING_CLOCK), -1);
        check("M9/M10/M15 EventReceiverMIDI callbacks", s.events.equals(List.of("event " + Wellen.EVENT_NOTE_ON + " 3",
                                                                                "on 1 60 100",
                                                                                "event " + Wellen.EVENT_NOTE_OFF + " 2"
                , "off3 1 60 64", "event " + Wellen.EVENT_PROGRAMCHANGE + " 3", "pc3 1 9", "event " + Wellen.EVENT_PITCHBEND + " 2", "pb 1 8192", "event " + MIDI.MIDI_CLOCK_TICK + " 0", "tick")), s.events);
        e.stop();
        s.events.clear();
        mIn.send(new ShortMessage(ShortMessage.NOTE_ON, 1, 60, 100), -1);
        check("EventReceiverMIDI.stop() detaches", s.events.isEmpty(), s.events);

        final EventReceiverMIDI a = EventReceiverMIDI.start(s, "this device does not exist");
        check("M9 unknown device → not initialized", !a.is_initialized(), "");
    }

    static void test_event_receiver_dispatch_on_draw_thread_requires_papplet() throws Exception {
        final MidiIn mIn = MidiIn.create_unconnected();
        final ReceiverSketch s = new ReceiverSketch();
        EventReceiverMIDI.start(s, mIn).dispatch_on_draw_thread(true); // prints warning, falls back to immediate
        mIn.send(new ShortMessage(ShortMessage.NOTE_ON, 1, 60, 100), -1);
        check("M13 non-PApplet parent falls back to immediate dispatch", s.events.contains("on 1 60 100"), s.events);
    }

    /* --- MidiOut --- */

    static void test_midi_out_messages() throws Exception {
        final MessageRecorder r = new MessageRecorder();
        final MidiOut o = new MidiOut(r);
        o.send_note_on(20, 200, -5);
        o.send_program_change(1, 5);
        o.send_pitch_bend(2, 8192);
        o.send_pitch_bend(2, 99999);
        o.send_all_notes_off(3);
        o.send_clock_tick();
        o.send_song_position_pointer(131);
        o.send_sysex(new byte[]{(byte) 0xF0, 0x01, (byte) 0xF7});
        o.send_note_off_to_all(0);
        check("M14 clamping, program change, pitch bend, CC 123, clock, SPP, sysex",
              r.hex.subList(0, 9).equals(List.of("9F 7F 00",
                                                 "C1 05",
                                                 "E2 00 40",
                                                 "E2 7F 7F",
                                                 "B3 40 00",
                                                 "B3 7B 00",
                                                 "F8",
                                                 "F2 03 01",
                                                 "F0 01 F7")), r.hex);
        check("M11 note off to all covers 0–127", r.hex.size() == 9 + 128 && r.hex.get(r.hex.size() - 1).equals("80 " +
                                                                                                                 "7F 00"), r.hex.size());
        check("M14 timestamp is -1", r.timestamps.stream().allMatch(t -> t == -1), r.timestamps);
    }

    static void test_midi_out_after_close() {
        final MidiOut o = new MidiOut(new Receiver() {
            boolean open = true;

            public void send(MidiMessage message, long timeStamp) {
                if (!open) {
                    throw new IllegalStateException("closed");
                }
            }

            public void close() {
                open = false;
            }
        });
        o.close();
        boolean mOK = true;
        try {
            o.send_note_on(0, 60, 100);
        } catch (RuntimeException e) {
            mOK = false;
        }
        check("M11 sending after close is ignored", mOK && !o.is_open(), "");
    }

    static void test_midi_out_to_midi_in_loopback() {
        final MidiIn mIn = MidiIn.create_unconnected();
        final Recorder r = new Recorder();
        mIn.add_listener(r);
        final MidiOut o = new MidiOut(mIn);
        o.send_note_on(0, 60, 100);
        o.send_note_off(0, 60, 10);
        o.send_control_change(1, 74, 64);
        o.send_program_change(2, 3);
        o.send_pitch_bend(3, 1234);
        o.send_channel_pressure(4, 5);
        o.send_poly_pressure(5, 6, 7);
        o.send_clock_start();
        o.send_clock_tick();
        o.send_clock_stop();
        o.send_song_position_pointer(1000);
        o.send_clock_continue();
        o.send_sysex(new byte[]{(byte) 0xF0, 0x42, (byte) 0xF7});
        check("MidiOut → MidiIn loopback", r.events.equals(List.of("on 0 60 100", "off 0 60 10", "cc 1 74 64", "pc 2 3",
                                                                   "pb 3 1234", "cp 4 5", "pp 5 6 7", "start", "tick",
                                                                   "stop", "spp 1000", "continue", "sysex [-16, 66, " +
                                                                           "-9]")), r.events);
    }

    /* --- ToneEngineMIDI + InstrumentMIDI --- */

    static void test_tone_engine_note_off_turns_off_last_note() {
        final MessageRecorder r = new MessageRecorder();
        final ToneEngineMIDI t = new ToneEngineMIDI(new MidiOut(r));
        t.instrument(2);
        t.note_on(64, 100);
        t.note_off();
        check("M4 note_off() turns off last played note", r.hex.equals(List.of("92 40 64", "82 40 00")), r.hex);
        t.stop();
    }

    static void test_tone_engine_held_notes_and_all_notes_off() {
        final MessageRecorder r = new MessageRecorder();
        final ToneEngineMIDI t = new ToneEngineMIDI(new MidiOut(r));
        t.instrument(0);
        t.note_on(60, 100);
        t.note_on(64, 100);
        t.note_off(60);
        check("M17 is_playing while another note is held", t.is_playing(), "");
        t.note_off(64);
        check("M17 not playing after all notes are off", !t.is_playing(), "");
        t.note_on(67, 100);
        r.hex.clear();
        t.all_notes_off();
        check("M11 all_notes_off sends note-off for held notes + CC 64/123",
              r.hex.subList(0, 3).equals(List.of("80 43 00", "B0 40 00", "B0 7B 00")) && r.hex.size() == 1 + 2 * 16,
              r.hex);
        t.stop();
    }

    static void test_tone_engine_explicit_instrument() {
        final MessageRecorder r = new MessageRecorder();
        final ToneEngineMIDI t = new ToneEngineMIDI(new MidiOut(r));
        t.instrument(1);
        t.note_on_instrument(9, 36, 127);
        t.note_off_instrument(9, 36);
        check("M13 note_on_instrument uses channel without changing selection", r.hex.equals(List.of("99 24 7F", "89 " +
                "24 00")) && t.instrument().ID() == 1, r.hex);
        t.stop();
    }

    static void test_tone_engine_timed_note_off_does_not_change_selection() throws Exception {
        final MessageRecorder r = new MessageRecorder();
        final ToneEngineMIDI t = new ToneEngineMIDI(new MidiOut(r));
        for (int i = 0; i < 3000; i++) {
            t.instrument(0);
            t.note_on(60, 100, 0.0f); // note-off is scheduled immediately on the timer thread
            t.instrument(5);
            t.note_on(70, 100);
            t.note_off(70);
        }
        Thread.sleep(200);
        int mWrongChannel = 0;
        int mNote60OffBeforeOn = 0;
        boolean m60On = false;
        for (String m : r.snapshot()) {
            if (m.endsWith(" 46 64") || m.endsWith(" 46 00")) { // note 70
                if (!(m.startsWith("95") || m.startsWith("85"))) {
                    mWrongChannel++;
                }
            }
        }
        check("G1/M13 timed note-off does not move notes to other channels", mWrongChannel == 0,
              mWrongChannel + " misrouted");
        /* every note 60 on channel 0 must be followed by its off ( never off before on ) */
        final List<String> mCh0 = new ArrayList<>();
        for (String m : r.snapshot()) {
            if (m.equals("90 3C 64") || m.equals("80 3C 00")) {
                mCh0.add(m);
            }
        }
        check("G1 timed note-off is sent after note-on", mCh0.size() == 6000 && mCh0.get(0).equals("90 3C 64"),
              mCh0.size());
        t.stop();
    }

    static void test_tone_engine_stop_is_safe() {
        final MessageRecorder r = new MessageRecorder();
        final ToneEngineMIDI t = new ToneEngineMIDI(new MidiOut(r));
        t.note_on(60, 100);
        t.stop();
        boolean mOK = true;
        try {
            t.stop();
            t.note_on(61, 100);
        } catch (RuntimeException e) {
            mOK = false;
        }
        check("M11 stop() turns notes off and is idempotent", mOK && r.hex.contains("80 3C 00"), r.hex);
    }

    static void test_instrument_midi_cc_mapping() {
        final MessageRecorder r = new MessageRecorder();
        final InstrumentMIDI i = new InstrumentMIDI(17, new MidiOut(r)); // channel 1
        i.set_amplitude(1.0f);
        i.set_pan(-1.0f);
        i.set_pan(1.0f);
        i.set_filter_frequency(20000);
        i.set_filter_resonance(0.5f);
        i.note_on(MIDI.NOTE_A4, 100);
        i.pitch_bend(0.0f);
        i.pitch_bend(440.0f * (float) Math.pow(2, 2.0 / 12.0) - 440.0f); // +2 semitones = max with range 2
        check("M17 CC mapping + pitch bend in semitones", r.hex.equals(List.of("B1 07 7F", "B1 0A 00", "B1 0A 7F", "B1 " +
                "4A 7F", "B1 47 40", "91 45 64", "E1 00 40", "E1 7F 7F")), r.hex);
        final ToneEngineMIDI t = new ToneEngineMIDI(new MidiOut(r));
        final InstrumentMIDI mReplacement = new InstrumentMIDI(3);
        t.replace_instrument(mReplacement);
        check("replace_instrument connects instrument to MIDI output", mReplacement.get_midi_out() == t.get_midi_out(),
              "");
        t.stop();
    }

    /* --- utilities --- */

    static void test_midi_utilities_and_constants() {
        boolean mOK = true;
        try {
            MidiUtilities.note_to_string(-1);
        } catch (RuntimeException e) {
            mOK = false;
        }
        check("M16 note_to_string handles negative notes", mOK && MidiUtilities.note_to_string(60).equals("C4"),
              MidiUtilities.note_to_string(60));
        check("M16 constants", MIDI.NOTE_C0 == 12 && MIDI.NOTE_CS4 == 61 && MIDI.NOTE_A4 == 69 && MIDI.NOTE_G9 == 127 && MIDI.CC_ALL_NOTES_OFF == 123 && MIDI.PULSES_PER_SIXTEENTH == 6, "");
    }

    static void test_device_resolution() {
        /* java always provides the "Gervill" synthesizer ( output ) and "Real Time Sequencer" ( input + output ) */
        final String[] mOutputs = MidiOut.available_outputs();
        final int mGervill = Arrays.asList(mOutputs).indexOf("Gervill");
        if (mGervill < 0) {
            System.out.println("+++ skipping device tests, `Gervill` not available");
            return;
        }
        final MidiOut mByName = new MidiOut("gerv");
        check("M12 case-insensitive prefix match", mByName.get_device_name().equals("Gervill"),
              mByName.get_device_name());
        final MidiOut mByIndex = new MidiOut(mGervill);
        check("M2 MidiOut(int) uses index from dumpMidiOutputDevices()", mByIndex.get_device_name().equals("Gervill"),
              mByIndex.get_device_name());
        mByName.close();
        mByIndex.close();
        check("M12 Wellen.isMidiDeviceAvailable uses same matching", Wellen.isMidiDeviceAvailable("GERVILL"), "");
        final MidiOut mMissing = new MidiOut("this device does not exist");
        check("M12 unknown device is reported and not initialized", !mMissing.is_initialized(), "");
    }

    static void test_midi_clock_out() throws Exception {
        final MidiIn mIn = MidiIn.create_unconnected();
        final BeatSketch s = new BeatSketch();
        final BeatMIDI b = BeatMIDI.start(s, mIn);
        final MidiClockOut c = new MidiClockOut(new MidiOut(mIn), 120);
        c.start();
        Thread.sleep(1000);
        c.stop();
        final int mTicks = s.beats.size();
        check("M14 MidiClockOut sends ~48 pulses per second at 120 BPM", mTicks >= 46 && mTicks <= 50, mTicks);
        check("M14 MidiClockOut → BeatMIDI starts at 0", !s.beats.isEmpty() && s.beats.get(0) == 0, s.beats);
        check("M14 BeatMIDI measures MidiClockOut tempo", Math.abs(b.bpm() - 120) < 3, b.bpm());
        check("M14 clock stop is received", !b.running(), "");
        c.dispose();
    }

    /* --- helpers --- */

    static void tick(MidiIn in, int n) throws Exception {
        for (int i = 0; i < n; i++) {
            in.send(new ShortMessage(ShortMessage.TIMING_CLOCK), -1);
        }
    }

    static void check(String name, boolean condition, Object detail) {
        if (condition) {
            fPassed++;
            System.out.println("+ PASS : " + name);
        } else {
            fFailed.add(name + " → " + detail);
            System.out.println("- FAIL : " + name + " → " + detail);
        }
    }

    static class Recorder implements MidiInListener {
        final List<String> events = Collections.synchronizedList(new ArrayList<>());

        public void receive_note_on(int c, int p, int v) {
            events.add("on " + c + " " + p + " " + v);
        }

        public void receive_note_off(int c, int p, int v) {
            events.add("off " + c + " " + p + " " + v);
        }

        public void receive_control_change(int c, int n, int v) {
            events.add("cc " + c + " " + n + " " + v);
        }

        public void receive_program_change(int c, int p) {
            events.add("pc " + c + " " + p);
        }

        public void receive_pitch_bend(int c, int v) {
            events.add("pb " + c + " " + v);
        }

        public void receive_channel_pressure(int c, int p) {
            events.add("cp " + c + " " + p);
        }

        public void receive_poly_pressure(int c, int n, int p) {
            events.add("pp " + c + " " + n + " " + p);
        }

        public void receive_sysex(byte[] d) {
            events.add("sysex " + Arrays.toString(d));
        }

        public void receive_song_select(int s) {
            events.add("song " + s);
        }

        public void receive_time_code_quarter_frame(int d) {
            events.add("mtc " + d);
        }

        public void clock_tick() {
            events.add("tick");
        }

        public void clock_start() {
            events.add("start");
        }

        public void clock_continue() {
            events.add("continue");
        }

        public void clock_stop() {
            events.add("stop");
        }

        public void clock_song_position_pointer(int o) {
            events.add("spp " + o);
        }
    }

    static class MessageRecorder implements Receiver {
        final List<String> hex = Collections.synchronizedList(new ArrayList<>());
        final List<Long> timestamps = Collections.synchronizedList(new ArrayList<>());

        public void send(MidiMessage message, long timestamp) {
            final StringBuilder sb = new StringBuilder();
            for (byte b : message.getMessage()) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(String.format("%02X", b & 0xFF));
            }
            hex.add(sb.toString());
            timestamps.add(timestamp);
        }

        List<String> snapshot() {
            synchronized (hex) {
                return new ArrayList<>(hex);
            }
        }

        public void close() {
        }
    }
}
