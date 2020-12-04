import welle.*; 
import netP5.*; 
import oscP5.*; 

void settings() {
    size(640, 480);
}

void setup() {
    /* disable ADSR */
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
    float mAmp = mouseY / (float) height;
    Tone.instrument().set_frequency(mFreq);
    Tone.instrument().set_amplitude(mAmp);
}
