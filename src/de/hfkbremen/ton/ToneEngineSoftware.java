package de.hfkbremen.ton;

import java.util.ArrayList;

import static de.hfkbremen.ton.Ton.DEFAULT_AUDIOBLOCK_SIZE;
import static de.hfkbremen.ton.Ton.DEFAULT_SAMPLING_RATE;
import static processing.core.PApplet.constrain;

public class ToneEngineSoftware extends ToneEngine implements AudioBufferRenderer {

    public static final int DEFAULT_DEVICE = -1;
    public boolean USE_AMP_FRACTION = false;
    private final ArrayList<InstrumentSoftware> mInstruments;
    private final AudioBufferManager mAudioPlayer;
    private int mCurrentInstrumentID;
    private AudioOutputCallback mAudioblockCallback = null;

    public ToneEngineSoftware(int pSamplingRate,
                              int pOutputDeviceID,
                              int pOutputChannels) {
        mInstruments = new ArrayList<>();
        for (int i = 0; i < NUMBERS_OF_INSTRUMENTS; i++) {
            final InstrumentSoftware mInstrument = new InstrumentSoftware(i, pSamplingRate);
            mInstruments.add(mInstrument);
        }

        mAudioPlayer = new AudioBufferManager(this,
                                              DEFAULT_SAMPLING_RATE,
                                              DEFAULT_AUDIOBLOCK_SIZE,
                                              pOutputDeviceID,
                                              pOutputChannels,
                                              0,
                                              0);
    }

    public ToneEngineSoftware() {
        this(DEFAULT_SAMPLING_RATE, DEFAULT_DEVICE, 2);
    }

    @Override
    public void note_on(int note, int velocity) {
        if (USE_AMP_FRACTION) {
            velocity /= NUMBERS_OF_INSTRUMENTS;
        }
        mInstruments.get(getInstrumentID()).note_on(note, velocity);
    }

    @Override
    public void note_off(int note) {
        note_off();
    }

    @Override
    public void note_off() {
        mInstruments.get(getInstrumentID()).note_off();
    }

    @Override
    public void control_change(int pCC, int pValue) { }

    @Override
    public void pitch_bend(int pValue) {
        final float mRange = 110;
        final float mValue = mRange * ((float) (constrain(pValue, 0, 16383) - 8192) / 8192.0f);
        mInstruments.get(getInstrumentID()).pitch_bend(mValue);
    }

    @Override
    public boolean isPlaying() {
        return mInstruments.get(getInstrumentID()).isPlaying();
    }

    @Override
    public Instrument instrument(int pInstrumentID) {
        mCurrentInstrumentID = pInstrumentID;
        return instrument();
    }

    @Override
    public Instrument instrument() {
        return instruments().get(mCurrentInstrumentID);
    }

    @Override
    public ArrayList<? extends Instrument> instruments() {
        return mInstruments;
    }

    @Override
    public void replace_instrument(Instrument pInstrument) {
        if (pInstrument instanceof InstrumentSoftware) {
            mInstruments.set(pInstrument.ID(), (InstrumentSoftware) pInstrument);
        } else {
            System.err.println("+++ @" + this.getClass()
                                             .getSimpleName() + ".replace_instrument(Instrument) / instrument must be" +
                               " of type `InstrumentSoftware`");
        }
    }

    @Override
    public void audioblock(float[][] pOutputSamples, float[][] pInputSamples) {
        // @TODO(implement multi channels)
        for (int i = 0; i < pOutputSamples[0].length; i++) {
            float mSample = 0;
            for (InstrumentSoftware mInstrument : mInstruments) {
                mSample += mInstrument.output();
            }
            pOutputSamples[0][i] = mSample;
            pOutputSamples[1][i] = mSample;
        }
        if (mAudioblockCallback != null) { mAudioblockCallback.audioblock(pOutputSamples); }
    }

    public void register_audioblock_callback(AudioOutputCallback pAudioblockCallback) {
        mAudioblockCallback = pAudioblockCallback;
    }

    private int getInstrumentID() {
        return Math.max(mCurrentInstrumentID, 0) % mInstruments.size();
    }

    public interface AudioOutputCallback {

        void audioblock(float[][] pOutputSamples);
    }
}
