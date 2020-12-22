package wellen.examples.instruments;

import processing.core.PApplet;
import wellen.ADSR;
import wellen.InstrumentInternal;
import wellen.Reverb;
import wellen.SampleDataSNARE;
import wellen.Sampler;
import wellen.Tone;
import wellen.Wavetable;
import wellen.Wellen;

public class ExampleInstruments08CustomDSPInstrument extends PApplet {

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
     *         public float output() {
     *             float mSample = 0.0f;
     *             return mSample;
     *         }
     *     }
     * ```
     *
     * note that the modules of `InstrumentInternal` ( e.g ADSR, VCO, LPF, LFOs, amplitude, frequency ) are still
     * available in the custom instrument but must be explicitly used in `output`. the implementation of the `output`
     * method in `InstrumentInternal` is a good starting point.
     *
     * an in-depth explanation of the behavior of each custom instrument can be found in the source code below as inline
     * comments.
     *
     * use keys `1`, `2`, or `3` to play a custom instrument.
     */

    private static final int INSTRUMENT_DEFAULT = 0;
    private static final int INSTRUMENT_SNARE_DRUM = 1;
    private static final int INSTRUMENT_KICK_DRUM = 2;
    private static final int INSTRUMENT_HIHAT = 3;
    private static final int INSTRUMENT_FAT_LEAD = 4;
    private static final int NUM_OF_INSTRUMENTS = 5;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Tone.replace_instrument(new CustomInstrumentKickDrum(INSTRUMENT_KICK_DRUM));
        Tone.replace_instrument(new CustomInstrumentSampler(INSTRUMENT_SNARE_DRUM));
        Tone.replace_instrument(new CustomInstrumentMultipleOscillators(INSTRUMENT_FAT_LEAD));
        Tone.replace_instrument(new CustomInstrumentNoise(INSTRUMENT_HIHAT));
    }

    public void draw() {
        background(255);
        fill(0);
        final float mTranslate = width / 6.0f;
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
    private static class CustomInstrumentSampler extends InstrumentInternal {

        private final Sampler mSampler;
        private final Reverb mReverb;
        private final float mGain;

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
         * @return returns next sample
         */
        public float output() {
            /* `output()` is called to request a new sample: sampler returns a new sample which is then multiplied by
             the amplitude. the result is processed by reverb and returned. */
            return mReverb.process(mSampler.output() * get_amplitude()) * mGain;
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
    private static class CustomInstrumentMultipleOscillators extends InstrumentInternal {

        private final Wavetable mLowerVCO;
        private final Wavetable mVeryLowVCO;

        public CustomInstrumentMultipleOscillators(int pID) {
            super(pID);

            mLowerVCO = new Wavetable(DEFAULT_WAVETABLE_SIZE);
            mLowerVCO.interpolate_samples(true);
            mVeryLowVCO = new Wavetable(DEFAULT_WAVETABLE_SIZE);
            mVeryLowVCO.interpolate_samples(true);
            Wavetable.fill(mVCO.get_wavetable(), Wellen.WAVESHAPE_TRIANGLE);
            Wavetable.fill(mLowerVCO.get_wavetable(), Wellen.WAVESHAPE_SINE);
            Wavetable.fill(mVeryLowVCO.get_wavetable(), Wellen.WAVESHAPE_SQUARE);
        }

        public float output() {
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
            return mADSRAmp * mSample;
        }
    }

    /**
     * custom DSP instrument that implements a kick drum. it uses a second ADSR envelope to control the frequency of the
     * VCO to create a pitch slide when a note is triggered.
     */
    private static class CustomInstrumentKickDrum extends InstrumentInternal {

        private final ADSR mFrequencyEnvelope;
        private final float mFrequencyRange = 80;
        private final float mDecaySpeed = 0.25f;

        public CustomInstrumentKickDrum(int pID) {
            super(pID);

            set_oscillator_type(Wellen.WAVESHAPE_SINE);
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
            /* make sure to trigger both ADSRs when a note is played */
            mADSR.start();
            mFrequencyEnvelope.start();
        }

    }

    /**
     * custom DSP instrument that implements a hi-hat. it uses the `random(float, float)` method to create noise and
     * shapes it with the built-in ADSR.
     */
    private static class CustomInstrumentNoise extends InstrumentInternal {

        public CustomInstrumentNoise(int pID) {
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

    public static void main(String[] args) {
        PApplet.main(ExampleInstruments08CustomDSPInstrument.class.getName());
    }
}
