package de.hfkbremen.ton.examples;

import com.jsyn.unitgen.MixerMono;
import com.jsyn.unitgen.SawtoothOscillator;
import com.jsyn.unitgen.UnitOscillator;
import de.hfkbremen.ton.InstrumentJSyn;
import de.hfkbremen.ton.ToneEngine;
import de.hfkbremen.ton.ToneEngineJSyn;
import processing.core.PApplet;

/**
 * this examples shows how to create a custom jsyn instrument. for further information on jsyn visit the website:
 * http://www.softsynth.com/jsyn/
 */
public class SketchExampleInstruments04InstrumentJSynCustom extends PApplet {

    private InstrumentJSynCustom mInstrument;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        ToneEngineJSyn mToneEngine = new ToneEngineJSyn(ToneEngine.INSTRUMENT_EMPTY);
        mInstrument = new InstrumentJSynCustom(mToneEngine, 0);
        mInstrument.amplitude(0.8f);
    }

    public void draw() {
        background(255);
        noFill();
        stroke(0);
        ellipse(mouseX, mouseY, 10, 10);
        mInstrument.frequency(map(mouseX, 0, width, 22.5f, 440.0f));
        mInstrument.set_freq_offset(map(mouseY, 0, height, -10.0f, 10.0f));
    }

    private static class InstrumentJSynCustom extends InstrumentJSyn {

        private final UnitOscillator mOsc1;
        private final UnitOscillator mOsc2;
        private final UnitOscillator mOsc3;

        private float mFreqOffset;

        public InstrumentJSynCustom(ToneEngineJSyn pToneEngine, int pID) {
            super(pToneEngine, pID);

            mOsc1 = new SawtoothOscillator();
            mSynth.add(mOsc1);
            mOsc1.start();

            mOsc2 = new SawtoothOscillator();
            mSynth.add(mOsc2);
            mOsc2.start();

            mOsc3 = new SawtoothOscillator();
            mSynth.add(mOsc3);
            mOsc3.start();

            MixerMono mMixerMono = new MixerMono(3);
            mOsc1.output.connect(mMixerMono.input.getConnectablePart(0));
            mOsc2.output.connect(mMixerMono.input.getConnectablePart(1));
            mOsc3.output.connect(mMixerMono.input.getConnectablePart(2));
            mMixerMono.amplitude.set(0.5f);

            mMixerMono.output.connect(0, mLineOut.input, 0);
            mMixerMono.output.connect(0, mLineOut.input, 1);
        }

        public void amplitude(float pAmp) {
            mAmp = pAmp;
            mOsc1.amplitude.set(mAmp * 0.6);
            mOsc2.amplitude.set(mAmp * 0.6);
            mOsc3.amplitude.set(mAmp * 1.0);
        }

        public void frequency(float freq) {
            mFreq = freq;
            mOsc1.frequency.set(mFreq);
            mOsc2.frequency.set(mFreq + mFreqOffset);
            mOsc3.frequency.set(mFreq / 2 - mFreqOffset);
        }

        public void set_freq_offset(float freq_offest) {
            mFreqOffset = freq_offest;
        }

    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleInstruments04InstrumentJSynCustom.class.getName());
    }
}
