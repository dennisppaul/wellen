package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.Instrument;
import de.hfkbremen.ton.InstrumentJSynOscillator;
import de.hfkbremen.ton.ToneEngine;
import de.hfkbremen.ton.ToneEngineJSyn;
import processing.core.PApplet;

public class SketchExampleInstruments03SettingFreqAndAmp extends PApplet {

    private final ToneEngineJSyn mToneEngine = new ToneEngineJSyn(ToneEngine.INSTRUMENT_EMPTY);
    private InstrumentJSynOscillator mInstrument;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        background(255);
        mInstrument = new InstrumentJSynOscillator(mToneEngine, 0);
        mInstrument.osc_type(Instrument.SAWTOOTH);
    }

    public void draw() {
        float mFreq = map(mouseX, 0, width, 110, 440);
        float mAmp = mouseY / (float) height;
        mInstrument.frequency(mFreq);
        mInstrument.amplitude(mAmp);

        background(255);
        noStroke();
        fill(255 - 255 * mAmp);
        float mScale = map(mFreq, 110, 440, 0.2f, 0.5f);
        ellipse(width * 0.5f, height * 0.5f, width * mScale, width * mScale);
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleInstruments03SettingFreqAndAmp.class.getName());
    }
}
