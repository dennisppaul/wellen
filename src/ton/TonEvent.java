package ton;

public interface TonEvent {

    int EVENT_UNDEFINED = -1;
    int EVENT_NOTE_ON = 0;
    int EVENT_NOTE_OFF = 1;
    int EVENT_CONTROLCHANGE = 2;
    int EVENT_PITCHBAND = 3;
    int EVENT_PROGRAMCHANGE = 4;

    int CHANNEL = 0;
    int NOTE = 1;
    int VELOCITY = 2;
}
