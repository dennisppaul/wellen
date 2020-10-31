package de.hfkbremen.ton.applications;

import com.jsyn.unitgen.MixerMono;
import com.jsyn.unitgen.SawtoothOscillator;
import com.jsyn.unitgen.UnitOscillator;
import de.hfkbremen.ton.ToneEngineJSyn;
import de.hfkbremen.ton.ToneEngine;
import processing.core.PApplet;
import processing.core.PVector;

public class AppOscJibberish extends PApplet {

    private Jibberer mJibberer;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        size(640, 480);
        frameRate(60);
        noFill();
        rectMode(CENTER);
        ellipseMode(CENTER);
        smooth();

        /* start jsyn */
        ToneEngineJSyn mSynth = new ToneEngineJSyn(ToneEngine.INSTRUMENT_EMPTY);

        MixerMono mMixerMono = new MixerMono(1);
        mMixerMono.amplitude.set(0.85f);
        mMixerMono.output.connect(0, mSynth.line_out().input, 0);
        mMixerMono.output.connect(0, mSynth.line_out().input, 1);

        mJibberer = new Jibberer(mSynth, mMixerMono, 0);
        mJibberer.triggerposition().set(width / 2.0f, height / 2.0f, 0);
        mJibberer.position().set(random(width), random(height), 0);
    }

    public void draw() {
        /* compute */
        if (mousePressed) {
            if (mouseX > mJibberer.position().x - 30
                    && mouseX < mJibberer.position().x + 30
                    && mouseY > mJibberer.position().y - 30
                    && mouseY < mJibberer.position().y + 30) {
                mJibberer.position().set(mouseX, mouseY, 0);
            }
        }

        mJibberer.update();

        /* draw */
        background(255);
        stroke(0, 32);
        line(mJibberer.triggerposition().x, mJibberer.triggerposition().y,
                mJibberer.position().x, mJibberer.position().y);
        stroke(255, 127, 0, 127);
        ellipse(mJibberer.position().x, mJibberer.position().y, 20, 20);
        stroke(0, 127);
        ellipse(mJibberer.triggerposition().x, mJibberer.triggerposition().y,
                mJibberer.mMaxDistance * 2, mJibberer.mMaxDistance * 2);
    }

    public class Jibberer {

        private final UnitOscillator mOsc;

        private final PVector myPosition;

        private final PVector mTriggerPosition;

        private final float mMaxDistance = 100;

        private float mFreqPointer = 0;

        private float mAmpPointer = 0;

        public Jibberer(ToneEngineJSyn pSynth, MixerMono pMixerMono, int pMixerChannel) {
            myPosition = new PVector();
            mTriggerPosition = new PVector();

            /* create oscillators */
            mOsc = new SawtoothOscillator();
            pSynth.add(mOsc);
            mOsc.start();

            mOsc.output.connect(pMixerMono.input.getConnectablePart(pMixerChannel));

            /* default values */
            mOsc.amplitude.set(0.0f);
            mOsc.frequency.set(200.0f);
        }

        PVector position() {
            return myPosition;
        }

        PVector triggerposition() {
            return mTriggerPosition;
        }

        void update() {
            float myDistanceRatio = (1 - min(1, myPosition.dist(mTriggerPosition) / mMaxDistance));

            mAmpPointer += 0.65f;
            float mAmp = noise(mAmpPointer) * noise(mAmpPointer * 1.3f);
            if (noise(mAmpPointer * 0.45f) > 0.5f) {
                mOsc.amplitude.set(myDistanceRatio * mAmp);
            } else {
                mOsc.amplitude.set(0);
            }

            /* get frequency from perlin noise */
            mFreqPointer += 0.03f;
            float mFreq = noise(mFreqPointer);
            mOsc.frequency.set(400 * mFreq + 75);
        }
    }

    public static void main(String[] args) {
        PApplet.main(AppOscJibberish.class.getName());
    }
}