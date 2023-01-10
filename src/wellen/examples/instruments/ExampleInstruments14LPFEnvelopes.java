package wellen.examples.instruments;

import processing.core.PApplet;
import wellen.Note;
import wellen.Scale;
import wellen.Tone;
import wellen.Wellen;

public class ExampleInstruments14LPFEnvelopes extends PApplet {
    /*
     * this example shows how to use the low-pass filter (LPF) with two attached envelopes for cutoff frequency and
     * resonance.
     */

    private int mNote;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Tone.instrument().enable_LPF(true);
        Tone.instrument().enable_LPF_envelope_cutoff(true);
        Tone.instrument().enable_LPF_envelope_resonance(true);
        Tone.instrument().get_LPF_envelope_cutoff().set_adsr(0.25f, 0.01f, 1.0f, 0.25f);
        Tone.instrument().get_LPF_envelope_resonance().set_adsr(0.25f, 0.01f, 1.0f, 0.25f);
        /* choose a waveform with a lot of harmonic content e.g sawtooth */
        Tone.instrument().set_release(0.25f);
        Tone.instrument().set_oscillator_type(Wellen.WAVEFORM_SAWTOOTH);
    }

    public void draw() {
        background(255);
        fill(0);
        float mSize = map(mNote, 33, 69, 80, 320);
        ellipse(width * 0.5f, height * 0.5f, Tone.is_playing() ? mSize : 5, Tone.is_playing() ? mSize : 5);
    }

    public void mousePressed() {
        mNote = Scale.get_note(Scale.CHORD_MAJOR_7TH, Note.NOTE_A1, (int) random(0, 5));
        Tone.note_on(mNote, 100);
    }

    public void mouseDragged() {
        float mAmplitude = map(mouseY, 0, height, 0.0f, 1.0f);
        int mInterpolationSpeedInSamples = Wellen.millis_to_samples(100);
        Tone.instrument().set_amplitude(mAmplitude, mInterpolationSpeedInSamples);
    }

    public void mouseReleased() {
        Tone.note_off();
    }

    public static void main(String[] args) {
        PApplet.main(ExampleInstruments14LPFEnvelopes.class.getName());
    }
}
