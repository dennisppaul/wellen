import de.hfkbremen.ton.*; 
import controlP5.*; 
import netP5.*; 
import oscP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 

final Wavetable mWavetable = new Wavetable(512);

final LowPassFilter mFilter = new LowPassFilter(Ton.DEFAULT_SAMPLING_RATE);

void settings() {
    size(640, 480);
}

void setup() {
    Wavetable.sawtooth(mWavetable.wavetable());
    mWavetable.set_frequency(2.0f * Ton.DEFAULT_SAMPLING_RATE / Ton.DEFAULT_AUDIOBLOCK_SIZE);
    mWavetable.set_amplitude(0.5f);
    DSP.dumpAudioDevices();
    DSP.start(this);
}

void draw() {
    background(255);
    DSP.draw_buffer(g, width, height);
}

void mouseMoved() {
    mFilter.set_frequency(map(mouseX, 0, width, 1.0f, 5000.0f));
    mFilter.set_resonance(map(mouseY, 0, height, 0.0f, 0.97f));
}

void audioblock(float[] pOutputSamples) {
    for (int i = 0; i < pOutputSamples.length; i++) {
        pOutputSamples[i] = mFilter.process(mWavetable.output());
    }
}
