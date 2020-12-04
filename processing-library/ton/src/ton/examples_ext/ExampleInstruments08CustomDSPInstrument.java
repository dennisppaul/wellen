package ton.examples_ext;

import processing.core.PApplet;
import ton.InstrumentInternal;
import ton.SampleDataSNARE;
import ton.Sampler;
import ton.Ton;
import ton.Wavetable;

public class ExampleInstruments08CustomDSPInstrument extends PApplet {

    private CustomInstrumentMultipleOscillators mInstrument;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Ton.replace_instrument(new CustomInstrumentSampler(1));
        mInstrument = new CustomInstrumentMultipleOscillators(0);
        Ton.replace_instrument(mInstrument);
        Ton.instrument(0);
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
                Ton.instrument(0);
                break;
            case '1':
                Ton.instrument(1);
                break;
            case '2':
                Ton.instrument(2);
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
            set_amplitude(_velocity_to_amplitude(pVelocity));
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

    public static void main(String[] args) {
        PApplet.main(ExampleInstruments08CustomDSPInstrument.class.getName());
    }
}
