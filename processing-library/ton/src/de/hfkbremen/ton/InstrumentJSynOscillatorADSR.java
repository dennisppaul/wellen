package de.hfkbremen.ton;

import com.jsyn.data.SegmentedEnvelope;
import com.jsyn.unitgen.SawtoothOscillator;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.SquareOscillator;
import com.jsyn.unitgen.TriangleOscillator;
import com.jsyn.unitgen.UnitGenerator;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.VariableRateMonoReader;
import com.jsyn.unitgen.WhiteNoise;
import com.softsynth.shared.time.TimeStamp;
import controlP5.ControlElement;

public class InstrumentJSynOscillatorADSR extends InstrumentJSynOscillator {

    protected VariableRateMonoReader mEnvPlayer;
    protected SegmentedEnvelope mEnvData;

    private boolean mDumpWarningLFO = true;
    private boolean mDumpWarningFILTER = true;

    public InstrumentJSynOscillatorADSR(ToneEngineJSyn mSynthesizerJSyn, int pID) {
        super(mSynthesizerJSyn, pID);
    }

    public void update_freq() {
        if (mOsc instanceof UnitOscillator) {
            UnitOscillator uo = (UnitOscillator) mOsc;
            uo.frequency.set(mFreq + mFreqOffset);
        }
    }

    public void amplitude(float pAmp) {
        mAmp = pAmp;
        if (mOsc instanceof UnitOscillator) {
            UnitOscillator uo = (UnitOscillator) mOsc;
            uo.amplitude.set(pAmp);
        } else if (mOsc instanceof WhiteNoise) {
            WhiteNoise uo = (WhiteNoise) mOsc;
            uo.amplitude.set(pAmp);
        }
    }

    public void frequency(float freq) {
        mFreq = freq;
        update_freq();
    }

    public void noteOff() {
        mEnvPlayer.dataQueue.queueOff(mEnvData, true, new TimeStamp(mSynth.getCurrentTime()));
    }

    public void noteOn(float pFreq, float pAmp) {
        update_env_data();
        mEnvData.setSustainBegin(2);
        mEnvData.setSustainEnd(2);
        mFreq = pFreq;
        update_freq();
        TimeStamp mTimeStamp = new TimeStamp(mSynth.getCurrentTime());
        mEnvPlayer.amplitude.set(pAmp, mTimeStamp);
        mEnvPlayer.dataQueue.queueOn(mEnvData, mTimeStamp);
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
            case SINE:
                mOsc = new SineOscillator();
                break;
            case TRIANGLE:
                mOsc = new TriangleOscillator();
                break;
            case SAWTOOTH:
                mOsc = new SawtoothOscillator();
                break;
            case SQUARE:
                mOsc = new SquareOscillator();
                break;
            case NOISE:
                mOsc = new WhiteNoise();
                break;
        }
        connectModules(mOsc);
    }

    public int get_osc_type() {
        int mOscID = -1;
        if (mOsc instanceof SineOscillator) {
            mOscID = SINE;
        } else if (mOsc instanceof TriangleOscillator) {
            mOscID = TRIANGLE;
        } else if (mOsc instanceof SawtoothOscillator) {
            mOscID = SAWTOOTH;
        } else if (mOsc instanceof SquareOscillator) {
            mOscID = SQUARE;
        } else if (mOsc instanceof WhiteNoise) {
            mOscID = NOISE;
        }
        return mOscID;
    }

    @Override
    public void lfo_amp(float pLFOAmp) {
        if (mDumpWarningLFO) {
            System.out.println("### LFO not implemented.");
            mDumpWarningLFO = false;
        }
    }

    @Override
    public float get_lfo_amp() {
        return 0;
    }

    @Override
    public void lfo_freq(float pLFOFreq) {
        if (mDumpWarningLFO) {
            System.out.println("### LFO not implemented.");
            mDumpWarningLFO = false;
        }
    }

    @Override
    public float get_lfo_freq() {
        return 0;
    }

    @Override
    public void filter_q(float f) {
        if (mDumpWarningFILTER) {
            System.out.println("### FILTER not implemented.");
            mDumpWarningFILTER = false;
        }
    }

    @Override
    public float get_filter_q() {
        return 0;
    }

    @Override
    public void filter_freq(float f) {
        if (mDumpWarningFILTER) {
            System.out.println("### FILTER not implemented.");
            mDumpWarningFILTER = false;
        }
    }

    @Override
    public float get_filter_freq() {
        return 0;
    }

    public void pitch_bend(float freq_offset) {
        mFreqOffset = freq_offset;
        update_freq();
    }

    @ControlElement(properties = {"min=0.0", "max=2.0", "type=knob", "radius=20", "resolution=1000"}, x = 0, y = 0)
    public void attack(float pAttack) {
        super.attack(pAttack);
    }

    @ControlElement(properties = {"min=0.0", "max=2.0", "type=knob", "radius=20", "resolution=1000"}, x = 50, y = 0)
    public void decay(float pDecay) {
        super.decay(pDecay);
    }

    @ControlElement(properties = {"min=0.0", "max=1.0", "type=knob", "radius=20", "resolution=100"}, x = 100, y = 0)
    public void sustain(float pSustain) {
        super.sustain(pSustain);
    }

    @ControlElement(properties = {"min=0.0", "max=2.0", "type=knob", "radius=20", "resolution=1000"}, x = 150, y = 0)
    public void release(float pRelease) {
        super.release(pRelease);
    }

    public void trigger() {
        mEnvPlayer.dataQueue.clear();
        update_env_data();
        mEnvPlayer.dataQueue.queue(mEnvData, 0, mEnvData.getNumFrames());
    }

    VariableRateMonoReader env() {
        return mEnvPlayer;
    }

    protected void setupModules() {
        if (mEnvPlayer == null) {
            update_env_data();
            mEnvPlayer = new VariableRateMonoReader();
            mSynth.add(mEnvPlayer);
            mEnvPlayer.start();
        }
    }

    protected void connectModules(UnitGenerator o) {
        setupModules();
        mSynth.add(o);
        if (o instanceof UnitOscillator) {
            UnitOscillator uo = (UnitOscillator) o;
            uo.amplitude.set(0);
            uo.frequency.set(220);
            uo.output.connect(0, mLineOut.input, 0);
            uo.output.connect(0, mLineOut.input, 1);
            mEnvPlayer.output.connect(uo.amplitude);
        } else if (o instanceof WhiteNoise) {
            WhiteNoise uo = (WhiteNoise) o;
            uo.amplitude.set(0);
            uo.output.connect(0, mLineOut.input, 0);
            uo.output.connect(0, mLineOut.input, 1);
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

    protected void update_env_data() {
        double[] mData = {mAttack, 1.0 * mAmp, // get_attack
                mDecay, // get_decay
                mSustain * mAmp, // get_sustain
                mRelease, 0.0, // get_release
        };
        mEnvData = new SegmentedEnvelope(mData);
    }
}
