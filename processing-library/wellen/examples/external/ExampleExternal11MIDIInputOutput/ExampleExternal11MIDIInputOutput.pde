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
    mMidiIn.add_listener(new MIDIInput());
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
    mMidiOut.send_note_on(10, 48, 100);
}
void mouseReleased() {
    mMidiOut.send_note_off(10, 48);
}
/*
 * all methods of `MidiInListener` are optional, implement only the ones that are needed. note that the methods
 * are called on the MIDI thread.
 */
class MIDIInput implements MidiInListener {
    void receive_note_on(int channel, int pitch, int velocity) {
        mNote = pitch;
        mVelocity = velocity;
        Tone.note_on_instrument(channel, pitch, velocity);
    }
    void receive_note_off(int channel, int pitch, int velocity) {
        mNote = pitch;
        mVelocity = 0;
        Tone.note_off_instrument(channel, pitch);
    }
    void receive_pitch_bend(int channel, int value) {
        println("pitch bend: " + value);
    }
}
