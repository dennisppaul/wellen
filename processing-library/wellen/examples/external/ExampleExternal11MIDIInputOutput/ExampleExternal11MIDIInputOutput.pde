import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to send + receive MIDI events.
 */
MidiOut mMidiOut;
MidiIn mMidiIn;
int mNote = 0;
int mVelocity = 0;
void settings() {
    size(640, 480);
}
void setup() {
    Wellen.dumpMidiInputDevices();
    mMidiIn = new MidiIn("Arturia KeyStep 37");
    mMidiIn.addListener(new MIDIInput());
    mMidiOut = new MidiOut("Arturia KeyStep 37");
}
void draw() {
    background(255);
    noStroke();
    fill(map(mVelocity, 0, 127, 255, 0));
    float mScale = Tone.is_playing() ? map(mNote, 24, 96, 5, height * 0.8f) : 5;
    ellipse(width * 0.5f, height * 0.5f, mScale, mScale);
}
void mousePressed() {
    mMidiOut.sendNoteOn(10, 48, 100);
}
class MIDIInput implements MidiInListener {
    void receiveProgramChange(int channel, int number, int value) {
    }
    void receiveControlChange(int channel, int number, int value) {
    }
    void clock_tick() {
    }
    void clock_start() {
    }
    void clock_continue() {
    }
    void clock_stop() {
    }
    void clock_song_position_pointer(int pOffset16th) {
    }
    void receiveNoteOff(int channel, int pitch) {
        Tone.instrument(channel);
        mNote = pitch;
        mVelocity = 0;
        Tone.note_off(mNote);
    }
    void receiveNoteOn(int channel, int pitch, int velocity) {
        Tone.instrument(channel);
        mNote = pitch;
        mVelocity = velocity;
        Tone.note_on(mNote, mVelocity);
    }
}
