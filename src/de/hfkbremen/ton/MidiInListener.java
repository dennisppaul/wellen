package de.hfkbremen.ton;

public interface MidiInListener {

    void receiveProgramChange(int channel, int number, int value);

    void receiveControlChange(int channel, int number, int value);

    void receiveNoteOff(int channel, int pitch);

    void receiveNoteOn(int channel, int pitch, int velocity);
}