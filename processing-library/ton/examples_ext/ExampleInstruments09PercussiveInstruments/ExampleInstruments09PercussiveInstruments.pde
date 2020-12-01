import de.hfkbremen.ton.*; 
import controlP5.*; 
import netP5.*; 
import oscP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 

static final int O = -1;

static final int I = 0;

static final int BASS = 0;

static final int SNARE = 1;

static final int HIHAT = 2;

static final int NUMBER_OF_INSTRUMENTS = 3;

final int[][] mSteps = new int[NUMBER_OF_INSTRUMENTS][];

void settings() {
    size(640, 480);
}

void setup() {
    Ton.instrument(BASS).osc_type(Ton.OSC_SQUARE);
    Ton.instrument(BASS).attack(0.01f);
    Ton.instrument(BASS).decay(0.04f);
    Ton.instrument(BASS).sustain(0.0f);
    Ton.instrument(BASS).release(0.0f);
    mSteps[BASS] = new int[]{I, O, O, O,
                             O, O, O, O,
                             I, O, O, O,
                             O, O, O, I,};
    Ton.instrument(SNARE).osc_type(Ton.OSC_NOISE);
    Ton.instrument(SNARE).attack(0.01f);
    Ton.instrument(SNARE).decay(0.2f);
    Ton.instrument(SNARE).sustain(0.0f);
    Ton.instrument(SNARE).release(0.0f);
    mSteps[SNARE] = new int[]{O, O, O, O,
                              I, O, O, O,
                              O, O, O, O,
                              I, O, O, O,};
    Ton.instrument(HIHAT).osc_type(Ton.OSC_NOISE);
    Ton.instrument(HIHAT).attack(0.01f);
    Ton.instrument(HIHAT).decay(0.04f);
    Ton.instrument(HIHAT).sustain(0.0f);
    Ton.instrument(HIHAT).release(0.0f);
    mSteps[HIHAT] = new int[]{I, O, I, O,
                              I, O, I, O,
                              I, O, I, O,
                              I, I, I, I,};
    ToneEngine.createInstrumentsGUI(this, Ton.instance(), BASS, SNARE, HIHAT);
    Beat.start(this, 130 * 4);
}

void draw() {
    background(50);
}

void beat(int pBeat) {
    for (int i = 0; i < NUMBER_OF_INSTRUMENTS; i++) {
        Ton.instrument(i);
        int mStep = mSteps[i][pBeat % mSteps[i].length];
        if (mStep == I) {
            int mNote = Scale.note(Scale.HALF_TONE, Note.NOTE_C3, mStep);
            Ton.note_on(mNote, 100);
        } else {
            Ton.note_off();
        }
    }
}
