package de.hfkbremen.ton;

import controlP5.ControlElement;
import ddf.minim.AudioOutput;
import ddf.minim.Minim;
import ddf.minim.ugens.ADSR;
import ddf.minim.ugens.Oscil;
import ddf.minim.ugens.Waves;

public class InstrumentMinim extends Instrument {

    private final Minim minim;
    private final AudioOutput out;
    private final Oscil mOsc;
    private final ADSR adsr;
    private float mAmp = 0.9f;
    private float mFreq = 220.0f;
    private boolean mDumpWarningLFO = true;
    private boolean mDumpWarningFILTER = true;

    public InstrumentMinim(Minim pMinim, int pName) {
        super(pName);
        minim = pMinim;
        out = minim.getLineOut(Minim.MONO);

        mOsc = new Oscil(mFreq, mAmp, Waves.SINE);
        adsr = new ADSR(1.0f, mAttack, mDecay, mSustain, mRelease, 0.0f, 0.0f);

        mOsc.patch(adsr);
        adsr.patch(out);
    }

    /* --- */
    @Override
    @ControlElement(properties = {"min=0.0", "max=10.0", "type=knob", "radius=20", "resolution=1000"}, x = 0, y = 0)
    public void attack(float pAttack) {
        super.attack(pAttack);
    }

    @Override
    @ControlElement(properties = {"min=0.0", "max=10.0", "type=knob", "radius=20", "resolution=1000"}, x = 50, y = 0)
    public void decay(float pDecay) {
        super.decay(pDecay);
    }

    @Override
    @ControlElement(properties = {"min=0.0", "max=1.0", "type=knob", "radius=20", "resolution=100"}, x = 100, y = 0)
    public void sustain(float pSustain) {
        super.sustain(pSustain);
    }

    @Override
    @ControlElement(properties = {"min=0.0", "max=10.0", "type=knob", "radius=20", "resolution=1000"}, x = 150, y = 0)
    public void release(float pRelease) {
        super.release(pRelease);
    }

    @ControlElement(properties = {"min=0.0",
                                  "max=" + (NUMBER_OF_OSCILLATORS - 1),
                                  "type=knob",
                                  "radius=20",
                                  "resolution=" + (NUMBER_OF_OSCILLATORS - 1)}, x = 200, y = 0)
    @Override
    public void osc_type(int pOsc) {
        switch (pOsc) {
            case SINE:
                mOsc.setWaveform(Waves.SINE);
                break;
            case TRIANGLE:
                mOsc.setWaveform(Waves.TRIANGLE);
                break;
            case SAWTOOTH:
                mOsc.setWaveform(Waves.SAW);
                break;
            case SQUARE:
                mOsc.setWaveform(Waves.SQUARE);
                break;
            case NOISE:
                mOsc.setWaveform(Waves.randomNoise());
                break;
        }
    }

    @Override
    public int get_osc_type() {
        int mOscID;
        if (mOsc.getWaveform() == Waves.SINE) {
            mOscID = SINE;
        } else if (mOsc.getWaveform() == Waves.TRIANGLE) {
            mOscID = TRIANGLE;
        } else if (mOsc.getWaveform() == Waves.SAW) {
            mOscID = SAWTOOTH;
        } else if (mOsc.getWaveform() == Waves.SQUARE) {
            mOscID = SQUARE;
        } else {
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

    @Override
    public void pitch_bend(float freq_offset) {

    }

    @Override
    public void amplitude(float pAmp) {
        mAmp = pAmp;
        mOsc.setAmplitude(mAmp);
    }

    @Override
    public float get_amplitude() {
        return mAmp;
    }

    @Override
    public void frequency(float pFreq) {
        mFreq = pFreq;
        mOsc.setFrequency(mFreq);
    }

    @Override
    public float get_frequency() {
        return mFreq;
    }

    @Override
    public void noteOff() {
        adsr.noteOff();
    }

    @Override
    public void noteOn(float pFrequency, float pAmplitude) {
        frequency(pFrequency);
        amplitude(pAmplitude);
        setADSR();
        adsr.noteOn();
    }

    private void setADSR() {
        adsr.setParameters(1.0f, mAttack, mDecay, mSustain, mRelease <= 0 ? 0.001f : mRelease, 0.0f, 0.0f);
    }
}
