import wellen.*; 
import wellen.dsp.*; 

/*
 * this example shows how to use the low-pass filter (LPF) with two attached envelopes for cutoff frequency and
 * resonance.
 *
 * the LPF envelopes are triggered by the <code>Tone.note_on(...)</code> event and are released by the
 * <code>Tone.note_off()</code> event, similar to the regular ADSR envelope. minimum and maximum values as well as
 * the envelope shapes can be specified for the envelopes.
 *
 * press mouse to play a note.
 */

int mNote;

void settings() {
    size(640, 480);
}

void setup() {
    Tone.instrument().set_release(0.25f);
    Tone.instrument().enable_LPF(true);
    Tone.instrument().enable_LPF_envelope_cutoff(true);
    Tone.instrument().enable_LPF_envelope_resonance(true);
    Tone.instrument().get_LPF_envelope_cutoff().set_adsr(0.25f, 0.0f, 1.0f, 0.25f);
    Tone.instrument().get_LPF_envelope_resonance().set_adsr(0.25f, 0.0f, 1.0f, 0.25f);
    Tone.instrument().set_LPF_envelope_cutoff_min(20);
    Tone.instrument().set_LPF_envelope_cutoff_max(2000);
    Tone.instrument().set_LPF_envelope_resonance_min(0.1f);
    Tone.instrument().set_LPF_envelope_resonance_max(0.7f);
    /* choose a waveform with a lot of harmonic content e.g SAWTOOTH */
    Tone.instrument().set_oscillator_type(Wellen.WAVEFORM_SAWTOOTH);
}

void draw() {
    background(255);
    fill(0);
    float mSize = map(mNote, 33, 69, 80, 320);
    ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? mSize : 5, Tone.is_playing() ? mSize : 5);
}

void mousePressed() {
    mNote = Scale.get_note(Scale.CHORD_MAJOR_7TH, Note.NOTE_A1, (int) random(0, 5));
    Tone.note_on(mNote, 100);
}

void mouseReleased() {
    Tone.note_off();
}
