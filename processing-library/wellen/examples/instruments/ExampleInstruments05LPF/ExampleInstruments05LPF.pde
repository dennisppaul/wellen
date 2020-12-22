import wellen.*; 

/*
 * this example demonstrates how to use a *Low-Pass Filter* (LPF) on a sawtooth oscillator in DSP.
 *
 * note that this functionality is not implemented for MIDI and OSC.
 */

void settings() {
    size(640, 480);
}

void setup() {
    Tone.instrument().enable_LPF(true);
    Tone.instrument().set_oscillator_type(Wellen.WAVESHAPE_SAWTOOTH);
}

void draw() {
    background(255);
    fill(0);
    ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
}

void mousePressed() {
    int mNote = 45 + (int) random(0, 12);
    Tone.note_on(mNote, 100);
}

void mouseReleased() {
    Tone.note_off();
}

void mouseDragged() {
    Tone.instrument().set_filter_resonance(map(mouseY, 0, height, 0.0f, 0.95f));
    Tone.instrument().set_filter_frequency(map(mouseX, 0, width, 0.0f, 2000.0f));
}
