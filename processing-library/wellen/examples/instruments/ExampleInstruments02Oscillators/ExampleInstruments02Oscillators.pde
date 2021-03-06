import wellen.*; 

/*
 * this example shows how to use different oscillators in an instrument.
 *
 * use keys `1` – `5` to select different wave shapes.
 *
 * note that this functionality is not implemented for MIDI and OSC.
 */

int mNote;

void settings() {
    size(640, 480);
}

void setup() {
    background(255);
}

void draw() {
    background(255);
    fill(0);
    float mSize = map(mNote, 33, 69, 80, 320);
    ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? mSize : 5, Tone.is_playing() ? mSize : 5);
}

void mousePressed() {
    mNote = Scale.get_note(Scale.CHORD_MAJOR_7TH, Note.NOTE_A2, (int) random(0, 10));
    Tone.note_on(mNote, 127);
}

void mouseReleased() {
    Tone.note_off();
}

void keyPressed() {
    if (key == '1') {
        Tone.instrument().set_oscillator_type(Wellen.WAVESHAPE_SINE);
    }
    if (key == '2') {
        Tone.instrument().set_oscillator_type(Wellen.WAVESHAPE_TRIANGLE);
    }
    if (key == '3') {
        Tone.instrument().set_oscillator_type(Wellen.WAVESHAPE_SAWTOOTH);
    }
    if (key == '4') {
        Tone.instrument().set_oscillator_type(Wellen.WAVESHAPE_SQUARE);
    }
    if (key == '5') {
        Tone.instrument().set_oscillator_type(Wellen.WAVESHAPE_NOISE);
    }
}
