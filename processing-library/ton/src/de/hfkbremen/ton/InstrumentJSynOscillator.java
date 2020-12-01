package de.hfkbremen.ton;

import com.jsyn.unitgen.SawtoothOscillator;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.SquareOscillator;
import com.jsyn.unitgen.TriangleOscillator;
import com.jsyn.unitgen.UnitGenerator;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.WhiteNoise;
import controlP5.ControlElement;

import static de.hfkbremen.ton.Ton.*;

public class InstrumentJSynOscillator extends InstrumentJSyn {

    protected UnitGenerator mOsc;
    protected float mFreqOffset;

    public InstrumentJSynOscillator(ToneEngineJSyn mSynthesizerJSyn, int pID) {
        super(mSynthesizerJSyn, pID);

        mFreqOffset = 0.0f;

        mOsc = new SineOscillator();
        connectModules(mOsc);
    }

    @ControlElement(properties = {"min=0.0",
            "max=" + (NUMBER_OF_OSCILLATORS - 1),
            "type=knob",
            "radius=20",
            "resolution=" + (NUMBER_OF_OSCILLATORS - 1)}, x = 200, y = 0)
    public void osc_type(int pOsc) {
        disconnectModules(mOsc);
        /*
         SINE,
         TRIANGLE,
         SAWTOOTH,
         SQUARE,
         NOISE
         */
        switch (pOsc) {
            case OSC_SINE:
                mOsc = new SineOscillator();
                break;
            case OSC_TRIANGLE:
                mOsc = new TriangleOscillator();
                break;
            case OSC_SAWTOOTH:
                mOsc = new SawtoothOscillator();
                break;
            case OSC_SQUARE:
                mOsc = new SquareOscillator();
                break;
            case OSC_NOISE:
                mOsc = new WhiteNoise();
                break;
        }
        connectModules(mOsc);
    }

    public int get_osc_type() {
        int mOscID = -1;
        if (mOsc instanceof SineOscillator) {
            mOscID = OSC_SINE;
        } else if (mOsc instanceof TriangleOscillator) {
            mOscID = OSC_TRIANGLE;
        } else if (mOsc instanceof SawtoothOscillator) {
            mOscID = OSC_SAWTOOTH;
        } else if (mOsc instanceof SquareOscillator) {
            mOscID = OSC_SQUARE;
        } else if (mOsc instanceof WhiteNoise) {
            mOscID = OSC_NOISE;
        }
        return mOscID;
    }

    public void lfo_amp(float pLFOAmp) {
    }

    public float get_lfo_amp() {
        return 0;
    }

    public void lfo_freq(float pLFOFreq) {
    }

    public float get_lfo_freq() {
        return 0;
    }

    public void filter_q(float f) {
    }

    public float get_filter_q() {
        return 0;
    }

    public void filter_freq(float f) {
    }

    public float get_filter_freq() {
        return 0;
    }

    public void pitch_bend(float freq_offset) {
        mFreqOffset = freq_offset;
        update_freq();
    }

    public void amplitude(float pAmp) {
        mAmp = pAmp;
        if (mOsc instanceof UnitOscillator) {
            UnitOscillator uo = (UnitOscillator) mOsc;
            uo.amplitude.set(mAmp);
        } else if (mOsc instanceof WhiteNoise) {
            WhiteNoise uo = (WhiteNoise) mOsc;
            uo.amplitude.set(mAmp);
        }
    }

    public void frequency(float freq) {
        mFreq = freq;
        update_freq();
    }

    public void note_off() {
        amplitude(0);
        mIsPlaying = false;
    }

    public void note_on(int note, int velocity) {
        frequency(_note_to_frequency(note));
        amplitude(_velocity_to_amplitude(velocity));
        mIsPlaying = true;
    }

//    public void attack(float pAttack) {
//        super.attack(pAttack);
//    }
//
//    public void decay(float pDecay) {
//        super.decay(pDecay);
//    }
//
//    public void sustain(float pSustain) {
//        super.sustain(pSustain);
//    }
//
//    public void release(float pRelease) {
//        super.release(pRelease);
//    }

    protected void connectModules(UnitGenerator o) {
        mSynth.add(o);
        if (o instanceof UnitOscillator) {
            UnitOscillator uo = (UnitOscillator) o;
            uo.amplitude.set(0);
            uo.frequency.set(220);
            uo.output.connect(0, mLineOut.input, 0);
            uo.output.connect(0, mLineOut.input, 1);
        } else if (o instanceof WhiteNoise) {
            WhiteNoise uo = (WhiteNoise) o;
            uo.amplitude.set(0);
            uo.output.connect(0, mLineOut.input, 0);
            uo.output.connect(0, mLineOut.input, 1);
        }
    }

    protected void disconnectModules(UnitGenerator o) {
        o.stop();
        if (o instanceof UnitOscillator) {
            UnitOscillator uo = (UnitOscillator) o;
            uo.amplitude.set(0);
            uo.output.disconnect(mLineOut.input);
            uo.output.disconnectAll();
        } else if (o instanceof WhiteNoise) {
            WhiteNoise uo = (WhiteNoise) o;
            uo.amplitude.set(0);
            uo.output.disconnect(mLineOut.input);
            uo.output.disconnectAll();
        }
        mSynth.remove(o);
    }

    protected void update_freq() {
        if (mOsc instanceof UnitOscillator) {
            UnitOscillator uo = (UnitOscillator) mOsc;
            uo.frequency.set(mFreq + mFreqOffset);
        }
    }
}
