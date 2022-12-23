import wellen.*; 
import wellen.dsp.*; 

/*
 * this example demonstrates how to record an input signal into a WAV file.
 * `s` starts the recording and `e` ends it.
 */

boolean mIsRecording = true;

final ArrayList<Float> mRecordedSamples = new ArrayList();

void settings() {
    size(640, 480);
}

void setup() {
    DSP.start(this, 1, 1);
}

void draw() {
    background(mIsRecording ? 0 : 255);
    stroke(mIsRecording ? 255 : 0);
    DSP.draw_buffers(g, width, height);
}

void audioblock(float[] pOutputSignal, float[] pInputSignal) {
    for (int i = 0; i < pOutputSignal.length; i++) {
        pOutputSignal[i] = pInputSignal[i];
        if (mIsRecording) {
            mRecordedSamples.add(pInputSignal[i]);
        }
    }
}

void keyPressed() {
    if (key == 's') {
        start_recording();
    }
    if (key == 'e') {
        stop_recording();
    }
}

void stop_recording() {
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

void start_recording() {
    mRecordedSamples.clear();
    mIsRecording = true;
}
