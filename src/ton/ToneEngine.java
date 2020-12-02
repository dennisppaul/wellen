package ton;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public abstract class ToneEngine {

    // @TODO(add javadoc to abstract classes)
    public static final int INSTRUMENT_EMPTY = 0;
    public static final int INSTRUMENT_WITH_OSCILLATOR = 1;
    public static final int INSTRUMENT_WITH_OSCILLATOR_ADSR = 2;
    public static final int INSTRUMENT_WITH_OSCILLATOR_ADSR_FILTER_LFO = 3;
    public static final int NUMBERS_OF_INSTRUMENTS = 16;
    private final Timer mTimer;

    ToneEngine() {
        mTimer = new Timer();
    }

    public static ToneEngine createEngine() {
        return new ToneEngineInternal();
    }

    public static ToneEngine createEngine(String... pName) {
        if (pName.length > 0) {
            if (pName[0].equalsIgnoreCase(Ton.TONE_ENGINE_INTERNAL)) {
                return new ToneEngineInternal();
            } else if (pName[0].equalsIgnoreCase(Ton.TONE_ENGINE_MIDI) && pName.length == 2) {
                return new ToneEngineMIDI(pName[1]);
            } else if (pName[0].equalsIgnoreCase(Ton.TONE_ENGINE_OSC) && pName.length >= 2) {
                if (pName.length == 2) {
                    return new ToneEngineOSC(pName[1]);
                } else if (pName.length == 3) {
                    try {
                        final String mIPTransmit = pName[1];
                        final int mPortTransmit = Integer.parseInt(pName[2]);
                        return new ToneEngineOSC(mIPTransmit, mPortTransmit);
                    } catch (NumberFormatException e) {
                        System.err.println("+++ WARNING @" + ToneEngine.class.getSimpleName() + ".createEngine" +
                                           " / could not parse ports");
                    }
                }
                return new ToneEngineOSC();
            }
            System.err.println("+++ WARNING @" + ToneEngine.class.getSimpleName() + ".createEngine" +
                    " / could not find specified tone engine: " + pName[0]);
            System.err.println("+++ hint: check engine name and number of parameters");
        }
        return createEngine();
    }

    /**
     * play a note
     *
     * @param note     pitch of note ranging from 0 to 127
     * @param velocity volume of note ranging from 0 to 127
     * @param duration duration in seconds before the note is turned off again
     */
    public final void note_on(int note, int velocity, float duration) {
        TimerTask mTask = new NoteOffTask(note, instrument().ID());
        mTimer.schedule(mTask, (long) (duration * 1000));
        note_on(note, velocity);
    }

    /**
     * play a note
     *
     * @param note     pitch of note ranging from 0 to 127
     * @param velocity volume of note ranging from 0 to 127
     */
    public abstract void note_on(int note, int velocity);

    /**
     * turn off a note
     *
     * @param note pitch of note to turn off
     */
    public abstract void note_off(int note);

    /**
     * turns off the last played note.
     */
    public abstract void note_off();

    public abstract void control_change(int pCC, int pValue);

    public abstract void pitch_bend(int pValue);

    public abstract boolean is_playing();

    public abstract Instrument instrument(int pInstrumentID);

    public abstract Instrument instrument();

    public abstract ArrayList<? extends Instrument> instruments();

    public abstract void replace_instrument(Instrument pInstrument);

    private class NoteOffTask extends TimerTask {

        final int note;
        final int instrument;

        public NoteOffTask(int pNote, int pInstrument) {
            note = pNote;
            instrument = pInstrument;
        }

        public void run() {
            int mCurrentInstrument = instrument().ID();
            instrument(instrument);
            note_off(note);
            instrument(mCurrentInstrument);
        }
    }
}
