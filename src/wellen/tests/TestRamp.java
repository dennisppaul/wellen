package wellen.tests;

import processing.core.PApplet;
import wellen.Envelope;
import wellen.Wellen;

public class TestRamp extends PApplet {

    private Envelope mEnvelope;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mEnvelope = new Envelope();
        background(255);
    }

    public void draw() {
        final int mAudioFramesPerVideoFrame = (int) (Wellen.DEFAULT_SAMPLING_RATE / frameRate);
        for (int i = 0; i < mAudioFramesPerVideoFrame; i++) {
            mEnvelope.output();
        }
        final float mValue = mEnvelope.get_current_value();
        final float x = frameCount % width;
        final float y = map(mValue, 0.0f, 2.0f, 0, height);
        point(x, y);
    }

    public void mousePressed() {
        mEnvelope.ramp(random(0.0f, 2.0f), random(0.0f, 2.0f), 1.5f);
        mEnvelope.start();
    }

    public void keyPressed() {
        mEnvelope.ramp_to(random(0.75f, 1.25f), random(0.25f, 0.75f));
        mEnvelope.start();
    }

    public static void main(String[] args) {
        PApplet.main(TestRamp.class.getName());
    }
}