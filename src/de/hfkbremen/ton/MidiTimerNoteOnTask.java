package de.hfkbremen.ton;

import java.util.TimerTask;

public class MidiTimerNoteOnTask extends TimerTask {

    private final MidiOut mMidiOutput;
    private final int mChannel;
    private final int mNote;
    private final int mVelocity;

    public MidiTimerNoteOnTask(MidiOut pMidiOutput, int pChannel, int pNote, int pVelocity) {
        mMidiOutput = pMidiOutput;
        mChannel = pChannel;
        mNote = pNote;
        mVelocity = pVelocity;
    }

    @Override
    public void run() {
        mMidiOutput.sendNoteOn(mChannel, mNote, mVelocity);
    }
}
