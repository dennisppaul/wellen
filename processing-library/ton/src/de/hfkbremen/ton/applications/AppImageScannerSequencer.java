package de.hfkbremen.ton.applications;

import de.hfkbremen.ton.Beat;
import de.hfkbremen.ton.Instrument;
import de.hfkbremen.ton.Note;
import de.hfkbremen.ton.Scale;
import de.hfkbremen.ton.ToneEngine;
import de.hfkbremen.ton.ToneEngineJSyn;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.video.Capture;

import java.util.ArrayList;

public class AppImageScannerSequencer extends PApplet {

    private final ToneEngine mSynth = new ToneEngineJSyn();
    private final ArrayList<ImageSampler> mSamplers = new ArrayList<>();
    private Capture mCapture;
    private int mCurrentSampler = 0;
    private Beat mBeat;
    private int mLastNote = -1;

    public void settings() {
        size(1280, 720);
    }

    public void setup() {
        background(255);

        println("### available cameras:");
        printArray(Capture.list());
        mCapture = new Capture(this, Capture.list()[0]);
        mCapture.start();

        /* set ADSR parameters for current instrument */
        Instrument mInstrument = mSynth.instrument();
        mInstrument.attack(0.01f);
        mInstrument.decay(0.1f);
        mInstrument.sustain(0.0f);
        mInstrument.release(0.01f);
        mInstrument.osc_type(Instrument.SAWTOOTH);
//        mInstrument.get_osc_type(Instrument.NOISE);

        final int mSpacing = 48;
        final int NUMBER_OF_SAMPLERS = 16;
        for (int i = 0; i < NUMBER_OF_SAMPLERS; i++) {
            mSamplers.add(new ImageSampler(i));
            mSamplers.get(i).x = width / 2 + (i - NUMBER_OF_SAMPLERS / 2) * mSpacing;
            mSamplers.get(i).y = height / 2;
        }

        mBeat = new Beat(this, 120 * 4);
    }

    public void draw() {
        background(255);

        if (mCapture.available()) {
            mCapture.read();
        }
        scale(-1, 1);
        translate(-width, 0);
        image(mCapture, 0, 0);

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
        final int mNote = Scale.note(Scale.MAJOR_CHORD_7, Note.NOTE_A2, (int) (mBrightnessNorm * mSteps));
        if (mNote != mLastNote) {
            mSynth.noteOn(mNote, 127);
        }
        mLastNote = mNote;
    }

    public static void main(String[] args) {
        PApplet.main(AppImageScannerSequencer.class.getName());
    }

    class ImageSampler {

        int x = 0;
        int y = 0;
        int radius = 16;
        boolean active = false;

        final int ID;

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
}
