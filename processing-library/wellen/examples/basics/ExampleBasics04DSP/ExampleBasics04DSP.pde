import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to perform digital signal processing (DSP) by continuously writing to an audio
 * buffer.
 */
float mAmp = 0.5f;
int mCounter = 0;
float mFreq = 440.0f;
void settings() {
    size(640, 480);
}
void setup() {
    DSP.start(this);
}
void draw() {
    background(255);
    stroke(0);
    Wellen.draw_tone(g, width, height);
}
void mouseMoved() {
    mFreq = map(mouseX, 0, width, 55, 440);
    mAmp = map(mouseY, 0, height, 0, 1);
}
void audioblock(float[] output_signal) {
    for (int i = 0; i < output_signal.length; i++) {
        mCounter++;
        output_signal[i] = mAmp * sin(2 * PI * mFreq * mCounter / DSP.get_sample_rate());
    }
}
