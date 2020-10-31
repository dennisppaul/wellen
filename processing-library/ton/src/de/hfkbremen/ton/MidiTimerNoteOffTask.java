package de.hfkbremen.ton;


import java.util.TimerTask;

public class MidiTimerNoteOffTask extends TimerTask {

    private final MidiOut mMidiOutput;
    private final int mChannel;
    private final int mNote;
    private final int mVelocity;

    public MidiTimerNoteOffTask(MidiOut pMidiOutput, int pChannel, int pNote, int pVelocity) {
        mMidiOutput = pMidiOutput;
        mChannel = pChannel;
        mNote = pNote;
        mVelocity = pVelocity;
    }

    @Override
    public void run() {
        mMidiOutput.sendNoteOff(mChannel, mNote, mVelocity);
    }
}
