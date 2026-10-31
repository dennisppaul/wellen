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
 * MIDI constants
 */
/**
 * MIDI constants.
 * <p>
 * note names follow scientific pitch notation i.e <code>NOTE_C4 = 60</code> ( middle C ) and <code>NOTE_A4 = 69</code>
 * ( 440Hz ). sharps are written with an <code>S</code> e.g <code>NOTE_CS4 = 61</code> ( C#4 ).
 */
public interface MIDI {
    /* --- channel voice messages ( status byte without channel ) --- */
    int MIDI_NOTE_OFF = 0x80; // ( = 128 )
    int MIDI_NOTE_ON = 0x90; // ( = 144 )
    int MIDI_POLY_PRESSURE = 0xA0; // ( = 160 ) polyphonic key pressure ( aftertouch )
    int MIDI_CONTROL_CHANGE = 0xB0; // ( = 176 )
    int MIDI_PROGRAM_CHANGE = 0xC0; // ( = 192 )
    int MIDI_CHANNEL_PRESSURE = 0xD0; // ( = 208 ) channel pressure ( aftertouch )
    int MIDI_PITCH_BEND = 0xE0; // ( = 224 )
    /* --- system common messages --- */
    int MIDI_SYSEX_START = 0xF0; // ( = 240 )
    int MIDI_TIME_CODE_QUARTER_FRAME = 0xF1; // ( = 241 )
    int MIDI_SONG_POSITION_POINTER = 0xF2; // ( = 242 )
    int MIDI_SONG_SELECT = 0xF3; // ( = 243 )
    int MIDI_TUNE_REQUEST = 0xF6; // ( = 246 )
    int MIDI_SYSEX_END = 0xF7; // ( = 247 )
    /* --- system realtime messages --- */
    int MIDI_CLOCK_TICK = 0xF8; // ( = 248 )
    int MIDI_CLOCK_START = 0xFA; // ( = 250 )
    int MIDI_CLOCK_CONTINUE = 0xFB; // ( = 251 )
    int MIDI_CLOCK_STOP = 0xFC; // ( = 252 )
    int MIDI_ACTIVE_SENSING = 0xFE; // ( = 254 )
    int MIDI_RESET = 0xFF; // ( = 255 )
    /* --- control change numbers --- */
    int CC_BANK_SELECT = 0;
    int CC_MODULATION = 1;
    int CC_BREATH = 2;
    int CC_FOOT = 4;
    int CC_PORTAMENTO_TIME = 5;
    int CC_DATA_ENTRY = 6;
    int CC_VOLUME = 7;
    int CC_BALANCE = 8;
    int CC_PAN = 10;
    int CC_EXPRESSION = 11;
    int CC_SUSTAIN = 64;
    int CC_PORTAMENTO = 65;
    int CC_SOSTENUTO = 66;
    int CC_SOFT_PEDAL = 67;
    int CC_RESONANCE = 71;
    int CC_RELEASE_TIME = 72;
    int CC_ATTACK_TIME = 73;
    int CC_CUTOFF = 74;
    int CC_DECAY_TIME = 75;
    int CC_ALL_SOUND_OFF = 120;
    int CC_RESET_ALL_CONTROLLERS = 121;
    int CC_LOCAL_CONTROL = 122;
    int CC_ALL_NOTES_OFF = 123;
    /* --- value ranges --- */
    int PITCH_BEND_MIN = 0;
    int PITCH_BEND_CENTER = 8192;
    int PITCH_BEND_MAX = 16383;
    int NUMBER_OF_CHANNELS = 16;
    /**
     * MIDI clock pulses per quarter note
     */
    int PPQN = 24;
    /**
     * MIDI clock pulses per sixteenth note ( = unit of the song position pointer )
     */
    int PULSES_PER_SIXTEENTH = PPQN / 4;
    /* --- notes --- */
    int NOTE_C0 = 12;
    int NOTE_CS0 = 13;
    int NOTE_D0 = 14;
    int NOTE_DS0 = 15;
    int NOTE_E0 = 16;
    int NOTE_F0 = 17;
    int NOTE_FS0 = 18;
    int NOTE_G0 = 19;
    int NOTE_GS0 = 20;
    int NOTE_A0 = 21;
    int NOTE_AS0 = 22;
    int NOTE_B0 = 23;
    int NOTE_C1 = 24;
    int NOTE_CS1 = 25;
    int NOTE_D1 = 26;
    int NOTE_DS1 = 27;
    int NOTE_E1 = 28;
    int NOTE_F1 = 29;
    int NOTE_FS1 = 30;
    int NOTE_G1 = 31;
    int NOTE_GS1 = 32;
    int NOTE_A1 = 33;
    int NOTE_AS1 = 34;
    int NOTE_B1 = 35;
    int NOTE_C2 = 36;
    int NOTE_CS2 = 37;
    int NOTE_D2 = 38;
    int NOTE_DS2 = 39;
    int NOTE_E2 = 40;
    int NOTE_F2 = 41;
    int NOTE_FS2 = 42;
    int NOTE_G2 = 43;
    int NOTE_GS2 = 44;
    int NOTE_A2 = 45;
    int NOTE_AS2 = 46;
    int NOTE_B2 = 47;
    int NOTE_C3 = 48;
    int NOTE_CS3 = 49;
    int NOTE_D3 = 50;
    int NOTE_DS3 = 51;
    int NOTE_E3 = 52;
    int NOTE_F3 = 53;
    int NOTE_FS3 = 54;
    int NOTE_G3 = 55;
    int NOTE_GS3 = 56;
    int NOTE_A3 = 57;
    int NOTE_AS3 = 58;
    int NOTE_B3 = 59;
    int NOTE_C4 = 60;
    int NOTE_CS4 = 61;
    int NOTE_D4 = 62;
    int NOTE_DS4 = 63;
    int NOTE_E4 = 64;
    int NOTE_F4 = 65;
    int NOTE_FS4 = 66;
    int NOTE_G4 = 67;
    int NOTE_GS4 = 68;
    int NOTE_A4 = 69;
    int NOTE_AS4 = 70;
    int NOTE_B4 = 71;
    int NOTE_C5 = 72;
    int NOTE_CS5 = 73;
    int NOTE_D5 = 74;
    int NOTE_DS5 = 75;
    int NOTE_E5 = 76;
    int NOTE_F5 = 77;
    int NOTE_FS5 = 78;
    int NOTE_G5 = 79;
    int NOTE_GS5 = 80;
    int NOTE_A5 = 81;
    int NOTE_AS5 = 82;
    int NOTE_B5 = 83;
    int NOTE_C6 = 84;
    int NOTE_CS6 = 85;
    int NOTE_D6 = 86;
    int NOTE_DS6 = 87;
    int NOTE_E6 = 88;
    int NOTE_F6 = 89;
    int NOTE_FS6 = 90;
    int NOTE_G6 = 91;
    int NOTE_GS6 = 92;
    int NOTE_A6 = 93;
    int NOTE_AS6 = 94;
    int NOTE_B6 = 95;
    int NOTE_C7 = 96;
    int NOTE_CS7 = 97;
    int NOTE_D7 = 98;
    int NOTE_DS7 = 99;
    int NOTE_E7 = 100;
    int NOTE_F7 = 101;
    int NOTE_FS7 = 102;
    int NOTE_G7 = 103;
    int NOTE_GS7 = 104;
    int NOTE_A7 = 105;
    int NOTE_AS7 = 106;
    int NOTE_B7 = 107;
    int NOTE_C8 = 108;
    int NOTE_CS8 = 109;
    int NOTE_D8 = 110;
    int NOTE_DS8 = 111;
    int NOTE_E8 = 112;
    int NOTE_F8 = 113;
    int NOTE_FS8 = 114;
    int NOTE_G8 = 115;
    int NOTE_GS8 = 116;
    int NOTE_A8 = 117;
    int NOTE_AS8 = 118;
    int NOTE_B8 = 119;
    int NOTE_C9 = 120;
    int NOTE_CS9 = 121;
    int NOTE_D9 = 122;
    int NOTE_DS9 = 123;
    int NOTE_E9 = 124;
    int NOTE_F9 = 125;
    int NOTE_FS9 = 126;
    int NOTE_G9 = 127;
}
