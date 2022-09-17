package wellen.examples.instruments;

import processing.core.PApplet;
import wellen.*;

public class ExampleInstruments09CustomDSPInstrument extends PApplet {

    /*
     * this example demonstrates how to implement custom instruments by extending default internal instruments.
     *
     * a custom instrument is created by extending the class `InstrumentInternal`. usually the `output()` method, which
     * is called by the underlying tone engine whenever new sample data is needed, is overridden to implement custom
     * instrument behavior.
     *
     * the code below shows a very basic implementation of a custom instrument ( without audible output ):
     *
     * ```
     *     class CustomInstrument extends InstrumentInternal {
     *
     *         public CustomInstrument(int pID) {
     *             super(pID);
     *         }
     *
     *         public void output(Signal pSignal) {
     *             pSignal[0] = 0.0f;
     *         }
     *     }
     * ```
     *
     * note that the modules of `InstrumentInternal` ( e.g ADSR, VCO, LPF, LFOs, amplitude, frequency ) are still
     * available in the custom instrument but must be explicitly used in `output`. the implementation of the `output`
     * method in `InstrumentInternal` is a good starting point for understanding how these modules work together.
     *
     * an in-depth explanation of the behavior of each custom instrument can be found in the source code below as inline
     * comments.
     *
     * alternatively, instruments from the collection `InstrumentInternalLibrary` can be used.
     *
     * use keys `1` – `6` to play a custom instrument.
     */

    private static final int INSTRUMENT_DEFAULT = 0;
    private static final int INSTRUMENT_SNARE_DRUM = 1;
    private static final int INSTRUMENT_KICK_DRUM = 2;
    private static final int INSTRUMENT_HIHAT = 3;
    private static final int INSTRUMENT_FAT_LEAD = 4;
    private static final int INSTRUMENT_DETUNED = 5;
    private static final int INSTRUMENT_BELL = 6;
    private static final int NUM_OF_INSTRUMENTS = 7;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Tone.replace_instrument(new CustomInstrumentSampler(INSTRUMENT_SNARE_DRUM));
        Tone.replace_instrument(new CustomInstrumentKickDrum(INSTRUMENT_KICK_DRUM));
        Tone.replace_instrument(new CustomInstrumentNoise(INSTRUMENT_HIHAT));
        Tone.replace_instrument(new CustomInstrumentMultipleOscillators(INSTRUMENT_FAT_LEAD));
        Tone.replace_instrument(new CustomInstrumentDetunedOscillatorsStereo(INSTRUMENT_DETUNED));
        /* instrument from the collection `InstrumentInternalLibrary` */
        Tone.replace_instrument(new InstrumentDSPLibrary.BELL(INSTRUMENT_BELL));
    }

    public void draw() {
        background(255);
        fill(0);
        final float mTranslate = width / (NUM_OF_INSTRUMENTS + 1.0f);
        for (int i = 0; i < NUM_OF_INSTRUMENTS; i++) {
            Tone.instrument(i);
            translate(mTranslate, 0);
            ellipse(0, height * 0.5f, Tone.is_playing() ? 100 : 5, Tone.is_playing() ? 100 : 5);
        }
    }

    public void keyPressed() {
        int mNote = 45 + (int) random(0, 12);
        switch (key) {
            case '1':
                Tone.instrument(INSTRUMENT_DEFAULT);
                Tone.note_on(mNote, 100);
                break;
            case '2':
                Tone.instrument(INSTRUMENT_SNARE_DRUM);
                Tone.note_on(mNote, 100);
                break;
            case '3':
                Tone.instrument(INSTRUMENT_KICK_DRUM);
                Tone.note_on(mNote, 100);
                break;
            case '4':
                Tone.instrument(INSTRUMENT_HIHAT);
                Tone.note_on(mNote, 25);
                break;
            case '5':
                Tone.instrument(INSTRUMENT_FAT_LEAD);
                Tone.note_on(mNote, 100);
                break;
            case '6':
                Tone.instrument(INSTRUMENT_DETUNED);
                Tone.note_on(mNote, 100);
                break;
            case '7':
                Tone.instrument(INSTRUMENT_BELL);
                Tone.note_on(mNote - 12, 100);
                break;
        }
    }

    public void keyReleased() {
        for (int i = 0; i < NUM_OF_INSTRUMENTS; i++) {
            Tone.instrument(i).note_off();
        }
    }

    /**
     * custom DSP instrument that implements a snare drum by playing a pre-recorded sample.
     */
    private static class CustomInstrumentSampler extends InstrumentDSP {

        private final float mGain;
        private final Reverb mReverb;
        private final Sampler mSampler;

        /**
         * the constructor of the custom instrument must call the *super constructor* passing the instrument’s ID with
         * `super(int)`.
         *
         * @param pID instrument ID
         */
        public CustomInstrumentSampler(int pID) {
            super(pID); /* call super constructor with instrument ID */

            mSampler = new Sampler();
            mSampler.load(SampleDataSNARE.data);
            mSampler.loop(false);

            mReverb = new Reverb();

            mGain = 2.0f;
        }

        /**
         * called by tone engine to request the next audio sample of the instrument.
         *
         * @return
         */
        public Signal output_signal() {
            /* `output(Signal)` is called to request a new sample: sampler returns a new sample which is then
            multiplied by the amplitude. the result is processed by reverb and returned. the final sample is stored
            in the supplied signal container. */
            return Signal.create(mReverb.process(mSampler.output() * get_amplitude()) * mGain);
        }

        /**
         * override this method to change the `note_off` behavior. in this case the interaction with the ADSR envelope
         * is removed.
         */
        public void note_off() {
            mIsPlaying = false;
        }

        /**
         * override this method to change the `note_on` behavior. in this case the sampler is rewound, the ADSR is
         * ignored, the velocity is interpreted, and the note value is ignored ( although it could be interpreted as
         * sample playback speed ).
         *
         * @param pNote     ignored in this instrument
         * @param pVelocity specifies the volume of the sampler with a value range [0, 127]
         */
        public void note_on(int pNote, int pVelocity) {
            mIsPlaying = true;
            /* use `velocity_to_amplitude(float)` to convert velocities with a value range [0, 127] to amplitude with
             a value range [0.0, 0.1] */
            set_amplitude(velocity_to_amplitude(pVelocity));
            mSampler.rewind();
        }
    }

    /**
     * custom DSP instrument that combines 3 oscillators to create a more complex sound.
     */
    private static class CustomInstrumentMultipleOscillators extends InstrumentDSP {

        private final Wavetable mLowerVCO;
        private final Wavetable mVeryLowVCO;

        public CustomInstrumentMultipleOscillators(int pID) {
            super(pID);

            mLowerVCO = new Wavetable(DEFAULT_WAVETABLE_SIZE);
            mLowerVCO.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
            mVeryLowVCO = new Wavetable(DEFAULT_WAVETABLE_SIZE);
            mVeryLowVCO.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
            Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVEFORM_TRIANGLE);
            Wavetable.fill(mLowerVCO.get_wavetable(), Wellen.WAVEFORM_SINE);
            Wavetable.fill(mVeryLowVCO.get_wavetable(), Wellen.WAVEFORM_SQUARE);
        }

        public Signal output_signal() {
            /* this custom instrument ignores LFOs and LPF */
            mVCO.set_frequency(get_frequency());
            mVCO.set_amplitude(get_amplitude() * 0.2f);
            mLowerVCO.set_frequency(get_frequency() * 0.5f);
            mLowerVCO.set_amplitude(get_amplitude());
            mVeryLowVCO.set_frequency(get_frequency() * 0.25f);
            mVeryLowVCO.set_amplitude(get_amplitude() * 0.075f);

            /* use inherited ADSR envelope to control the amplitude */
            final float mADSRAmp = mADSR.output();
            /* multiple samples are combined by a simple addition */
            float mSample = mVCO.output();
            mSample += mLowerVCO.output();
            mSample += mVeryLowVCO.output();

            return Signal.create(mADSRAmp * mSample);
        }
    }

    /**
     * custom DSP instrument that implements a kick drum. it uses a second ADSR envelope to control the frequency of the
     * VCO to create a pitch slide when a note is triggered.
     */
    private static class CustomInstrumentKickDrum extends InstrumentDSP {

        private final float mDecaySpeed = 0.25f;
        private final ADSR mFrequencyEnvelope;
        private final float mFrequencyRange = 80;

        public CustomInstrumentKickDrum(int pID) {
            super(pID);

            set_oscillator_type(Wellen.WAVEFORM_SINE);
            set_amplitude(0.5f);
            set_frequency(90);

            /* this ADSR envelope is used to control the frequency instead of amplitude */
            mFrequencyEnvelope = new ADSR();

            /* the *built-in* ADSR is still available and used to control the amplitude, as usual. */
            mADSR.set_attack(0.001f);
            mADSR.set_decay(mDecaySpeed);
            mADSR.set_sustain(0.0f);
            mADSR.set_release(0.0f);

            mFrequencyEnvelope.set_attack(0.001f);
            mFrequencyEnvelope.set_decay(mDecaySpeed);
            mFrequencyEnvelope.set_sustain(0.0f);
            mFrequencyEnvelope.set_release(0.0f);
        }

        public Signal output_signal() {
            final float mFrequencyOffset = mFrequencyEnvelope.output() * mFrequencyRange;
            mVCO.set_frequency(get_frequency() + mFrequencyOffset);
            mVCO.set_amplitude(get_amplitude());

            final float mSample = mVCO.output();
            final float mADSRAmp = mADSR.output();

            return Signal.create(mSample * mADSRAmp);
        }

        public void note_off() {
            mIsPlaying = false;
        }

        public void note_on(int pNote, int pVelocity) {
            mIsPlaying = true;
            /* make sure to trigger both ADSRs when a note is played */
            mADSR.start();
            mFrequencyEnvelope.start();
        }
    }

    /**
     * custom DSP instrument that implements a hi-hat. it uses the `random(float, float)` method to create noise and
     * shapes it with the built-in ADSR.
     */
    private static class CustomInstrumentNoise extends InstrumentDSP {

        public CustomInstrumentNoise(int pID) {
            super(pID);
            mADSR.set_attack(0.005f);
            mADSR.set_decay(0.05f);
            mADSR.set_sustain(0.0f);
            mADSR.set_release(0.0f);
        }

        public Signal output_signal() {
            return Signal.create(Wellen.random(-get_amplitude(), get_amplitude()) * mADSR.output());
        }
    }

    /**
     * custom instrument that produces a stereo signal from 2 slightly detunes oscillators.
     */
    private static class CustomInstrumentDetunedOscillatorsStereo extends InstrumentDSP {

        /**
         * detunes the oscillators in percentage of the main frequency. one oscillator is detuned below the main
         * frequency and the other above.
         * <p>
         * e.g `detune = 0.01, main_frequency = 220.0`
         * <p>
         * > `osc_a_frequency = main_frequency * ( 1.0 - detune) = 217.8`
         * <p>
         * > `osc_a_frequency = main_frequency * ( 1.0 + detune) = 222.2`
         */
        private float mDetune;
        /**
         * spreads the oscillators over left and right channel: `0.0` no spread, `1.0` fully spread over both channels
         */
        private float mSpread;
        private final Wavetable mVCOSecond;

        public CustomInstrumentDetunedOscillatorsStereo(int pID) {
            super(pID);
            set_channels(2);
            set_detune(0.01f);
            set_spread(0.5f);

            Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVEFORM_TRIANGLE);

            mVCOSecond = new Wavetable(DEFAULT_WAVETABLE_SIZE);
            mVCOSecond.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
            Wavetable.fill(mVCOSecond.get_wavetable(), Wellen.WAVEFORM_TRIANGLE);
        }

        public float get_detune() {
            return mDetune;
        }

        public void set_detune(float pDetune) {
            mDetune = pDetune;
        }

        public float get_spread() {
            return mSpread;
        }

        public void set_spread(float pSpread) {
            mSpread = pSpread;
        }

        public Signal output_signal() {
            /* this custom instrument ignores LFOs and LPF */
            mVCO.set_frequency(get_frequency() * (1.0f - mDetune));
            mVCO.set_amplitude(get_amplitude());
            mVCOSecond.set_frequency(get_frequency() * (1.0f + mDetune));
            mVCOSecond.set_amplitude(get_amplitude());

            /* use inherited ADSR envelope to control the amplitude */
            final float mADSRAmp = mADSR.output();

            /* spread signal across stereo channels */
            final float mSignalA = mVCO.output();
            final float mSignalB = mVCOSecond.output();
            final float mMix = mSpread * 0.5f + 0.5f;
            final float mInvMix = 1.0f - mMix;
            final Signal pSignal = new Signal();
            pSignal.left(mSignalA * mMix + mSignalB * mInvMix);
            pSignal.right(mSignalA * mInvMix + mSignalB * mMix);
            pSignal.left_mult(mADSRAmp);
            pSignal.right_mult(mADSRAmp);
            return pSignal;
        }
    }

    public static void main(String[] args) {
        PApplet.main(ExampleInstruments09CustomDSPInstrument.class.getName());
    }
}
