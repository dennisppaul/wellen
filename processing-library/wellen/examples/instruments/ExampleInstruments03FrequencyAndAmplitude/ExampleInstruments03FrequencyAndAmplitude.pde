import wellen.*; 

/*
 * this example demonstrates how to control the tone engine’s frequency and amplitude directly ( instead of setting
 * it by a note and its velocity ). in order to control frequency and amplitude directly the ADSR envelope, normally
 * controlling the amplitude, needs to be disabled.
 *
 * note that this functionality is not implemented for MIDI and OSC.
 */

void settings() {
    size(640, 480);
}

void setup() {
    /* disable ADSR to release amplitude control */
    Tone.instrument().enable_ADSR(false);
}

void draw() {
    background(255);
    noStroke();
    fill(255 - 255 * Tone.instrument().get_amplitude());
    float mScale = map(Tone.instrument().get_frequency(), 110, 440, 0.5f, 0.2f);
    ellipse(width * 0.5f, height * 0.5f, width * mScale, width * mScale);
}

void mouseMoved() {
    float mFreq = map(mouseX, 0, width, 110, 440);
    float mAmp = map(mouseY, 0, height, 0, 1);
    Tone.instrument().set_frequency(mFreq);
    Tone.instrument().set_amplitude(mAmp);
}
