package wellen.examples.technique;

import processing.core.PApplet;
import wellen.Beat;
import wellen.InstrumentDSP;
import wellen.Note;
import wellen.Scale;
import wellen.Tone;
import wellen.Wellen;

public class TechniqueAlgorithmicComposition04FunctionSineWaves extends PApplet {

    private static final int INSTRUMENT_BASE = 0;
    private static final int INSTRUMENT_FLUTE = 1;
    private static final int INSTRUMENT_NOISE = 2;
    private static final int O = -2;
    private static final int X = -1;
    private final int[] mBaseSequence = {0, O, X, 0, X, X, 0, X, X, 0, X, X, 7, O, X, 12};
    private float mTime;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Beat.start(this, 240);
        Tone.instrument(INSTRUMENT_BASE).set_oscillator_type(Wellen.WAVEFORM_TRIANGLE);
        Tone.instrument(INSTRUMENT_FLUTE).set_oscillator_type(Wellen.WAVEFORM_SAWTOOTH);

        //@TODO("this might be broken!")
        Tone.replace_instrument(InstrumentDSP.class, INSTRUMENT_NOISE);
        Tone.instrument(INSTRUMENT_NOISE).set_oscillator_type(Wellen.WAVEFORM_NOISE);
        Tone.instrument(INSTRUMENT_NOISE).note_on(1, 127);
        Tone.instrument(INSTRUMENT_NOISE).set_sustain(1.0f);
        Tone.instrument(INSTRUMENT_NOISE).set_amplitude(0.0f);
    }

    public void draw() {
        background(255);

        mTime += 1.0f / frameRate;
        playNoise(mTime);
    }

    public void beat(int beatCount) {
        playBaseSequence(beatCount);
        playMelodyWithEcho(beatCount);
    }

    private int getNote(int beatCount) {
        float r = beatCount % 32; /* 32 beats == 1 phase */
        r /= 32.0f;
        r *= TWO_PI;
        float mNoteStep = sin(r) * 0.5f + 0.5f;
        mNoteStep *= 5;
        int mNote = Scale.get_note(Scale.FIFTH, Note.NOTE_C3, (int) mNoteStep);
        return mNote;
    }

    private int getVelocity(int beatCount) {
        float r = beatCount % 18; /* 18 beats == 1 phase */
        r /= 18.0f;
        r *= TWO_PI;
        float mVelocity = sin(r) * 0.5f + 0.5f;
        mVelocity *= 20;
        mVelocity += 3;
        return (int) mVelocity;
    }

    private void playBaseSequence(int beatCount) {
        Tone.instrument(INSTRUMENT_BASE);
        int mCounter = beatCount % mBaseSequence.length;
        int mStep = mBaseSequence[mCounter];
        if (mStep == X) {
            Tone.note_off();
        } else if (mStep == O) {
            /* do nothing, continue playing note */
        } else {
            int mNote = Scale.get_note(Scale.HALF_TONE, Note.NOTE_C2, mStep);
            Tone.note_on(mNote, 110);
        }
    }

    private void playMelody(int beatCount, float pVelocityScale) {
        Tone.instrument(INSTRUMENT_FLUTE);
        int mNote = getNote(beatCount);
        int mVelocity = (int) (getVelocity(beatCount) * pVelocityScale);
        Tone.note_on(mNote, mVelocity, 0.1f);
    }

    private void playMelodyWithEcho(int beatCount) {
        if (beatCount % 4 == 0) {
            playMelody(beatCount / 4, 1.0f);
        } else if (beatCount % 4 == 1) {
            playMelody(beatCount / 4, 0.5f);
        } else if (beatCount % 4 == 2) {
            playMelody(beatCount / 4, 0.25f);
        } else {
            playMelody(beatCount / 4, 0.125f);
        }
    }

    private void playNoise(float beatCount) {
        float r = beatCount;
        r *= 0.5;
        float mAmplitude = abs(sin(r * r + sin(r * 0.3f) * TWO_PI)) * abs(sin(r / 20.0f));
        Tone.instrument(INSTRUMENT_NOISE).set_amplitude(map(mAmplitude, 0, 1.0f, 0.001f, 0.03f));
    }

    public static void main(String[] args) {
        PApplet.main(TechniqueAlgorithmicComposition04FunctionSineWaves.class.getName());
    }
}
