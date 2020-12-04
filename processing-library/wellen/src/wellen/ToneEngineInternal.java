package wellen;

import java.util.ArrayList;

import static processing.core.PApplet.constrain;

public class ToneEngineInternal extends ToneEngine implements AudioBufferRenderer {

    public boolean USE_AMP_FRACTION = false;
    private final ArrayList<InstrumentInternal> mInstruments;
    private final AudioBufferManager mAudioPlayer;
    private int mCurrentInstrumentID;
    private AudioOutputCallback mAudioblockCallback = null;

    public ToneEngineInternal(int pSamplingRate,
            int pOutputDeviceID,
            int pOutputChannels) {
        mInstruments = new ArrayList<>();
        for (int i = 0; i < NUMBERS_OF_INSTRUMENTS; i++) {
            final InstrumentInternal mInstrument = new InstrumentInternal(i, pSamplingRate);
            mInstruments.add(mInstrument);
        }

        if (pOutputChannels > 0) {
            mAudioPlayer = new AudioBufferManager(this,
                    Wellen.DEFAULT_SAMPLING_RATE,
                    Wellen.DEFAULT_AUDIOBLOCK_SIZE,
                    pOutputDeviceID,
                    pOutputChannels,
                    0,
                    0);
        } else {
            mAudioPlayer = null;
        }
    }

    public ToneEngineInternal() {
        this(Wellen.DEFAULT_SAMPLING_RATE, Wellen.DEFAULT_AUDIO_DEVICE, 2);
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
    public void control_change(int pCC, int pValue) {
    }

    @Override
    public void pitch_bend(int pValue) {
        final float mRange = 110;
        final float mValue = mRange * ((float) (constrain(pValue, 0, 16383) - 8192) / 8192.0f);
        mInstruments.get(getInstrumentID()).pitch_bend(mValue);
    }

    @Override
    public boolean is_playing() {
        return mInstruments.get(getInstrumentID()).is_playing();
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
        if (pInstrument instanceof InstrumentInternal) {
            mInstruments.set(pInstrument.ID(), (InstrumentInternal) pInstrument);
        } else {
            System.err.println("+++ WARNING @" + getClass().getSimpleName() +
                    ".replace_instrument(Instrument) / instrument must be" +
                    " of type `InstrumentInternal`");
        }
    }

    @Override
    public void audioblock(float[][] pOutputSamples, float[][] pInputSamples) {
        if (pOutputSamples.length == 1) {
            audioblock(pOutputSamples[0]);
        } else if (pOutputSamples.length == 2) {
            audioblock(pOutputSamples[0], pOutputSamples[1]);
        } else {
            System.err.println("+++ WARNING @" + getClass().getSimpleName() +
                    ".audioblock / multiple output channels are " +
                    "not supported.");
        }
        if (mAudioblockCallback != null) {
            mAudioblockCallback.audioblock(pOutputSamples);
        }
    }

    public void audioblock(float[] pSamplesLeft, float[] pSamplesRight) {
        for (int i = 0; i < pSamplesLeft.length; i++) {
            float mSampleL = 0;
            float mSampleR = 0;
            for (InstrumentInternal mInstrument : mInstruments) {
                final float mSample = mInstrument.output();
                final float mPan = mInstrument.get_pan() * 0.5f + 0.5f;
                mSampleR += mSample * mPan;
                mSampleL += mSample * (1.0f - mPan);
            }
            pSamplesLeft[i] = mSampleL;
            pSamplesRight[i] = mSampleR;
        }
    }

    public void audioblock(float[] pSamples) {
        for (int i = 0; i < pSamples.length; i++) {
            float mSample = 0;
            for (InstrumentInternal mInstrument : mInstruments) {
                mSample += mInstrument.output();
            }
            pSamples[i] = mSample;
        }
    }

    public void register_audioblock_callback(AudioOutputCallback pAudioblockCallback) {
        mAudioblockCallback = pAudioblockCallback;
    }

    private int getInstrumentID() {
        return Math.max(mCurrentInstrumentID, 0) % mInstruments.size();
    }

    public static ToneEngineInternal no_output() {
        return new ToneEngineInternal(Wellen.DEFAULT_SAMPLING_RATE, Wellen.DEFAULT_AUDIO_DEVICE, Wellen.NO_CHANNELS);
    }

    public interface AudioOutputCallback {

        void audioblock(float[][] pOutputSamples);
    }
}
