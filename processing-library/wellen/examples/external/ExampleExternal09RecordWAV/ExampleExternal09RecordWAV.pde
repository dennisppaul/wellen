import wellen.*; 

/*
 * this example demonstrates how to record an input signal into a WAV file.
 * `s` starts the recording and `e` ends it.
 */

boolean mIsRecording = false;

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
    DSP.draw_buffer(g, width, height);
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
        mRecordedSamples.clear();
        mIsRecording = true;
    }
    if (key == 'e') {
        mIsRecording = false;
        /* export samples to WAV file */
        final String WAV_FILE_NAME = "recording-" + now() + ".wav";
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

String now() {
    return
    nf(year(), 4) +
    nf(month(), 2) +
    nf(day(), 2) +
    "_" +
    nf(hour(), 2) +
    nf(minute(), 2) +
    nf(second(), 2)
    ;
}
