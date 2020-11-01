package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.Instrument;
import de.hfkbremen.ton.ToneEngine;
import de.hfkbremen.ton.ToneEngineJSyn;
import processing.core.PApplet;

public class SketchExampleInstruments03FrequencyAndAmplitude extends PApplet {

    private final ToneEngineJSyn mToneEngine = new ToneEngineJSyn(ToneEngine.INSTRUMENT_WITH_OSCILLATOR);

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        background(255);
        mToneEngine.mute();
        mToneEngine.instrument().osc_type(Instrument.SAWTOOTH);
    }

    public void draw() {
        background(255);
        noStroke();
        fill(255 - 255 * mToneEngine.instrument().get_amplitude());
        float mScale = map(mToneEngine.instrument().get_frequency(), 110, 440, 0.5f, 0.2f);
        ellipse(width * 0.5f, height * 0.5f, width * mScale, width * mScale);
    }

    public void mouseMoved() {
        float mFreq = map(mouseX, 0, width, 110, 440);
        float mAmp = mouseY / (float) height;
        mToneEngine.instrument().frequency(mFreq);
        mToneEngine.instrument().amplitude(mAmp);
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleInstruments03FrequencyAndAmplitude.class.getName());
    }
}
