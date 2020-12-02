package ton;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EventReceiverMIDI implements MidiInListener {

    private static final String METHOD_NAME = "event_receive";
    private static EventReceiverMIDI mInstance = null;
    private final Object mParent;
    private Method mMethod = null;

    public EventReceiverMIDI(Object pPApplet) {
        mParent = pPApplet;
        try {
            mMethod = pPApplet.getClass().getDeclaredMethod(METHOD_NAME, int.class, float[].class);
        } catch (NoSuchMethodException | SecurityException ex) {
            ex.printStackTrace();
        }
    }

    public static EventReceiverMIDI start(Object pParent, String pMidiInputDevice) {
        if (mInstance == null) {
            mInstance = new EventReceiverMIDI(pParent);
            MidiIn mMidiIn = new MidiIn(pMidiInputDevice);
            mMidiIn.addListener(mInstance);
        }
        return mInstance;
    }

    @Override
    public void receiveProgramChange(int channel, int number, int value) {
        sendEvent(TonEvent.EVENT_PROGRAMCHANGE, new float[]{channel, number, value});
    }

    @Override
    public void receiveControlChange(int channel, int number, int value) {
        sendEvent(TonEvent.EVENT_CONTROLCHANGE, new float[]{channel, number, value});
    }

    @Override
    public void receiveNoteOff(int channel, int pitch) {
        sendEvent(TonEvent.EVENT_NOTE_OFF, new float[]{channel, pitch});
    }

    @Override
    public void receiveNoteOn(int channel, int pitch, int velocity) {
        sendEvent(TonEvent.EVENT_NOTE_ON, new float[]{channel, pitch, velocity});
    }

    @Override
    public void clock_tick() {
        sendEvent(MIDI.MIDI_CLOCK_TICK, new float[]{});
    }

    @Override
    public void clock_start() {
        sendEvent(MIDI.MIDI_CLOCK_START, new float[]{});
    }

    @Override
    public void clock_continue() {
        sendEvent(MIDI.MIDI_CLOCK_CONTINUE, new float[]{});
    }

    @Override
    public void clock_stop() {
        sendEvent(MIDI.MIDI_CLOCK_STOP, new float[]{});
    }

    @Override
    public void clock_song_position_pointer(int pOffset16th) {
        sendEvent(MIDI.MIDI_SONG_POSITION_POINTER, new float[]{pOffset16th});
    }

    private void sendEvent(int pEvent, float[] pData) {
        try {
            mMethod.invoke(mParent, pEvent, pData);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
