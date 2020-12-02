import ton.*; 
import netP5.*; 
import oscP5.*; 

final int[] mNotes = {Note.NOTE_C3, Note.NOTE_C4, Note.NOTE_A2, Note.NOTE_A3};

int mBeatCount;

Trigger mTrigger;

Wavetable mWavetable;

float mSignal;

void settings() {
    size(640, 480);
}

void setup() {
    mTrigger = new Trigger(this);
    mTrigger.trigger_falling_edge(true);
    mTrigger.trigger_falling_edge(true);
    mWavetable = new Wavetable(64); /* use wavetable as LFO */
    Wavetable.sine(mWavetable.wavetable());
    mWavetable.interpolate_samples(true); /* interpolate between samples to remove *steps* from the signal */
    mWavetable.set_frequency(1.0f / 3.0f); /* set phase duration to 3SEC */
    Ton.start();
    DSP.start(this); /* DSP is only used to create trigger events */
}

void draw() {
    background(255);
    noStroke();
    fill(0);
    float mScale = (mBeatCount % 2) * 0.25f + 0.25f;
    ellipse(width * 0.5f, height * 0.5f, width * mScale, width * mScale);
    /* draw current signal signal */
    stroke(255);
    ellipse(width * 0.5f, map(mSignal, -1.0f, 1.0f, 0, height), 10, 10);
}

void mouseMoved() {
    /* set oscillation speed a value between 0.1SEC and 5SEC */
    mWavetable.set_frequency(1.0f / map(mouseX, 0, width, 0.1f, 5.0f));
}

void audioblock(float[] pOutputSamples) {
    for (int i = 0; i < pOutputSamples.length; i++) {
        mSignal = mWavetable.output();
        mTrigger.input(mSignal);
    }
}

void trigger() {
    mBeatCount++;
    int mNote = mNotes[mBeatCount % mNotes.length];
    Ton.note_on(mNote, 100);
}
