import de.hfkbremen.ton.*; 
import controlP5.*; 
import netP5.*; 
import oscP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 
float[] mDelayBuffer = new float[4096];
int mDelayID = 0;
int mDelayOffset = 512;
float mDecay = 0.9f;
float mMix = 0.25f;

void settings() {
    size(640, 480);
}

void setup() {
    /* by sending the output of `Ton` to a virtual soundcard ( e.g *Blackhole* in MacOS ) the generated sounds
     * can be used as an input for `DSP`. this presents a pipeline to apply e.g filters and effects ( e.g an echo )
     * to internally generated sounds.
     */
    DSP.dumpAudioDevices();
    final int OUTPUT_BUILT_IN_OUTPUT = 10;
    final int INPUT_BACKHOLE_2CH = 5;
    Ton.start("jsyn", INPUT_BACKHOLE_2CH, 2);
    DSP.start(this, OUTPUT_BUILT_IN_OUTPUT, 1, INPUT_BACKHOLE_2CH, 1);
}

void draw() {
    background(255);
    stroke(0);
    final int mBufferSize = DSP.buffer_size();
    if (DSP.buffer() != null) {
        for (int i = 0; i < mBufferSize; i++) {
            final float x = map(i, 0, mBufferSize, 0, width);
            point(x, map(DSP.buffer()[i], -1, 1, 0, height));
        }
    }
}

void mouseMoved() {
    mMix = map(mouseX, 0, width, 0.2f, 0.95f);
    mDelayOffset = (int) map(mouseY, 0, height, 1, mDelayBuffer.length);
}

void keyPressed() {
    Ton.note_on(48, 100);
}

void keyReleased() {
    Ton.note_off();
}

void audioblock(float[] pOutputSamples, float[] pInputSamples) {
    for (int i = 0; i < pInputSamples.length; i++) {
        mDelayID++;
        mDelayID %= mDelayBuffer.length;
        int mOffsetID = mDelayID + mDelayOffset;
        mOffsetID %= mDelayBuffer.length;
        pOutputSamples[i] = pInputSamples[i] * (1.0f - mMix) + mDelayBuffer[mOffsetID] * mMix;
        mDelayBuffer[mDelayID] = pOutputSamples[i] * mDecay;
    }
}
