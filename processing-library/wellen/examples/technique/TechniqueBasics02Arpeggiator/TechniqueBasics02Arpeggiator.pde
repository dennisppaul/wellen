import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to use `Arpeggiator` to play a predefined *pattern* of notes. `play(int, int)`
 * produces a series of notes to be played sequentially.
 *
 * press keys 1 – 5 to trigger different patterns.
 */

int fColor;

Beat fBeat;

Arpeggiator fArpeggiator;

boolean fToggle;

void settings() {
    size(640, 480);
}

void setup() {
    Wellen.dumpMidiInputDevices();
    fBeat = Beat.start(this, 120 * 24);
    /* the pattern is composed of 8 notes with a length of 1/32 ( 8 * (1/32) = (1/4) ) i.e the pattern has a
     * length of 1/4 which means 24 pulses ( or ticks ) when synced with a MIDI clock.
     */
    fArpeggiator = new Arpeggiator(24);
    fArpeggiator.pattern(0 * 3, 0, 0.8f);
    fArpeggiator.pattern(1 * 3, 0, 0.6f);
    fArpeggiator.pattern(2 * 3, 3, 0.4f);
    fArpeggiator.pattern(3 * 3, 5, 0.3f);
    fArpeggiator.pattern(4 * 3, 4, 0.2f);
    fArpeggiator.pattern(6 * 3, 5, 0.1f);
}

void draw() {
    background(255);
    if (fToggle) {
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, 100, 100);
    }
}

void keyPressed() {
    switch (key) {
        case '1':
            fArpeggiator.play(0, 100);
            break;
        case '2':
            fArpeggiator.play(1, 100);
            break;
        case '3':
            fArpeggiator.play(3, 100);
            break;
        case '4':
            fArpeggiator.play(4, 100);
            break;
        case '5':
            fArpeggiator.play(5, 100);
            break;
    }
}

void beat(int beat) {
    if (beat % 24 == 0) {
        fToggle = !fToggle;
    }
    /* step through the arpeggiator at clock speed i.e 24 steps ( or pulses ) per quarter note */
    if (fArpeggiator.step()) {
        int mNote = Scale.get_note(Scale.MINOR_PENTATONIC, Note.NOTE_C3, fArpeggiator.note());
        Tone.note_on(mNote, fArpeggiator.velocity());
    } else {
        Tone.note_off();
    }
}
