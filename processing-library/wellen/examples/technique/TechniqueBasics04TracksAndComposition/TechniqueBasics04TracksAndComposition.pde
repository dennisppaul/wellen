import wellen.*; 

/*
 * this example demonstrates how to build a composition with tracks.
 */

final DSPComposition mComposition = new DSPComposition();

void settings() {
    size(640, 480);
}

void setup() {
    mComposition.tracks().add(new Track_0());
    mComposition.tracks().add(new Track_1());
    Beat.start(this, 120 * 4);
    DSP.start(this, 2);
}

void draw() {
    background(255);
    DSP.draw_buffer(g, width, height);
}

void mouseMoved() {
    mComposition.track(1).volume = map(mouseX, 0, width, 0, 0.5f);
    mComposition.track(0).volume = map(mouseY, 0, height, 0, 0.5f);
}

void beat(int pBeat) {
    mComposition.beat(pBeat % 128); /* loop after 128 beats */
}

void audioblock(float[] pOutputSignalLeft, float[] pOutputSignalRight) {
    for (int i = 0; i < pOutputSignalLeft.length; i++) {
        Signal s = mComposition.output_signal();
        pOutputSignalLeft[i] = s.left();
        pOutputSignalRight[i] = s.right();
    }
}

static class Track_0 extends DSPTrack {
    
final Oscillator mOSC = new OscillatorFunction();
    
final Oscillator mLFO = new OscillatorFunction();
    
Track_0() {
        mOSC.set_amplitude(0.25f);
        mOSC.set_waveform(Wellen.WAVESHAPE_TRIANGLE);
        mLFO.set_frequency(8);
        mLFO.set_oscillation_range(110 - 2, 110 + 2);
        set_in_outpoint(0, 63);
    }
    
Signal output_signal() {
        mOSC.set_frequency(mLFO.output());
        return Signal.create(mOSC.output());
    }
}

static class Track_1 extends DSPTrack {
    
final Oscillator mOSC = new OscillatorFunction();
    
final float mMaxAmplitude = 0.5f;
    
Track_1() {
        mOSC.set_frequency(225);
        mOSC.set_amplitude(0.0f);
    }
    
Signal output_signal() {
        return Signal.create(mOSC.output());
    }
    
void beat(int pBeat) {
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
