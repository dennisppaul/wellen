package wellen.tests;

import processing.core.PApplet;
import wellen.DSP;
import wellen.SAM;
import wellen.Wellen;

import java.util.ArrayList;

public class TestExportSAM extends PApplet {

    /*
     * this example demonstrates how to record an input signal into a WAV file.
     * `s` starts the recording and `e` ends it.
     */

    private boolean mIsRecording = false;
    private final ArrayList<Float> mRecordedSamples = new ArrayList<>();
    private SAM mSAM;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        mSAM = new SAM();
        mSAM.say("hello");

        mIsRecording = true;
        DSP.start(this, 1);
    }

    public void draw() {
        background(mIsRecording ? 0 : 255);
        stroke(mIsRecording ? 255 : 0);
        DSP.draw_buffers(g, width, height);
    }

    public void audioblock(float[] pOutputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = mSAM.output() * 0.5f;
            if (mIsRecording) {
                mRecordedSamples.add(pOutputSignal[i]);
            }
        }
    }

    public void keyPressed() {
        if (key == ' ') {
            mIsRecording = false;
            /* export samples to WAV file */
            final String WAV_FILE_NAME = "SAM-recording-" + Wellen.now() + ".wav";
            final int WAV_FILE_LENGTH = mRecordedSamples.size();
            final int NUM_OF_CHANNELS = 1;
            final float[][] mExportSamples = new float[NUM_OF_CHANNELS][WAV_FILE_LENGTH];
            for (int i = 0; i < WAV_FILE_LENGTH; i++) {
                mExportSamples[0][i] = mRecordedSamples.get(i);
            }
            Wellen.exportWAV(this,
                             WAV_FILE_NAME,
                             mExportSamples,
                             32,
                             Wellen.DEFAULT_SAMPLING_RATE,
                             Wellen.WAV_FORMAT_IEEE_FLOAT_32BIT);
            println("+++ recorded file ...... : " + WAV_FILE_NAME);
            println("+++ recorded samples ... : " + WAV_FILE_LENGTH);
        }
    }

    public static void main(String[] args) {
        Wellen.run_sketch_with_resources(TestExportSAM.class);
    }
}