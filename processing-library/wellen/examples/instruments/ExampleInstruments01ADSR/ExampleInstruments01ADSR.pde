import wellen.*; 

/*
 * this example shows how to use an instrument with an amplitude envelope ( ADSR ). the envelope controls the
 * amplitude of a tone over time. the attack stage is started by calling `note_on()` fading the amplitude from 0 to
 * 1, moving via the decay stage to the sustain stage fading the amplitude to the sustain level. there the envelope
 * remains until `note_off()` is called which starts the release stage which fades the amplitude back to 0.
 *
 * note that this functionality is not implemented for MIDI and OSC.
 */

void settings() {
    size(640, 480);
}

void setup() {
    println(ADSR.ADSR_DIAGRAM);
    randomizeADSR();
}

void draw() {
    background(Tone.is_playing() ? 0 : 255);
    int mColor = Tone.is_playing() ? 255 : 0;
    stroke(mColor);
    fill(mColor);
    translate(width * 0.2f, height * 0.75f);
    scale(1, -1);
    ellipse(0, 0, 6, 6);
    float mStart = draw_connection_line(0.0f, Tone.instrument().get_attack(), 0.0f, 1.0f);
    mStart = draw_connection_line(mStart, Tone.instrument().get_decay(), 1.0f, Tone.instrument().get_sustain());
    mStart = draw_connection_line(mStart, 0.5f, Tone.instrument().get_sustain(), Tone.instrument().get_sustain());
    mStart = draw_connection_line(mStart, Tone.instrument().get_release(), Tone.instrument().get_sustain(), 0.0f);
    line(0.0f, 0.0f, mStart, 0.0f);
}

void mousePressed() {
    int mNote = Scale.get_note(Scale.CHORD_MAJOR_7TH, Note.NOTE_A2, (int) random(0, 10));
    Tone.note_on(mNote, 100);
}

void mouseReleased() {
    Tone.note_off();
}

void keyPressed() {
    randomizeADSR();
}

void randomizeADSR() {
    Tone.instrument().set_attack(random(0.0f, 0.5f));
    Tone.instrument().set_decay(random(0.0f, 0.5f));
    Tone.instrument().set_sustain(random(0.0f, 1.0f));
    Tone.instrument().set_release(random(0.0f, 1.0f));
    System.out.println("A: " + Tone.instrument().get_attack());
    System.out.println("D: " + Tone.instrument().get_decay());
    System.out.println("S: " + Tone.instrument().get_sustain());
    System.out.println("R: " + Tone.instrument().get_release());
    System.out.println("-------------");
}

float draw_connection_line(float pStartX, float pDuration, float pLevelStart, float pLevelEnd) {
    final float mScaleX = width * 0.25f;
    final float mScaleY = height * 0.25f;
    PVector mStart = new PVector().set(pStartX, mScaleY * pLevelStart);
    PVector mEnd = new PVector().set(pStartX + mScaleX * pDuration, mScaleY * pLevelEnd);
    strokeWeight(3);
    line(mStart.x, mStart.y, mEnd.x, mEnd.y);
    strokeWeight(0.5f);
    line(mEnd.x, 0, mEnd.x, mEnd.y);
    ellipse(mEnd.x, mEnd.y, 6, 6);
    return mEnd.x;
}
