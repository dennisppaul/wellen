package ton.examples_ext;

import processing.core.PApplet;
import ton.ADSR;
import ton.InstrumentInternal;
import ton.SampleDataSNARE;
import ton.Sampler;
import ton.Ton;
import ton.Wavetable;

/**
 * this example demonstrates how to implement custom instruments by extending default internal instruments. an in-depth
 * explanation of the behavior of the individual instruments can be found inline.
 */
public class ExampleInstruments08CustomDSPInstrument extends PApplet {

    private static final int INSTRUMENT_DEFAULT = 0;
    private static final int INSTRUMENT_SAMPLER = 1;
    private static final int INSTRUMENT_KICK_DRUM = 2;
    private static final int INSTRUMENT_MULTIPLE_OSCILLATOR = 3;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Ton.replace_instrument(new CustomInstrumentKickDrum(INSTRUMENT_KICK_DRUM));
        Ton.replace_instrument(new CustomInstrumentSampler(INSTRUMENT_SAMPLER));
        Ton.replace_instrument(new CustomInstrumentMultipleOscillators(INSTRUMENT_MULTIPLE_OSCILLATOR));
    }

    public void draw() {
        background(255);
        fill(0);
        ellipse(width * 0.5f, height * 0.5f, Ton.is_playing() ? 100 : 5, Ton.is_playing() ? 100 : 5);
    }

    public void keyPressed() {
        int mNote = 45 + (int) random(0, 12);
        switch (key) {
            case '0':
                Ton.instrument(INSTRUMENT_DEFAULT);
                break;
            case '1':
                Ton.instrument(INSTRUMENT_SAMPLER);
                break;
            case '2':
                Ton.instrument(INSTRUMENT_KICK_DRUM);
                break;
            case '3':
                Ton.instrument(INSTRUMENT_MULTIPLE_OSCILLATOR);
                break;
        }
        Ton.note_on(mNote, 100);
    }

    public void keyReleased() {
        Ton.note_off();
    }

    private static class CustomInstrumentSampler extends InstrumentInternal {

        private final Sampler mSampler;

        public CustomInstrumentSampler(int pID) {
            super(pID);
            mSampler = new Sampler();
            mSampler.load(SampleDataSNARE.data);
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

    private static class CustomInstrumentMultipleOscillators extends InstrumentInternal {

        private final Wavetable mLowerVCO;
        private final Wavetable mVeryLowVCO;

        public CustomInstrumentMultipleOscillators(int pID) {
            super(pID);
            mLowerVCO = new Wavetable(DEFAULT_WAVETABLE_SIZE);
            mLowerVCO.interpolate_samples(true);
            mVeryLowVCO = new Wavetable(DEFAULT_WAVETABLE_SIZE);
            mVeryLowVCO.interpolate_samples(true);
            Wavetable.fill(mVCO.wavetable(), Ton.OSC_TRIANGLE);
            Wavetable.fill(mLowerVCO.wavetable(), Ton.OSC_SINE);
            Wavetable.fill(mVeryLowVCO.wavetable(), Ton.OSC_SQUARE);
        }

        public float output() {
            /* this custom instrumetn ignores LFOs and LPF for now */
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

    private static class CustomInstrumentKickDrum extends InstrumentInternal {

        private final ADSR mFrequencyEnvelope;
        private final float mFrequencyRange = 80;
        private final float mDecaySpeed = 0.25f;

        public CustomInstrumentKickDrum(int pID) {
            super(pID);

            set_oscillator_type(Ton.OSC_SINE);
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

        public static void main(String[] args) {
            PApplet.main(ExampleInstruments08CustomDSPInstrument.class.getName());
        }
    }
}
