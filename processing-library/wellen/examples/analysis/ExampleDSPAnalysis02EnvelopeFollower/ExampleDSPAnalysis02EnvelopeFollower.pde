import wellen.*; 
import wellen.dsp.*; 

import wellen.analysis.*;
/*
 * this example demonstrates how to detect an envelope from an input signal. it uses that information to set the
 * amplitude of an oscillator.
 */
final EnvelopeFollower fEnvelopeFollower = new EnvelopeFollower();
float[] fEnvelopeFollowerBuffer;
final Wavetable fWavetable = new Wavetable();
void settings() {
    size(640, 480);
}
void setup() {
    fWavetable.set_waveform(10, Wellen.WAVEFORM_SQUARE);
    fWavetable.set_frequency(110);
    fEnvelopeFollower.set_attack(0.0002f);
    fEnvelopeFollower.set_release(0.0004f);
    DSP.start(this, 1, 1);
}
void mouseMoved() {
    fEnvelopeFollower.set_attack(map(mouseX, 0, width, 0, Wellen.seconds_to_samples(0.1f)));
    fEnvelopeFollower.set_release(map(mouseY, 0, height, 0, Wellen.seconds_to_samples(0.1f)));
}
void draw() {
    background(255);
    noStroke();
    fill(0);
    circle(width * 0.5f, height * 0.5f, height * 0.98f);
    fill(255);
    float mEnvelopeAverage = getEnvelopeAverage();
    circle(width * 0.5f, height * 0.5f, mEnvelopeAverage * 100);
    stroke(255);
    DSP.draw_buffers(g, width, height);
    Wellen.draw_buffer(g, width, height, fEnvelopeFollowerBuffer);
}
void audioblock(float[] output_signal, float[] pInputSignal) {
    fEnvelopeFollowerBuffer = fEnvelopeFollower.process(pInputSignal);
    for (int i = 0; i < output_signal.length; i++) {
        fWavetable.set_amplitude(fEnvelopeFollowerBuffer[i]);
        output_signal[i] = fWavetable.output();
    }
}
float getEnvelopeAverage() {
    float mEnvelopeAverage = 0;
    if (fEnvelopeFollowerBuffer != null) {
        for (float v : fEnvelopeFollowerBuffer) {
            mEnvelopeAverage += v;
        }
        mEnvelopeAverage /= fEnvelopeFollowerBuffer.length;
    }
    return mEnvelopeAverage;
}
