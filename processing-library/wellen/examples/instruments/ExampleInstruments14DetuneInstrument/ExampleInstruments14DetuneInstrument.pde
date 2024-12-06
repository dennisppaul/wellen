import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to use the detune functionality of an instrument to create richter sounds.
 */
void settings() {
    size(640, 480);
}
void setup() {
    Tone.instrument(0).set_oscillator_type(Wellen.WAVEFORM_SAWTOOTH);
    Tone.instrument(0).enable_sub_oscillator(true);
    Tone.instrument(0).set_sub_ratio(1.02f);
    Tone.instrument(0).set_sub_amplitude(0.8f);
    Tone.instrument(0).set_sub_oscillator_type(Wellen.WAVEFORM_SAWTOOTH);
}
void draw() {
    background(255);
    fill(0);
    ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
}
void mousePressed() {
    int mNote = (int) random(0, 6);
    mNote = Scale.get_note(Scale.FIFTH, Note.NOTE_C1, mNote);
    Tone.note_on(mNote, 100);
}
void mouseReleased() {
    Tone.note_off();
}
void mouseDragged() {
    Tone.instrument(0).set_sub_amplitude(map(mouseX, 0, width, 0.1f, 1.0f));
    Tone.instrument(0).set_sub_ratio(map(mouseY, 0, height, 0.1f, 2.1f));
}
void keyPressed() {
    switch (key) {
        case '1':
            Tone.instrument(0).set_sub_ratio(1.0f); // no detune
            break;
        case '2':
            Tone.instrument(0).set_sub_ratio(1.25f); // Große Terz
            break;
        case '3':
            Tone.instrument(0).set_sub_ratio(1.33f); // Quarte
            break;
        case '4':
            Tone.instrument(0).set_sub_ratio(1.5f); // Quinte
            break;
        case '5':
            Tone.instrument(0).set_sub_ratio(2.0f); // Oktave
            break;
        default:
            Tone.instrument(0).set_sub_amplitude(0.8f);
    }
}
