import wellen.*; 

/*
 * this example demonstrates how to parse MIDI events sent from an external MIDI device ( i.e a MIDI keyboard ) and
 * parse the events to play notes with the tone engine.
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
    float mScale = map(mNote, 24, 96, 5, height * 0.8f);
    ellipse(width * 0.5f, height * 0.5f, mScale, mScale);
}

void event_receive(int pEvent, float[] pData) {
    /* parse event + data. see `Event` for all *defined* events. */
    if (pEvent == Wellen.EVENT_NOTE_ON) {
        mNote = (int) pData[Wellen.EVENT_NOTE];
        mVelocity = (int) pData[Wellen.EVENT_VELOCITY];
        Tone.note_on(mNote, mVelocity);
    } else if (pEvent == Wellen.EVENT_NOTE_OFF) {
        mNote = (int) pData[Wellen.EVENT_NOTE];
        mVelocity = 0;
        Tone.note_off(mNote);
    }
}
