package de.hfkbremen.ton;

import com.jsyn.engine.SynthesisEngine;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.UnitOscillator;

import java.util.ArrayList;
import java.util.Timer;

import static de.hfkbremen.ton.DSP.DEFAULT_SAMPLING_RATE;
import static processing.core.PApplet.constrain;

public class ToneEngineJSyn extends ToneEngine {

    public static final int DEFAULT_DEVICE = -1;
    private static final boolean USE_AMP_FRACTION = false;
    private final SynthesisEngine mSynth;
    private final LineOut mLineOut;
    private final ArrayList<InstrumentJSyn> mInstruments;
    private final Timer mTimer;
    private int mCurrentInstrumentID;

    public ToneEngineJSyn() {
        this(INSTRUMENT_WITH_OSCILLATOR_ADSR);
    }

    public ToneEngineJSyn(int pDefaultInstrumentType) {
        this(pDefaultInstrumentType, DEFAULT_SAMPLING_RATE, DEFAULT_DEVICE, 1, DEFAULT_DEVICE, 2);
    }

    public ToneEngineJSyn(int pDefaultInstrumentType, int pOutputDeviceID, int pOutputChannels) {
        this(pDefaultInstrumentType, DEFAULT_SAMPLING_RATE, DEFAULT_DEVICE, 1, pOutputDeviceID, pOutputChannels);
    }

    public ToneEngineJSyn(int pDefaultInstrumentType,
                          int pSamplingRate,
                          int pInputDeviceID,
                          int pInputChannels,
                          int pOutputDeviceID,
                          int pOutputChannels) {
        mSynth = new SynthesisEngine();
        prepareExitHandler();

        mLineOut = new LineOut();
        mSynth.add(mLineOut);
        mLineOut.start();

        mInstruments = new ArrayList<>();
        final float mDefaultAmp = 0.75f;
        for (int i = 0; i < NUMBERS_OF_INSTRUMENTS; i++) {
            final InstrumentJSyn mInstrumentJSyn;
            switch (pDefaultInstrumentType) {
                case INSTRUMENT_EMPTY:
                    mInstrumentJSyn = new InstrumentJSyn(this, i);
                    break;
                case INSTRUMENT_WITH_OSCILLATOR:
                    mInstrumentJSyn = new InstrumentJSynOscillator(this, i);
                    mInstrumentJSyn.amplitude(0.0f);
                    break;
//                case INSTRUMENT_WITH_OSCILLATOR_ADSR:
//                    mInstrumentJSyn = new InstrumentJSynOscillatorADSR(this, i);
//                    break;
                case INSTRUMENT_WITH_OSCILLATOR_ADSR_FILTER_LFO:
                    mInstrumentJSyn = new InstrumentJSynOscillatorADSRFilterLFO(this, i);
                    mInstrumentJSyn.amplitude(mDefaultAmp);
                    break;
                default: /* default implies `INSTRUMENT_WITH_OSCILLATOR_ADSR` */
                    mInstrumentJSyn = new InstrumentJSynOscillatorADSR(this, i);
                    mInstrumentJSyn.amplitude(mDefaultAmp);
            }
            mInstruments.add(mInstrumentJSyn);
        }
        mCurrentInstrumentID = 0;
        mSynth.start(pSamplingRate, pInputDeviceID, pInputChannels, pOutputDeviceID, pOutputChannels);
        mTimer = new Timer();
    }

    public void mute() {
        for (Instrument i : instruments()) {
            i.amplitude(0.0f);
        }
    }

    public SynthesisEngine synth() {
        return mSynth;
    }

    public void add(UnitOscillator pOsc) {
        synth().add(pOsc);
    }

    public LineOut line_out() {
        return mLineOut;
    }

    public void note_on(int note, int velocity) {
        if (USE_AMP_FRACTION) {
            velocity /= NUMBERS_OF_INSTRUMENTS;
        }
        getInstrument(getInstrumentID()).note_on(note, velocity);
    }

    public void note_off(int note) {
        note_off();
    }

    public void note_off() {
        getInstrument(getInstrumentID()).note_off();
    }

    public void control_change(int pCC, int pValue) {
        // not used in jsyn
    }

    public void pitch_bend(int pValue) {
        final float mRange = 110;
        final float mValue = mRange * ((float) (constrain(pValue, 0, 16383) - 8192) / 8192.0f);
        mInstruments.get(getInstrumentID()).pitch_bend(mValue);
    }

    public boolean isPlaying() {
        return mInstruments.get(getInstrumentID()).isPlaying();
    }

    public final Instrument instrument(int pInstrumentID) {
        mCurrentInstrumentID = pInstrumentID;
        return instruments().get(mCurrentInstrumentID);
    }

    public Instrument instrument() {
        return mInstruments.get(mCurrentInstrumentID);
    }

    public ArrayList<? extends Instrument> instruments() {
        return mInstruments;
    }

    public InstrumentJSyn getInstrument(int pInstrumentID) {
        return mInstruments.get(mCurrentInstrumentID);
    }

    public InstrumentJSynOscillatorADSR getInstrumentBasic(int pInstrumentID) {
        if (mInstruments.get(mCurrentInstrumentID) instanceof InstrumentJSynOscillatorADSR) {
            return (InstrumentJSynOscillatorADSR) mInstruments.get(mCurrentInstrumentID);
        } else {
            return null;
        }
    }

    public InstrumentJSynOscillatorADSRFilterLFO getInstrumentFilterLFO(int pInstrumentID) {
        if (mInstruments.get(mCurrentInstrumentID) instanceof InstrumentJSynOscillatorADSRFilterLFO) {
            return (InstrumentJSynOscillatorADSRFilterLFO) mInstruments.get(mCurrentInstrumentID);
        } else {
            return null;
        }
    }

    private void prepareExitHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(mSynth::stop));
//        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//            public void run() {
//                mSynth.stop();
//            }
//        }));
    }

    private int getInstrumentID() {
        return Math.max(mCurrentInstrumentID, 0) % mInstruments.size();
    }
}
