package de.hfkbremen.ton;

import com.jsyn.engine.SynthesisEngine;
import com.jsyn.unitgen.LineOut;

public class InstrumentJSyn extends Instrument {

    protected final SynthesisEngine mSynth;
    protected final LineOut mLineOut;
    protected float mAmp;
    protected float mFreq;

    public InstrumentJSyn(ToneEngineJSyn mSynthesizerJSyn, int pID) {
        super(pID);
        mSynth = mSynthesizerJSyn.synth();
        mLineOut = mSynthesizerJSyn.line_out();
        mAmp = 0.9f;
        mFreq = 0.0f;
    }

    @Override
    public void osc_type(int pOsc) {

    }

    @Override
    public int get_osc_type() {
        return 0;
    }

    @Override
    public void lfo_amp(float pLFOAmp) {

    }

    @Override
    public float get_lfo_amp() {
        return 0;
    }

    @Override
    public void lfo_freq(float pLFOFreq) {

    }

    @Override
    public float get_lfo_freq() {
        return 0;
    }

    @Override
    public void filter_q(float f) {

    }

    @Override
    public float get_filter_q() {
        return 0;
    }

    @Override
    public void filter_freq(float f) {
    }

    @Override
    public float get_filter_freq() {
        return 0;
    }

    @Override
    public void pitch_bend(float freq_offset) {
    }

    @Override
    public void amplitude(float pAmp) {
        mAmp = pAmp;
    }

    @Override
    public float get_amplitude() {
        return mAmp;
    }

    @Override
    public void frequency(float freq) {
        mFreq = freq;
    }

    @Override
    public float get_frequency() {
        return mFreq;
    }

    @Override
    public void noteOff() {
        amplitude(0);
    }

    @Override
    public void noteOn(float pFreq, float pAmp) {
        amplitude(pAmp);
        frequency(pFreq);
    }
}
