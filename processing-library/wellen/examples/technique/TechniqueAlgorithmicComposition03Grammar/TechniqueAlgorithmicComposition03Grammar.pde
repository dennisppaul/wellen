import wellen.*; 


final String mInput = "RADIO, LIVE TRANSMISSION.\n" +
                              "RADIO, LIVE TRANSMISSION.\n" +
                              "LISTEN TO THE SILENCE, LET IT RING ON.\n" +
                              "EYES, DARK GREY LENSES FRIGHTENED OF THE SUN.\n" +
                              "WE WOULD HAVE A FINE TIME LIVING IN THE NIGHT,\n" +
                              "LEFT TO BLIND DESTRUCTION,\n" +
                              "WAITING FOR OUR SIGHT.";

final int mBaseNote = Note.NOTE_C3;

final int[] mScale = Scale.MINOR_PENTATONIC;

final int mMaxNoteCounter = 12;

boolean mSkipNote;

int mNoteCounter = 0;

int mNoteStep = 0;

int mCharCounter = 0;

char mCurrentChar = 0;

void settings() {
    size(640, 480);
    pixelDensity(displayDensity());
}

void setup() {
    textFont(createFont("Helvetica-Bold", 10));
    Tone.instrument(1).set_oscillator_type(Wellen.WAVESHAPE_SAWTOOTH);
    Tone.instrument(2).set_oscillator_type(Wellen.WAVESHAPE_SINE);
    Beat.start(this, 240);
}

void draw() {
    background(255);
    fill(0);
    noStroke();
    textSize(10);
    text(mInput, 20, 31);
    textSize(300);
    text(mCurrentChar, 20, 400);
    stroke(0, 91);
    line(20, height * 0.3f, width * 0.5f, height * 0.3f);
}

void beat(int pBeatCount) {
    mCurrentChar = mInput.charAt(mCharCounter);
    grammar(mCurrentChar);
    mCharCounter++;
    mCharCounter %= mInput.length();
    Tone.instrument(0);
    if (!mSkipNote) {
        int mNote = Scale.get_note(mScale, mBaseNote, mNoteCounter);
        Tone.note_on(mNote, 100);
    } else {
        Tone.note_off();
    }
    Tone.instrument(1);
    Tone.note_on(Note.NOTE_C2, 15, 0.05f);
    Tone.instrument(2);
    Tone.note_on(Note.NOTE_C2, 100, 0.15f);
}

void grammar(char c) {
    mSkipNote = false;
    switch (c) {
        case 'A':
        case 'E':
        case 'I':
        case 'O':
        case 'U':
            mNoteStep = 2;
            break;
        case 'B':
        case 'C':
        case 'D':
        case 'F':
        case 'G':
        case 'H':
        case 'J':
        case 'K':
        case 'L':
        case 'M':
        case 'N':
        case 'P':
        case 'Q':
        case 'R':
        case 'S':
        case 'T':
        case 'V':
        case 'W':
            mNoteStep = -1;
            break;
        case 'X':
        case 'Y':
        case 'Z':
            mNoteStep = 3;
            break;
        case '\n':
            mNoteStep = 0;
            mNoteCounter = 0;
            break;
        default:
            mSkipNote = true;
    }
    mNoteCounter += mNoteStep;
    if (mNoteCounter < 0) {
        mNoteCounter += mMaxNoteCounter;
    } else if (mNoteCounter > mMaxNoteCounter) {
        mNoteCounter -= mMaxNoteCounter;
    }
}
