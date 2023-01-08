/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
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

package wellen;

/**
 * MIDI constants
 */
public interface MIDI {
    int MIDI_CLOCK_CONTINUE = 0xFB; // ( = 251 )
    int MIDI_CLOCK_START = 0xFA; // ( = 250 )
    int MIDI_CLOCK_STOP = 0xFC; // ( = 252 )
    int MIDI_CLOCK_TICK = 0xF8; // ( = 248 )
    int MIDI_SONG_POSITION_POINTER = 0xF2; // ( = 242 )
    int NOTE_A0 = 21;
    int NOTE_A1 = 33;
    int NOTE_A2 = 45;
    int NOTE_A3 = 57;
    int NOTE_A4 = 69;
    int NOTE_A5 = 81;
    int NOTE_A6 = 93;
    int NOTE_A7 = 105;
    int NOTE_A8 = 117;
    int NOTE_B0 = 23;
    int NOTE_B1 = 35;
    int NOTE_B2 = 47;
    int NOTE_B3 = 59;
    int NOTE_B4 = 71;
    int NOTE_B5 = 83;
    int NOTE_B6 = 95;
    int NOTE_B7 = 107;
    int NOTE_B8 = 119;
    int NOTE_C1 = 24;
    int NOTE_C2 = 36;
    int NOTE_C3 = 48;
    int NOTE_C4 = 60;
    int NOTE_C5 = 72;
    int NOTE_C6 = 84;
    int NOTE_C7 = 96;
    int NOTE_C8 = 108;
    int NOTE_C9 = 120;
    int NOTE_D1 = 26;
    int NOTE_D2 = 38;
    int NOTE_D3 = 50;
    int NOTE_D4 = 62;
    int NOTE_D5 = 74;
    int NOTE_D6 = 86;
    int NOTE_D7 = 98;
    int NOTE_D8 = 110;
    int NOTE_D9 = 122;
    int NOTE_E1 = 28;
    int NOTE_E2 = 40;
    int NOTE_E3 = 52;
    int NOTE_E4 = 64;
    int NOTE_E5 = 76;
    int NOTE_E6 = 88;
    int NOTE_E7 = 100;
    int NOTE_E8 = 112;
    int NOTE_E9 = 124;
    int NOTE_F1 = 29;
    int NOTE_F2 = 41;
    int NOTE_F3 = 53;
    int NOTE_F4 = 65;
    int NOTE_F5 = 77;
    int NOTE_F6 = 89;
    int NOTE_F7 = 101;
    int NOTE_F8 = 113;
    int NOTE_F9 = 125;
    int NOTE_G1 = 31;
    int NOTE_G2 = 43;
    int NOTE_G3 = 55;
    int NOTE_G4 = 67;
    int NOTE_G5 = 79;
    int NOTE_G6 = 91;
    int NOTE_G7 = 103;
    int NOTE_G8 = 115;
    int NOTE_G9 = 127;
    int PPQN = 24;
}
