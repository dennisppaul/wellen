import ton.*; 
import netP5.*; 
import oscP5.*; 

void settings() {
    size(640, 480);
}

void setup() {
    /* disable ADSR */
    Ton.instrument().enable_ADSR(false);
}

void draw() {
    background(255);
    noStroke();
    fill(255 - 255 * Ton.instrument().get_amplitude());
    float mScale = map(Ton.instrument().get_frequency(), 110, 440, 0.5f, 0.2f);
    ellipse(width * 0.5f, height * 0.5f, width * mScale, width * mScale);
}

void mouseMoved() {
    float mFreq = map(mouseX, 0, width, 110, 440);
    float mAmp = mouseY / (float) height;
    Ton.instrument().set_frequency(mFreq);
    Ton.instrument().set_amplitude(mAmp);
}
