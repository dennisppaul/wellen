import de.hfkbremen.ton.*; 
import netP5.*; 
import oscP5.*; 

int mNote = 0;

int mVelocity = 0;

void settings() {
    size(640, 480);
}

void setup() {
    Ton.dumpMidiInputDevices();
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
    if (pEvent == TonEvent.EVENT_NOTE_ON) {
        mNote = (int) pData[TonEvent.NOTE];
        mVelocity = (int) pData[TonEvent.VELOCITY];
        Ton.note_on(mNote, mVelocity);
    } else if (pEvent == TonEvent.EVENT_NOTE_OFF) {
        mNote = (int) pData[TonEvent.NOTE];
        mVelocity = 0;
        Ton.note_off(mNote);
    }
}
