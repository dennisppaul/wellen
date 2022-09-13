package wellen.tests;

import processing.core.PApplet;
import wellen.FMSynthesis;
import wellen.Wavetable;
import wellen.Wellen;

public class TestSynthFMTerrain extends PApplet {

    public static final int NUM_OF_INTERPOLATIONS = 32;
    private FMSynthesis mFMSynthesis;
    private final float mVisuallyStableFrequency =
    (float) Wellen.DEFAULT_SAMPLING_RATE / Wellen.DEFAULT_AUDIOBLOCK_SIZE;
    private final float[][] mWaveSynthFMTerrain = new float[NUM_OF_INTERPOLATIONS][Wellen.DEFAULT_AUDIOBLOCK_SIZE];

    public void settings() {
        size(640, 480, P3D);
        pixelDensity(2);
    }

    public void setup() {
        Wavetable mCarrier = new Wavetable(2048);
        mCarrier.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
        Wavetable.fill(mCarrier.get_wavetable(), Wellen.OSC_SINE);
        mCarrier.set_frequency(2.0f * mVisuallyStableFrequency);

        Wavetable mModulator = new Wavetable(2048);
        mModulator.set_interpolation(Wellen.WAVESHAPE_INTERPOLATE_LINEAR);
        Wavetable.fill(mModulator.get_wavetable(), Wellen.OSC_SINE);
        mModulator.set_frequency(2.0f * mVisuallyStableFrequency);

        mFMSynthesis = new FMSynthesis(mCarrier, mModulator);
        mFMSynthesis.set_amplitude(0.33f);

        for (int i = 0; i < NUM_OF_INTERPOLATIONS; i++) {
            mWaveSynthFMTerrain[i] = new float[Wellen.DEFAULT_AUDIOBLOCK_SIZE];
            final float mModularDepths = map(i, 0, NUM_OF_INTERPOLATIONS, 0, 5);
            mFMSynthesis.get_modulator().reset();
            mFMSynthesis.get_carrier().reset();
            mFMSynthesis.set_modulation_depth(mModularDepths);
            for (int j = 0; j < mWaveSynthFMTerrain[i].length; j++) {
                mWaveSynthFMTerrain[i][j] = mFMSynthesis.output();
            }
        }
    }

    public void draw() {
        background(255);
        stroke(0);
        strokeWeight(0.25f);
        translate(width * 0.5f, height * 0.5f, -200);
        rotateY(map(mouseX, 0, width, -PI, PI));
        rotateX(map(mouseY, 0, height, PI, -PI));
        beginShape(LINE_STRIP);
        for (int i = 0; i < mWaveSynthFMTerrain.length; i++) {
            for (int j = 0; j < mWaveSynthFMTerrain[i].length; j++) {
                float x = map(i, 0, mWaveSynthFMTerrain.length, -width * 0.5f, width * 0.5f);
                float y = map(j, 0, mWaveSynthFMTerrain[i].length, -height * 0.5f, height * 0.5f);
                float z = mWaveSynthFMTerrain[i][j] * 100;
                vertex(x, y, z);
            }
        }
        endShape();
    }

    public static void main(String[] args) {
        PApplet.main(TestSynthFMTerrain.class.getName());
    }
}