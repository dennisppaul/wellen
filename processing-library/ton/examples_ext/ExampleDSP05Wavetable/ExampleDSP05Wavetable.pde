import de.hfkbremen.ton.*; 
import controlP5.*; 
import netP5.*; 
import oscP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 


final Wavetable mWavetable = new Wavetable(16);
void settings() {
    size(640, 480);
}
void setup() {
    DSP.dumpAudioDevices();
    DSP.start(this);
    triangle(mWavetable.wavetable());
}
void draw() {
    background(255);
    DSP.draw_buffer(g, width, height);
}
void mouseMoved() {
    mWavetable.set_frequency(map(mouseX, 0, width, 55, 220));
    mWavetable.set_amplitude(map(mouseY, 0, height, 0.0f, 0.9f));
}
void keyPressed() {
    switch (key) {
        case '1':
            sine(mWavetable.wavetable());
            break;
        case '2':
            sawtooth(mWavetable.wavetable());
            break;
        case '3':
            triangle(mWavetable.wavetable());
            break;
        case '4':
            square(mWavetable.wavetable());
            break;
        case '5':
            randomize(mWavetable.wavetable());
            break;
    }
}
void audioblock(float[] pOutputSamples) {
    for (int i = 0; i < pOutputSamples.length; i++) {
        pOutputSamples[i] = mWavetable.process();
    }
}
void randomize(float[] pWavetable) {
    for (int i = 0; i < pWavetable.length; i++) {
        pWavetable[i] = random(-1, 1);
    }
}
static void sine(float[] pWavetable) {
    for (int i = 0; i < pWavetable.length; i++) {
        pWavetable[i] = PApplet.sin(2.0f * PI * ((float) i / (float) (pWavetable.length)));
    }
}
static void sawtooth(float[] pWavetable) {
    for (int i = 0; i < pWavetable.length; i++) {
        pWavetable[i] = 2.0f * ((float) i / (float) (pWavetable.length - 1)) - 1.0f;
    }
}
static void triangle(float[] pWavetable) {
    final int q = pWavetable.length / 4;
    final float qf = pWavetable.length * 0.25f;
    for (int i = 0; i < q; i++) {
        pWavetable[i] = i / qf;
        pWavetable[i + (q * 1)] = (qf - i) / qf;
        pWavetable[i + (q * 2)] = -i / qf;
        pWavetable[i + (q * 3)] = -(qf - i) / qf;
    }
}
static void square(float[] pWavetable) {
    for (int i = 0; i < pWavetable.length / 2; i++) {
        pWavetable[i] = 1.0f;
        pWavetable[i + pWavetable.length / 2] = -1.0f;
    }
}
static class Wavetable {
    final float[] mWavetable;
    float mFrequency;
    float mStepSize;
    float mArrayPtr;
    float mAmplitude;
    Wavetable(int pWavetableSize) {
        mWavetable = new float[pWavetableSize];
        mArrayPtr = 0;
        mAmplitude = 0.75f;
        set_frequency(220);
    }
    void set_frequency(float pFrequency) {
        if (mFrequency != pFrequency) {
            mFrequency = pFrequency;
            mStepSize = mFrequency * ((float) mWavetable.length / (float) DSP.DEFAULT_SAMPLING_RATE);
        }
    }
    void set_amplitude(float pAmplitude) {
        mAmplitude = pAmplitude;
    }
    float[] wavetable() {
        return mWavetable;
    }
    float process() {
        mArrayPtr += mStepSize;
        final int i = (int) mArrayPtr;
        final float mFrac = mArrayPtr - i;
        final int j = i % mWavetable.length;
        mArrayPtr = j + mFrac;
        return mWavetable[j] * mAmplitude;
    }
}
