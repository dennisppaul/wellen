import wellen.*; 

/*
 * this example demonstrates how to make SAM sing.
 *
 * move and drag mouse to change parameters.
 */

SAM mSAM;

TextFragment[] mWords;

void settings() {
    size(640, 480);
}

void setup() {
    mSAM = new SAM();
    mSAM.set_sing_mode(true);
    mWords = new TextFragment[]{
    new TextFragment("EHEHEHEH", Note.NOTE_C3),
    new TextFragment("    ", Note.NOTE_C3),
    new TextFragment("VERIY", Note.NOTE_D3),
    new TextFragment("     ", Note.NOTE_D3),
    new TextFragment("TAYM", Note.NOTE_D3 + 1),
    new TextFragment("AY", Note.NOTE_F3),
    new TextFragment("  ", Note.NOTE_F3),
    new TextFragment("SIYIY", Note.NOTE_G3),
    new TextFragment("     ", Note.NOTE_G3),
    new TextFragment("YUW", Note.NOTE_F3),
    new TextFragment("   ", Note.NOTE_F3),
    new TextFragment("FAOAOAOAO", Note.NOTE_D3 + 1),
    new TextFragment("   ", Note.NOTE_D3 + 1),
    new TextFragment("LIHNX", Note.NOTE_F3),
    new TextFragment("     ", Note.NOTE_F3),
    new TextFragment("AY", Note.NOTE_C3),
    new TextFragment("GEHT", Note.NOTE_A3 + 1),
    new TextFragment("    ", Note.NOTE_A3 + 1),
    new TextFragment("DAWN", Note.NOTE_G3 + 1),
    new TextFragment("    ", Note.NOTE_G3 + 1),
    new TextFragment("AAN", Note.NOTE_G3),
    new TextFragment("MAY", Note.NOTE_F3),
    new TextFragment("   ", Note.NOTE_F3),
    new TextFragment("NIYZ", Note.NOTE_D3 + 1),
    new TextFragment("    ", Note.NOTE_D3 + 1),
    new TextFragment("AEND", Note.NOTE_F3),
    new TextFragment("    ", Note.NOTE_F3),
    new TextFragment("PREY", Note.NOTE_C3),
    new TextFragment("    ", Note.NOTE_C3),
    new TextFragment("", Note.NOTE_C3),
    new TextFragment("", Note.NOTE_C3),
    new TextFragment("", Note.NOTE_C3),
    };
    // | 1     |       | 2     |       | 3     |       | 4     |       |
    // | EH----------- | VERIY-------- | TAYM- | AY ---------- | SIY----
    // | 5     |       | 6     |       | 7     |       | 8     |       |
    // ------- |YUW----------- | FAO---------- | LIHNX-------- | AY--- |
    // | 9     |       | 10    |       | 11    |       | 12    |       |
    // | GEHT--------- | DAWN--------- | AAN-- | MAY---------- | NIYZ---
    // | 13    |       | 14    |       | 15    |       | 16    |       |
    // ------- | AEND--------- | PREY--------- |                       |
    DSP.start(this);
    Beat.start(this, 240);
}
static class TextFragment {
    final String text;
    final int pitch;
    TextFragment(String pText, int pPitch) {
        text = pText;
        pitch = pPitch;
    }
}

void draw() {
    background(255);
    stroke(0);
    DSP.draw_buffer(g, width, height);
}

void beat(int pBeatCount) {
    int mWordIndex = pBeatCount % mWords.length;
    mSAM.set_pitch(SAM.get_pitch_from_MIDI_note(mWords[mWordIndex].pitch));
    mSAM.say(mWords[mWordIndex].text, true);
    Tone.note_on(pBeatCount % 2 == 0 ? Note.NOTE_C2 : Note.NOTE_C3, 50);
}

void mouseMoved() {
    mSAM.set_mouth((int) map(mouseX, 0, width, 0, 255));
    mSAM.set_throat((int) map(mouseY, 0, height, 0, 255));
}

void mouseDragged() {
    mSAM.set_speed((int) map(mouseX, 0, width, 0, 255));
}

void audioblock(float[] pOutputSignal) {
    for (int i = 0; i < pOutputSignal.length; i++) {
        pOutputSignal[i] = mSAM.output() * 0.5f;
    }
}
