import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to send a MIDI clock signal i.e how to use wellen as MIDI clock master for other
 * applications or devices ( e.g `ExampleExternal02MIDIClock` running in another sketch, a DAW or a drum machine ).
 *
 * press SPACE to start or stop the clock. move the mouse horizontally to change the tempo.
 *
 * note that on macOS java's built-in MIDI implementation can not send MIDI clock messages. in this case add
 * CoreMIDI4J ( https://github.com/DerekCook/CoreMidi4J ) to the sketch's `code` folder.
 */
MidiClockOut mClock;
void settings() {
    size(640, 480);
}
void setup() {
    Wellen.dumpMidiOutputDevices();
    final MidiOut mMidiOut = new MidiOut("Bus 1");
    if (!mMidiOut.can_send_clock_and_sysex()) {
        println("+++ this MIDI output can not send clock messages ( see comment above )");
    }
    mClock = new MidiClockOut(mMidiOut, 120);
}
void draw() {
    mClock.set_bpm(map(mouseX, 0, width, 60, 180));
    background(mClock.is_running() && (mClock.get_tick_count() / 24) % 2 == 0 ? 255 : 0);
    fill(127);
    text(nf(mClock.get_bpm(), 0, 1) + " BPM", 10, 20);
}
void keyPressed() {
    if (key == ' ') {
        if (mClock.is_running()) {
            mClock.stop();
        } else {
            mClock.start();
        }
    }
}
