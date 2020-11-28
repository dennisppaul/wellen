package de.hfkbremen.ton;

import processing.core.PApplet;

public class InstrumentSoftware extends Instrument implements DSPNodeOutput {

    public static final int SINE = 0;
    public static final int TRIANGLE = 1;
    public static final int SAWTOOTH = 2;
    public static final int SQUARE = 3;
    public static final int NOISE = 4;
    protected final float DEFAULT_ATTACK = 0.005f;
    protected final float DEFAULT_DECAY = 0.01f;
    protected final float DEFAULT_SUSTAIN = 0.5f;
    protected final float DEFAULT_RELEASE = 0.075f;
    private final int mSamplingRate;
    protected float mAmp;
    protected float mFreq;
    protected boolean mIsPlaying = false;
    protected float mFreqOffset;
    private int mCounter = 0;

    public InstrumentSoftware(int pID, int pSamplingRate) {
        super(pID);
        mSamplingRate = pSamplingRate;
        amplitude(0.0f);
        frequency(220.0f);
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
        mFreqOffset = freq_offset;
        update_freq();
    }

    @Override
    public void amplitude(float pAmp) {
        mAmp = pAmp;
        update_amp();
    }

    @Override
    public float get_amplitude() {
        return mAmp;
    }

    @Override
    public void frequency(float freq) {
        mFreq = freq;
        update_freq();
    }

    @Override
    public float get_frequency() {
        return mFreq;
    }

    @Override
    public void note_off() {
        amplitude(0);
        mIsPlaying = false;
    }

    @Override
    public void note_on(int note, int velocity) {
        frequency(_note_to_frequency(note));
        amplitude(_velocity_to_amplitude(velocity));
        mIsPlaying = true;
    }

    @Override
    public boolean isPlaying() {
        return mIsPlaying;
    }

    @Override
    public float output() {
        // @TODO(replace with wavetable)
        mCounter++;
        return mAmp * PApplet.sin(2 * PApplet.PI * mFreq * mCounter / (float)mSamplingRate);
    }

    protected void update_freq() {
    }

    protected void update_amp() {
    }
}