package de.hfkbremen.ton;

public interface MIDI {
    int MIDI_CLOCK_TICK = 0xF8; // ( = 248 )
    int MIDI_CLOCK_START = 0xFA; // ( = 250 )
    int MIDI_CLOCK_CONTINUE = 0xFB; // ( = 251 )
    int MIDI_CLOCK_STOP = 0xFC; // ( = 252 )
}
