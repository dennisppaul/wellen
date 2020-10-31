package de.hfkbremen.ton;

import com.jsyn.unitgen.Add;
import com.jsyn.unitgen.FilterLowPass;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.UnitGenerator;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.VariableRateMonoReader;
import com.jsyn.unitgen.WhiteNoise;
import controlP5.ControlElement;

public class InstrumentJSynOscillatorADSRFilterLFO extends InstrumentJSynOscillatorADSR {

    private FilterLowPass mLowPassFilter;
    private SineOscillator mLFO;
    private Add mAddUnit;

    public InstrumentJSynOscillatorADSRFilterLFO(ToneEngineJSyn mSynthesizerJSyn, int pID) {
        super(mSynthesizerJSyn, pID);
    }

    protected void setupModules() {
        if (mLowPassFilter == null) {
            mLowPassFilter = new FilterLowPass();
            mLowPassFilter.output.connect(0, mLineOut.input, 0);
            mLowPassFilter.output.connect(0, mLineOut.input, 1);
            mLowPassFilter.frequency.set(2000);
            mLowPassFilter.Q.set(1);
            mSynth.add(mLowPassFilter);
        }
        if (mLFO == null) {
            mLFO = new SineOscillator();
            mLFO.amplitude.set(3);
            mLFO.frequency.set(10.0f);
            mSynth.add(mLFO);
        }
        if (mAddUnit == null) {
            mAddUnit = new Add();
            mAddUnit.inputA.set(220);
            mLFO.output.connect(mAddUnit.inputB);
            mSynth.add(mAddUnit);
        }
    }

    protected void connectModules(UnitGenerator o) {
        super.setupModules();
        setupModules();
        mSynth.add(o);
        if (o instanceof UnitOscillator) {
            UnitOscillator uo = (UnitOscillator) o;
            uo.amplitude.set(0);
            uo.output.connect(mLowPassFilter.input);
            mEnvPlayer.output.connect(uo.amplitude);
            mAddUnit.output.connect(uo.frequency);
        } else if (o instanceof WhiteNoise) {
            WhiteNoise uo = (WhiteNoise) o;
            uo.amplitude.set(0);
            uo.output.connect(mLowPassFilter.input);
            mEnvPlayer.output.connect(uo.amplitude);
        }
    }

    protected void disconnectModules(UnitGenerator o) {
        o.stop();
        if (o instanceof UnitOscillator) {
            UnitOscillator uo = (UnitOscillator) o;
            uo.amplitude.set(0);
            uo.output.disconnect(mLineOut.input);
            uo.output.disconnectAll();
            mEnvPlayer.output.disconnect(uo.amplitude);
        } else if (o instanceof WhiteNoise) {
            WhiteNoise uo = (WhiteNoise) o;
            uo.amplitude.set(0);
            uo.output.disconnect(mLineOut.input);
            uo.output.disconnectAll();
            mEnvPlayer.output.disconnect(uo.amplitude);
        }
        mSynth.remove(o);
    }

    public void update_freq() {
        mAddUnit.inputA.set(mFreq + mFreqOffset);
    }

    @ControlElement(properties = {"min=0.0", "max=100.0", "type=knob", "radius=20", "resolution=1000"}, x = 250, y = 0)
    public void lfo_amp(float pLFOAmp) {
        mLFO.amplitude.set(pLFOAmp);
    }

    public float get_lfo_amp() {
        return (float) mLFO.amplitude.get();
    }

    @ControlElement(properties = {"min=0.0", "max=100.0", "type=knob", "radius=20", "resolution=1000"}, x = 300, y = 0)
    public void lfo_freq(float pLFOFreq) {
        mLFO.frequency.set(pLFOFreq);
    }

    public float get_lfo_freq() {
        return (float) mLFO.frequency.get();
    }

    @ControlElement(properties = {"min=0.0", "max=5", "type=knob", "radius=20", "resolution=100"}, x = 350, y = 0)
    public void filter_q(float pQ) {
        mLowPassFilter.Q.set(pQ);
    }

    public float get_filter_q() {
        return (float) mLowPassFilter.Q.get();
    }

    @ControlElement(properties = {"min=0.0", "max=30000", "type=knob", "radius=20", "resolution=300"}, x = 400, y = 0)
    public void filter_freq(float pFreq) {
        mLowPassFilter.frequency.set(pFreq);
    }

    public float get_filter_freq() {
        return (float) mLowPassFilter.frequency.get();
    }

    VariableRateMonoReader env() {
        return mEnvPlayer;
    }
}
