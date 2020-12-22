import wellen.*; 


static final int X = -1;

static final int O = -2;

static final int INSTRUMENT_BASE = 0;

static final int INSTRUMENT_FLUTE = 1;

static final int INSTRUMENT_NOISE = 2;

final int[] mBaseSequence = {0, O, X, 0,
                                     X, X, 0, X,
                                     X, 0, X, X,
                                     7, O, X, 12};

float mTime;

void settings() {
    size(640, 480);
}

void setup() {
    Beat.start(this, 240);
    Tone.instrument(INSTRUMENT_BASE).set_oscillator_type(Wellen.WAVESHAPE_TRIANGLE);
    Tone.instrument(INSTRUMENT_FLUTE).set_oscillator_type(Wellen.WAVESHAPE_SAWTOOTH);
    //@TODO("this might be broken!")
    Tone.replace_instrument(InstrumentInternal.class, INSTRUMENT_NOISE);
    Tone.instrument(INSTRUMENT_NOISE).set_oscillator_type(Wellen.WAVESHAPE_NOISE);
    Tone.instrument(INSTRUMENT_NOISE).note_on(1, 127);
    Tone.instrument(INSTRUMENT_NOISE).set_sustain(1.0f);
    Tone.instrument(INSTRUMENT_NOISE).set_amplitude(0.0f);
}

void draw() {
    background(255);
    mTime += 1.0f / frameRate;
    playNoise(mTime);
}

void beat(int pBeatCount) {
    playBaseSequence(pBeatCount);
    playMelodyWithEcho(pBeatCount);
}

void playMelodyWithEcho(int pBeatCount) {
    if (pBeatCount % 4 == 0) {
        playMelody(pBeatCount / 4, 1.0f);
    } else if (pBeatCount % 4 == 1) {
        playMelody(pBeatCount / 4, 0.5f);
    } else if (pBeatCount % 4 == 2) {
        playMelody(pBeatCount / 4, 0.25f);
    } else {
        playMelody(pBeatCount / 4, 0.125f);
    }
}

void playNoise(float pBeatCount) {
    float r = pBeatCount;
    r *= 0.5;
    float mAmplitude = abs(sin(r * r + sin(r * 0.3f) * TWO_PI)) * abs(sin(r / 20.0f));
    Tone.instrument(INSTRUMENT_NOISE).set_amplitude(map(mAmplitude, 0, 1.0f, 0.001f, 0.03f));
}

void playMelody(int pBeatCount, float pVelocityScale) {
    Tone.instrument(INSTRUMENT_FLUTE);
    int mNote = getNote(pBeatCount);
    int mVelocity = (int) (getVelocity(pBeatCount) * pVelocityScale);
    Tone.note_on(mNote, mVelocity, 0.1f);
}

int getVelocity(int pBeatCount) {
    float r = pBeatCount % 18; /* 18 beats == 1 phase */
    r /= 18.0f;
    r *= TWO_PI;
    float mVelocity = sin(r) * 0.5f + 0.5f;
    mVelocity *= 20;
    mVelocity += 3;
    return (int) mVelocity;
}

int getNote(int pBeatCount) {
    float r = pBeatCount % 32; /* 32 beats == 1 phase */
    r /= 32.0f;
    r *= TWO_PI;
    float mNoteStep = sin(r) * 0.5f + 0.5f;
    mNoteStep *= 5;
    int mNote = Scale.get_note(Scale.FIFTH, Note.NOTE_C3, (int) mNoteStep);
    return mNote;
}

void playBaseSequence(int pBeatCount) {
    Tone.instrument(INSTRUMENT_BASE);
    int mCounter = pBeatCount % mBaseSequence.length;
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
