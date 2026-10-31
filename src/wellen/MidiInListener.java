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

/**
 * interface for listeners of {@link wellen.MidiIn}.
 * <p>
 * all methods have empty default implementations, so a listener only needs to implement the methods it is interested
 * in. methods are called on the MIDI thread, not on the processing animation thread.
 * <p>
 * since v0.14 methods use <code>snake_case</code> names ( e.g {@link #receive_note_on(int, int, int)} ). the older
 * <code>camelCase</code> methods ( e.g {@link #receiveNoteOn(int, int, int)} ) are deprecated but still called: the
 * default implementation of each new method forwards to its old counterpart.
 */
public interface MidiInListener {

    /* --- channel voice messages --- */

    /**
     * @param channel  MIDI channel ( 0–15 )
     * @param pitch    note ( 0–127 )
     * @param velocity velocity ( 1–127 )
     */
    default void receive_note_on(int channel, int pitch, int velocity) {
        receiveNoteOn(channel, pitch, velocity);
    }

    /**
     * called for note-off messages <i>and</i> for note-on messages with velocity 0.
     *
     * @param channel  MIDI channel ( 0–15 )
     * @param pitch    note ( 0–127 )
     * @param velocity release velocity ( 0–127, <code>64</code> if sent as note-on with velocity 0 )
     */
    default void receive_note_off(int channel, int pitch, int velocity) {
        receiveNoteOff(channel, pitch);
    }

    /**
     * @param channel MIDI channel ( 0–15 )
     * @param number  controller number ( 0–127 ), see <code>MIDI.CC_*</code>
     * @param value   controller value ( 0–127 )
     */
    default void receive_control_change(int channel, int number, int value) {
        receiveControlChange(channel, number, value);
    }

    /**
     * @param channel MIDI channel ( 0–15 )
     * @param program program number ( 0–127 )
     */
    default void receive_program_change(int channel, int program) {
        receiveProgramChange(channel, program, 0);
    }

    /**
     * @param channel MIDI channel ( 0–15 )
     * @param value   pitch bend value ( 0–16383 ) with <code>MIDI.PITCH_BEND_CENTER = 8192</code> as center
     */
    default void receive_pitch_bend(int channel, int value) {
    }

    /**
     * @param channel  MIDI channel ( 0–15 )
     * @param pressure channel pressure ( aftertouch ) ( 0–127 )
     */
    default void receive_channel_pressure(int channel, int pressure) {
    }

    /**
     * @param channel  MIDI channel ( 0–15 )
     * @param pitch    note ( 0–127 )
     * @param pressure polyphonic key pressure ( aftertouch ) ( 0–127 )
     */
    default void receive_poly_pressure(int channel, int pitch, int pressure) {
    }

    /* --- system messages --- */

    /**
     * @param data complete SysEx message including status bytes <code>0xF0</code> and <code>0xF7</code>. note that
     *             some systems split long messages into several chunks, continuation chunks start with
     *             <code>0xF7</code>.
     */
    default void receive_sysex(byte[] data) {
    }

    /**
     * @param song song number ( 0–127 )
     */
    default void receive_song_select(int song) {
    }

    /**
     * @param data data byte of MIDI time code quarter frame message ( upper nibble: message type, lower nibble:
     *             value )
     */
    default void receive_time_code_quarter_frame(int data) {
    }

    /**
     * called for every MIDI clock pulse ( 24 pulses per quarter note )
     */
    default void clock_tick() {
    }

    default void clock_start() {
    }

    default void clock_continue() {
    }

    default void clock_stop() {
    }

    /**
     * @param offset_16th song position in sixteenth notes ( one sixteenth = 6 clock pulses )
     */
    default void clock_song_position_pointer(int offset_16th) {
    }

    /* --- deprecated --- */

    /**
     * @deprecated program change messages have no value. use {@link #receive_program_change(int, int)} instead.
     */
    @Deprecated
    default void receiveProgramChange(int channel, int number, int value) {
    }

    /**
     * @deprecated use {@link #receive_control_change(int, int, int)} instead.
     */
    @Deprecated
    default void receiveControlChange(int channel, int number, int value) {
    }

    /**
     * @deprecated use {@link #receive_note_off(int, int, int)} instead.
     */
    @Deprecated
    default void receiveNoteOff(int channel, int pitch) {
    }

    /**
     * @deprecated use {@link #receive_note_on(int, int, int)} instead.
     */
    @Deprecated
    default void receiveNoteOn(int channel, int pitch, int velocity) {
    }
}
