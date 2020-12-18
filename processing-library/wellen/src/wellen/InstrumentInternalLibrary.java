package wellen;

/**
 * a collection of DSP instruments. these instruments can be used to replace the default instrument ( i.e
 * `InstrumentInternal` ) e.g by using the method `Tone.replace_instrument(InstrumentInternal)`.
 * <p>
 * all instruments are extended from `InstrumentInternal`, however some the original functionality is extended, changed
 * or even removed.
 * <p>
 * note that these instruments only work with the internal tone engine. however, due to the modular nature of the
 * library these instruments can also be integrated into `DSP` applications.
 */
public class InstrumentInternalLibrary {
    public static class SAMPLER extends InstrumentInternal {

        private final Sampler mSampler;

        public SAMPLER(int pID, float[] pSampleData) {
            super(pID);

            mSampler = new Sampler();
            mSampler.set_data(pSampleData);
            mSampler.loop(false);
        }

        public float output() {
            return mSampler.output() * get_amplitude();
        }


        public void note_off() {
            mIsPlaying = false;
        }

        public void note_on(int pNote, int pVelocity) {
            mIsPlaying = true;
            set_amplitude(velocity_to_amplitude(pVelocity));
            mSampler.rewind();
        }
    }

    public static class FAT_LEAD extends InstrumentInternal {

        private final Wavetable mLowerVCO;
        private final Wavetable mVeryLowVCO;

        public FAT_LEAD(int pID) {
            super(pID);

            mLowerVCO = new Wavetable(DEFAULT_WAVETABLE_SIZE);
            mLowerVCO.interpolate_samples(true);
            mVeryLowVCO = new Wavetable(DEFAULT_WAVETABLE_SIZE);
            mVeryLowVCO.interpolate_samples(true);
            Wavetable.fill(mVCO.get_wavetable(), Wellen.OSC_TRIANGLE);
            Wavetable.fill(mLowerVCO.get_wavetable(), Wellen.OSC_SINE);
            Wavetable.fill(mVeryLowVCO.get_wavetable(), Wellen.OSC_SQUARE);
        }

        public float output() {
            mVCO.set_frequency(get_frequency());
            mVCO.set_amplitude(get_amplitude() * 0.2f);
            mLowerVCO.set_frequency(get_frequency() * 0.5f);
            mLowerVCO.set_amplitude(get_amplitude());
            mVeryLowVCO.set_frequency(get_frequency() * 0.25f);
            mVeryLowVCO.set_amplitude(get_amplitude() * 0.075f);

            final float mADSRAmp = mADSR.output();
            float mSample = mVCO.output();
            mSample += mLowerVCO.output();
            mSample += mVeryLowVCO.output();
            return mADSRAmp * mSample;
        }
    }

    public static class KICK_DRUM extends InstrumentInternal {

        private final ADSR mFrequencyEnvelope;
        private final float mFrequencyRange = 80;
        private final float mDecaySpeed = 0.25f;

        public KICK_DRUM(int pID) {
            super(pID);

            set_oscillator_type(Wellen.OSC_SINE);
            set_amplitude(0.5f);
            set_frequency(90);

            mFrequencyEnvelope = new ADSR();

            mADSR.set_attack(0.001f);
            mADSR.set_decay(mDecaySpeed);
            mADSR.set_sustain(0.0f);
            mADSR.set_release(0.0f);

            mFrequencyEnvelope.set_attack(0.001f);
            mFrequencyEnvelope.set_decay(mDecaySpeed);
            mFrequencyEnvelope.set_sustain(0.0f);
            mFrequencyEnvelope.set_release(0.0f);
        }

        public float output() {
            final float mFrequencyOffset = mFrequencyEnvelope.output() * mFrequencyRange;
            mVCO.set_frequency(get_frequency() + mFrequencyOffset);
            mVCO.set_amplitude(get_amplitude());

            float mSample = mVCO.output();
            final float mADSRAmp = mADSR.output();
            return mSample * mADSRAmp;
        }

        public void note_off() {
            mIsPlaying = false;
        }

        public void note_on(int pNote, int pVelocity) {
            mIsPlaying = true;
            mADSR.start();
            mFrequencyEnvelope.start();
        }

    }

    public static class HI_HAT extends InstrumentInternal {

        public HI_HAT(int pID) {
            super(pID);
            mADSR.set_attack(0.005f);
            mADSR.set_decay(0.05f);
            mADSR.set_sustain(0.0f);
            mADSR.set_release(0.0f);
        }

        public float output() {
            return Wellen.random(-get_amplitude(), get_amplitude()) * mADSR.output();
        }
    }
}
