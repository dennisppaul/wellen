import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to receive MIDI events sent from an external MIDI device ( i.e a MIDI keyboard )
 * play the notes with the tone engine.
 */
int mNote = 0;
int mVelocity = 0;
void settings() {
    size(640, 480);
}
void setup() {
    Wellen.dumpMidiInputDevices();
    EventReceiverMIDI.start(this, "Arturia KeyStep 37");
}
void draw() {
    background(255);
    noStroke();
    fill(map(mVelocity, 0, 127, 255, 0));
    float mScale = Tone.is_playing() ? map(mNote, 24, 96, 5, height * 0.8f) : 5;
    ellipse(width * 0.5f, height * 0.5f, mScale, mScale);
}
void midi_note_off(int channel, int pitch) {
    Tone.instrument(channel);
    mNote = pitch;
    mVelocity = 0;
    Tone.note_off(mNote);
}
void midi_note_on(int channel, int pitch, int velocity) {
    Tone.instrument(channel);
    mNote = pitch;
    mVelocity = velocity;
    Tone.note_on(mNote, mVelocity);
}
