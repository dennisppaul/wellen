package wellen.examples.external;

import processing.core.PApplet;
import wellen.DSP;
import wellen.Wellen;

import java.util.ArrayList;

public class ExampleExternal10RecordWAV extends PApplet {

    /*
     * this example demonstrates how to record an input signal into a WAV file.
     * `s` starts the recording and `e` ends it.
     */

    private boolean mIsRecording = false;
    private final ArrayList<Float> mRecordedSamples = new ArrayList<>();

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        DSP.start(this, 1, 1);
    }

    public void draw() {
        background(mIsRecording ? 0 : 255);
        stroke(mIsRecording ? 255 : 0);
        DSP.draw_buffers(g, width, height);
    }

    public void audioblock(float[] pOutputSignal, float[] pInputSignal) {
        for (int i = 0; i < pOutputSignal.length; i++) {
            pOutputSignal[i] = pInputSignal[i];
            if (mIsRecording) {
                mRecordedSamples.add(pInputSignal[i]);
            }
        }
    }

    public void keyPressed() {
        if (key == 's') {
            mRecordedSamples.clear();
            mIsRecording = true;
        }
        if (key == 'e') {
            mIsRecording = false;
            /* export samples to WAV file */
            final String WAV_FILE_NAME = "recording-" + Wellen.now() + ".wav";
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
        PApplet.main(ExampleExternal10RecordWAV.class.getName());
    }
}