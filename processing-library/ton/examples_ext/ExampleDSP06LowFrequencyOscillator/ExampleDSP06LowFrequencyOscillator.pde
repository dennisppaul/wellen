import de.hfkbremen.ton.*; 
import controlP5.*; 
import netP5.*; 
import oscP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 

final Wavetable mVCO = new Wavetable(512);

final Wavetable mFrequencyLFO = new Wavetable(512);

final Wavetable mAmplitudeLFO = new Wavetable(512);

final float mBaseFrequency = 2.0f * DSP.DEFAULT_SAMPLING_RATE / DSP.DEFAULT_AUDIOBLOCK_SIZE;

void settings() {
    size(640, 480);
}

void setup() {
    Wavetable.sine(mVCO.wavetable());
    mVCO.set_frequency(mBaseFrequency);
    mVCO.set_amplitude(0.25f);
    /* setup LFO for frequency */
    Wavetable.sine(mFrequencyLFO.wavetable());
    mFrequencyLFO.interpolate(true);
    mFrequencyLFO.set_frequency(0);
    /* setup LFO for amplitude */
    Wavetable.sine(mAmplitudeLFO.wavetable());
    mAmplitudeLFO.interpolate(true);
    mAmplitudeLFO.set_frequency(0);
    DSP.dumpAudioDevices();
    DSP.start(this);
}

void draw() {
    background(255);
    DSP.draw_buffer(g, width, height);
}

void mouseMoved() {
    mFrequencyLFO.set_frequency(map(mouseX, 0, width, 0.1f, 100.0f));
    mFrequencyLFO.set_amplitude(map(mouseY, 0, height, 0.0f, 1.0f));
}

void mouseDragged() {
    mAmplitudeLFO.set_frequency(map(mouseX, 0, width, 0.1f, 100.0f));
    mAmplitudeLFO.set_amplitude(map(mouseY, 0, height, 0.0f, 1.0f));
}

void keyPressed() {
    switch (key) {
        case '1':
            Wavetable.fill(mFrequencyLFO.wavetable(), Wavetable.TYPE_SINE);
            break;
        case '2':
            Wavetable.fill(mFrequencyLFO.wavetable(), Wavetable.TYPE_TRIANGLE);
            break;
        case '3':
            Wavetable.fill(mFrequencyLFO.wavetable(), Wavetable.TYPE_SAWTOOTH);
            break;
        case '4':
            Wavetable.fill(mFrequencyLFO.wavetable(), Wavetable.TYPE_SQUARE);
            break;
        case 'q':
            Wavetable.fill(mAmplitudeLFO.wavetable(), Wavetable.TYPE_SINE);
            break;
        case 'w':
            Wavetable.fill(mAmplitudeLFO.wavetable(), Wavetable.TYPE_TRIANGLE);
            break;
        case 'e':
            Wavetable.fill(mAmplitudeLFO.wavetable(), Wavetable.TYPE_SAWTOOTH);
            break;
        case 'r':
            Wavetable.fill(mAmplitudeLFO.wavetable(), Wavetable.TYPE_SQUARE);
            break;
        case 'a':
            Wavetable.fill(mVCO.wavetable(), Wavetable.TYPE_SINE);
            break;
        case 's':
            Wavetable.fill(mVCO.wavetable(), Wavetable.TYPE_TRIANGLE);
            break;
        case 'd':
            Wavetable.fill(mVCO.wavetable(), Wavetable.TYPE_SAWTOOTH);
            break;
        case 'f':
            Wavetable.fill(mVCO.wavetable(), Wavetable.TYPE_SQUARE);
            break;
    }
}

void audioblock(float[] pOutputSamples) {
    for (int i = 0; i < pOutputSamples.length; i++) {
        /* get frequency from LFO, map value range from [-1.0, 1.0] to [-40.0, 40.0] */
        float mFreq = map(mFrequencyLFO.output(), -1.0f, 1.0f, -40, 40);
        /* get ampliude from LFO, map value range from [-1.0, 1.0] to [0.0, 1.0] */
        float mAmp = map(mAmplitudeLFO.output(), -1.0f, 1.0f, 0, 1);
        /* set VCO */
        mVCO.set_frequency(mFreq + mBaseFrequency);
        mVCO.set_amplitude(mAmp);
        pOutputSamples[i] = mVCO.output();
    }
}
