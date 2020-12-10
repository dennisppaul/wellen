package wellen.applications;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.video.Capture;
import wellen.Beat;
import wellen.Instrument;
import wellen.Note;
import wellen.Scale;
import wellen.Tone;
import wellen.Wellen;

import java.util.ArrayList;

public class AppImageScannerSequencer extends PApplet {

    /*
     * this application requires the processing [video](https://processing.org/reference/libraries/video/) library to be
     * installed as well as some kind of camera to be connected.
     */

    private final ArrayList<ImageSampler> mSamplers = new ArrayList<>();
    private Capture mCapture;
    private int mCurrentSampler = 0;
    private int mLastNote = -1;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        background(255);

        println("+++ available cameras:");
        printArray(Capture.list());
        mCapture = new Capture(this, Capture.list()[0]);
        mCapture.start();

        /* set ADSR parameters for current instrument */
        Instrument mInstrument = Tone.instrument();
        mInstrument.set_attack(0.01f);
        mInstrument.set_decay(0.1f);
        mInstrument.set_sustain(0.0f);
        mInstrument.set_release(0.01f);
        mInstrument.set_oscillator_type(Wellen.OSC_SINE);

        final int mSpacing = 36;
        final int NUMBER_OF_SAMPLERS = 16;
        for (int i = 0; i < NUMBER_OF_SAMPLERS; i++) {
            mSamplers.add(new ImageSampler(i));
            mSamplers.get(i).x = width / 2 + (i - NUMBER_OF_SAMPLERS / 2) * mSpacing;
            mSamplers.get(i).y = height / 2;
        }

        Beat.start(this, 120 * 4);
    }

    public void draw() {
        background(255);

        if (mCapture.available()) {
            mCapture.read();
        }
        scale(-1, 1);
        translate(-width, 0);
        image(mCapture, 0, 0, width, height);

        for (ImageSampler mSampler : mSamplers) {
            mSampler.draw(g);
            mSampler.active = mSampler.ID == mCurrentSampler;
        }
    }

    public void beat(int pBeat) {
        mCurrentSampler++;
        mCurrentSampler %= mSamplers.size();
        for (ImageSampler mSampler : mSamplers) {
            mSampler.sample(mCapture);
        }
        float mBrightnessNorm = mSamplers.get(mCurrentSampler).sample(mCapture);
        final int mSteps = 10;
        final int mNote = Scale.get_note(Scale.MAJOR_CHORD_7, Note.NOTE_C2, (int) (mBrightnessNorm * mSteps));
        if (mNote != mLastNote) {
            Tone.note_on(mNote, 100);
        }
        mLastNote = mNote;
    }

    class ImageSampler {

        final int ID;
        int x = 0;
        int y = 0;
        int radius = 12;
        boolean active = false;
        private float mBrightnessNorm = 0;
        private int mSampleRed = 0;
        private int mSampleGreen = 0;
        private int mSampleBlue = 0;

        ImageSampler(int pID) {
            ID = pID;
        }

        float sample(PImage pPimage) {
            if (pPimage.width == 0 || pPimage.height == 0) {
                return 0.0f;
            }
            mBrightnessNorm = 0;
            mSampleRed = 0;
            mSampleGreen = 0;
            mSampleBlue = 0;
            for (int iX = 0; iX < radius; iX++) {
                for (int iY = 0; iY < radius; iY++) {
                    int mX = (x + (iX - radius / 2)) % pPimage.width;
                    int mY = (y + (iY - radius / 2)) % pPimage.height;
                    int mColor = pPimage.get(mX, mY);
                    mSampleRed += red(mColor);
                    mSampleGreen += green(mColor);
                    mSampleBlue += blue(mColor);
                    mBrightnessNorm += brightness(mColor) / 255.0f;
                }
            }
            mSampleRed /= radius * radius;
            mSampleGreen /= radius * radius;
            mSampleBlue /= radius * radius;
            mBrightnessNorm /= radius * radius;
            return mBrightnessNorm;
        }

        void draw(PGraphics g) {
            final float mScale = 2 * (active ? 1.5f : 1.0f);
            g.noFill();
            g.stroke(0, 63);
            g.ellipse(x, y, radius * 2 * mScale, radius * 2 * mScale);

            if (active) {
                g.noStroke();
                g.fill(0);
                g.ellipse(x, y, radius * 2, radius * 2);
            }

            g.fill(mSampleRed, mSampleGreen, mSampleBlue);
            g.stroke(0, 127);
            g.ellipse(x, y, radius * 2, radius * 2);
        }
    }

    public static void main(String[] args) {
        PApplet.main(AppImageScannerSequencer.class.getName());
    }
}

