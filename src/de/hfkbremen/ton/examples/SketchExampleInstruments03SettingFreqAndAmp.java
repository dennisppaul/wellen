package de.hfkbremen.ton.examples;

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
        float mFreq = 110 + 330 * mouseX / (float) width;
        float mAmp = mouseY / (float) height;
        mInstrument.frequency(mFreq);
        mInstrument.amplitude(mAmp);

        background(mAmp * 255);
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleInstruments03SettingFreqAndAmp.class.getName());
    }
}
