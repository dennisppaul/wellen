package de.hfkbremen.ton.applications;

import de.hfkbremen.ton.Ton;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

public class AppOscJibberish extends PApplet {

    private final ArrayList<Jibberer> mJibberers = new ArrayList<>();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Ton.start("jsyn-minimal");
        for (int i = 0; i < 3; i++) {
            Jibberer mJibberer = new Jibberer(i);
            mJibberer.triggerposition().set(width / 2.0f, height / 2.0f);
            mJibberer.position().set(random(width), random(height));
            mJibberers.add(mJibberer);
        }
    }

    public void draw() {
        background(255);
        for (Jibberer mJibberer : mJibberers) {
            mJibberer.drag();
            mJibberer.update();
            mJibberer.draw();
        }
    }

    private class Jibberer {

        private final PVector mPosition;
        private final PVector mTriggerPosition;
        private final float mMaxDistance = 100;
        private final int mID;
        private float mFreqPointer = 0;
        private float mAmpPointer = 0;
        private final float mBaseFreq;
        private final float mFreqStep;
        private final float mAmpStep;

        public Jibberer(int pID) {
            mID = pID;
            mPosition = new PVector();
            mTriggerPosition = new PVector();
            mBaseFreq = random(200, 400);
            mFreqStep = random(0.02f, 0.04f);
            mAmpStep = random(0.5f, 0.8f);

            Ton.instrument(mID).set_osc_type(Ton.OSC_SAWTOOTH);
            Ton.instrument(mID).set_amplitude(0.0f);
            Ton.instrument(mID).set_frequency(200.0f);
        }

        void drag() {
            if (mousePressed) {
                if (mouseX > position().x - 30
                    && mouseX < position().x + 30
                    && mouseY > position().y - 30
                    && mouseY < position().y + 30) {
                    position().set(mouseX, mouseY, 0);
                }
            }
        }

        PVector position() {
            return mPosition;
        }

        PVector triggerposition() {
            return mTriggerPosition;
        }

        void update() {
            float mDistanceRatio = (1 - min(1, mPosition.dist(mTriggerPosition) / mMaxDistance));
            mAmpPointer += mAmpStep;
            float mAmp = noise(mAmpPointer) * noise(mAmpPointer * 1.3f);
            if (noise(mAmpPointer * 0.45f) > 0.5f) {
                Ton.instrument(mID).set_amplitude(mDistanceRatio * mAmp);
            } else {
                Ton.instrument(mID).set_amplitude(0);
            }

            /* get frequency from perlin noise */
            mFreqPointer += mFreqStep;
            float mFreq = noise(mFreqPointer);
            Ton.instrument(mID).set_frequency(mBaseFreq * mFreq + 75);
        }

        void draw() {
            noFill();
            stroke(0, 32);
            line(triggerposition().x, triggerposition().y, position().x, position().y);
            stroke(0, 127);
            ellipse(triggerposition().x, triggerposition().y, mMaxDistance * 2, mMaxDistance * 2);
            fill(0);
            noStroke();
            ellipse(position().x, position().y, 20, 20);
        }
    }

    public static void main(String[] args) {
        PApplet.main(AppOscJibberish.class.getName());
    }
}