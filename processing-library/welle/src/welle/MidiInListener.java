package welle;

public interface MidiInListener {
    void receiveProgramChange(int channel, int number, int value);
    void receiveControlChange(int channel, int number, int value);
    void receiveNoteOff(int channel, int pitch);
    void receiveNoteOn(int channel, int pitch, int velocity);
    void clock_tick();
    void clock_start();
    void clock_continue();
    void clock_stop();
    void clock_song_position_pointer(int pOffset16th);
}