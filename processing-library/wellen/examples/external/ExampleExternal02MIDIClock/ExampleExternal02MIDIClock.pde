import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to use a beat triggered by an external MIDI beat clock ( e.g generated by an
 * external MIDI device or an internal MIDI application ).
 */
/*
 * steps to enable MIDI clock in ableton live:
 *
 * 1. connect your MIDI device:
 *     - ensure your MIDI interface or hardware device is connected to your computer.
 * 2. open ableton live preferences:
 *     - go to `Live > Settings…` (macOS) or `Options > Preferences` (Windows).
 *     - navigate to `Link/Tempo/MIDI` tab.
 * 3. set up MIDI clock output:
 *     - in the `MIDI Ports` section, locate the output port for your connected MIDI device.
 *     - enable `Sync` for the relevant output port.
 * 4. activate MIDI clock:
 *     - close the preferences window.
 *     - ensure `Options > External Sync` it is toggled off.
 * 5. start playback:
 *     - hit play in ableton live, and the MIDI clock will start transmitting, syncing any connected hardware or software.
 */
BeatMIDI mBeatMIDI;
int mColor;
void settings() {
    size(640, 480);
}
void setup() {
    Wellen.dumpMidiInputDevices();
    mBeatMIDI = BeatMIDI.start(this, "Arturia KeyStep 37");
}
void draw() {
    background(mBeatMIDI.running() ? mColor : 0);
}
void beat(int beat) {
    /* MIDI clock runs at 24 *pulses per quarter* note (PPQ), therefore `beat % 12` triggers eighth note. */
    if (beat % 12 == 0) {
        mColor = color(random(127, 255), random(127, 255), random(127, 255));
        int mOffset = 4 * ((beat / 24) % 8);
        Tone.note_on(36 + mOffset, 90);
        System.out.println(mBeatMIDI.bpm());
    } else if (beat % 12 == 6) {
        Tone.note_off();
    }
}
