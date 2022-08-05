package wellen.examples.technique;

import processing.core.PApplet;
import wellen.Beat;
import wellen.DSP;
import wellen.DSPComposition;
import wellen.DSPTrack;
import wellen.Oscillator;
import wellen.OscillatorFunction;
import wellen.Signal;
import wellen.Wellen;

public class TechniqueBasics04TracksAndComposition extends PApplet {

    /*
     * this example demonstrates how to build a composition with tracks.
     */

    private final DSPComposition mComposition = new DSPComposition();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mComposition.tracks().add(new Track_0());
        mComposition.tracks().add(new Track_1());
        Beat.start(this, 120 * 4);
        DSP.start(this, 2);
    }

    public void draw() {
        background(255);
        DSP.draw_buffer(g, width, height);
    }

    public void mouseMoved() {
        mComposition.track(1).volume = map(mouseX, 0, width, 0, 0.5f);
        mComposition.track(0).volume = map(mouseY, 0, height, 0, 0.5f);
    }

    public void beat(int pBeat) {
        mComposition.beat(pBeat % 128); /* loop after 128 beats */
    }

    public void audioblock(float[] pOutputSignalLeft, float[] pOutputSignalRight) {
        for (int i = 0; i < pOutputSignalLeft.length; i++) {
            Signal s = mComposition.output_signal();
            pOutputSignalLeft[i] = s.left();
            pOutputSignalRight[i] = s.right();
        }
    }

    private static class Track_0 extends DSPTrack {
        private final Oscillator mOSC = new OscillatorFunction();
        private final Oscillator mLFO = new OscillatorFunction();

        public Track_0() {
            mOSC.set_amplitude(0.25f);
            mOSC.set_waveform(Wellen.WAVESHAPE_TRIANGLE);

            mLFO.set_frequency(8);
            mLFO.set_oscillation_range(110 - 2, 110 + 2);

            set_in_outpoint(0, 63);
        }

        public Signal output_signal() {
            mOSC.set_frequency(mLFO.output());
            return Signal.create(mOSC.output());
        }
    }

    private static class Track_1 extends DSPTrack {
        private final Oscillator mOSC = new OscillatorFunction();
        private final float mMaxAmplitude = 0.5f;

        public Track_1() {
            mOSC.set_frequency(225);
            mOSC.set_amplitude(0.0f);
        }

        public Signal output_signal() {
            return Signal.create(mOSC.output());
        }

        public void beat(int pBeat) {
            if ((pBeat / 16) % 4 < 3) {
                final int mPhase = 16;
                float mAmp = (pBeat % mPhase) / (mPhase * 0.5f);
                mAmp -= 1.0f;
                mAmp = abs(mAmp);
                mAmp = 1.0f - mAmp;
                mAmp *= mMaxAmplitude;
                mOSC.set_amplitude(mAmp);
            } else {
                mOSC.set_amplitude(0.0f);
            }
        }
    }

    public static void main(String[] args) {
        PApplet.main(TechniqueBasics04TracksAndComposition.class.getName());
    }
}

